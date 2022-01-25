package pc.li52d.i1718;

import pc.li52d.monitors.utils.NodeList;
import pc.li52d.monitors.utils.TimeoutHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BatchBuilder {

    private List<Object> batch;
    private int batchSize;
    private Lock monitor;
    private NodeList<BatchWaiter> waiters;
    private Condition batchDone;

    private static class BatchWaiter {
        List<Object> completedBatch;
    }

    public BatchBuilder(int batchSize) {
        batch = new ArrayList<>();
        this.batchSize = batchSize;
        monitor = new ReentrantLock();
        waiters = new NodeList<>();
        batchDone = monitor.newCondition();
    }

    private boolean addValue(Object val) {
        batch.add(val);
        return batch.size() == batchSize;

    }

    private void notifyCompleted() {
        for(BatchWaiter bw : waiters) {
            bw.completedBatch = batch;
        }
        batch = new ArrayList<>();
        batchDone.signalAll();
    }

    public List<Object> await(Object value, int timeout) throws InterruptedException {
        monitor.lock();
        try {
            // fast path
            if (addValue(value)) {
                notifyCompleted();
                return batch;
            }
            if (timeout == 0) return null;

            // wait path
            BatchWaiter bw = new BatchWaiter();
            NodeList.Node<BatchWaiter> node = waiters.addLast(bw);
            TimeoutHolder th = new TimeoutHolder(timeout);
            do {
                try {
                    batchDone.await(th.remaining(), TimeUnit.MILLISECONDS);
                    if (bw.completedBatch != null) return bw.completedBatch;
                    if (th.timeout()) {
                        waiters.remove(node);
                        batch.remove(value);
                        return null;
                    }
                }
                catch(InterruptedException e)  {
                    if (bw.completedBatch != null) {
                        Thread.currentThread().interrupt();
                        return bw.completedBatch;
                    }
                    else {
                        waiters.remove(node);
                        batch.remove(value);
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