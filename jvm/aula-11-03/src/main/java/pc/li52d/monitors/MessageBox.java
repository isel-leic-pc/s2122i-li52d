package pc.li52d.monitors;

import pc.li52d.monitors.utils.NodeList;
import pc.li52d.monitors.utils.TimeoutHolder;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MessageBox<T> {

    public Optional<T> waitForMessage(long timeout)
        throws InterruptedException {
        return Optional.empty();
    }

    public int sendToAll(T message) {
         return 0;
    }

    public int nWaiters() {
        return 0;
    }
}
