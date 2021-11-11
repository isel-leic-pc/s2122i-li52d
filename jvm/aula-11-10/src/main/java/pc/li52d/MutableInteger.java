package pc.li52d;

public class MutableInteger {
    private  int value;

    public  synchronized void set(int value) {

        if (value == 0)
            this.value = value;
    }

    public synchronized int get() {
        return value;
    }
}
