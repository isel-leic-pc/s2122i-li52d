package pc.li52d.lockfree;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TwoLockQueue<E> {

    private static class Node<E> {
        final E value;
        volatile Node<E> next;

        Node(E value) {
            this.value = value;
            this.next = null;
        }
        Node() {
            this(null);
        }
    }

    private Node<E> head, tail;
    private Lock putLock, getLock;

    public TwoLockQueue() {
        head = tail = new Node<>();
        putLock = new ReentrantLock();
        getLock = new ReentrantLock();
    }

    public void offer(E elem) {
        putLock.lock();
        try {
            Node<E> newNode = new Node<>(elem);
            tail.next = newNode;
            tail = newNode;
        }
        finally {
            putLock.unlock();
        }
    }

    public E poll( ) {
        getLock.lock();
        try {
            if (head.next == null)
                return null;
            E elem = head.next.value;
            head = head.next;
            return elem;
        }
        finally {
            getLock.unlock();
        }
    }
}
