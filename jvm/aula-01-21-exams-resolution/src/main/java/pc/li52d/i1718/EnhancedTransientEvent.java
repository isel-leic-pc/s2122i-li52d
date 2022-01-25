package pc.li52d.i1718;

import pc.li52d.monitors.utils.NodeList;
import pc.li52d.monitors.utils.TimeoutHolder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EnhancedTransientEvent {

    private static class EventWaiter {
        Condition signalDone;
        boolean done;

        EventWaiter(Condition cond) {
            signalDone = cond;
        }
    }

    private NodeList<EventWaiter> waiters = new NodeList<>();
    private Lock monitor = new ReentrantLock();


    public boolean await(int timeout) throws InterruptedException {
        monitor.lock();
        try {
            TimeoutHolder th = new TimeoutHolder(timeout);
            EventWaiter waiter = new EventWaiter(monitor.newCondition());
            NodeList.Node<EventWaiter> node = waiters.addLast(waiter);

            do {
                try {
                    waiter.signalDone.await(th.remaining(), TimeUnit.MILLISECONDS);
                    if (waiter.done) return true;
                    if (th.timeout()) {
                        waiters.remove(node);
                        return false;
                    }
                }
                catch(InterruptedException e) {
                    if (waiter.done) {
                        Thread.currentThread().interrupt();
                        return true;
                    }
                    waiters.remove(node);
                    throw e;
                }
            }
            while(true);
        }
        finally {
            monitor.unlock();
        }
    }

    public void signal(int maxWakeups) {
        monitor.lock();
        try {
            while(maxWakeups > 0 && !waiters.empty()) {
                EventWaiter waiter = waiters.removeFirst();
                waiter.done = true;
                waiter.signalDone.signal();
            }
        }
        finally {
            monitor.unlock();
        }
    }

}
