package net.javacoding.jspider.core.util.html;

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

    @Before
    public void setup() throws Exception {
        inputStream = getClass().getClassLoader().getResourceAsStream( "sobre.html" );
        url = new URL( "http://www.farecompare.mx//sobre.html" );
    }

    @Test
    public void testFindUrls() throws Exception {
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
                }

                @Override
                public void malformedUrlFound( String malformedURL ) {
                    System.out.println( "malformedUrlFound: " + malformedURL );
                }
            }, line);
        }

    }
}
