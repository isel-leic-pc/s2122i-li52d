/**
 * A BatchReqQueue is used to efficiently substitute a request queue
 * when all the requesters are awaked at the same time, with the same state
 */
package pc.li52d.monitors.utils;

public class BatchReqQueue<T> {

    // the state wrapper for the batch round
    public static class Round<T> {
        public final T value;
        public Round(T  v) {
            value = v;
        }
    }

    private int countWaiters;
    private Round<T> current;


    public BatchReqQueue(T t) {

        newBatch(t);
    }

    public Round<T> add() {
        countWaiters++;
        return current;
    }

    public void remove(Round<T> r) {
        if (current == null  ||
           current != r || countWaiters == 0)
            throw new IllegalArgumentException();
        countWaiters--;
    }

    public void newBatch(T val) {
        current = new Round<>(val);
        countWaiters = 0;
    }

    public Round<T> getCurrent() {
        return current;
    }

    public int getWaitersCount() {
        return countWaiters;
    }

    public boolean isCompleted(Round<T> r) {
        return current != r;
    }

}
