package pc.li52d.optimized;

import pc.li52d.monitors.utils.TimeoutHolder;

import java.util.concurrent.atomic.AtomicInteger;

public class Semaphore {
    // the monitor state
    private AtomicInteger units;

    // count the waiting threads in acquire operation
    private volatile int waiters;

    // the monitor object
    private final Object monitor;

    public Semaphore(int initial) {
        units = new AtomicInteger(initial > 0 ? initial:0);
        monitor = new Object();
    }

    private boolean tryAcquire(int n) {
        /*
        even using AtomicInteger, this code in non atomic

        if (units.get() >= n) {
            units.addAndGet(-n);
            return true;
        }
        return false;
         */
        do {
            int observed = units.get();
            if (observed < n) return false;
            if (units.compareAndSet(observed, observed - n))
                return true;
        }
        while(true);
    }

    public boolean acquire(int n, long millis)
        throws InterruptedException {

        // fast path
        if (tryAcquire(n)) return true;
        // fast (unsuccessful) path
        if (millis == 0) {
            // timeout immediately occurs
            // this is a kind of try operation
            return false;
        }
        synchronized (monitor) {
            // waiting path
            TimeoutHolder th = new TimeoutHolder(millis);
            waiters++;
            // the code presented in the lecture
            // was incorrect because didn't decrement
            // waiters in case of an InterruptedException
            // this code corrects it
            try {
                do {
                    // first try success scenario
                    if (tryAcquire(n)) {
                        return true;
                    }
                    // timeout occurs?
                    if (th.timeout()) {
                        return false;
                    }
                    monitor.wait(th.remaining());

                } while (true);
            }
            finally {
                waiters--;
            }
        }
    }

    public void release(int n) {
        units.addAndGet(n);
        if (waiters > 0) {
            synchronized (monitor) {
                monitor.notifyAll();
            }
        }
    }
}
