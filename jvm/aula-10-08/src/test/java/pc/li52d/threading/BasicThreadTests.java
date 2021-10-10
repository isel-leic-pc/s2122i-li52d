package pc.li52d.threading;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pc.l52d.threading.basics.BasicExamples;

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
        log.info("Hello from new thread {}, id = {} ",
            Thread.currentThread().getName(),
            Thread.currentThread().getId());
    }

    @Test
    public void helloFromThread() {
        var testThread = Thread.currentThread();
        log.info("test thread is {}, id {}",
            testThread.getName(),
            testThread.getId());

        Thread t = new Thread(() -> {
            try {
                sleep(2000);
                log.info("Hello from new thread {}, id = {} ",
                    Thread.currentThread().getName(),
                    Thread.currentThread().getId());
            }
            catch(Exception e) {
                log.info("exception {} on thread {} ",
                    e, Thread.currentThread().getName());
            }
        });

        t.setDaemon(false);
        t.start();

        log.info("created thread is {}", t.getId());
        log.info("Is daemon? {}", t.isDaemon());


        // threads can belong to a given group
        // the child threads belong by default to the parent group

        // check this for new thread  and test thread
        var tgroup = t.getThreadGroup();
        assertEquals(testThread.getThreadGroup(), tgroup);

        // show all tgroup group threads
        Thread[] grouped = null;
        tgroup.enumerate(grouped =new Thread[tgroup.activeCount()]);

        for(Thread thr : grouped) {
            log.info("group thread {}", thr.getName());
        }

        log.info("Thread t isAlive={}", t.isAlive());

        // all tgroup thread can be destroyed
        // JUnit does this at the end of each test terminating all threads
        // created in the test. Confirm this by commenting the next line
        uninterruptibleJoin(t);
        log.info("After join thread t isAlive={}", t.isAlive());
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
            log.info("I'm new  thread {} isAlive state is {}",
                Thread.currentThread().getName(),
                Thread.currentThread().isAlive());
            sleep(2000);
            log.info("after sleep in thread {}",
                Thread.currentThread().getName());
        });
        t.start();
        t.interrupt();
        log.info("Created thread {} isAlive state is {}", t.getName(), t.isAlive());
        uninterruptibleJoin(t);
        log.info("after join");
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
