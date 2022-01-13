package pc.li52d;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static pc.li52d.threading.Utils.sleep;


public class Problematic {
    private static final Logger logger = LoggerFactory.getLogger(Problematic.class);

    // try remove the volatile qualifier
    private volatile static  boolean ready;
    private  static int number;


    public static void main(String[] args)
        throws InterruptedException {

        logger.info("Start!");

        Thread t1 = new Thread(() -> {
            while(!ready) ;
            logger.info("number: {}", number);
        });

        t1.start();

        // give thread time to start - ugly :(
        sleep(1000);


        number = 42;
        ready = true;

        t1.join();
        logger.info("Done!");
    }
}
