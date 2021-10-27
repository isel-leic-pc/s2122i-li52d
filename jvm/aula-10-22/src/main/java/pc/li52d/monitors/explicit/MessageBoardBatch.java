/**
 * A message board synchronizer using execution delegation pattern.
 * In this case we use a BatchReqQueue since the waiters are all awoken
 * in broadcast and with exactly the same state, so just one representative is needed
 * for all waiters and that is exactly what a BatchReqQueue provides.
 */
package pc.li52d.monitors.explicit;

import pc.li52d.monitors.utils.BatchReqQueue;
import pc.li52d.monitors.utils.TimeoutHolder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static pc.li52d.monitors.utils.TimeoutHolder.INFINITE;

public class MessageBoardBatch<M> {

    private static class Message<M> {
        public final M content;

        // absolute time limit for message
        private final long valLimit;

        public Message() {
            content = null;
            valLimit = 0;
        }

        public Message(M msg, int expMillis) {
            this.content = msg;
            this.valLimit =
                System.currentTimeMillis() + expMillis;
        }

        public boolean isValid() {
            return valLimit >= System.currentTimeMillis();
        }
    }

    private static class Request<M>  {
        public  M msg;
        public boolean done;

        public void complete(M msg) {
            this.msg = msg;
            this.done = true;
        }
    }

    public final static Message EMPTY_MESSAGE = new Message();

    private final Lock monitor;
    private final Condition hasMsg;
    private Message<M> container;
    private final BatchReqQueue<Request<M>> requests;

    public MessageBoardBatch() {
        monitor = new ReentrantLock();
        hasMsg = monitor.newCondition();
        container = EMPTY_MESSAGE;
        requests = new BatchReqQueue<>(new Request<>());
    }

    private void notifyWaiters(M message) {
        BatchReqQueue.Round<Request<M>> current = requests.getCurrent();
        current.value.complete(message);

        // a new round is needed
        requests.newBatch(new Request<>());
        hasMsg.signalAll();
    }

    public void publish(M message, int exposureDuration) {
        monitor.lock();
        try {
            if (exposureDuration == 0) {
                container = EMPTY_MESSAGE;
            }
            else {
                container = new Message<M>(message, exposureDuration);
            }
            notifyWaiters(message);
        }
        finally {
            monitor.unlock();
        }
    }

    public Optional<M> consume(long millis)
        throws InterruptedException {
        monitor.lock();
        try {
            // fast path
            if (container.isValid()) {
                return Optional.of(container.content);
            }
            if (millis == 0)
                return Optional.empty();
            // prepare wait

            BatchReqQueue.Round<Request<M>> round = requests.add();
            TimeoutHolder th = new TimeoutHolder(millis);

            try {
                do {
                    hasMsg.await(th.remaining(),
                        TimeUnit.MILLISECONDS);
                    if (round.value.done) {
                        return Optional.of(round.value.msg);
                    }
                    if (th.timeout()) {
                        requests.remove(round);
                        return Optional.empty();
                    }
                }
                while(true);
            }
            catch(InterruptedException e) {
                if (round.value.done) {
                    Thread.currentThread().interrupt();
                    return Optional.of(round.value.msg);
                }
                requests.remove(round);
                throw e;
            }
        }
        finally {
            monitor.unlock();
        }
    }

    public Optional<M> consume()
        throws InterruptedException {
        return consume(INFINITE);
    }
}
