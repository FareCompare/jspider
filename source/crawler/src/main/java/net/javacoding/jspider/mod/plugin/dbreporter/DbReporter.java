package net.javacoding.jspider.mod.plugin.dbreporter;

import net.javacoding.jspider.api.event.EventVisitor;
import net.javacoding.jspider.api.event.JSpiderEvent;
import net.javacoding.jspider.api.event.engine.EngineRelatedEvent;
import net.javacoding.jspider.api.event.engine.SpideringStartedEvent;
import net.javacoding.jspider.api.event.engine.SpideringStoppedEvent;
import net.javacoding.jspider.api.event.folder.FolderDiscoveredEvent;
import net.javacoding.jspider.api.event.folder.FolderRelatedEvent;
import net.javacoding.jspider.api.event.resource.EMailAddressDiscoveredEvent;
import net.javacoding.jspider.api.event.resource.EMailAddressReferenceDiscoveredEvent;
import net.javacoding.jspider.api.event.resource.MalformedBaseURLFoundEvent;
import net.javacoding.jspider.api.event.resource.MalformedURLFoundEvent;
import net.javacoding.jspider.api.event.resource.ResourceDiscoveredEvent;
import net.javacoding.jspider.api.event.resource.ResourceFetchErrorEvent;
import net.javacoding.jspider.api.event.resource.ResourceFetchedEvent;
import net.javacoding.jspider.api.event.resource.ResourceForbiddenEvent;
import net.javacoding.jspider.api.event.resource.ResourceIgnoredForFetchingEvent;
import net.javacoding.jspider.api.event.resource.ResourceIgnoredForParsingEvent;
import net.javacoding.jspider.api.event.resource.ResourceParsedEvent;
import net.javacoding.jspider.api.event.resource.ResourceReferenceDiscoveredEvent;
import net.javacoding.jspider.api.event.resource.ResourceRelatedEvent;
import net.javacoding.jspider.api.event.site.RobotsTXTFetchedEvent;
import net.javacoding.jspider.api.event.site.RobotsTXTMissingEvent;
import net.javacoding.jspider.api.event.site.SiteDiscoveredEvent;
import net.javacoding.jspider.api.event.site.SiteRelatedEvent;
import net.javacoding.jspider.api.event.site.UserAgentObeyedEvent;
import net.javacoding.jspider.core.logging.Log;
import net.javacoding.jspider.core.logging.LogFactory;
import net.javacoding.jspider.core.storage.Storage;
import net.javacoding.jspider.core.storage.jdbc.DBUtil;
import net.javacoding.jspider.core.storage.jdbc.JdbcStorageImpl;
import net.javacoding.jspider.core.storage.spi.StorageSPI;
import net.javacoding.jspider.core.util.ReportTable;
import net.javacoding.jspider.core.util.config.ConfigurationFactory;
import net.javacoding.jspider.core.util.config.JSpiderConfiguration;
import net.javacoding.jspider.core.util.config.PropertySet;
import net.javacoding.jspider.core.util.statistics.StopWatch;
import net.javacoding.jspider.spi.Plugin;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <p><tt>DbReporter</tt> </p>
 *
 * @author <a href="mailto:cstillwell@farecompare.com">cstillwell</a>
 * @version $Revision: 1.1 $
 */
public class DbReporter implements Plugin, EventVisitor {

    public static final String SLACK_CHANNEL = "#teamjeff";
    private Log log = LogFactory.getLog( this.getClass() );
    private URL baseURL;
    private String jspiderRun;

    public DbReporter() {
        log.info( "DbReporter<init>" );
        initJspiderRun();
    }

    public DbReporter (PropertySet config ) {
        log.info( "DbReporter<init>" );
        initJspiderRun();
    }

    private void initJspiderRun() {
        jspiderRun = System.getProperty( "jspider.run", "" );
        log.info( "jspiderRun=" + jspiderRun );
    }

    //
    // Plugin
    //

