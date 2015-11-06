package net.javacoding.jspider.core.storage.spi;

import net.javacoding.jspider.core.event.impl.ResourceParsedErrorEvent;
import net.javacoding.jspider.core.event.impl.ResourceParsedOkEvent;
import net.javacoding.jspider.core.event.impl.URLFoundEvent;
import net.javacoding.jspider.core.event.impl.URLSpideredErrorEvent;
import net.javacoding.jspider.core.event.impl.URLSpideredOkEvent;
import net.javacoding.jspider.core.model.FolderInternal;
import net.javacoding.jspider.core.model.ResourceInternal;
import net.javacoding.jspider.core.model.ResourceReferenceInternal;
import net.javacoding.jspider.core.model.SiteInternal;
import net.javacoding.jspider.core.storage.exception.InvalidStateTransitionException;

import java.net.URL;

/**
 * $Id: ResourceDAOSPI.java,v 1.1 2003/04/11 16:37:08 vanrogu Exp $
 */
public interface ResourceDAOSPI {
    void create( int id, ResourceInternal resource );

    void registerURLReference( URL url, URL referer );

    ResourceInternal[] findAllResources();

    ResourceInternal[] getRefereringResources( ResourceInternal resource );

    ResourceInternal[] getReferencedResources( ResourceInternal resource );

    ResourceReferenceInternal[] getIncomingReferences( ResourceInternal resource );

    ResourceReferenceInternal[] getOutgoingReferences( ResourceInternal resource );

    ResourceInternal[] getRootResources( SiteInternal site );

    ResourceInternal[] getBySite( SiteInternal site );

    ResourceInternal[] findByFolder( FolderInternal folder );

    ResourceInternal getResource( int id );

    ResourceInternal getResource( URL url );

    void setSpidered( URL url, URLSpideredOkEvent event );

    void setIgnoredForParsing( URL url ) throws InvalidStateTransitionException;

    void setIgnoredForFetching( URL url, URLFoundEvent event ) throws InvalidStateTransitionException;

    void setForbidden( URL url, URLFoundEvent event ) throws InvalidStateTransitionException;

    void setError( URL url, ResourceParsedErrorEvent event ) throws InvalidStateTransitionException;

    void setParsed( URL url, ResourceParsedOkEvent event ) throws InvalidStateTransitionException;

    void setError( URL url, URLSpideredErrorEvent event ) throws InvalidStateTransitionException;
}
