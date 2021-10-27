package pc.li52d.monitors;

import pc.li52d.monitors.utils.NodeList;
import pc.li52d.monitors.utils.TimeoutHolder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SemaphoreFifoSN {
    /**
     *  this class instances represent
     *  a pending acquire request
     */
    private static class Request {
        public final int units;
        public final Condition condition;
        public boolean done;

        public Request(int units, Condition condition) {
            this.units = units;
            this.done = false;
            this.condition = condition;
        }
    }

    private int units;
    private final NodeList<Request> requests;
    private final ReentrantLock monitor;

    public SemaphoreFifoSN(int initial) {
        if (initial > 0)
            units = initial;
        monitor = new ReentrantLock();
        requests = new NodeList<>();
    }

    /**
     * An auxiliary method that tries
     * to process all possible pending requests
     */
    private void notifyWaiters() {
        while (requests.size() > 0 && units >= requests.first().units) {

            Request r = requests.removeFirst();
            units-= r.units;
            r.done = true;
            r.condition.signal();

        }
    }

    public boolean acquire(int n, long millis)
        throws InterruptedException {
        monitor.lock();
        try {
            // fast path
            if (units >= n && requests.empty()) {
                units -= n;
                return true;
            }
            if (millis == 0) return false;

            // prepare wait
            var req = new Request(n, monitor.newCondition());
            var node = requests.addLast(req);
            TimeoutHolder th = new TimeoutHolder(millis);

            // do wait
            do {
                try {
                    // non interruption path
                    req.condition.await(th.remaining(),
                        TimeUnit.MILLISECONDS);
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
        finally {
            monitor.unlock();
        }

    }

    public void release(int n) {
        monitor.lock();
        try {
            units += n;
            notifyWaiters();
        }
        finally {
            monitor.unlock();
        }
    }
}