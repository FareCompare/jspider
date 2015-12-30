package net.javacoding.jspider.core.task;

import net.javacoding.jspider.core.exception.TaskAssignmentException;
import net.javacoding.jspider.core.task.work.DecideOnSpideringTask;

import java.net.URL;
import java.util.Set;


/**
 * Interface that will be implemented upon each object that will act as a task
 * scheduler.
 * The Task scheduler will keep track of all work that is being done and all
 * tasks that still have to be carried out.
 *
 * $Id: Scheduler.java,v 1.10 2003/04/25 21:28:59 vanrogu Exp $
 *
 * @author  Gunther Van Roey
 */
public interface Scheduler {

    /**
     * Schedules a Worker Task to be executed.  The scheduler will keep a
     * reference to the task and return it later on to be processed.
     * @param task task to be scheduled
     */
    void schedule( WorkerTask task );

    /**
     * Block a task for a certain siteURL.  This is used to block any
     * resource handling for a site for which we didn't interpret the
     * robots.txt file yet.
     * @param siteURL the site for which the task is
     * @param task the task to be temporarily blocked
     */
    void block( URL siteURL, DecideOnSpideringTask task );

    /**
     * Returns all tasks that were blocked for the specified site, and
     * removes them from the blocked resources pool.
     * @param siteURL the site we want to unblock all resources for
     * @return array with all tasks that were blocked for this site
     */
    DecideOnSpideringTask[] unblock( URL siteURL );

    /**
     * Flags a task as done.  This way, we are able to remove the task from
     * the in-process list.
     * @param task task that was completed
     */
    void flagDone( WorkerTask task );

    /**
     * Returns a thinker task to be processed
     * @return Task to be carried out
     * @throws TaskAssignmentException if all the work is done or no suitable
     * items are found for the moment.
     */
    WorkerTask getThinkerTask() throws TaskAssignmentException;

    /**
     * Returns a fetch task to be processed
     * @return Task to be carried out
     * @throws TaskAssignmentException if all the work is done or no suitable
     * items are found for the moment.
     */
    WorkerTask getFetchTask() throws TaskAssignmentException;

    /**
     * Determines whether all the tasks are done.   If there are no more tasks
     * scheduled for process, and no ongoing tasks, it is impossible that new
     * work will arrive, so the spidering is done.
     * @return boolean value determining whether all work is done
     */
    boolean allTasksDone();

    /**
     * Statistics method.
     * @return blocked jobs counter
     */
    int getBlockedCount();

    Set<URL> getBlockedUrls();

    boolean hasOnlyBlockedTasks();

    /**
     * Statistics method.
     * @return assigned jobs counter
     */
    int getAssignedCount();

    /**
     * Statistics method.
     * @return total jobs counter
     */
    int getJobCount();

    /**
     * Statistics method.
     * @return total thinker jobs counter
     */
    int getThinkerJobCount();

    /**
     * Statistics method.
     * @return total spider jobs counter
     */
    int getSpiderJobCount();

    /**
     * Statistics method.
     * @return jobs finished counter
     */
    int getJobsDone();

    /**
     * Statistics method.
     * @return finished spider jobs counter
     */
    int getSpiderJobsDone();

    /**
     * Statistics method.
     * @return finished thinker jobs counter
     */
    int getThinkerJobsDone();

    int getSpiderQueueSize();
    int getThinkerQueueSize();
}
