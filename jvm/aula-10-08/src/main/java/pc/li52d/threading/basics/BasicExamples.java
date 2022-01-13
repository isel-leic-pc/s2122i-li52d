package pc.li52d.threading.basics;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static pc.li52d.threading.Utils.sleep;


public class BasicExamples {
    private static final Logger log = LoggerFactory.getLogger(BasicExamples.class);

    public static void helloFromThread() {
        Thread t = new Thread(() -> {
            sleep(2000);
            log.info("Hello from new thread {}, id = {} ",
                Thread.currentThread().getName(),
                Thread.currentThread().getId());
        });
        t.setDaemon(true);
        t.start();
        log.info("Thread t isAlive={}", t.isAlive());

    }

    public static void main(String[] args) {
        log.info("On main thread: {}", Thread.currentThread().getName());
        helloFromThread();
    }

}
