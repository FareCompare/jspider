package net.javacoding.jspider.core.task.impl;

import net.javacoding.jspider.core.exception.NoSuitableItemFoundException;
import net.javacoding.jspider.core.exception.SpideringDoneException;
import net.javacoding.jspider.core.exception.TaskAssignmentException;
import net.javacoding.jspider.core.task.Scheduler;
import net.javacoding.jspider.core.task.WorkerTask;
import net.javacoding.jspider.core.task.work.DecideOnSpideringTask;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of a Task scheduler
 *
 * $Id: SchedulerImpl.java,v 1.17 2003/04/25 21:29:04 vanrogu Exp $
 *
 * @author  Gunther Van Roey
 */
public class SchedulerImpl implements Scheduler {

    /** List of fetch tasks to be carried out. */
    protected BlockingQueue<WorkerTask> fetchTasks;

    /** List of thinker tasks to be carried out. */
    protected BlockingQueue<WorkerTask> thinkerTasks;

    /** Set of tasks that have been assigned. */
    protected final Set<WorkerTask> assignedSpiderTasks;
    protected final Set<WorkerTask> assignedThinkerTasks;

    protected volatile int spiderTasksDone;
    protected volatile int thinkerTasksDone;

    protected final ConcurrentHashMap<URL,ArrayList<DecideOnSpideringTask>> blocked;

    int blockedCount = 0;

    public int getBlockedCount( ) {
        return blockedCount;
    }

    public int getAssignedCount( ) {
        return assignedSpiderTasks.size() + assignedThinkerTasks.size();
    }

    public int getJobCount() {
        return getThinkerJobCount() + getSpiderJobCount();
    }

    public int getThinkerJobCount() {
        return thinkerTasksDone + assignedThinkerTasks.size ( ) + thinkerTasks.size();
    }

    public int getSpiderJobCount() {
        return spiderTasksDone + assignedSpiderTasks.size ( ) + fetchTasks.size();
    }

    public int getJobsDone() {
      return getSpiderJobsDone() + getThinkerJobsDone();
    }

    public int getSpiderJobsDone() {
        return spiderTasksDone;
    }

    public int getThinkerJobsDone() {
        return thinkerTasksDone;
    }

    public int getSpiderQueueSize() {
        return fetchTasks.size();
    }

    public int getThinkerQueueSize() {
        return thinkerTasks.size();
    }


    /**
     * Public constructor?
     */
    public SchedulerImpl() {
        fetchTasks = new LinkedBlockingQueue<>( );
        thinkerTasks = new LinkedBlockingQueue<>();
        assignedThinkerTasks = new HashSet<>();
        assignedSpiderTasks = new HashSet<>();
        blocked = new ConcurrentHashMap<>();
    }

    public void block(URL siteURL, DecideOnSpideringTask task) {
        synchronized (blocked) {
            ArrayList<DecideOnSpideringTask> al = blocked.get(siteURL);
            if ( al == null ) {
                al = new ArrayList<>();
                ArrayList<DecideOnSpideringTask> previous = blocked.putIfAbsent( siteURL, al );
                if ( previous != null ) {
                    al = previous;
                }
            }
            int before;
            int after;
            synchronized ( al ) {
                before = al.size();
                al.add(task);
                after = al.size();
            }

            int diff = after-before;
            blockedCount+=diff;
        }
    }

    public DecideOnSpideringTask[] unblock(URL siteURL) {
        synchronized (blocked) {
            ArrayList<DecideOnSpideringTask> al = blocked.remove( siteURL );
            if ( al == null ) {
                return new DecideOnSpideringTask[0];
            } else {
                synchronized (al) {
                    blockedCount-=al.size();
                    return al.toArray(new DecideOnSpideringTask[al.size()]);
                }
            }
        }
    }

    /**
     * Schedules a task to be processed.
     * @param task task to be scheduled
     */
    public void schedule(WorkerTask task) {
        if (task.getType() == WorkerTask.WORKERTASK_SPIDERTASK ) {
            fetchTasks.add(task);
        } else {
            thinkerTasks.add(task);
        }
    }

    /**
     * Flags a task as done.
     * @param task task that was complete
     */
    public void flagDone(WorkerTask task) {
        if (task.getType() == WorkerTask.WORKERTASK_THINKERTASK ) {
            synchronized (assignedThinkerTasks) {
                assignedThinkerTasks.remove(task);
                thinkerTasksDone++;
            }
        }else{
            synchronized (assignedSpiderTasks) {
                assignedSpiderTasks.remove(task);
                spiderTasksDone++;
            }
        }
    }

    public WorkerTask getThinkerTask() throws TaskAssignmentException {
        if (thinkerTasks.size() > 0) {
            try {
                WorkerTask task = thinkerTasks.poll( 1, TimeUnit.SECONDS );
                if ( task != null ) {
                    synchronized (assignedThinkerTasks) {
                        assignedThinkerTasks.add(task);
                    }
                    return task;
                }
            } catch ( InterruptedException e ) {
                throw new NoSuitableItemFoundException();
            }
        }
        if (allTasksDone()) {
            throw new SpideringDoneException();
        } else {
            throw new NoSuitableItemFoundException();
        }
    }

    /**
     * Returns a fetch task to be carried out.
     * @return WorkerTask task to be done
     * @throws TaskAssignmentException notifies when the work is done or there
     * are no current outstanding tasks.
     */
    public synchronized WorkerTask getFetchTask() throws TaskAssignmentException {
        if (fetchTasks.size() > 0) {
            try {
                WorkerTask task = fetchTasks.poll( 1, TimeUnit.SECONDS );
                if ( task != null ) {
                    synchronized (assignedSpiderTasks) {
                        assignedSpiderTasks.add(task);
                    }
                    return task;
                }
            } catch ( InterruptedException e ) {
                throw new NoSuitableItemFoundException();
            }
        }
        if (allTasksDone()) {
            throw new SpideringDoneException();
        } else {
            throw new NoSuitableItemFoundException();
        }
    }


    /**
     * Determines whether all the tasks are done.   If there are no more tasks
     * scheduled for process, and no ongoing tasks, it is impossible that new
     * work will arrive, so the spidering is done.
     * @return boolean value determining whether all work is done
     */
    public synchronized boolean allTasksDone() {
        return (fetchTasks.size() == 0 &&
                thinkerTasks.size() == 0 &&
                assignedSpiderTasks.size() == 0 &&
                assignedThinkerTasks.size() == 0 &&
                blocked.size() == 0 );
    }

    /*
    public synchronized String toString ( ) {
        StringBuffer sb = new StringBuffer();
        Iterator it = this.thinkerTasks.iterator();
        while ( it.hasNext() ) {
            System.out.println("TH . " + it.next().getClass());
        }
        it = this.assignedThinkerTasks.iterator();
        while ( it.hasNext() ) {
            System.out.println("TH A " + it.next().getClass());
        }
        it = this.fetchTasks.iterator();
        while ( it.hasNext() ) {
            System.out.println("SP . " + it.next().getClass());
        }
        it = this.assignedSpiderTasks.iterator();
        while ( it.hasNext() ) {
            System.out.println("SP A " + it.next().getClass());
        }
        return sb.toString();
    }   */
}
