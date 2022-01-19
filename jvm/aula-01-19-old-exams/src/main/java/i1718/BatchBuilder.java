package i1718;

import pc.li52d.monitors.utils.NodeList;
import pc.li52d.monitors.utils.TimeoutHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BatchBuilder {

    private static class BatchWaiter {
        private Condition batchCompleted;
        private List<Object> batch;

        public BatchWaiter(Condition c) {
            batchCompleted = c;
        }

        public void complete(List<Object> batch) {
            this.batch = batch;
            batchCompleted.signal();
        }
    }

    private NodeList<BatchWaiter> waiters;
    private Lock monitor;
    private List<Object> currBatch;
    private int batchSize;

    public BatchBuilder(int batchSize) {
        this.batchSize = batchSize;
        waiters = new NodeList<>();
        monitor = new ReentrantLock();
        currBatch = new ArrayList<>();
    }

    private void completeBatch() {
        for(BatchWaiter waiter : waiters) {
            waiter.complete(currBatch);
        }
        waiters.clear();
        currBatch = new ArrayList<>();
    }

    private List<Object> processValue(Object value) {
        currBatch.add(value);
        if (currBatch.size() == batchSize) {
            List<Object> curr = currBatch;
            completeBatch();
            return curr;
        }
        return null;
    }

    public List<Object> await(Object value, int timeout) throws InterruptedException {
        monitor.lock();
        try {
            // fast path
            List<Object> result = processValue(value);
            if (result != null) return result;
            if (timeout == 0)  {
                currBatch.remove(currBatch.size()-1);
                return null;
            }
            // wait path
            TimeoutHolder th = new TimeoutHolder(timeout);
            BatchWaiter waiter = new BatchWaiter(monitor.newCondition());
            NodeList.Node<BatchWaiter> node = waiters.addLast(waiter);

            do {
                try {
                    waiter.batchCompleted.await(th.remaining(), TimeUnit.MILLISECONDS);
                    if (waiter.batch != null) return waiter.batch;
                    if (th.timeout()) {
                        currBatch.remove(value);
                        waiters.remove(node);
                        return null;
                    }
                }
                catch(InterruptedException e) {
                    if (waiter.batch != null) {
                        Thread.currentThread().interrupt();
                        return waiter.batch;
                    }
                    else {
                        currBatch.remove(value);
                        waiters.remove(node);
                    }
                    throw e;
                }
            }
            while(true);
        }
        finally {
            monitor.unlock();
        }
    }
}