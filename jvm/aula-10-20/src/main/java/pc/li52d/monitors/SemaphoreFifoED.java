/**
 * An implementation of a fair semaphore
 * with execution delegation style
 */
package pc.li52d.monitors;

import pc.li52d.monitors.utils.NodeList;
import pc.li52d.monitors.utils.TimeoutHolder;

public class SemaphoreFifoED {

    /**
     *  this class instances represent
     *  a pending acquire request
     */
    private static class Request {
        public final int units;
        public boolean done;

        public Request(int units) {
            this.units = units;
            this.done = false;
        }
    }

    private int units;
    private NodeList<Request> requests;
    private Object monitor;

    public SemaphoreFifoED(int initial) {
        if (initial > 0)
            units = initial;
        monitor = new Object();
        requests = new NodeList<>();
    }

    /**
     * An auxiliary method that tries
     * to process all possible pending requests
     */
    private void notifyWaiters() {
        boolean toNotify = false;
        while (requests.size() > 0 && units >= requests.first().units) {

            Request r = requests.removeFirst();
            units-= r.units;
            r.done = true;
            toNotify = true;
        }
        if (toNotify) {
            monitor.notifyAll();
        }
    }

    public boolean acquire(int n, long millis)
        throws InterruptedException {
        synchronized (monitor) {
            // fast path
            if (units >= n && requests.empty()) {
                units -= n;
                return true;
            }

            // prepare wait
            var req = new Request(n);
            var node = requests.addLast(req);
            TimeoutHolder th = new TimeoutHolder(millis);

            // do wait
            do {
                try {
                    // non interruption path
                    monitor.wait(th.remaining());
                    if (req.done) return true;
                    if (th.timeout()) {
                        requests.remove(node);
                        // note that we must notify
                        // in this case, since the
                        // timeouted request
                        // can be at the front of the list
                        notifyWaiters();
                        return false;
                    }
                }
                catch(InterruptedException e) {
                    // note that the request
                    // can be already accepted
                    // so in the case we return success
                    // but redoing a thread interrupt request
                    if (req.done) {
                        Thread.currentThread().interrupt();
                        return true;
                    }
                    // note that we must notify
                    // in this case, since the
                    // interrupted request
                    // can be at the front of the list
                    requests.remove(node);
                    notifyWaiters();
                    throw e;
                }
            }
            while(true);

        }
    }

    public void release(int n) {
        synchronized (monitor) {
            units += n;
            notifyWaiters();
        }
    }
}

