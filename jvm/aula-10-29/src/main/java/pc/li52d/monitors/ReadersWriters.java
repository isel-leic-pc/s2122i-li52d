package pc.li52d.monitors;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReadersWriters {
    private Lock monitor;

    private int nReaders;
    private boolean writer;


    public ReadersWriters() {
        monitor = new ReentrantLock();
        // to complete
    }

    public void enterRead( )
        throws  InterruptedException {
        monitor.lock();
        try {
            // to complete
        }
        finally {
            monitor.unlock();
        }
    }

    public void leaveRead() {
        monitor.lock();
        try {
             // to complete
        }
        finally {
            monitor.unlock();
        }
    }

    public void leaveWrite() {
        monitor.lock();
        try {
           // to complete
        }
        finally {
            monitor.unlock();
        }
    }

    public void enterWrite()
        throws InterruptedException {
        monitor.lock();
        try {
            // to complete
        }
        finally {
            monitor.unlock();
        }
    }
}
