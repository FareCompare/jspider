package net.javacoding.jspider.core.storage.spi;

import net.javacoding.jspider.core.model.FolderInternal;
import net.javacoding.jspider.core.model.SiteInternal;

/**
 * $Id: FolderDAOSPI.java,v 1.1 2003/04/11 16:37:08 vanrogu Exp $
 */
public interface FolderDAOSPI {
    FolderInternal findById( int id );

    FolderInternal[] findSubFolders( FolderInternal folder );

    FolderInternal[] findSiteRootFolders( SiteInternal site );

    FolderInternal createFolder( int id, FolderInternal parent, String name );

    FolderInternal createFolder( int id, SiteInternal site, String name );
}
