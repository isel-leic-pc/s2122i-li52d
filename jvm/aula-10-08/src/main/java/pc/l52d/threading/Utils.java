package pc.l52d.threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    public static void uninterruptibleJoin(Thread t) {
        while(true) {
            try {
                t.join();
                return;
            }
            catch(InterruptedException e) {
                log.info("join for thread {} was interrupted", t.getName());

            }
        }
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch(InterruptedException e) {
            log.info("sleep on thread {} was interrupted", Thread.currentThread().getId());
            throw new RuntimeException(e);
        }
    }

    public static void wasteCpuForGivenSeconds(int secs) {
        Instant actual = Instant.now();
        Instant limit = actual.plus(secs, ChronoUnit.SECONDS);
        while (actual.isBefore(limit)) {
            Thread.yield();
            actual = Instant.now();
        }
    }
}
