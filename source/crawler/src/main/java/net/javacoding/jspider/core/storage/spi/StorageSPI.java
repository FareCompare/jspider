package net.javacoding.jspider.core.storage.spi;

/**
 * $Id: StorageSPI.java,v 1.1 2003/04/11 16:37:08 vanrogu Exp $
 *
 * todo add id-references as much as possible
 */
public interface StorageSPI {
    SiteDAOSPI getSiteDAO();

    ResourceDAOSPI getResourceDAO();

    ContentDAOSPI getContentDAO();

    DecisionDAOSPI getDecisionDAO();

    CookieDAOSPI getCookieDAO();

    EMailAddressDAOSPI getEMailAddressDAO();

    FolderDAOSPI getFolderDAO();
}
