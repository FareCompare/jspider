package net.javacoding.jspider.core.impl;


import net.javacoding.jspider.api.event.resource.*;
import net.javacoding.jspider.api.event.site.*;
import net.javacoding.jspider.api.model.*;
import net.javacoding.jspider.core.Agent;
import net.javacoding.jspider.core.SpiderContext;
import net.javacoding.jspider.core.dispatch.EventDispatcher;
import net.javacoding.jspider.core.event.CoreEvent;
import net.javacoding.jspider.core.event.CoreEventVisitor;
import net.javacoding.jspider.core.event.impl.*;
import net.javacoding.jspider.core.exception.SpideringDoneException;
import net.javacoding.jspider.core.exception.TaskAssignmentException;
import net.javacoding.jspider.core.logging.Log;
import net.javacoding.jspider.core.logging.LogFactory;
import net.javacoding.jspider.core.model.SiteInternal;
import net.javacoding.jspider.core.storage.Storage;
import net.javacoding.jspider.core.task.*;
import net.javacoding.jspider.core.task.work.*;
import net.javacoding.jspider.core.util.URLUtil;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.locks.Lock;


/**
 *
 * $Id: AgentImpl.java,v 1.32 2003/04/29 17:53:47 vanrogu Exp $
 *
 * @author Gunther Van Roey
 */
public class AgentImpl implements Agent, CoreEventVisitor {

    protected Storage storage;
    protected SpiderContext context;
    protected EventDispatcher eventDispatcher;
    protected Scheduler scheduler;
    protected Log log;


    public AgentImpl(SpiderContext context) {
        this.context = context;
        this.storage = context.getStorage();
        this.eventDispatcher = context.getEventDispatcher();
        this.scheduler = new SchedulerFactory().createScheduler(context);

        log = LogFactory.getLog(Agent.class);

    }

    public void start() {
        URL baseURL = context.getBaseURL();
        Lock lock = context.getLock( baseURL );
        lock.lock();
        try {
            visit( null, new URLFoundEvent( context, null, baseURL ) );
        } finally {
            lock.unlock();
        }
    }

    public synchronized void flagDone(WorkerTask task) {
        scheduler.flagDone(task);
        notifyAll();
    }

