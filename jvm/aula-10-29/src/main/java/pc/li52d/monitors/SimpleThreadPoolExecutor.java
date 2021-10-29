package pc.li52d.monitors;


import pc.li52d.monitors.utils.NodeList;
import pc.li52d.monitors.utils.TimeoutHolder;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleThreadPoolExecutor {

    private enum State { ACTIVE, SHUTDOWN_STARTED, TERMINATED}

    private static class WorkItem {
        public final Runnable cmd;
        public final Condition condition;
        public  boolean delivered;

        public WorkItem(Runnable cmd, Condition condition) {
            this.cmd = cmd;
            this.condition = condition;
            delivered = false;
        }
    }

    private static class WorkRequest {
        public Condition condition;
        public Runnable cmd;

        public WorkRequest(Condition condition) {
            this.condition = condition;
            cmd = null;
        }
    }

    private State state;
    private NodeList<WorkItem> requests;
    private NodeList<WorkRequest> workRequests;

    private int threadCount;
    private final Lock monitor;
    private final int maxPool;
    private final long keepAliveTime;
    private Condition shutdownCompleted; // work avaiable in requests queue

    public SimpleThreadPoolExecutor(int maxPool, long keepAliveTime) {
        this.monitor = new ReentrantLock();
        threadCount = 0;
        this.maxPool = maxPool;
        this.keepAliveTime = keepAliveTime;
        requests = new NodeList<>();
        workRequests = new NodeList<>();
        state = State.ACTIVE;

        shutdownCompleted = monitor.newCondition();
        // to complete
    }

    private static void safeExec(Runnable cmd) {
        try {
            cmd.run();
        }
        catch(Exception e) {
            // eventually log the exception...
        }
    }

    /**
     * To complete!
     * @param cmd
     */
    private void workerFunc(Runnable cmd) {
        do {
            safeExec(cmd);
            monitor.lock();
            try {
                if (requests.size() > 0) {
                    WorkItem wi = requests.removeFirst();
                    wi.delivered = true;
                    wi.condition.signal();
                    cmd = wi.cmd;
                }
                else {
                    if (state == State.SHUTDOWN_STARTED) {
                        // another fast path is in charge
                        // the fact that the thread pool is
                        // no longer active and the thread must terminate
                        // This implies the responsability to
                        // decrement thread count and if the new value is zero,
                        // change the state to TERMINATED and awake
                        // the awaitTermination waiters...


                        // to complete
                    }
                    else {
                        // prepare wait

                        // create timeout holder
                        TimeoutHolder th = null; // = ....;
                        // create work request
                        WorkRequest wr = null; // = ...;

                        do {
                            try {
                                wr.condition.await(th.remaining(), TimeUnit.MILLISECONDS);
                                // check the work request and proceed
                                // acording his state...
                                // this is similar to the fastpaths above
                                // Considerer create an auxiliary method to avoid
                                // redundancy

                                // then check possible timeout
                                // if so, terminate the thread similarly
                                // to the shutdown situation
                            } catch (InterruptedException e) {
                                // one way of process the exception is ignore it
                                // since the threads are managed by the thread pool
                                // another is terminate the thread in a similar
                                // way to termination due to timeout in the
                                // condition.await
                                // or shutdown state
                            }
                        }
                        while (true);
                    }
                }
            }
            finally {
                monitor.unlock();
            }
        }
        while(true);
    }

    /**
     * Completed!
     * @param cmd
     * @param timeout
     * @return
     * @throws InterruptedException
     */
    public boolean execute(Runnable cmd, long timeout) throws InterruptedException {

        monitor.lock();
        try {
            if (state != State.ACTIVE)
                throw new RejectedExecutionException();
            // fast path

            if (workRequests.size() > 0) {
                // execution delegation action
                WorkRequest req = workRequests.removeFirst();
                req.cmd = cmd;
                req.condition.signal();
                return true;
            }
            else if (threadCount < maxPool) {
                threadCount++;
                new Thread( () -> workerFunc(cmd));
                return true;
            }
            if (timeout == 0) return false;
            WorkItem wi = new WorkItem(cmd, monitor.newCondition());
            NodeList.Node<WorkItem> node = requests.addLast(wi);
            TimeoutHolder th = new TimeoutHolder(timeout);

            do {
                try {
                    wi.condition.await(th.remaining(), TimeUnit.MILLISECONDS);
                    if (wi.delivered) return true;
                    if (th.timeout()) {
                        requests.remove(node);
                        return false;
                    }
                }
                catch(InterruptedException e) {
                    if (wi.delivered) {
                        Thread.currentThread().interrupt();
                        return true;
                    }
                    requests.remove(node);
                    throw e;
                }
            }
            while(true);


        }
        finally {
            monitor.unlock();
        }
    }

    /**
     * To complete!
     */
    public void shutdown() {
        monitor.lock();
        try {
             if (threadCount == 0 && requests.empty() ) {
                 state = State.TERMINATED;
                 shutdownCompleted.signalAll();
             }
             else {
                 state = State.SHUTDOWN_STARTED;

             }

             // Now, we must traverse the inactive threads requests
             // and fulfill the requests telling that the
             // request for work was aborted because the
             // the synchronizer is no longer in the active state
        }
        finally {
            monitor.unlock();
        }
    }

    /**
     * This implementation is completed
     * Note that this operation has no side effects
     * on the sinchronizer state, so we could use
     * here the classic style for monitors, i.e. checking
     * the TERMINATED (terminal) state after the wait
     * @param timeout
     * @return
     * @throws InterruptedException
     */
    public boolean awaitTermination(long timeout) throws InterruptedException {
        monitor.lock();
        try {
            if (state == State.TERMINATED) return true;
            if (timeout ==  0) return false;
            TimeoutHolder th = new TimeoutHolder(timeout);
            do {
                shutdownCompleted.await(th.remaining(), TimeUnit.MILLISECONDS);
                if (state == State.TERMINATED) return true;
                if (th.timeout()) return false;
            }
            while(true);
        }
        finally {
            monitor.unlock();
        }
    }
}