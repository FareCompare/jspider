package net.javacoding.jspider.core.storage.spi;

import net.javacoding.jspider.api.model.Cookie;

/**
 * $Id: CookieDAOSPI.java,v 1.1 2003/04/11 16:37:08 vanrogu Exp $
 */
public interface CookieDAOSPI {
    Cookie[] find( int id );

    void save( int id, Cookie[] cookies );
}
