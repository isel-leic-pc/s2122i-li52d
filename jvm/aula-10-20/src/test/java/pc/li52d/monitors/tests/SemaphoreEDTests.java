package pc.li52d.monitors.tests;

import org.junit.Test;
import pc.li52d.monitors.SemaphoreFifoED;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static pc.li52d.threading.Utils.sleep;
import static pc.li52d.threading.Utils.uninterruptibleJoin;
import static pc.li52d.monitors.utils.TimeoutHolder.INFINITE;


public class SemaphoreEDTests {
    private enum State {
        Started,
        Done,
        Timeout,
        Interrupted
    }

    @Test
    public void acquire_with_timeout_test()
        throws InterruptedException {
        final State[] state = {State.Started};
        final SemaphoreFifoED sem = new SemaphoreFifoED(0);
        final long timeout = 3000;

        Thread t = new Thread(() -> {
            try {
                System.out.println("thread started");
                if (!sem.acquire(1, timeout)) {
                    System.out.println("time ouccurs in acquire");
                    state[0] = State.Timeout;
                }
                System.out.println("thread end");
            }
            catch(InterruptedException e) {
                state[0] = State.Interrupted;
            }
        });

        t.start();
        t.join(timeout + 2000);

        assertEquals(State.Timeout, state[0]);
    }

    private void multiple_acquire_check_order_round_test()  {
        int[] toAcquireValues = { 7, 6, 5, 4 };

        AtomicInteger index = new AtomicInteger(0);

        int[] acquiredValues = new int[toAcquireValues.length];
        State[] threadStates = new State[toAcquireValues.length];
        Arrays.fill(threadStates, State.Started );

        List<Thread> threads = new ArrayList<>();

        // semaphore to test
        SemaphoreFifoED sem = new SemaphoreFifoED(0);

        System.out.println("Start acquire threads");
        for(int n : toAcquireValues ) {
            Thread t = new Thread(() -> {
                try {
                    sem.acquire(n, INFINITE);
                    int i;
                    acquiredValues[i= index.getAndIncrement()] = n;
                    threadStates[i] = State.Done;
                }
                catch(InterruptedException e) {
                    threadStates[index.getAndIncrement()] = State.Interrupted;
                }
            });
            threads.add(t);
            t.start();

            // give a time to force acquire order just for test purposes
            sleep(500);
        }


        System.out.println("Start releases");
        for (int i= 0; i < toAcquireValues.length; ++i) {
            sem.release(toAcquireValues[i]);
            // wait a moment to give time of the acquire thread to put is
            // result in the acquiredValues array...
            sleep(500);
        }


        // wait for all acquire threads termination
        threads.forEach(t -> uninterruptibleJoin(t));

        System.out.println("Check assertions");
        assertFalse( Arrays
            .stream(threadStates)
            .anyMatch(s -> s != State.Done));

        assertArrayEquals(toAcquireValues, acquiredValues);
    }

    /**
     * Check if the order of aquisition is FIFO
     * This is a tricky test because we need to rely on sleeps
     * to have confidence of the flow we want
     */
    @Test
    public void multiple_acquire_check_order_test() {
        multiple_acquire_check_order_round_test();
    }

    @Test
    public void multiple_acquire_check_order_many_rounds_test() {


        for (int i= 0; i < 100; ++i)
            multiple_acquire_check_order_round_test();
    }
}
