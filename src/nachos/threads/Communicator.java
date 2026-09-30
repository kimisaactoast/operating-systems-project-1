package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */

    private Lock lock;
    private Condition2 speakerCond;
    private Condition2 listenerCond;

    private int wordMessage;
    private boolean hasMessage;
    private boolean speakerActive;

    public Communicator() {
        lock = new Lock();

        speakerCond = new Condition2(lock);
        listenerCond = new Condition2(lock);

        hasMessage = false;
        speakerActive = false;
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
        lock.acquire();

        // wait until communicator is free
        while (speakerActive) {
            speakerCond.sleep();
        }

        // claim communicator and set the message
        speakerActive = true;
        wordMessage = word;
        hasMessage = true;

        // wake up listening waiter
        listenerCond.wake();

        // wait for listener to consume message
        while (hasMessage) {
            speakerCond.sleep();
        }

        // release communicator for next speaker
        speakerActive = false;

        speakerCond.wakeAll();

        lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
        lock.acquire();

        while (!hasMessage) {
            listenerCond.sleep();
        }

        int word = wordMessage;
        hasMessage = false;

        speakerCond.wakeAll();

        lock.release();

	    return word;
    }
}
