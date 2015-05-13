package net.javacoding.jspider.core.util.html;

import net.javacoding.jspider.core.util.statistics.StopWatch;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * <p><tt>TestURLFinder</tt> </p>
 *
 * @author <a href="mailto:cstillwell@farecompare.com">cstillwell</a>
 * @version $Revision: 1.1 $
 */
public class TestURLFinder {

    private InputStream inputStream;
    private URL url;
    private int foundCount;

    @Before
    public void setup() throws Exception {
//        inputStream = getClass().getClassLoader().getResourceAsStream( "sobre.html" );
//        url = new URL( "http://www.farecompare.mx//sobre.html" );

//        inputStream = getClass().getClassLoader().getResourceAsStream( "NYC-BOS-market.html" );
//        url = new URL( "http://www.farecompare.com/flights/New_York-NYC/Boston-BOS/market.html" );

//        inputStream = getClass().getClassLoader().getResourceAsStream( "LON-city.html" );
//        url = new URL( "http://www.farecompare.mx/vuelos/Londres-LON/city.html" );

//        inputStream = getClass().getClassLoader().getResourceAsStream( "es-BOS.city.html" );
//        url = new URL( "http://alpha.farecompare.com/es/vuelos/Boston-BOS/city.html" );

        inputStream = getClass().getClassLoader().getResourceAsStream( "USMA-state.html" );
        url = new URL( "http://alpha.farecompare.com/es/vuelos/Massachusetts-USMA/state.html" );
    }

    @Test
    public void testFindUrls() throws Exception {
        StopWatch timer = new StopWatch( true );
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ( (line = br.readLine()) != null ) {
            URLFinder.findURLs(new URLFinderCallback() {

                @Override
                public URL getContextURL() {
                    return url;
                }

                @Override
                public void setContextURL( URL url ) {
                    throw new UnsupportedOperationException(  );
                }

                @Override
                public void malformedContextURLFound( String malformedURL ) {
                    System.out.println( "malformedContextURLFound: " + malformedURL );
                }

                @Override
                public void urlFound( URL foundURL ) {
                    System.out.println( "urlFound: " + foundURL );
                    foundCount++;
                }

                @Override
                public void malformedUrlFound( String malformedURL ) {
                    System.out.println( "malformedUrlFound: " + malformedURL );
                }
            }, line);
        }
        System.out.printf("Found %,d URLs, duration %s%n", foundCount, timer );
    }
}
