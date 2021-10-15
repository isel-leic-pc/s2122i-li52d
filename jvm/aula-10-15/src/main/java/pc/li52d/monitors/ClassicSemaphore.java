package pc.li52d.monitors;

import pc.li52d.monitors.utils.TimeoutHolder;

public class ClassicSemaphore {
    // the monitor state
    private int units;

    // the monitor object
    private final Object monitor;

    public ClassicSemaphore(int initial) {
        if (initial > 0) units = initial;
        monitor = new Object();
    }

    public boolean acquire(int n, long millis)
        throws InterruptedException {
        synchronized (monitor) {
            // fast path
            if (units >= n) {
                units -= n;
                return true;
            }

            // fast (unsuccessful) path
            if (millis == 0) {
                // timeout immediately occurs
                // this is a kind of try operation
                return false;
            }

            // waiting path
            TimeoutHolder th = new TimeoutHolder(millis);

            do {
                monitor.wait(th.remaining());
                // first try success scenario
                if (units >=  n) {
                    units -= n;
                    return true;
                }
                // timeout occurs?
                if (th.timeout())
                    return false;
            } while (true);
        }
    }

    public void release(int n) {
        synchronized (monitor) {
            units += n;
            monitor.notifyAll();
        }
    }
}
