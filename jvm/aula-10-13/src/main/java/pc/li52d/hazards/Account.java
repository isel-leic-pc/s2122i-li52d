package pc.li52d.hazards;

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

     public  synchronized boolean transferTo1(Account dst, long amount) {
        if (balance < amount) return false;

        balance -= amount;
        dst.balance += amount;
        return true;
    }

    public  boolean transferToDeadLock(Account dst, long amount) {

        synchronized (this) {

            if (balance < amount) return false;
            synchronized(dst) {
                balance -= amount;
                dst.balance += amount;
            }
        }
        return true;
    }

    public  boolean transferToOk(Account dst, long amount) {
        Account a, b;

        if (this.hashCode() < dst.hashCode()) {
            a = this;
            b = dst;
        }
        else {
            a = dst;
            b = this;
        }
        synchronized (a) {

            if (balance < amount) return false;
            synchronized(b) {
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