    public WorkerTask getThinkerTask() throws TaskAssignmentException {
        while (true) {
            try {
                return scheduler.getThinkerTask();
            } catch (SpideringDoneException e) {
                throw e;
            } catch (TaskAssignmentException e) {
                try {
                    Thread.sleep( 1000 );
                    checkBlockedUrls();
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public WorkerTask getSpiderTask() throws TaskAssignmentException {
        while (true) {
            try {
                return scheduler.getFetchTask();
            } catch (SpideringDoneException e) {
                throw e;
            } catch (TaskAssignmentException e) {
                try {
                    Thread.sleep( 1000 );
                    checkBlockedUrls();
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * @param foundURL
     */
    public void scheduleForSpidering(URL foundURL) {
        Lock lock = context.getLock( foundURL );
        lock.lock();
        try {
            URL siteURL = URLUtil.getSiteURL(foundURL);
            Site site = storage.getSiteDAO().find(siteURL);
            scheduler.schedule( new SpiderHttpURLTask( context, foundURL, site ) );
        } finally {
            lock.unlock();
        }
    }

    public void scheduleForParsing(URL url) {
        Lock lock = context.getLock( url );
        lock.lock();
        try {
            scheduler.schedule(
                    new InterpreteHTMLTask( context, (FetchedResource) storage.getResourceDAO().getResource( url ) ) );
        } finally {
            lock.unlock();
        }
    }

    public void registerEvent(URL url, CoreEvent event) {
        Lock lock = context.getLock( url );
        lock.lock();
        try {
            event.accept(url, this);
        } finally {
            lock.unlock();
        }
    }


    public void visit(URL url, CoreEvent event ) {
        log.error( "ERROR -- UNHANDLED COREEVENT IN AGENT !!!" );
    }

    public void visit(URL url, URLSpideredOkEvent event) {
        storage.getResourceDAO().setSpidered(url, event);
        eventDispatcher.dispatch( new ResourceFetchedEvent( storage.getResourceDAO().getResource( url ) ) );
        scheduler.schedule( new DecideOnParsingTask( context, url ) );
    }

    public void visit(URL url, URLSpideredErrorEvent event) {
        storage.getResourceDAO().setError( url, event );
        eventDispatcher.dispatch(
                new ResourceFetchErrorEvent( storage.getResourceDAO().getResource(url), event.getHttpStatus()));
    }

    public void visit(URL url, ResourceParsedOkEvent event) {
        storage.getResourceDAO().setParsed(url, event);
        eventDispatcher.dispatch( new ResourceParsedEvent( storage.getResourceDAO().getResource( url ) ) );
    }

    public void visit(URL url, ResourceParsedErrorEvent event) {
        storage.getResourceDAO().setError( url, event );
    }

    public void visit(URL url, URLFoundEvent event) {
        final URL foundURL = event.getFoundURL();
        final URL siteURL = URLUtil.getSiteURL(foundURL);
        Site site = storage.getSiteDAO().find(siteURL);

        boolean newSite = site == null;
        if ( newSite ) {
            site = storage.getSiteDAO().createSite(siteURL);
            context.registerNewSite(site);
            storage.getSiteDAO().save(site);
            eventDispatcher.dispatch(new SiteDiscoveredEvent(site));
        }

        Resource resource = storage.getResourceDAO().getResource( foundURL );
        boolean newResource = (resource == null);
        if ( newResource) {
            resource = storage.getResourceDAO().registerURL(foundURL);
        }

        if ( newSite ) {

            if (site.getFetchRobotsTXT()) {
                if (site.mustHandle()) {
                    URL robotsTXTUrl = URLUtil.getRobotsTXTURL(siteURL);
                    scheduler.schedule(new FetchRobotsTXTTaskImpl(context, robotsTXTUrl, site));
                    if (newResource) {
                        scheduler.block(siteURL, new DecideOnSpideringTask(context, new URLFoundEvent(context, url, foundURL)));
                    }
                }

            } else {
                if (site.mustHandle()) {
                    ((SiteInternal) site).registerRobotsTXTSkipped();
                    context.registerRobotsTXTSkipped(site);
                    storage.getSiteDAO().save(site);
                    eventDispatcher.dispatch(new RobotsTXTSkippedEvent(site));
                    if (newResource) {
                        scheduler.schedule(new DecideOnSpideringTask(context, event));
                    }
                }
            }
        } else if (site.isRobotsTXTHandled()) {
            if (newResource) {
                scheduler.schedule(new DecideOnSpideringTask(context, event));
            }
        } else {
            if (site.mustHandle()) {
                if (newResource) {
                    scheduler.block(siteURL, new DecideOnSpideringTask(context, new URLFoundEvent(context, url, foundURL)));
                }
            }
        }

        if (newResource) {
            if ( !site.mustHandle()) {
                storage.getResourceDAO().setIgnoredForFetching(foundURL, event);
            }
            eventDispatcher.dispatch(new ResourceDiscoveredEvent( resource ));
        }
        storage.getResourceDAO().registerURLReference(foundURL, url);
        if ( url != null ) {
            eventDispatcher.dispatch( new ResourceReferenceDiscoveredEvent( storage.getResourceDAO().getResource( url ),
                                                                            resource ) );
        }

    }

    public void visit(URL url, RobotsTXTSpideredOkEvent event) {
        URL robotsTxtURL = event.getRobotsTXTURL();
        URL siteURL = URLUtil.getSiteURL(robotsTxtURL);
        SiteInternal site = (SiteInternal) storage.getSiteDAO().find(siteURL);

        unblock( siteURL );

        storage.getResourceDAO().registerURL(robotsTxtURL);
        storage.getResourceDAO().setSpidered(robotsTxtURL, event);
        storage.getResourceDAO().setIgnoredForParsing(robotsTxtURL);
        Resource resource = storage.getResourceDAO().getResource(robotsTxtURL );
        byte[] bytes = event.getBytes();
        site.registerRobotsTXT();
        eventDispatcher.dispatch(new ResourceDiscoveredEvent(resource));
        eventDispatcher.dispatch(new ResourceFetchedEvent(resource));
        eventDispatcher.dispatch(new RobotsTXTFetchedEvent(site, new String(bytes)));
        context.registerRobotsTXT(site, new ByteArrayInputStream(bytes ) );
        storage.getSiteDAO().save(site);
    }

    public void visit(URL url, RobotsTXTSpideredErrorEvent event) {
        URL robotsTxtURL = event.getRobotsTXTURL();
        URL siteURL = URLUtil.getSiteURL(robotsTxtURL);
        Site site = storage.getSiteDAO().find(siteURL);
        ((SiteInternal) site).registerRobotsTXTError();

        unblock( siteURL );

        storage.getResourceDAO().registerURL(robotsTxtURL);
        storage.getResourceDAO().setError(robotsTxtURL, event);
        eventDispatcher.dispatch(new RobotsTXTFetchErrorEvent(site, event.getException()));
        context.registerRobotsTXTError(site);
        storage.getSiteDAO().save(site);
    }

    public void visit(URL url, RobotsTXTUnexistingEvent event) {
        URL robotsTxtURL = event.getRobotsTXTURL();
        URL siteURL = URLUtil.getSiteURL(robotsTxtURL);
        Site site = storage.getSiteDAO().find(siteURL);
        ((SiteInternal) site).registerNoRobotsTXTFound();

        unblock( siteURL );
        storage.getSiteDAO().save( site );
        eventDispatcher.dispatch(new RobotsTXTMissingEvent(site));
    }

    public void visit(URL url, RobotsTXTSkippedEvent event) {
        Site site = event.getSite();
        ((SiteInternal) site).registerRobotsTXTSkipped();
        URL siteURL = site.getURL();
        unblock( siteURL );
        storage.getSiteDAO().save(site);
    }

    private void unblock( URL siteURL ) {
        DecideOnSpideringTask[] tasks = scheduler.unblock(siteURL);
        for ( DecideOnSpideringTask task : tasks ) {
            scheduler.schedule( task );
        }
    }

    private void checkBlockedUrls() {
        if ( scheduler.hasOnlyBlockedTasks() ) {
            log.warn( "Scheduler has only blocked tasks.  Will auto unblock." );
            Set<URL> blockedUrls = scheduler.getBlockedUrls();
            for ( URL url : blockedUrls ) {
                log.info( "Unblocking URL: " + url );
                unblock( url );
            }
        }
    }
}
