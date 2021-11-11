package pc.li52d.monitors;

import pc.li52d.monitors.utils.TimeoutHolder;


import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A simple future implementation where the future is the owner
 * of an activity that asynchronously will produce the future value.
 * This means, for instance, that the value production may terminate with an exception
 * as well with some legal value.
 * Note this is fundamentally different from the futures for BlockingMessageQueue,
 * where they are just a container for the value produced from an incoming
 * enqueue operation.
 * @param <T>
 */
public class SimpleFuture<T> implements Future<T> {
    // the future states
    private  enum State { NEW, STARTED, CANCELLED, COMPLETED, ERROR }

    public static <T> Future<T> create(Callable<T> supplier) {
        SimpleFuture<T> fut = new SimpleFuture<>(supplier);
        fut.exec();
        return fut;
    }

    private final Callable<T> supplier;
    private final Lock monitor;
    private final Condition done;

    // mutable state
    private Exception exception;
    private T value;
    private State state;
    private Thread thread;

    private void exec() {
        monitor.lock();
        thread = new Thread(this::run);
        thread.start();
    }

    private void run() {
        monitor.lock();
        try {
            if (state != State.NEW) return;
            state = State.STARTED;
        }
        finally {
            monitor.unlock();
        }
        try {
            trySet(supplier.call(), null);
        }
        catch(InterruptedException e) {
            // possibly consequence of a cancel operation,
            // just ignore it
        }
        catch(Exception e) {
            // an exception ocurred on the value supplier
            // try set the final state accordingly
            trySet(null, e);
        }



    }


    private SimpleFuture(Callable<T> supplier) {
        this.supplier = supplier;
        monitor = new ReentrantLock();
        done = monitor.newCondition();
        state = State.NEW;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        monitor.lock();
        try {
            if (state != State.NEW && state != State.STARTED)
                return false;
            state = State.CANCELLED;
            done.signalAll();
            if (mayInterruptIfRunning)
                thread.interrupt();
            return true;
        }
        finally {
            monitor.unlock();
        }
    }

    /**
     * Note the lock acquiring even just for return some state of the future
     * @return
     */
    @Override
    public boolean isCancelled() {
        monitor.lock();
        boolean cancelled = state == State.CANCELLED;
        monitor.unlock();
        return cancelled;
    }

    /**
     * Note the lock acquiring even just for return some state of the future
     * @return
     */
    @Override
    public boolean isDone() {

        monitor.lock();
        boolean done =  state == State.COMPLETED ||
                        state == State.CANCELLED ||
                        state == State.ERROR;

        monitor.unlock();
        return done;
    }

    private T tryGetValue() throws ExecutionException {
        switch(state) {
            case COMPLETED:
                return value;
            case ERROR:
                throw new ExecutionException(exception);
            case CANCELLED:
                throw new CancellationException();
        }
        return null;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        do {
            try {
                return get(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                // this never occurr in practice
            }
        }
        while(true);
    }

    @Override
    public T get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        monitor.lock();
        try {
            T val;
            if ((val = tryGetValue()) != null) return val;

            TimeoutHolder th = new TimeoutHolder(unit.toMillis(timeout));
            do {
                done.await(th.remaining(), TimeUnit.MILLISECONDS);
                if ((val = tryGetValue()) != null) return val;
                if (th.timeout()) throw new TimeoutException();
            }
            while(true);
        }
        finally {
            monitor.unlock();
        }
    }

    private void trySet(T val, Exception exception) {
        monitor.lock();
        try {
            if (state != State.STARTED) return;
            if (exception != null) {
                this.exception = exception;
                state = State.ERROR;
            }
            else {
                value = val;
                state = State.COMPLETED;
            }
            done.signalAll();
        }
        finally {
            monitor.unlock();
        }
    }
}
