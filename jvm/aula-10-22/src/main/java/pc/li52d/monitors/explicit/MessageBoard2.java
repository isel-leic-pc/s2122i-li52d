/**
 * A variant message board synchronizer using execution delegation pattern.
 * In this case the value of the NodeList nodes is just a messsage.
 * This works presuming that messages can't be null.
 */
package pc.li52d.monitors.explicit;

import pc.li52d.monitors.utils.NodeList;
import pc.li52d.monitors.utils.TimeoutHolder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static pc.li52d.monitors.utils.TimeoutHolder.INFINITE;

public class MessageBoard2<M> {

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


    public final static Message EMPTY_MESSAGE = new Message();

    private final Lock monitor;
    private final Condition hasMsg;
    private Message<M> container;

    private final NodeList<M> requests;

    public MessageBoard2() {
        monitor = new ReentrantLock();
        hasMsg = monitor.newCondition();
        container = EMPTY_MESSAGE;
        requests = new NodeList<>();
    }

    /**
     * Auxiliary function to process all consumer pending requests
     * Note that all waiting nodes are processed and
     * received exactly the same state, in this case, just
     * the message
     * @param message
     */
    private void notifyWaiters(M message) {
        while (!requests.empty()) {
            var node = requests.removeFirstNode();
            node.value = message;
        }
        hasMsg.signalAll();
    }

    public void publish(M message, int exposureDuration) {
        monitor.lock();
        try {
            if (exposureDuration == 0) {
                // a message with an exposure duration of 0
                // doesn't persist, it's just send to the
                // waiting consumers
                container = EMPTY_MESSAGE;
            }
            else {
                container =
                    new Message<>(message, exposureDuration);
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

            NodeList.Node<M> node =
                requests.addLast(null);

            TimeoutHolder th = new TimeoutHolder(millis);

            try {
                do {
                    hasMsg.await(th.remaining(),
                        TimeUnit.MILLISECONDS);
                    if (node.value != null) {
                        return Optional.of(node.value);
                    }
                    if (th.timeout()) {
                        requests.remove(node);
                        return Optional.empty();
                    }
                }
                while(true);
            }
            catch(InterruptedException e) {
                if (node.value != null) {
                    Thread.currentThread().interrupt();
                    return Optional.of(node.value);
                }
                requests.remove(node);
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
