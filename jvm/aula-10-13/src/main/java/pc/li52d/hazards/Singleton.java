package pc.li52d.hazards;

import java.util.function.Supplier;

public class Singleton<T> {
    private T value;

    private Supplier<T> factory;

    public Singleton(Supplier<T> f) {
        factory = f;
    }

    public synchronized T getInstance0() {
        if (value == null) {
            value = factory.get();
        }
        return value;
    }

    public  T getInstance() {
        if (value == null) {
            synchronized (this) {
                if (value == null)
                    value = factory.get();
            }
        }
        return value;
    }
}