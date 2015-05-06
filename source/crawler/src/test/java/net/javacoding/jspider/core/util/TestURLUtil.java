package net.javacoding.jspider.core.util;

import org.junit.Test;

import java.net.URL;

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
        URL baseURL = new URL(urlString);
        URL normalize = URLUtil.normalize( baseURL );
        System.out.println( normalize );
    }
}
