package pc.li52d.monitors;

import pc.li52d.monitors.utils.BatchReqQueue;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CyclicBarrier {
    private int partners;
    private int remaining;

    private Lock monitor;
    private Condition passBarrier;

    private enum State {Closed, Opened, Broken}

    ;

    private BatchReqQueue<State> queue;
    private boolean broken;

    public CyclicBarrier(int partners) {

    }

    private void nextSynch() {
        remaining = partners;
        queue.newBatch(State.Closed);
    }

    private void openBarrier() {

    }

    private void breakBarrier() {

    }


    public int await(long timeout)
        throws InterruptedException,
        BrokenBarrierException,
        TimeoutException {
        monitor.lock();
        try {
            return 0;
        } finally {
            monitor.unlock();
        }
    }

    public void reset() {
        monitor.lock();
        try {
            breakBarrier();
            nextSynch();
        } finally {
            monitor.unlock();
        }
    }

    public boolean isBroken() {
        monitor.lock();
        try {
            return false;
        } finally {
            monitor.unlock();
        }
    }
}
