package net.javacoding.jspider.core.threading;

import java.net.URL;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p><tt>LockTable</tt> </p>
 *
 * @author <a href="mailto:cstillwell@farecompare.com">cstillwell</a>
 * @version $Revision: 1.1 $
 */
public class LockTable {

    private int lockTableSize;

    private Lock[] locks;

    public LockTable(int size ) {
        lockTableSize = size;
        locks = new Lock[lockTableSize];
        for ( int i = 0; i < lockTableSize; i++ ) {
            locks[i] = new ReentrantLock(  );
        }
    }

    public Lock getLock( URL url ) {
        return locks[Math.abs( url.hashCode() % lockTableSize )];
    }
}
