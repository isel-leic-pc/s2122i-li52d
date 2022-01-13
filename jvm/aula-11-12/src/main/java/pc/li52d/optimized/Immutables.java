package pc.li52d.optimized;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static pc.li52d.threading.Utils.sleep;

public class Immutables {

    private static final Logger logger = LoggerFactory.getLogger(Immutables.class);


    public static class Point {
        public  final int x;
        public  final int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "(" + x + ", "+ y + ")";
        }
    }

    public  static Point globalPoint;

    public static void main(String[] args)
        throws InterruptedException{
        while (true) {
            globalPoint = null;

            Thread t1 = new Thread(() -> {
                logger.info("thread start");
                while (globalPoint == null) Thread.yield();
                System.out.println(globalPoint);
            });
            t1.start();

            sleep(1000);

            logger.info("set globalPoint");
            globalPoint = new Point(2,3);
            t1.join();
        }
    }
}
