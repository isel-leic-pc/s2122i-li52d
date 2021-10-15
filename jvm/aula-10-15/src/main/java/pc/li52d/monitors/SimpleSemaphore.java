package pc.li52d.monitors;

import pc.li52d.monitors.utils.TimeoutHolder;

public class SimpleSemaphore {
    // the monitor state
    private int units;

    // the monitor object
    private Object monitor;

    public SimpleSemaphore(int initial) {
        if (initial > 0) units = initial;
        monitor = new Object();
    }

    public boolean acquire(long millis)
                        throws InterruptedException {
        synchronized (monitor) {
            // fast path
            if (units > 0) {
                --units;
                return true;
            }

            // fast ( unsuccessful) path
            if (millis == 0) {
                return false;
            }

            // waiting path

            TimeoutHolder th = new TimeoutHolder(millis);
            try {
                do {
                    monitor.wait(th.remaining());
                    // first try success scenario
                    if (units > 0) {
                        --units;
                        return true;
                    }
                    // timeout occurs?
                    if (th.timeout()) return false;
                } while (true);
            }
            catch(InterruptedException e) {
                if (units > 0) {
                    // regenerate notification to avoid loose notifications
                    monitor.notify();
                }
                throw e;
            }
        }
    }

    public void release() {
        synchronized (monitor) {
            ++units;
            monitor.notify();
        }
    }
}
