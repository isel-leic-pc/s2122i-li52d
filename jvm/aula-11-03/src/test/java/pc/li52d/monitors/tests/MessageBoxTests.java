package pc.li52d.monitors.tests;


import static org.junit.Assert.*;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pc.li52d.monitors.utils.TimeoutHolder;
import pc.li52d.monitors.MessageBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static pc.li52d.threading.Utils.sleep;

public class MessageBoxTests {
    private static final Logger log = LoggerFactory.getLogger(MessageBoxTests.class);

    private static class Box<T> {
        public T value;

        Box(T initial) {
            value = initial;
        }
    }

    @Test
    public void simple_wait_for_message_test()
        throws InterruptedException {

        final int value = 2000;

        MessageBox<Integer> mb = new MessageBox<>();
        boolean[] interrupted = {false};
        int[] nsend = {0};

        Box<Optional<Integer>> result = new Box<>(Optional.empty());


        Thread sender = new Thread(() -> {
            while(mb.nWaiters() == 0);
            nsend[0] = mb.sendToAll(value);
        });

        Thread receiver = new Thread(()-> {
            try {
                result.value = mb.waitForMessage(2000);
            }
            catch(InterruptedException e) {
                interrupted[0] = true;
            }
        });


        sender.start();
        receiver.start();

        sender.join(2000);
        receiver.join(5000);

        assertFalse(sender.isAlive());
        assertFalse(receiver.isAlive());
        int valueReceived = result.value.orElse(0);

        assertEquals(value, valueReceived);
        assertFalse(interrupted[0]);
    }

    @Test
    public void simple_wait_for_message_with_timeout_test()
        throws InterruptedException {
        MessageBox<Integer> mb = new MessageBox<>();
        boolean[] interrupted = {false};
        Optional[] result =new Optional[1];

        Thread t = new Thread(()-> {
            try {
                result[0] = mb.waitForMessage(2000);
            }
            catch(InterruptedException e) {
                interrupted[0] = true;
            }
        });

        t.start();
        t.join(5000);
        assertFalse(t.isAlive());
        assertTrue(result[0] != null && result[0].isPresent());
        assertFalse(interrupted[0]);
    }

    @Test
    public void simple_wait_for_message_interrupted_test()
        throws InterruptedException {
        MessageBox<Integer> mb = new MessageBox<>();
        boolean[] interrupted = {false};

        Thread t = new Thread(()-> {
            try {
                mb.waitForMessage(2000);
            }
            catch(InterruptedException e) {
                interrupted[0] = true;
            }
        });

        t.start();
        while(t.getState() != Thread.State.TIMED_WAITING &&
            t.getState() != Thread.State.TERMINATED) {
            log.info("thread state = {}", t.getState());
            sleep(50);
        }




        t.interrupt();
        t.join(5000);

        assertFalse(t.isAlive());
        assertEquals(0, mb.nWaiters());
        assertTrue(interrupted[0]);
    }





    private static class MessageStats {
        public final AtomicInteger sendCount = new AtomicInteger();
        public final AtomicInteger receiveCount = new AtomicInteger();
        public final AtomicInteger delivered = new AtomicInteger();
    }

    private static final int NSENDERS = 10;
    private static final int NRECEIVERS = 10;

    private static final int NITERATIONS = 10000;

    private  ConcurrentHashMap<Integer,MessageStats> stats;
    private MessageBox<Integer> mb;
    private AtomicInteger timeoutCount;
    private AtomicInteger interruptCount;
    private AtomicInteger zeroCount;

    void sender( ) {
        for(int i=0; i < NITERATIONS; ++i) {
            int key =
                Math.abs(ThreadLocalRandom.current().nextInt()) % 100;
            int tsenders = mb.sendToAll(key);
            if (tsenders == 0) {
                zeroCount.incrementAndGet();
            }
            else {
                MessageStats ms =
                stats.computeIfAbsent(key, (k) -> new MessageStats());
                ms.sendCount.addAndGet(tsenders);
                ms.delivered.incrementAndGet();
            }
            sleep(1);
        }
    }

    void receiver(   ) {
        try {
            while(true) {
                Optional<Integer> okey =
                    mb.waitForMessage(TimeoutHolder.INFINITE);
                if (!okey.isPresent()) timeoutCount.incrementAndGet();
                else {
                    MessageStats ms = stats.computeIfAbsent(okey.get(), (k) -> new MessageStats());
                    ms.receiveCount.incrementAndGet();
                }
            }
        }
        catch(InterruptedException e) {
            interruptCount.incrementAndGet();
        }
    }


    @Test
    public void multiple_senders_receivers_load_test()
        throws InterruptedException {
        stats = new ConcurrentHashMap<>();
        mb = new MessageBox<>();
        timeoutCount = new AtomicInteger();
        interruptCount = new AtomicInteger();
        zeroCount = new AtomicInteger();
        List<Thread> senders = new ArrayList<>();
        List<Thread> receivers = new ArrayList<>();

        // create senders
        for (int i=0; i < NSENDERS; ++i) {
            Thread t = new Thread(this::sender);
            senders.add(t);
            t.start();
        }

        // create receivers
        for (int i=0; i < NRECEIVERS; ++i) {
            Thread t = new Thread(this::receiver);
            receivers.add(t);
            t.start();
        }

        // wait while there are alive senders
        do {
            sleep(1000);
            if (!senders.stream().anyMatch(t -> t.isAlive()))
                break;
        }
        while(true);

        // assertions

        for (Thread rcv : receivers) {
            rcv.join(1);
            assertTrue(rcv.isAlive());
        }

        receivers.forEach(r -> r.interrupt());

        for (Thread rcv : receivers) {
            rcv.join(200);
            assertFalse(rcv.isAlive());
        }

        int[] totalDelivered = {0};
        int[] nkeys = {0};

        stats.forEach((k,v) -> {
            assertEquals(v.receiveCount.get(), v.sendCount.get());
            //log.info("key: {}, senders= {}, receivers= {}\n", k, v.sendCount.get(), v.receiveCount.get());
            totalDelivered[0]+= v.delivered.get();
            nkeys[0]++;
        });

        totalDelivered[0] += zeroCount.get();

        assertEquals(NITERATIONS*NSENDERS, totalDelivered[0]);
        log.info("totalSend = {}", totalDelivered[0]);
        log.info("interruptCount = {}", interruptCount.get());
        log.info("timeoutCount = {}", timeoutCount.get());
        log.info("zeroCount = {}", zeroCount.get());
        log.info("nkeys = {}", nkeys[0]);
    }
}
