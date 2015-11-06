package net.javacoding.jspider.core.storage.spi;

import java.io.InputStream;

/**
 * $Id: ContentDAOSPI.java,v 1.1 2003/04/11 16:37:07 vanrogu Exp $
 */
public interface ContentDAOSPI {
    InputStream getInputStream( int id );

    void setBytes( int i, byte[] bytes );
}
