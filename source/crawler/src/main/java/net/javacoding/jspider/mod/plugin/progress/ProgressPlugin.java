package net.javacoding.jspider.mod.plugin.progress;

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
import net.javacoding.jspider.spi.Plugin;

import java.util.concurrent.TimeUnit;

/**
 * <p><tt>ProgressPlugin</tt> </p>
 *
 * @author <a href="mailto:cstillwell@farecompare.com">cstillwell</a>
 * @version $Revision: 1.1 $
 */
public class ProgressPlugin implements Plugin, EventVisitor {

    private String prefix = "[Progress] ";
    private long startTime;
    private long nextReportTime;
    private long reportInterval = TimeUnit.SECONDS.toMillis( 30 );

    private Log log = LogFactory.getLog( this.getClass() );

    private int resourceDiscoveredCount;
    private int malformedUrlCount;

    public ProgressPlugin() {
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getDescription() {
        return "Used to show progress of resources discovered";
    }

    @Override
    public String getVendor() {
        return "FC";
    }

    @Override
    public void initialize() {
        log.info( "initialize" );
    }

    @Override
    public void notify( JSpiderEvent event ) {

    }

    @Override
    public void shutdown() {
        log.info( "shutdown" );
    }

    @Override
    public void visit( EMailAddressDiscoveredEvent event ) {
        //todo implement
    }

    @Override
    public void visit( JSpiderEvent event ) {
        println(event);
    }

    @Override
    public void visit( EngineRelatedEvent event ) {
        //todo implement
    }

    @Override
    public void visit( SpideringStartedEvent event ) {
        startTime = System.currentTimeMillis();
        nextReportTime = startTime + reportInterval;
        println( "ProgressPlugin received SpideringStartedEvent event" );
    }

    @Override
    public void visit( SpideringStoppedEvent event ) {
        println( "ProgressPlugin received SpideringStoppedEvent event, duration " + duration() );
    }

    @Override
    public void visit( FolderRelatedEvent event ) {
        //todo implement
    }

    @Override
    public void visit( FolderDiscoveredEvent event ) {
        //todo implement
    }

    @Override
    public void visit( ResourceRelatedEvent event ) {
        //todo implement
    }

    @Override
    public void visit( EMailAddressReferenceDiscoveredEvent event ) {
        //todo implement
    }

    @Override
    public void visit( MalformedURLFoundEvent event ) {
        malformedUrlCount++;
    }

    @Override
    public void visit( MalformedBaseURLFoundEvent event ) {
        //todo implement
    }

    @Override
    public void visit( ResourceDiscoveredEvent event ) {
        resourceDiscoveredCount++;
        long currentTime = System.currentTimeMillis();
        if ( currentTime >= nextReportTime ) {
            reportProgress();
            nextReportTime = currentTime + reportInterval;
        }
    }

    @Override
    public void visit( ResourceFetchedEvent event ) {
        //todo implement
    }

    @Override
    public void visit( ResourceFetchErrorEvent event ) {
        //todo implement
    }

    @Override
    public void visit( ResourceForbiddenEvent event ) {
        //todo implement
    }

    @Override
    public void visit( ResourceParsedEvent event ) {
        //todo implement
    }

    @Override
    public void visit( ResourceIgnoredForFetchingEvent event ) {
        //todo implement
    }

    @Override
    public void visit( ResourceIgnoredForParsingEvent event ) {
        //todo implement
    }

    @Override
    public void visit( ResourceReferenceDiscoveredEvent event ) {
        //todo implement
    }

    @Override
    public void visit( SiteRelatedEvent event ) {
        //todo implement
    }

    @Override
    public void visit( SiteDiscoveredEvent event ) {
        //todo implement
    }

    @Override
    public void visit( RobotsTXTMissingEvent event ) {
        //todo implement
    }

    @Override
    public void visit( RobotsTXTFetchedEvent event ) {
        //todo implement
    }

    @Override
    public void visit( UserAgentObeyedEvent event ) {
        //todo implement
    }

    private void reportProgress() {
        println( String.format( "Discovered %,d resources, %,d malformed, duration %s",
                                 resourceDiscoveredCount, malformedUrlCount, duration() ) );
    }

    private String duration() {
        return formatDuration( System.currentTimeMillis() - startTime );
    }

    public String formatDuration( long duration ) {

        long ONE_SECOND = TimeUnit.SECONDS.toMillis( 1 );
        long ONE_MINUTE = TimeUnit.MINUTES.toMillis( 1 );
        long ONE_HOUR = TimeUnit.HOURS.toMillis( 1 );

        final long hours = duration / (ONE_HOUR);
        duration = duration % (ONE_HOUR);

        final long minutes = duration / (ONE_MINUTE);
        duration = duration % (ONE_MINUTE);

        final long seconds = duration / ONE_SECOND;
        final long milli = duration % ONE_SECOND;

        return String.format( "%02d:%02d:%02d.%03d%n", hours, minutes, seconds, milli );
    }

    protected void println(Object object) {
        System.out.println(prefix + object);
    }

}
