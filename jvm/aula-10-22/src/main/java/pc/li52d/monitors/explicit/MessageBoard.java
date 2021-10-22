/**
 * A message board synchronizer using execution delegation pattern.
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


public class MessageBoard<M> {

    private static class Message<M> {
        public final M content;

        // absolute time limit for message
        private final long valLimit;

        // construtor for an invalid message
        public Message() {
            content = null;
            valLimit = 0;
        }

        // construtor for a valid message
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
    }

    public final static Message EMPTY_MESSAGE = new Message();

    private Lock monitor;
    private Condition hasMsg;
    private Message<M> container;
    private NodeList<Request<M>> requests;

    public MessageBoard() {
        monitor = new ReentrantLock();
        hasMsg = monitor.newCondition();
        container = EMPTY_MESSAGE;
        requests = new NodeList<>();
    }

    /**
     * Auxiliary function to process all consumer pending requests
     * Note that all waiting nodes are processed and
     * received exactly the same state
     * @param message
     */
    private void notifyWaiters(M message) {
        for(Request<M> req : requests) {
            req.msg = message;
            req.done = true;
        }
        requests.clear();
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
            Request<M> req = new Request();
            NodeList.Node<Request<M>> node = requests.addLast(req);

            TimeoutHolder th = new TimeoutHolder(millis);

            try {
                do {
                    hasMsg.await(th.remaining(),
                                 TimeUnit.MILLISECONDS);
                    if (req.done) {
                        return Optional.of(req.msg);
                    }
                    if (th.timeout()) {
                        requests.remove(node);
                        return Optional.empty();
                    }
                }
                while(true);
            }
            catch(InterruptedException e) {
                if (req.done) {
                    Thread.currentThread().interrupt();
                    return Optional.of(req.msg);
                }
                requests.remove(node);
                throw e;
            }
        }
        finally {
            monitor.unlock();
        }
    }

    public Optional<M> consume() throws InterruptedException {
        return consume(INFINITE);
    }
}
