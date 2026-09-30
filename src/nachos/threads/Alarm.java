package nachos.threads;

import nachos.machine.*;
import java.util.LinkedList;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */

    private class WaitEntry {
        KThread thread;
        long wakeUpTime;

        WaitEntry(KThread thread, long wakeUpTime) {
            this.thread = thread;
            this.wakeUpTime = wakeUpTime;
        }
    }

    private LinkedList<WaitEntry> waitList;

    public Alarm() {
        waitList = new LinkedList<WaitEntry>();

        Machine.timer().setInterruptHandler(new Runnable() {
            public void run() { timerInterrupt(); }
            });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
	// KThread.currentThread().yield();
        // read the current system time
        // for loop to check the list

        boolean intStatus = Machine.interrupt().disable();
        long currentTime = Machine.timer().getTime();

        LinkedList<WaitEntry> toWake = new LinkedList<WaitEntry>();

        java.util.Iterator<WaitEntry> iterator = waitList.iterator();
        while (iterator.hasNext()) {
            WaitEntry entry = iterator.next();
            if (entry.wakeUpTime <= currentTime) {
                iterator.remove();
                entry.thread.ready();
            }
        }

        Machine.interrupt().restore(intStatus);
        KThread.currentThread().yield();
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
	// for now, cheat just to get something working (busy waiting is bad) below is wrong code
	
    // long wakeTime = Machine.timer().getTime() + x;
	// while (wakeTime > Machine.timer().getTime())
	    // KThread.yield();

        // define a list, element will be (KThread, wakeup time)
        // a pair of linked list
        // hash map <KThread, wakeup time>
        // requesting thread is put in the list and put to sleep

        long wakeUpTime = Machine.timer().getTime() + x;

        boolean intStatus = Machine.interrupt().disable();

        waitList.add(new WaitEntry(KThread.currentThread(), wakeUpTime));

        KThread.sleep();

        Machine.interrupt().restore(intStatus);
    }

}
