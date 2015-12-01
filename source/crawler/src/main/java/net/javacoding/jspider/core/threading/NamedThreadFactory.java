package net.javacoding.jspider.core.threading;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p><tt>NamedThreadFactory</tt> is based off of the default thread factory from
 * {@link java.util.concurrent.Executors#defaultThreadFactory()}, but allows you to
 * specify a name that gets prepended to each thread created in the thread pool.
 * The intent is to allow developers to give a unique name for threads used
 * in a thread pool so they can be more easily identified in thread dumps.
 * <p>
 * <b>Example:</b>
 * <pre>
 * _executor = new ThreadPoolExecutor( corePoolSize,
 *                                     maximumPoolSize,
 *                                     keepAliveTime,
 *                                     TimeUnit.SECONDS,
 *                                     new LinkedBlockingQueue<Runnable>(),
 *                                     new NamedThreadFactory( "PurgeProcessor" ) );
 * </pre>
 * In the above example threads will be created with a name similar to: PurgeProcessor-pool-1-thread-1,
 * where the thread number is incremented for each new thread created.
 * </p>
 *
 * @see java.util.concurrent.Executors#defaultThreadFactory()
 *
 * @author <a href="mailto:cstillwell@farecompare.com">cstillwell</a>
 * @version $Revision: 1.1 $
 */
public class NamedThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public NamedThreadFactory( String poolName, String threadName ) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                              Thread.currentThread().getThreadGroup();
        namePrefix = poolName + "-" +
                      poolNumber.getAndIncrement() +
                     "-" + threadName;
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                              namePrefix + threadNumber.getAndIncrement(),
                              0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}


