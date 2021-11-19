package pc.li52d;

import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Petterson's mutual exclusion algorithm for two threads
 */
public class PettersonLock {

    // An array of two volatile semantic integers,
    // for telling the threads interest in lock acquisition
    private AtomicIntegerArray interested
        = new AtomicIntegerArray(2);

    // for define the acquisition order in case
    // of mutual interest
    private volatile  int turn;

    public void lock(int id) {
        int other = 1 - id;  // the other thread id
        // the observed order of the instructions between
        // lines 20-23 (inclusive) is crucial for algorithm correction
        interested.set(id, 1);
        turn = other;
        while (interested.get(other) == 1 && turn == other) ;
    }

    public void unlock(int id) {
        interested.set(id, 0);
    }

    // test program
    private static final int NTRIES = 20000000;
    static int count;
    static final PettersonLock mutex = new PettersonLock();

    private static void counterThread(int id) {
        for (int i = 0; i < NTRIES; ++i) {
            mutex.lock(id);
            count += 1;
            mutex.unlock(id);
        }
    }

    public static void main(String[] args)
        throws InterruptedException {

        while (true) {
            long startTicks;

            count = 0;
            startTicks = System.currentTimeMillis();
            Thread t1 = new Thread(() -> counterThread(0));
            Thread t2 = new Thread(() -> counterThread(1));

            t1.start();
            t2.start();

            t1.join();
            t2.join();


            System.out.printf("Expected = %d, Real=%d in %dms!\n",
                NTRIES * 2, count, System.currentTimeMillis() - startTicks);
            if (NTRIES * 2 != count)
                throw new IllegalStateException();
        }
    }
}

