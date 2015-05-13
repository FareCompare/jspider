package net.javacoding.jspider.core.util;

import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * <p><tt>TestURLUtil</tt> </p>
 *
 * @author <a href="mailto:cstillwell@farecompare.com">cstillwell</a>
 * @version $Revision: 1.1 $
 */
public class TestURLUtil {

    public TestURLUtil() {
    }

    @Test
    public void test01() throws Exception {
        String urlString = "http://alpha.farecompare.com/es";
        URL baseURL = new URL( urlString );
        URL normalize = URLUtil.normalize( baseURL );
        System.out.println( normalize );
    }

    @Test
    public void test02() throws Exception {
        String urlString = "http://alpha.farecompare.com/es";
        URL baseURL = new URL( urlString );
        String path = baseURL.getPath();
        String baseUrlPath = URLUtil.stripResource( path );
        assertEquals("/es", baseUrlPath);

        URL url = new URL("http://alpha.farecompare.com/es/vuelos/Filadelfia-PHL/Nueva_York-NYC/market.html");
        assertEquals( "/es/vuelos/Filadelfia-PHL/Nueva_York-NYC/", URLUtil.stripResource( url.getPath() ) );

        url = new URL("http://alpha.farecompare.com/flights/Atlanta-ATL/Fort_Lauderdale-FLL/market.html");
        assertEquals( "/flights/Atlanta-ATL/Fort_Lauderdale-FLL/", URLUtil.stripResource( url.getPath() ) );
    }
}
