package net.javacoding.jspider.core.threading;

/**
 * <p><tt>ThreadPoolTerminatedException</tt> </p>
 *
 * @author <a href="mailto:cstillwell@farecompare.com">cstillwell</a>
 * @version $Revision: 1.1 $
 */
public class ThreadPoolTerminatedException extends RuntimeException {
    public ThreadPoolTerminatedException( String message ) {
        super( message );
    }
}