    @Override
    public String getDescription() {
        return "Used to produce reports from the seo_spider output tables";
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public String getVendor() {
        return "FC";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    //
    // EventSink
    //
    @Override
    public void initialize() {
        log.info( "initialize" );
    }

    @Override
    public void notify( JSpiderEvent event ) {
        event.accept( this );
    }

    @Override
    public void shutdown() {
        log.info( "shutdown" );
    }

    //
    // EventVisitor
    //

    @Override
    public void visit( SpideringStartedEvent event ) {
        baseURL = event.getBaseURL();
    }

    @Override
    public void visit( SpideringStoppedEvent event ) {
        log.info( "SpideringStoppedEvent received - begin database reports." );
        Storage storage = event.getStorage();
        StorageSPI spi = storage.getStorageSPI();
        if ( spi instanceof JdbcStorageImpl ) {
            JdbcStorageImpl jdbcStorage = (JdbcStorageImpl) spi;
            DBUtil dbUtil = jdbcStorage.getDbUtil();
            reportCountsByHttpStatus( dbUtil );
            reportNon200Urls( dbUtil );
        }
        else {
            log.warn(
                    "The DbReporter plogin should not be configured when not using JdbcStorageImpl.  DB reports bypassed." );
        }
    }

    private void reportNon200Urls( DBUtil dbUtil ) {
        reportNon200UrlsAll( dbUtil );
        reportNon200UrlsFlights( dbUtil );
        reportNotCrawled( dbUtil );
    }

    private void reportNotCrawled( DBUtil dbUtil ) {
        log.warn( "reportNotCrawled not implemented!!" );
        //todo implement
    }

    private void reportNon200UrlsFlights( DBUtil dbUtil ) {
        StopWatch timer = new StopWatch( true );
        JSpiderConfiguration jspiderConfig = ConfigurationFactory.getConfiguration();
        String fileName = "jspider-" + jspiderRun + "-non_200_report_flights.csv";
        File output = new File( jspiderConfig.getDefaultOutputFolder().getAbsoluteFile(), fileName );
        if ( output.delete() ) {
            log.info( "Deleted old report file: " + output );
        }

        String sql = "    SELECT 'id', 'url', 'httpstatus', 'id', 'referer'\n" +
                     "    UNION (\n" +
                     "    SELECT r1.id, r1.url, r1.httpstatus, r2.id, r2.url as referer\n" +
                     "      INTO OUTFILE ?\n" +
                     "           FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"'\n" +
                     "           LINES TERMINATED BY '\\n'\n" +
                     "      FROM jspider_resource r1, jspider_resource r2, jspider_resource_reference x\n" +
                     "     WHERE r1.id = x.referee\n" +
                     "       AND r2.id = referer\n" +
                     "       AND r1.httpstatus <> 0\n" +
                     "       AND r1.httpstatus <> 200\n" +
                     "       AND r1.url like ?\n" +
                     "     ORDER BY 1\n" +
                     "     );\n";
        ResultSet rs = null;


        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            int i = 0;
            ps.setString( ++i, output.getAbsolutePath() );
            ps.setString( ++i, getFlightsFilter() );
            rs = ps.executeQuery();
        } catch ( SQLException e ) {
            log.error( "SQLException", e );
        } finally {
            dbUtil.safeClose( rs, log );
        }
        log.info( "reportNon200UrlsFlights completed: " + output + ", duration " + timer );

    }

    private String getFlightsFilter() {
        String urlString = baseURL.toString();
        if ( urlString.endsWith( ".mx" ) || urlString.endsWith( "/es" ) ) {
            return urlString + "/vuelos/%";
        }
        return urlString + "/flights/%";
    }

    private void reportNon200UrlsAll( DBUtil dbUtil ) {
        StopWatch timer = new StopWatch( true );
        JSpiderConfiguration jspiderConfig = ConfigurationFactory.getConfiguration();
        String fileName = "jspider-" + jspiderRun + "-non_200_report_all.csv";
        File output = new File( jspiderConfig.getDefaultOutputFolder().getAbsoluteFile(), fileName );
        if ( output.delete() ) {
            log.info( "Deleted old report file: " + output );
        }

        String sql = "    SELECT 'id', 'url', 'httpstatus', 'id', 'referer'\n" +
                     "    UNION (\n" +
                     "    SELECT r1.id, r1.url, r1.httpstatus, r2.id, r2.url as referer\n" +
                     "      INTO OUTFILE ?\n" +
                     "           FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"'\n" +
                     "           LINES TERMINATED BY '\\n'\n" +
                     "      FROM jspider_resource r1, jspider_resource r2, jspider_resource_reference x\n" +
                     "     WHERE r1.id = x.referee\n" +
                     "       AND r2.id = referer\n" +
                     "       AND r1.httpstatus <> 0\n" +
                     "       AND r1.httpstatus <> 200\n" +
                     "     ORDER BY 1\n" +
                     "     );\n";
        ResultSet rs = null;
        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            int i = 0;
            ps.setString( ++i, output.getAbsolutePath() );
            rs = ps.executeQuery();
        } catch ( SQLException e ) {
            log.error( "SQLException", e );
        } finally {
            dbUtil.safeClose( rs, log );
        }
        String msg = "reportNon200UrlsAll completed: " + output + ", duration " + timer;
        sendMessage( msg, SLACK_CHANNEL );
        log.info( msg );
    }

