package pc.li52d.monitors.explicit;

import pc.li52d.monitors.utils.NodeList;
import pc.li52d.monitors.utils.TimeoutHolder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MessageBoard<M> {

    private static class PendingConsumer<M> {
        M content;
        public boolean hasContent;
    }

    public MessageBoard() {

    }

    public void publish(M message, int exposureTime) {

    }

    public Optional<M> consume(long millis)
        throws InterruptedException {
        return Optional.empty();
    }
}
