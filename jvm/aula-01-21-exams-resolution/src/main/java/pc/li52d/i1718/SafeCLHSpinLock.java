package pc.li52d.i1718;

import java.util.concurrent.atomic.AtomicReference;

public class SafeCLHSpinLock {

        public class CLHNode {
            // the field must be volatile
            public volatile boolean succMustWait = true; // The default is to wait for a lock
        }

        private AtomicReference<CLHNode> tail = new AtomicReference<>();
        // the tail of the wait queue; when null the lock is free

        public CLHNode acquire() {
            CLHNode myNode = new CLHNode();
            do {
                // insert my node at tail of queue and get my predecessor
                CLHNode obsPred = tail.get();
                if (tail.compareAndSet(obsPred, myNode)) {
                    // If there is a predecessor spin until the lock is free; otherwise we got the lock
                    if (obsPred != null) {
                        while (obsPred.succMustWait) Thread.yield();
                    }
                    return myNode;
                }
            }
            while(true);
        }

        public void release(CLHNode myNode /* the node returned from corresponding acquire */ ) {
            // If we are the last node on the queue, then set tail to null; else release successor
            do {
                CLHNode obsPred = tail.get();
                if (obsPred != myNode) {
                    myNode.succMustWait = false; // release our successor
                    return;
                }
                else if (tail.compareAndSet(obsPred, null))
                    return;
            }
            while(true);

        }

}
