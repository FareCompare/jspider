package net.javacoding.jspider.mod.plugin.statusbasedfilewriter;

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
import net.javacoding.jspider.api.model.FetchTriedResource;
import net.javacoding.jspider.api.model.Resource;
import net.javacoding.jspider.core.logging.Log;
import net.javacoding.jspider.core.logging.LogFactory;
import net.javacoding.jspider.core.util.config.ConfigurationFactory;
import net.javacoding.jspider.spi.Plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;

/**
 * $Id: StatusBasedFileWriterPlugin.java,v 1.10 2003/04/08 15:50:38 vanrogu Exp $
 */
public class StatusBasedFileWriterPlugin implements Plugin, EventVisitor {

    public static final String MODULE_NAME = "Status based  Filewriter JSpider plugin";
    public static final String MODULE_VERSION = "v1.0";
    public static final String MODULE_DESCRIPTION = "A JSpider plugin that writes a report file per HTTP status";
    public static final String MODULE_VENDOR = "http://www.javacoding.net";

    protected Log log;

    protected HashMap<Integer,PrintWriter> fileWriters;

    public StatusBasedFileWriterPlugin ( ) {
        log = LogFactory.getLog ( StatusBasedFileWriterPlugin.class );
        fileWriters = new HashMap<>( );
        log.info("initialized." );
    }

    public void initialize() {
    }

    public void shutdown() {
    }

    public String getName() {
        return MODULE_NAME;
    }

    public String getVersion() {
        return MODULE_VERSION;
    }

    public String getDescription() {
        return MODULE_DESCRIPTION;
    }

    public String getVendor() {
        return MODULE_VENDOR;
    }

    public void notify(JSpiderEvent event) {
        event.accept(this);
    }

    public void visit(JSpiderEvent event) {
    }

    public void visit(EngineRelatedEvent event) {
    }

    public void visit(SpideringStartedEvent event) {
    }

    public void visit(SpideringStoppedEvent event) {
        Collection printWriters = fileWriters.values();
        for ( Object printWriter : printWriters ) {
            PrintWriter pw = (PrintWriter) printWriter;
            pw.close();
        }
    }

    public void visit(FolderRelatedEvent event) {
    }

    public void visit(FolderDiscoveredEvent event) {
    }

    public void visit(ResourceRelatedEvent event) {
    }

    public void visit(EMailAddressDiscoveredEvent event) {
    }

    public void visit(EMailAddressReferenceDiscoveredEvent event) {
    }

    public void visit(MalformedURLFoundEvent event) {
    }

    public void visit(MalformedBaseURLFoundEvent event) {
    }

    public void visit(ResourceDiscoveredEvent event) {
    }

    public void visit(ResourceFetchedEvent event) {
        FetchTriedResource resource = event.getResource();
        int state = resource.getHttpStatus();
        writeInFile ( state, resource );
    }

    public void visit(ResourceFetchErrorEvent event) {
        FetchTriedResource resource = event.getResource();
        int state = resource.getHttpStatus();
        writeInFileWithReferer ( state, resource );
    }

    public void visit(ResourceForbiddenEvent event) {
    }

    public void visit(ResourceParsedEvent event) {
    }

    public void visit(ResourceIgnoredForFetchingEvent event) {
    }

    public void visit(ResourceIgnoredForParsingEvent event) {
    }

    public void visit(ResourceReferenceDiscoveredEvent event) {
    }

    public void visit(SiteRelatedEvent event) {
    }

    public void visit(SiteDiscoveredEvent event) {
    }

    public void visit(RobotsTXTMissingEvent event) {
    }

    public void visit(RobotsTXTFetchedEvent event) {
    }

    public void visit(UserAgentObeyedEvent event) {
    }

    protected void writeInFile ( int state, Resource resource ) {
        PrintWriter pw = getFileWriter(state);
        pw.println(resource.getURL());
    }

    protected void writeInFileWithReferer ( int state, Resource resource ) {
        PrintWriter pw = getFileWriter(state);
        pw.println(resource.getURL());
        pw.println("  REFERED BY:");
        Resource[] referers = resource.getReferers();
        for ( Resource referer : referers ) {
            pw.println( "  " + referer.getURL() );
        }
    }

    protected PrintWriter getFileWriter ( int state ) {
        try {
            PrintWriter retVal = fileWriters.get( state );
            if ( retVal == null ) {
                log.info("creating file for status '" + state + "'" );
                retVal = new PrintWriter ( new FileOutputStream (new File(ConfigurationFactory.getConfiguration().getDefaultOutputFolder(), state + ".out")));
                log.debug("opened file for status '" + state + "'" );
                fileWriters.put( state, retVal);
            }
            return retVal;
        } catch (IOException e) {
            log.error("i/o exception writing file for state " + state, e);
        }
        return null;
    }

}
