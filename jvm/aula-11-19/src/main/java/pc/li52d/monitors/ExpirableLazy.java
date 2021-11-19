package pc.li52d.monitors;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class ExpirableLazy<T> {
    private static Object TO_BUILD = new Object();

    // states
    private static final int UNAVAIABLE = 1;
    private static final int AVAIABLE = 2;
    private static final int BUILDING = 3;

    private final long timeToLive;

    private final Supplier<T> supplier;
    private final Lock monitor;
    private final Condition valueProduced;

    private T value;
    private long expirationTime;
    private int state;

    public ExpirableLazy(Supplier<T> provider, long timeToLive) {
        this.supplier = provider;
        this.timeToLive = timeToLive;
        this.monitor = new ReentrantLock();
        this.valueProduced = monitor.newCondition();
        this.state = UNAVAIABLE;
    }

    private boolean isValid() {
        return state == AVAIABLE &&
            expirationTime > System.currentTimeMillis();
    }

    private Object tryGetValue() {
        if (isValid()) return value;
        if  (state != BUILDING) {
            state = BUILDING;
            return TO_BUILD;
        }
        return null;
    }

    private T getValue() throws InterruptedException {
        monitor.lock();
        try {
            // fast path
            Object val = tryGetValue();
            if (val == TO_BUILD) return null;
            if (val != null) return (T) val;
            // wait
            try {
                do {
                    valueProduced.await();
                    val = tryGetValue();
                    if (val == TO_BUILD) return null;
                    if (val != null) return (T) val;
                }
                while (true);
            }
            catch(InterruptedException e) {
                if (state == UNAVAIABLE) valueProduced.signal();
                throw e;
            }
        } finally {
            monitor.unlock();
        }
    }

    private T produceValue() throws ExecutionException {
        try {
            T val = supplier.get();
            processResult(val);
            return val;
        }
        catch(Exception e) {
            processResult(null);
            throw new ExecutionException(e);
        }
    }

    private void processResult(T r)  {
        monitor.lock();
        try {
            if (r == null) {
                state = UNAVAIABLE;
                valueProduced.signal();
            }
            else {
                value = r;
                expirationTime = System.currentTimeMillis() + timeToLive;
                state = AVAIABLE;
                valueProduced.signalAll();
            }
        }
        finally {
            monitor.unlock();
        }
    }

    public T get() throws InterruptedException, ExecutionException {

        T val = getValue();
        if (val != null) return value;

        // try produce new value
        return produceValue();

    }

}
