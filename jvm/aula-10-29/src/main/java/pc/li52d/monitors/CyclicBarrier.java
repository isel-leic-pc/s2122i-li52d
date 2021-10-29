package pc.li52d.monitors;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CyclicBarrier {
    private final int partners;

    private final Lock monitor;
    private final Condition passBarrier;


    public CyclicBarrier(int partners) {
        this.partners = partners;
        this.monitor = new ReentrantLock();
        this.passBarrier = monitor.newCondition();

        // to complete
    }


    private void openBarrier() {
        // to complete
    }

    private void breakBarrier() {
        // to complete
    }


    public int await(long timeout)
        throws InterruptedException,
        BrokenBarrierException,
        TimeoutException {
        monitor.lock();
        try {
            // to complete
            return 0;
        } finally {
            monitor.unlock();
        }
    }

    public void reset() {
        monitor.lock();
        try {
            // to complete
        } finally {
            monitor.unlock();
        }
    }

    public boolean isBroken() {
        monitor.lock();
        try {
            // to complete
            return false;
        } finally {
            monitor.unlock();
        }
    }
}
