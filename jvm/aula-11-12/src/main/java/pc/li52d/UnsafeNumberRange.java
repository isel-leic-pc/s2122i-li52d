package pc.li52d;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class UnsafeNumberRange {

    private static class RangeHolder {
        private final int lower;
        private final int  upper;

        public RangeHolder(int lower, int upper) {
            this.lower = lower;
            this.upper= upper;
        }
    }

    // Must keep the invariant lower <= upper!
    private AtomicReference<RangeHolder> range =
        new AtomicReference<>(new RangeHolder(0,0));

    public void setLower(int l) {
        /*
        if (l > upper)
            throw new IllegalArgumentException();
        lower = l;
         */

        do {
            RangeHolder obs = range.get();
            if (l > obs.upper)
                throw new IllegalArgumentException();
            RangeHolder nr = new RangeHolder(l, obs.upper);

            if (range.compareAndSet(obs, nr))
               return;
        }
        while(true);
    }

    public void setUpper(int u) {
        /*
        if (u < lower)
            throw new IllegalArgumentException();
        upper = u;
         */
        do {
            RangeHolder obs = range.get();
            if (u < obs.lower)
                throw new IllegalArgumentException();
            RangeHolder nr = new RangeHolder(obs.lower, u);

            if (range.compareAndSet(obs, nr))
                return;
        }
        while(true);

    }
}

