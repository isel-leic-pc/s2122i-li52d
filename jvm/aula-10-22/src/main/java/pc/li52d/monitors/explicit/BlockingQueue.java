/**
 * A classic monitor style implementation for a blocking
 * queue using explicits locks in order to have
 * different condition variables for producers and consumers
 * For simplicity teh operations don't hve timeout options
 */

package pc.li52d.monitors.explicit;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingQueue<T> {
    private int capacity;
    private final Queue<T> items;
    private Lock monitor;
    private Condition hasItems;
    private Condition hasSpace;

    public BlockingQueue(int capacity) {
        items = new LinkedList<>();
        monitor = new ReentrantLock();
        hasSpace = monitor.newCondition();
        hasItems = monitor.newCondition();
    }

    public T get()
            throws InterruptedException {
        monitor.lock();
        try {
            while (items.isEmpty()) {
                hasItems.await();
            }
            T item = items.poll();
            hasSpace.signal();
            return item;
        }
        catch(InterruptedException e) {
            // this check is needed in order to not loose
            // the eventual notification
            if (!items.isEmpty())
                hasItems.signal();
            throw e;
        }
        finally {
            monitor.unlock();
        }
    }

    private boolean fullQueue() {
        return items.size() == capacity;
    }

    public void put(T item)
        throws InterruptedException {
        monitor.lock();
        try {
            while (fullQueue()) {
                hasSpace.await();
            }
            items.add(item);
            hasItems.signal();
        }
        catch(InterruptedException e) {
            // this check is needed in order to not loose
            // the eventual notification
            if (!fullQueue())
                hasSpace.signal();
            throw e;
        }
        finally {
            monitor.unlock();
        }
    }
}
