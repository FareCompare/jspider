package net.javacoding.jspider.core.util.statistics;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Timer class used for measuring performance.
 *
 * @author Chris Stillwell
 * @version 1.0
 * @since Creation date: (1/30/01 2:37:05 PM)
 */
public class StopWatch implements Serializable {
    /**
     * One second in milliseconds.
     */
    private static final long ONE_SECOND = TimeUnit.SECONDS.toMillis( 1 );

    /**
     * One minute in milliseconds.
     */
    private static final long ONE_MINUTE = TimeUnit.MINUTES.toMillis( 1 );

    /**
     * One hour in milliseconds.
     */
    private static final long ONE_HOUR = TimeUnit.HOURS.toMillis( 1 );

    /**
     * Used by toFullDisplayString.
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "MM/dd/yyyy hh:mm:ss,SSS a" );

    /**
     * Indicates if this StopWatch is _stopped or running.
     */
    private boolean _stopped = true;

    /**
     * The start time.
     */
    private long _startTime;

    /**
     * The stop time.
     */
    private long _stopTime;

    /**
     * Creates a StopWatch in a _stopped state.
     */
    public StopWatch() {
        this( false );
    }

    /**
     * Creates a StopWatch and starts it if <code>start</code> is
     * <code>true</code>.
     *
     * @param start if <code>true</code> the StopWatch is started.
     */
    public StopWatch( boolean start ) {
        if ( start ) {
            this.start();
        }
    }

    /**
     * Returns the current duration this StopWatch has been running.
     *
     * @return the duration in milliseconds.
     */
    public long duration() {
        long dur;

        if ( _stopped ) {
            dur = _stopTime - _startTime;
        }
        else {
            dur = System.currentTimeMillis() - _startTime;
        }

        return dur;
    }

    /**
     * Returns the current elapsed time in seconds this StopWatch has been
     * running.
     *
     * @return the elapsed time in seconds.
     */
    public double elapsedTime() {
        double dur;

        if ( _stopped ) {
            dur = _stopTime - _startTime;
        }
        else {
            dur = System.currentTimeMillis() - _startTime;
        }

        return dur / ONE_SECOND;
    }

    /**
     * Displays the current duration in the format HH:MM:SS.mm.
     *
     * @return the duration as a formatted string.
     * @since Creation date: (1/30/01 2:46:03 PM)
     */
    public String formatDuration() {
        long dur = duration();
        return formatDuration( dur );
    }

    public static String formatDuration( long duration ) {

        final long hours = duration / (ONE_HOUR);
        duration = duration % (ONE_HOUR);

        final long minutes = duration / (ONE_MINUTE);
        duration = duration % (ONE_MINUTE);

        final long seconds = duration / ONE_SECOND;
        final long milli = duration % ONE_SECOND;

        return String.format( "%02d:%02d:%02d.%03d%n", hours, minutes, seconds, milli );
    }

    /**
     * Resets the start time of this StopWatch to the current time and the stop
     * time to zero.
     */
    public void reset() {
        _stopTime = 0;
        start();
    }

    /**
     * Starts this StopWatch.
     */
    public final void start() {
        _startTime = System.currentTimeMillis();
        _stopped = false;
    }

    /**
     * Stops this StopWatch.
     */
    public void stop() {
        _stopTime = System.currentTimeMillis();
        _stopped = true;
    }

    /**
     * Formats the current duration of this StopWatch.
     *
     * @return a formatted string.
     * @see #formatDuration()
     * @since Creation date: (1/30/01 2:49:19 PM)
     */
    public String toString() {
        return formatDuration();
    }

    /**
     * Indicates if this StopWatch is stopped or not.
     * @return <code>true</code> if stopped.
     */
    public boolean isStopped() {
        return _stopped;
    }

    /**
     * Return the start time as set by System.currentTimeMillis() for when this StopWatch was started.
     * @return the start time or 0 if this StopWatch has not been started.
     */
    public long getStartTime() {
        return _startTime;
    }

    /**
     * Return the stop time as set by System.currentTimeMillis() for when this StopWatch was stopped.
     * @return the stop time or 0 if this StopWatch has not been stopped.
     */
    public long getStopTime() {
        return isStopped() ? _stopTime : 0;
    }

    /**
     * Return a formatted string that contains the start time, stop time, and duration.
     * @return a formatted string
     */
    public String toFullDisplayString() {
        if ( getStartTime() == 0 ) {
            return "StopWatch not started";
        }
        StringBuilder sb = new StringBuilder();
        synchronized (DATE_FORMAT) {
            sb.append( "Start Time " ).append( DATE_FORMAT.format( new Date( getStartTime() ) ) );
            if ( isStopped() ) {
                sb.append( " Stop Time " ).append( DATE_FORMAT.format( new Date( getStopTime() ) ) );
            }
        }
        sb.append( " Duration " ).append( formatDuration() );
        return sb.toString();
    }
}