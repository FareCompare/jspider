package net.javacoding.jspider.core.threading;

import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.concurrent.locks.Lock;

import static org.junit.Assert.assertNotNull;

/**
 * <p><tt>TestLockTable</tt> </p>
 *
 * @author <a href="mailto:cstillwell@farecompare.com">cstillwell</a>
 * @version $Revision: 1.1 $
 */
public class TestLockTable {

    private LockTable lockTable;

    public TestLockTable() {
    }

    @Before
    public void setup() {
        lockTable = new LockTable( 100 );
    }

    @Test
    public void testGetIndex() throws Exception {
        int lockTableSize = 100;

        URL url = new URL( "http://www.farecompare.mx" );
        System.out.println(Math.abs( url.hashCode() % lockTableSize ));

        url = new URL( "http://www.farecompare.mx/vuelos/Londres-LON/city.html" );
        System.out.println( Math.abs( url.hashCode() % lockTableSize ));

        url = new URL( "http://www.farecompare.mx/vuelos/Monterrey-MTY/Londres-LON/market.html" );
        System.out.println( Math.abs( url.hashCode() % lockTableSize ));
    }

    @Test
    public void test01() throws Exception {
        URL url = new URL( "http://www.farecompare.mx" );
        Lock lock = lockTable.getLock( url );
        assertNotNull(lock) ;
    }

}
