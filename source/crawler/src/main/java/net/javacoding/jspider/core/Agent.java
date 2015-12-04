package net.javacoding.jspider.core;


import net.javacoding.jspider.core.event.CoreEvent;
import net.javacoding.jspider.core.exception.TaskAssignmentException;
import net.javacoding.jspider.core.task.WorkerTask;

import java.net.URL;


/**
 * Inteface of the JSpider agent, the instance to which all worker tasks will
 * report and via which tasks can be obtained from the scheduler.
 *
 * $Id: Agent.java,v 1.5 2003/03/27 17:44:01 vanrogu Exp $
 *
 * @author Gunther Van Roey
 */
public interface Agent {

    void start();

    /**
     * Registers that a certain taks is completed.
     * @param task the Thinker task that is completed
     */
    void flagDone( WorkerTask task );

    /**
     * Returns a thinker task that must be executed
     * @return the task to be carried out
     * @throws net.javacoding.jspider.core.exception.TaskAssignmentException if there are no tasks (for now or anymore)
     */
    WorkerTask getThinkerTask() throws TaskAssignmentException;

    /**
     * Returns a spider task that must be executed
     * @return the task to be carried out
     * @throws net.javacoding.jspider.core.exception.TaskAssignmentException if there are no tasks (for now or anymore)
     */
    WorkerTask getSpiderTask() throws TaskAssignmentException;

    /**
     * Asks to schedule spidering on a certain URL.
     * @param url the URL to be spidered
     */
    void scheduleForSpidering( URL url );

    /**
     * Asks to schedule parsing for a certain fetched URL.
     * @param url the URL to be spidered
     */
    void scheduleForParsing( URL url );

    /**
     * Notifies the agent of a certain event.
     * @param url the URL that was being processed while the event was raised
     * @param event the event that was raised
     */
    void registerEvent( URL url, CoreEvent event );

}
