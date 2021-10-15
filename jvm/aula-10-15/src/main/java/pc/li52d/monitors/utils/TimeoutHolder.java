package pc.li52d.monitors.utils;


public class TimeoutHolder {
    private long toExpire;

    public static final long INFINITE = -1;


    private boolean noTimeout() {
        return toExpire == Long.MAX_VALUE;
    }

    public TimeoutHolder(long millis) {
        toExpire = (millis == INFINITE)
            ? toExpire = Long.MAX_VALUE
            : System.currentTimeMillis() + millis;
    }

    public long remaining() {
        if (noTimeout()) return toExpire;
        return Math.max(0, toExpire - System.currentTimeMillis());
    }

    public boolean timeout() {
        return remaining() == 0;
    }
}

