package net.javacoding.jspider.api.event.monitor;

import java.util.Map;

/**
 * $Id: ThreadPoolMonitorEvent.java,v 1.3 2003/03/28 17:26:26 vanrogu Exp $
 */
public class ThreadPoolMonitorEvent extends MonitorEvent {

    protected String name;
    protected int occupied;
    protected int occupationPct;
    protected int idle;
    protected int idlePct;
    protected int blocked;
    protected int blockedPct;
    protected int busy;
    protected int busyPct;
    protected int size;

    public ThreadPoolMonitorEvent( String name, Map<String,Integer> counts ) {
        this.name = name;
        size = counts.get( "size" );

        occupied = counts.get("occupied");
        occupationPct = (occupied * 100) / size;

        idle = counts.get("idle");
        idlePct = (idle * 100) / size;

        blocked = counts.get("blocked");
        blockedPct = (blocked * 100) / size;

        busy = counts.get("busy");
        busyPct = (busy * 100) / size;
    }

    public String toString() {
        return String.format("ThreadPool %s occupied: %s (%s%%) [idle: %s (%s%%), blocked: %s (%s%%), busy: %s (%s%%)], size: %s",
                      name, occupied, occupationPct, idle, idlePct, blocked, blockedPct, busy, busyPct, size );
    }

    public String getComment() {
        return toString();
    }

    public String getName() {
        return name;
    }

    public int getOccupationPct() {
        return occupationPct;
    }

    public int getIdlePct() {
        return idlePct;
    }

    public int getBlockedPct() {
        return blockedPct;
    }

    public int getBusyPct() {
        return busyPct;
    }

    public int getSize() {
        return size;
    }

}
