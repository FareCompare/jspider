package net.javacoding.jspider.core.task.work;


import net.javacoding.jspider.api.model.HTTPHeader;
import net.javacoding.jspider.api.model.Resource;
import net.javacoding.jspider.api.model.Site;
import net.javacoding.jspider.core.SpiderContext;
import net.javacoding.jspider.core.event.CoreEvent;
import net.javacoding.jspider.core.event.impl.URLFoundEvent;
import net.javacoding.jspider.core.event.impl.URLSpideredErrorEvent;
import net.javacoding.jspider.core.event.impl.URLSpideredOkEvent;
import net.javacoding.jspider.core.logging.Log;
import net.javacoding.jspider.core.logging.LogFactory;
import net.javacoding.jspider.core.task.WorkerTask;
import net.javacoding.jspider.core.util.URLUtil;
import net.javacoding.jspider.core.util.http.HTTPHeaderUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.locks.Lock;


/**
 *
 * $Id: SpiderHttpURLTask.java,v 1.19 2003/04/10 16:19:14 vanrogu Exp $
 *
 * @author Gunther Van Roey
 */
public class SpiderHttpURLTask extends BaseWorkerTaskImpl {

    private int BUFFER_SIZE = 1024 * 128;

    private static final Log log = LogFactory.getLog( SpiderHttpURLTask.class );

    protected URL url;
    protected Site site;


    public SpiderHttpURLTask(SpiderContext context, URL url, Site site) {
        super(context, WorkerTask.WORKERTASK_SPIDERTASK);
        this.url = url;
        this.site = site;
    }

    public void prepare() {
        context.throttle(site);
    }

    public void execute() {
        Lock lock = context.getLock( url );
        lock.lock();
        try {
            Resource resource = context.getStorage().getResourceDAO().getResource( url );
            if ( resource != null && resource.getState() != Resource.STATE_DISCOVERED ) {
                // Some other thread has already spidered this URL
                return;
            }
            doSpider();
        } finally {
            lock.unlock();
        }
    }

    private void doSpider() {
        CoreEvent event = null;
        URLConnection connection = null;

        InputStream inputStream = null;

        int httpStatus = 0;
        HTTPHeader[] headers = null;

        try {

            connection = url.openConnection();

            if (connection instanceof HttpURLConnection ) {
                ((HttpURLConnection) connection).setInstanceFollowRedirects(false);
            }

            connection.setRequestProperty("User-agent", site.getUserAgent());
            context.preHandle(connection, site);

            long start = System.currentTimeMillis();
            connection.connect();

            if (connection instanceof HttpURLConnection) {
                httpStatus = ((HttpURLConnection) connection).getResponseCode();
                switch (httpStatus) {
                    case HttpURLConnection.HTTP_MOVED_PERM:
                    case HttpURLConnection.HTTP_MOVED_TEMP:
                        String redirectURL = connection.getHeaderField("location");
                        notifyEvent(url, new URLFoundEvent(context, url, URLUtil.normalize( new URL( redirectURL ) )));
                        break;
                    default:
                        break;
                }
            }
            inputStream = new BufferedInputStream(connection.getInputStream());
            ByteArrayOutputStream os = new ByteArrayOutputStream(BUFFER_SIZE);

            int size = 0;
            try (
                    BufferedOutputStream out = new BufferedOutputStream( os, BUFFER_SIZE );
                    InputStream is = new BufferedInputStream( inputStream )
            ) {
                int i = is.read();
                while ( i != -1 ) {
                    size++;
                    out.write( i );
                    i = is.read();
                }
                out.flush();
            } catch ( IOException e ) {
                log.warn( "i/o exception during fetch", e );
            }

            String contentType = connection.getContentType();
            int timeMs = (int) (System.currentTimeMillis() - start);

            headers = HTTPHeaderUtil.getHeaders( connection );

            if (httpStatus >= 200 && httpStatus < 303) {
                event = new URLSpideredOkEvent(context, url, httpStatus, connection, contentType, timeMs, size, os.toByteArray(), headers);
            } else {
                event = new URLSpideredErrorEvent(context, url, httpStatus, connection, headers, null);
            }

            context.postHandle(connection, site);

        } catch (FileNotFoundException e) {
            headers = HTTPHeaderUtil.getHeaders(connection);
            event = new URLSpideredErrorEvent(context, url, 404, connection, headers, e);
        } catch (Exception e) {
            if ( httpStatus ==  HttpURLConnection.HTTP_FORBIDDEN ) {
                log.warn( "HTTP Status-Code 403: Forbidden - " + url, e );
            } else {
                log.warn("exception during spidering", e);
            }
            event = new URLSpideredErrorEvent(context, url, httpStatus, connection, headers, e);
        } finally {
            notifyEvent(url, event);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.warn( "i/o exception closing inputstream", e );
                }
            }
        }
    }

}
