package pc.li52d.hazards;

import static pc.l52d.threading.Utils.sleep;

public class Account {
    private long balance; // in cents
    private static Object globalLock = new Object();

    public Account(long initialBalance) {
        balance = initialBalance;
    }


    public  static boolean transfer(Account src, Account dst,
                                   long amount) {
        synchronized(globalLock) {
            if (src.balance < amount) return false;

            src.balance -= amount;
            dst.balance += amount;
            return true;
        }
    }
    public  boolean transferTo0(Account dst, long amount) {
        if (balance < amount) return false;

        balance -= amount;
        dst.balance += amount;
        return true;
    }

    public  boolean transferTo(Account dst, long amount) {

        synchronized (this) {

            if (balance < amount) return false;
            synchronized(dst) {
                balance -= amount;
                dst.balance += amount;
            }
        }
        return true;
    }

    public long getBalance() {
        synchronized (this) {
            return balance;
        }
    }
}
