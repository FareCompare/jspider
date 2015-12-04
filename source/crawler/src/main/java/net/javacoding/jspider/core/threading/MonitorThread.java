package net.javacoding.jspider.core.threading;

import net.javacoding.jspider.api.event.monitor.MonitorEvent;
import net.javacoding.jspider.core.dispatch.EventDispatcher;
import net.javacoding.jspider.core.logging.Log;
import net.javacoding.jspider.core.logging.LogFactory;

/**
 * $Id: MonitorThread.java,v 1.3 2003/02/27 16:47:48 vanrogu Exp $
 */
public abstract class MonitorThread extends Thread {

    private static final Log log = LogFactory.getLog( MonitorThread.class );
    protected EventDispatcher dispatcher;
    protected int interval;

    public MonitorThread ( EventDispatcher dispatcher, int interval, String subject ) {
        super ( subject + " monitor" );
        setDaemon(true);
        this.dispatcher = dispatcher;
        this.interval = interval;
    }

    public void run ( ) {
        MonitorEvent event;
        try {
            while ( true ) {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                event = doMonitorTask ( );
                dispatcher.dispatch(event);
            }
        } catch ( Exception e ) {
            log.error( getName() + " thread ended with exception", e );
        }

    }

    public abstract MonitorEvent doMonitorTask ( );

}
