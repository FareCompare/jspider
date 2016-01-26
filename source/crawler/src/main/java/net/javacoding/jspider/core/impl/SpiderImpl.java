package net.javacoding.jspider.core.impl;


import net.javacoding.jspider.api.event.engine.SpideringStartedEvent;
import net.javacoding.jspider.api.event.engine.SpideringStoppedEvent;
import net.javacoding.jspider.api.event.engine.SpideringSummaryEvent;
import net.javacoding.jspider.core.Spider;
import net.javacoding.jspider.core.SpiderContext;
import net.javacoding.jspider.core.logging.Log;
import net.javacoding.jspider.core.logging.LogFactory;
import net.javacoding.jspider.core.task.dispatch.DispatchSpiderTasks;
import net.javacoding.jspider.core.task.dispatch.DispatchThinkerTasks;
import net.javacoding.jspider.core.threading.ThreadPoolMonitorThread;
import net.javacoding.jspider.core.threading.WorkerThreadPool;
import net.javacoding.jspider.core.util.config.ConfigConstants;
import net.javacoding.jspider.core.util.config.ConfigurationFactory;
import net.javacoding.jspider.core.util.config.MappedPropertySet;
import net.javacoding.jspider.core.util.config.PropertySet;
import net.javacoding.jspider.core.util.statistics.StopWatch;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;


/**
 *
 * $Id: SpiderImpl.java,v 1.18 2003/04/02 20:55:06 vanrogu Exp $
 *
 * @author Gunther Van Roey
 */
public class SpiderImpl implements Spider {

    public static final int DEFAULT_MONITORING_INTERVAL = 1000;
    private Log log = LogFactory.getLog(Spider.class);

    protected WorkerThreadPool spiders;
    protected WorkerThreadPool thinkers;

    public SpiderImpl(SpiderContext context, int spiderThreads, int thinkerThreads) {
        log.info( "Spider born - threads: spiders: " + spiderThreads + ", thinkers: " + thinkerThreads );
        spiders = new WorkerThreadPool("Spiders", "Spider", spiderThreads);
        thinkers = new WorkerThreadPool("Thinkers", "Thinker", thinkerThreads);

        PropertySet props = ConfigurationFactory.getConfiguration().getJSpiderConfiguration();
        PropertySet threadProps = new MappedPropertySet ( ConfigConstants.CONFIG_THREADING, props);
        PropertySet spidersProps = new MappedPropertySet ( ConfigConstants.CONFIG_THREADING_SPIDERS, threadProps);
        PropertySet thinkerProps = new MappedPropertySet ( ConfigConstants.CONFIG_THREADING_THINKERS, threadProps);
        PropertySet spidersMonitoringProps = new MappedPropertySet ( ConfigConstants.CONFIG_THREADING_MONITORING, spidersProps);
        PropertySet thinkerMonitoringProps = new MappedPropertySet ( ConfigConstants.CONFIG_THREADING_MONITORING, thinkerProps);

        if (spidersMonitoringProps.getBoolean(ConfigConstants.CONFIG_THREADING_MONITORING_ENABLED, false)) {
            int interval = spidersMonitoringProps.getInteger(ConfigConstants.CONFIG_THREADING_MONITORING_INTERVAL, DEFAULT_MONITORING_INTERVAL);
            new ThreadPoolMonitorThread(context.getEventDispatcher(), interval, spiders);
        }
        if (thinkerMonitoringProps.getBoolean(ConfigConstants.CONFIG_THREADING_MONITORING_ENABLED, false)) {
            int interval = thinkerMonitoringProps.getInteger(ConfigConstants.CONFIG_THREADING_MONITORING_INTERVAL, DEFAULT_MONITORING_INTERVAL);
            new ThreadPoolMonitorThread(context.getEventDispatcher(), interval, thinkers);
        }
    }

    public void crawl(SpiderContext context) {
//        System.out.println("Press Enter to start.");
//        try (
//                BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );
//        ) {
//            in.readLine();
//        } catch ( IOException e ) {
//            e.printStackTrace();
//        }

        long start = System.currentTimeMillis();

        context.getEventDispatcher().dispatch(new SpideringStartedEvent(context.getBaseURL()));

        DispatchSpiderTasks dispatchSpiderTask = new DispatchSpiderTasks(spiders, context);
        DispatchThinkerTasks dispatchThinkerTask = new DispatchThinkerTasks(thinkers, context);

        synchronized (dispatchSpiderTask) {

            context.getAgent().start();

            spiders.assignGroupTask(dispatchSpiderTask);
            thinkers.assignGroupTask(dispatchThinkerTask);

            try {
                // now wait for the spidering to be ended.
                dispatchSpiderTask.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        log.debug("Stopping spider workers...");
        spiders.stopAll();
        log.info("Stopped spider workers...");
        log.debug("Stopping thinker workers...");
        thinkers.stopAll();
        log.info("Stopped thinker workers...");

        context.getEventDispatcher().dispatch(new SpideringSummaryEvent(context.getStorage().getSummary()));
        context.getEventDispatcher().dispatch(new SpideringStoppedEvent(context.getStorage()));

        context.getEventDispatcher().shutdown();

        log.info( "Spidering done!" );
        String duration = StopWatch.formatDuration( System.currentTimeMillis() - start );
        log.info( "Elapsed time : " + duration );

        sendMessage( "JSpider complete, duration " + duration, "#teamjeff" );
    }


    private void sendMessage( String message, String channel ) {
        String sender;
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            sender = "jspider@" + hostName;
        } catch ( UnknownHostException e ) {
            log.warn( e.getMessage(), e );
            sender = "jspider";
        }

        log.info( "Sending to : " + channel + " message:" + message );

        GetMethod method = new GetMethod( String.format("https://slack.com/api/chat.postMessage?token=xoxp-6525398036-6525294374-12436352048-88ceba57d1&channel=%s&username=%s&pretty=1&text=%s",
                                                        URLEncoder.encode( channel ),
                                                        URLEncoder.encode( sender ),
                                                        URLEncoder.encode( message) )
                                                        );
        try {
            HttpClient httpClient = new HttpClient();
            httpClient.executeMethod( method );
        } catch ( IOException e ) {
            String xmlResponse = null;
            try {
                xmlResponse = method.getResponseBodyAsString();
            } catch ( IOException e1 ) {
                xmlResponse = "Failed to response body!";
                log.error( xmlResponse, e1 );
            }
            log.error( "Failed to send message to slack. Response:\n" + xmlResponse, e );
        }
    }
}
