package net.javacoding.jspider.core.storage;


import net.javacoding.jspider.api.model.Summary;
import net.javacoding.jspider.core.storage.spi.StorageSPI;


/**
 *
 * $Id: Storage.java,v 1.17 2003/04/11 16:37:05 vanrogu Exp $
 *
 * @author G�nther Van Roey
 */
public interface Storage {

    SiteDAO getSiteDAO ( );
    ResourceDAO getResourceDAO ( );
    DecisionDAO getDecisionDAO ( );
    CookieDAO getCookieDAO ( );
    EMailAddressDAO getEMailAddressDAO ( );
    Summary getSummary ( );
    StorageSPI getStorageSPI();
}
