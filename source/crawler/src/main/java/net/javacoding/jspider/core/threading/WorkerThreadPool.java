package net.javacoding.jspider.core.threading;


import net.javacoding.jspider.core.task.DispatcherTask;
import net.javacoding.jspider.core.task.WorkerTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Thread Pool implementation that will be used for pooling the spider and
 * parser threads.
 *
 * $Id: WorkerThreadPool.java,v 1.7 2003/02/27 16:47:49 vanrogu Exp $
 *
 * @author G�nther Van Roey
 */
public class WorkerThreadPool extends ThreadGroup {

    /** Task Dispatcher thread associated with this threadpool. */
    protected DispatcherThread dispatcherThread;

    /** Array of threads in the pool. */
    protected WorkerThread[] pool;

    /** Size of the pool. */
    protected int poolSize;

    private ThreadPoolExecutor executor;

    /**
     * Public constructor
     * @param poolName name of the threadPool
     * @param threadName name for the worker Threads
     * @param poolSize number of threads in the pool
     */
    public WorkerThreadPool(String poolName, String threadName, int poolSize) {
        super(poolName);

        this.poolSize = poolSize;
//        executor = new ThreadPoolExecutor( poolSize,
//                                              poolSize,
//                                              60,
//                                              TimeUnit.SECONDS,
//                                              new SynchronousQueue<Runnable>(),
//                                              new NamedThreadFactory( poolName, threadName ) );


        dispatcherThread = new DispatcherThread(this, threadName + " dispatcher", this);
        pool = new WorkerThread[poolSize];
        for (int i = 0; i < poolSize; i++) {
            pool[i] = new WorkerThread(this, threadName, i);
            synchronized (this) {
                try {
                    pool[i].start();
                    wait(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Assigns a worker task to the pool.  The threadPool will select a worker
     * thread to execute the task.
     * @param task the WorkerTask to be executed.
     */
    public synchronized void assign(WorkerTask task) {
        while (true) {
            for (int i = 0; i < poolSize; i++) {
                if (pool[i].isAvailable()) {
                    pool[i].assign(task);
                    return;
                }
            }
            try {
                wait(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Assigns a DispatcherTask to the threadPool.  The dispatcher thread
     * associated with the threadpool will execute it.
     * @param task DispatcherTask that will keep the workers busy
     */
    public void assignGroupTask(DispatcherTask task) {
        dispatcherThread.assign(task);
    }

    public Map<String, Integer> getCounts() {
        int occupied = 0;
        int blocked = 0;
        int busy = 0;
        int idle = 0;
        int terminated = 0;
        for (int i = 0; i < poolSize; i++) {
            WorkerThread thread = pool[i];
            if (thread.isOccupied()) {
                occupied++;
            } else {
                synchronized (thread) {
                    thread.notify();
                }
            }
            switch ( thread.getState() ) {
                case NEW:
                    break;
                case RUNNABLE:
                    busy++;
                    break;
                case BLOCKED:
                    blocked++;
                    break;
                case WAITING:
                case TIMED_WAITING:
                    idle++;
                    break;
                case TERMINATED:
                    terminated++;
                    break;
            }
        }
        Map<String, Integer> counts = new HashMap<>(  );
        counts.put( "occupied", occupied );
        counts.put( "blocked", blocked );
        counts.put( "busy", busy );
        counts.put( "idle", idle );
        counts.put( "terminated", terminated );
        counts.put( "size", poolSize );
        return counts;
    }

    /**
     * Causes all worker threads to die.
     */
    public void stopAll() {
        for (int i = 0; i < pool.length; i++) {
            WorkerThread thread = pool[i];
            thread.stopRunning();
        }
    }

    /**
     * Returns the number of worker threads that are in the pool.
     * @return the number of worker threads in the pool
     */
    public int getSize ( ) {
        return poolSize;
    }

}