    private void reportCountsByHttpStatus( DBUtil dbUtil ) {
        StopWatch timer = new StopWatch( true );
        ReportTable report = new ReportTable( "|" );
        report.addHeaders( "status", "count" );
        String sql = "SELECT httpstatus, COUNT(*) FROM jspider_resource GROUP BY 1 UNION SELECT 'TOTAL', COUNT(*) FROM jspider_resource";
        ResultSet rs = null;
        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            rs = ps.executeQuery();
            while ( rs.next() ) {
                Object httpStatus = rs.getObject( 1 );
                int count = rs.getInt( 2 );
                report.addRow( String.valueOf( httpStatus ), String.format( "%,10d", count ) );
            }
            String reportText = report.buildReport();
            sendMessage( "JSpider Counts by HTTP Status\n" + reportText, SLACK_CHANNEL );
            log.info( "Counts By HTTP Status\n" + reportText );
        } catch ( SQLException e ) {
            log.error( "SQLException", e );
        } finally {
            dbUtil.safeClose( rs, log );
        }
        log.info( "CountsByHttpStatus completed, duration " + timer );
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

    @Override
    public void visit( EMailAddressDiscoveredEvent event ) {

    }

    @Override
    public void visit( JSpiderEvent event ) {

    }

    @Override
    public void visit( EngineRelatedEvent event ) {

    }

    @Override
    public void visit( FolderRelatedEvent event ) {

    }

    @Override
    public void visit( FolderDiscoveredEvent event ) {

    }

    @Override
    public void visit( ResourceRelatedEvent event ) {

    }

    @Override
    public void visit( EMailAddressReferenceDiscoveredEvent event ) {

    }

    @Override
    public void visit( MalformedURLFoundEvent event ) {

    }

    @Override
    public void visit( MalformedBaseURLFoundEvent event ) {

    }

    @Override
    public void visit( ResourceDiscoveredEvent event ) {

    }

    @Override
    public void visit( ResourceFetchedEvent event ) {

    }

    @Override
    public void visit( ResourceFetchErrorEvent event ) {

    }

    @Override
    public void visit( ResourceForbiddenEvent event ) {

    }

    @Override
    public void visit( ResourceParsedEvent event ) {

    }

    @Override
    public void visit( ResourceIgnoredForFetchingEvent event ) {

    }

    @Override
    public void visit( ResourceIgnoredForParsingEvent event ) {

    }

    @Override
    public void visit( ResourceReferenceDiscoveredEvent event ) {

    }

    @Override
    public void visit( SiteRelatedEvent event ) {

    }

    @Override
    public void visit( SiteDiscoveredEvent event ) {

    }

    @Override
    public void visit( RobotsTXTMissingEvent event ) {

    }

    @Override
    public void visit( RobotsTXTFetchedEvent event ) {

    }

    @Override
    public void visit( UserAgentObeyedEvent event ) {

    }
}