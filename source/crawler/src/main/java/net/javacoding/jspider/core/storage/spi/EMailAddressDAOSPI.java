package net.javacoding.jspider.core.storage.spi;

import net.javacoding.jspider.core.model.EMailAddressInternal;
import net.javacoding.jspider.core.model.EMailAddressReferenceInternal;
import net.javacoding.jspider.core.model.ResourceInternal;

/**
 * $Id: EMailAddressDAOSPI.java,v 1.1 2003/04/11 16:37:08 vanrogu Exp $
 */
public interface EMailAddressDAOSPI {
    EMailAddressInternal find( String address );

    void register( ResourceInternal resource, EMailAddressInternal address );

    EMailAddressInternal[] findByResource( ResourceInternal resource );

    EMailAddressReferenceInternal[] findReferencesByResource( ResourceInternal resource );
}
