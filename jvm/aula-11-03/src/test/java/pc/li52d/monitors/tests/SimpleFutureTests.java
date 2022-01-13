package pc.li52d.monitors.tests;

import org.junit.Assert;
import org.junit.Test;
import pc.li52d.monitors.SimpleFuture;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static pc.li52d.threading.Utils.sleep;

public class SimpleFutureTests {

    @Test
    public void simple_simple_future_test() {
        int expected_value = 1000;

        Future<Integer> fut = SimpleFuture.create(() -> {
            sleep(2000);
            return expected_value;
        });

        try {
            int result = fut.get(5000, TimeUnit.MILLISECONDS);
            Assert.assertEquals(expected_value, result);
        }
        catch(Exception e) {
            Assert.fail();
        }
    }
}
