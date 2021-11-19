package pc.li52d.monitors;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class ExpirableLazyOpt<T> {
    private static final Object TO_BUILD = new Object();

    private static final int UNAVAIABLE = 1;
    private static final int AVAIABLE = 0x4000000;
    private static final int BUILDING = 3;

    private final Supplier<T> supplier;
    private final long timeToLive;
    private final Lock monitor;
    private final Condition valueProduced;
    private final AtomicInteger state;

    // the volatile necessity for these fields
    // will be reconsidered in next lecture
    private volatile int avaiable_version;
    private volatile int waiters;
    private volatile T value;
    private volatile long expirationTime;


    public ExpirableLazyOpt(Supplier<T> provider, long timeToLive) {
        this.supplier = provider;
        this.timeToLive = timeToLive;
        this.monitor = new ReentrantLock();
        this.valueProduced = monitor.newCondition();
        this.state = new AtomicInteger(UNAVAIABLE);
        this.waiters = 0;
        this.avaiable_version = 0;
    }

    private boolean isValid(int obsState) {
        return (obsState & AVAIABLE) == AVAIABLE &&
            expirationTime > System.currentTimeMillis();
    }

    private Object tryGetValue() {
        int obsState = state.get();
        T val = value;
        if (obsState == state.get() &&
            isValid(obsState)) return val;
        // Note that a thread here can observe
        // the AVAIABLE state and get preempted before
        // the CAS, so thar other thread can sucessfully
        // do the CAS, produce and set a new value
        // passing again to AVAIABLE state, so the
        // first thread, when resuming execution, could
        // successfully do the CAS, jeopardizing the
        // previous build!
        // The version associated to AVAIABLE state
        // avoid this ABA like problem!
        if (obsState != BUILDING &&
          state.compareAndSet(obsState, BUILDING)) {
            return TO_BUILD;
        }
        return null;

    }

    private T getValue() throws InterruptedException {
        // fast path
        Object val = tryGetValue();
        if (val == TO_BUILD) return null;
        if (val != null) return (T) val;

        monitor.lock();
        try {
            // wait
            try {
                waiters++;
                do {
                    val = tryGetValue();
                    if (val == TO_BUILD) return null;
                    if (val != null) return (T) val;
                    valueProduced.await();
                }
                while (true);
            }
            catch(InterruptedException e) {
                if (state.get() == UNAVAIABLE) valueProduced.signal();
                throw e;
            }
            finally {
                waiters--;
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

    private void setResult(T r) {
        // not there is no need for atomicity here
        // since just one thread can be at BUILDING state at a time
        if (r == null) {
            value = null;
            state.set(UNAVAIABLE);
        }
        else {
            value = r;
            expirationTime = System.currentTimeMillis() + timeToLive;
            // the use of the version associated to AVAIABLE state
            // avoid the ABA like problem
            int v = avaiable_version + 1;
            if (v == AVAIABLE) v = 0;
            avaiable_version = v;
            state.set(AVAIABLE | avaiable_version);
        }
    }

    private void processResult(T r)  {
        setResult(r);
        if (waiters > 0) {
            monitor.lock();
            try {
                if (waiters > 0) {
                    if (r == null) {
                        valueProduced.signal();
                    } else {
                        valueProduced.signalAll();
                    }
                }
            } finally {
                monitor.unlock();
            }
        }
    }

    public T get() throws InterruptedException, ExecutionException {
        T val = getValue();
        if (val != null) return val;

        // try produce new value
        return produceValue();
    }

}
