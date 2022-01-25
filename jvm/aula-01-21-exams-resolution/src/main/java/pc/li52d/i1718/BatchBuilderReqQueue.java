package pc.li52d.i1718;

import pc.li52d.monitors.utils.BatchReqQueue;
import pc.li52d.monitors.utils.NodeList;
import pc.li52d.monitors.utils.TimeoutHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BatchBuilderReqQueue {


    private int batchSize;
    private Lock monitor;
    private BatchReqQueue<List<Object>> batchQueue;
    private Condition batchDone;


    public BatchBuilderReqQueue(int batchSize) {
        this.batchSize = batchSize;
        monitor = new ReentrantLock();
        batchQueue = new BatchReqQueue<>(new ArrayList<>());
        batchDone = monitor.newCondition();
    }

    private boolean addValue(Object val) {
        List<Object> currBatch =   batchQueue.getCurrent().value;
        currBatch.add(val);
        return currBatch.size() == batchSize;

    }

    private void notifyCompleted() {
        batchQueue.newBatch(new ArrayList<>());
        batchDone.signalAll();
    }

    public List<Object> await(Object value, int timeout) throws InterruptedException {
        monitor.lock();
        try {
            // fast path
            if (addValue(value)) {
                List<Object> currBatch = batchQueue.getCurrent().value;
                notifyCompleted();
                return currBatch;
            }
            if (timeout == 0) return null;

            // wait path
            BatchReqQueue.Round<List<Object>> current = batchQueue.getCurrent();
            TimeoutHolder th = new TimeoutHolder(timeout);
            do {
                try {
                    batchDone.await(th.remaining(), TimeUnit.MILLISECONDS);
                    if (current != batchQueue.getCurrent()) return current.value;
                    if (th.timeout()) {
                        batchQueue.remove(current);
                        current.value.remove(value);
                        return null;
                    }
                }
                catch(InterruptedException e)  {
                    if (current != batchQueue.getCurrent()) {
                        Thread.currentThread().interrupt();
                        return current.value;
                    }
                    else {
                        batchQueue.remove(current);
                        current.value.remove(value);
                        throw e;
                    }
                }
            }
            while(true);
        }
        finally {
            monitor.unlock();
        }
    }
}