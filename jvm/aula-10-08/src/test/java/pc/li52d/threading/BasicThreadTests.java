package pc.li52d.threading;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static pc.l52d.threading.Utils.*;

public class BasicThreadTests {
    private static final Logger log = LoggerFactory.getLogger(BasicThreadTests.class);

    private static final int NTHREADS = 20;
    private static final int NREPS = 100000;


    private  void helloFunc() {
        sleep(2000);
        System.out.printf("Hello from new thread %s, id = %d\n",
            Thread.currentThread().getName(),
            Thread.currentThread().getId());
    }

    @Test
    public void helloFromThread() {
        var testThread = Thread.currentThread();
        System.out.printf("test thread is %s, id %d\n",
            testThread.getName(),
            testThread.getId());

        Thread t = new Thread(() -> {
            try {
                sleep(2000);
                System.out.printf("Hello from new thread %s, id = %d\n",
                    Thread.currentThread().getName(),
                    Thread.currentThread().getId());
            }
            catch(Exception e) {
                System.out.printf("exception %s on thread %s\n",
                    e, Thread.currentThread().getName());
            }
        });

        t.setDaemon(false);
        t.start();

        System.out.println("created thread is " + t.getId());
        System.out.println("Is daemon? " + t.isDaemon());


        // threads can belong to a given group
        // the child threads belong by default to the parent group

        // check this for new thread  and test thread
        var tgroup = t.getThreadGroup();
        assertEquals(testThread.getThreadGroup(), tgroup);

        // show all tgroup group threads
        Thread[] grouped = null;
        tgroup.enumerate(grouped =new Thread[tgroup.activeCount()]);

        for(Thread thr : grouped) {
            System.out.println("group thread " + thr.getName());
        }

        System.out.println("Thread t isAlive= " + t.isAlive());

        // all tgroup thread can be destroyed
        // JUnit does this at the end of each test terminating all threads
        // created in the test. Confirm this by commenting the next line
        uninterruptibleJoin(t);
        System.out.println("After join thread t isAlive= " +t.isAlive());
    }

    @Test
    public  void hello_from_thread_code_in_method_reference() {
        Thread t = new Thread(this::helloFunc);

        t.start();

        log.info("Thread t isAlive={}", t.isAlive());

        uninterruptibleJoin(t);
        log.info("After join thread t isAlive={}", t.isAlive());
    }

    @Test
    public void interrupt_thread_test() {
        Thread t = new Thread(() -> {
            wasteCpuForGivenSeconds(4);
            System.out.printf("I'm new  thread %s isAlive state is %d\n",
                Thread.currentThread().getName(),
                Thread.currentThread().isAlive());
            sleep(2000);
            System.out.printf("after sleep in thread {}\n",
                Thread.currentThread().getName());
        });
        t.start();
        t.interrupt();
        System.out.printf("Created thread %s isAlive state is %s",
            t.getName(), t.isAlive() ? "true" : "false");
        uninterruptibleJoin(t);
        System.out.println("after join");
    }



    private int counter = 0;

    private  void incLoop() {
        for(int j =0; j < NREPS; ++j) {
            synchronized (this) {
                counter++;
            }
        }
    }

    @Test
    public void multi_thread_counter_increment_without_sync_test() {
        counter = 0;

        List<Thread> threads = new ArrayList<>();

        for(int i= 0; i < NTHREADS; ++i) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < NREPS; ++j) {
                    counter++;
                }
            });
            threads.add(t);
            t.start();
        }


        for(Thread t : threads) {
            uninterruptibleJoin(t);
        }
        assertEquals(NREPS*NTHREADS, counter);

    }

    @Test
    public void multi_thread_counter_increment_with_sync_test() {

        List<Thread> threads = new ArrayList<>();

        for(int i= 0; i < NTHREADS; ++i) {
            Thread t = new Thread(this::incLoop);
            threads.add(t);
            t.start();
        }


        for(Thread t : threads) {
            uninterruptibleJoin(t);
        }
        assertEquals(NREPS*NTHREADS, counter);

    }
}
