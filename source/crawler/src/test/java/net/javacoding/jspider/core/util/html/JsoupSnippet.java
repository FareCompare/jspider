package net.javacoding.jspider.core.util.html;

import net.javacoding.jspider.core.util.statistics.StopWatch;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;


/**
 * <p><tt>JsoupSnippet</tt> </p>
 *
 * @author <a href="mailto:cstillwell@farecompare.com">cstillwell</a>
 * @version $Revision: 1.1 $
 */
public class JsoupSnippet {

    public JsoupSnippet() {
    }

    @Test
    public void test01() throws Exception {
        StopWatch totalTimer = new StopWatch( true );
//        Connection connect = Jsoup.connect( "http://www.farecompare.com" );
//        Connection connect = Jsoup.connect( "http://www.farecompare.com/flights/London-LON/city.html" );
        Connection connect = Jsoup.connect( "http://www.farecompare.com/flights/New_York-NYC/London-LON/market.html#quote" );
        Document doc = connect.get();

        Elements elements = doc.select( "a" );
        System.out.printf("Found %,d links%n", elements.size() );
        for ( Element element : elements ) {
            StopWatch timer = new StopWatch( true );
            try {
                String href = element.absUrl( "href" );
                if ( href.length() == 0 ) {
                    System.out.println("Skipping: " + element );
                    continue;
                }
                System.out.println( "urlFound: " + href );
                URL url = new URL( href );
                URLConnection connection = url.openConnection();

                if (connection instanceof HttpURLConnection ) {
                    ((HttpURLConnection) connection).setInstanceFollowRedirects(false);
                }

                connection.connect();
                if (connection instanceof HttpURLConnection ) {
                    int httpStatus = ((HttpURLConnection) connection).getResponseCode();
                    System.out.printf( "%s : %s, duration %s%n", httpStatus, href, timer );
                } else {
                    System.out.println("??? : " + href );
                }
            } catch ( Exception e ) {
                System.out.println("Failed: " + element );
            }
        }
        System.out.printf("Processed %,d urls, duration %s%n", elements.size(), totalTimer );
    }

    @Test
    public void testFindUrls() throws Exception {
        String url = "http://www.farecompare.com/flights/New_York-NYC/London-LON/market.html#quote";
        Set<String> urls = new TreeSet<>( findUrls( url ) );
        for ( String u : urls ) {
            System.out.println(u);
        }
    }

    public Set<String> findUrls( String page ) throws IOException {
        StopWatch totalTimer = new StopWatch( true );
        Set<String> urls = new HashSet<>(  );
        Connection connect = Jsoup.connect( page );
        Document doc = connect.get();


        Elements elements = doc.select( "a[href]" );
        System.out.printf("Found %,d href links%n", elements.size() );
        for ( Element element : elements ) {
            try {
                String href = element.absUrl( "href" );
                if ( href.length() == 0 ) {
                    System.out.println( "Skipping: " + element );
                    continue;
                }
                if ( href.endsWith( "#" )) {
                    href = href.substring( 0, href.length()-1 );
                }
                urls.add( href );
            } catch ( Exception e ) {
                System.out.println( "Failed: " + element );
            }
        }

        elements = doc.select( "img[src]" );
        System.out.printf("Found %,d img links%n", elements.size() );
        for ( Element element : elements ) {
            try {
                String href = element.absUrl( "src" );
                if ( href.length() == 0 ) {
                    System.out.println( "Skipping: " + element );
                    continue;
                }
                urls.add( href );
            } catch ( Exception e ) {
                System.out.println( "Failed: " + element );
            }
        }

        System.out.printf("Found %,d urls, duration %s%n", urls.size(), totalTimer );
        return urls;
    }
}
