package net.javacoding.jspider.core.util.html;

import net.javacoding.jspider.core.util.URLUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

/**
 * $Id: URLFinder.java,v 1.9 2003/04/10 16:19:17 vanrogu Exp $
 */
public class URLFinder {

    public static final String basePattern = "<base href=";

    public static final String[] patterns = {
      "href=",
      "src=",
      "background="
    };

    public static void findURLs(URLFinderCallback callback, String line) {
        String lineLowerCase = line.toLowerCase();
        findBase(callback, line, lineLowerCase, basePattern);
        for (int i = 0; i < patterns.length; i++) {
            String pattern = patterns[i];
            findURLs(callback, line, lineLowerCase, pattern);
        }
    }

    private static void findBase(URLFinderCallback callback, String line, String lineLowerCase, String pattern) {
        int pos = lineLowerCase.indexOf(pattern);
        if ( pos != -1 ) {
            String url = "";
            try {
                url = extractURL(line, pos + pattern.length());
                URL baseURL = URLUtil.normalize(new URL(url));
                callback.setContextURL(baseURL);
            } catch (MalformedURLException e) {
                callback.malformedContextURLFound(url);
            }
        }
    }

    private static void findURLs(URLFinderCallback callback, String line, String lineLowerCase, String pattern) {
        int pos = lineLowerCase.indexOf(pattern);
        while (pos != -1) {
            String uri = "";
            try {
                uri = extractURL(line, pos + pattern.length());
                if ( !"javascript:".equals( uri ) ) {
                    URL baseURL = callback.getContextURL();
                    if ( ! URLUtil.isFileSpecified(baseURL)) {
                        // Force a slash in case of a folder (to avoid buggy relative refs)
                        baseURL = new URL(baseURL.toString() + "/");
                    }
                    URL foundURL = URLUtil.normalize(new URL(baseURL, uri));
                    callback.urlFound(foundURL);
                }
            } catch (MalformedURLException e) {
                callback.malformedUrlFound(uri);
            }
            pos = lineLowerCase.indexOf(pattern, pos + pattern.length());
        }
    }

    protected static String extractURL(String string, int pos) {
        char c = string.charAt(pos);
        String ret = "";

        // This was broken when parsing a url with an embedded apostrophe like <a href="Martha's_Vineyard-MVY/Boston-BOS/market.html">
        String tokens = " \"\'>";
        if (c == '\'' ) {
            string = string.substring(pos + 1);
            tokens = " \'>";
        } else if ( c == '"') {
            string = string.substring( pos + 1 );
            tokens = " \">";
        } else if ( c == '\\' ) {
            string = string.substring( pos + 1 );
        } else {
            string = string.substring(pos);
        }
        if (string.length() > 0) {
            c = string.charAt(0);
            if (c == '\'' || c == '\"' || c == '>') {
                ret = "";
            } else {
                StringTokenizer st = new StringTokenizer(string, tokens);
                ret = st.nextToken();
                c = ret.charAt( ret.length() - 1 );
                if ( c == '\'' || c == '"' ) {
                    ret = ret.substring( 0, ret.length() - 1 );
                }
            }
        }
        int p = ret.indexOf('#');
        if (p > -1) {
            return ret.substring(0, p);
        } else {
            return ret;
        }
    }

}
