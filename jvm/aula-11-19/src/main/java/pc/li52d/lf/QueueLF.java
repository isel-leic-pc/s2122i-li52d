package pc.li52d.lf;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.DoubleUnaryOperator;

public class QueueLF<E> {

    private static class Node<E> {
        final E value;
        final AtomicReference<Node<E>> next;

        Node(E value) {
            this.value = value;
            this.next = new AtomicReference<>();
        }

        Node() {
            this(null);
        }
    }

    private final AtomicReference<Node<E>> head, tail;

    public QueueLF() {
        Node<E> dummy = new Node<>();
        head = new AtomicReference<>(dummy);
        tail = new AtomicReference<>(dummy);
    }

    public void offer(E elem) {
        Node<E> newNode = new Node<>(elem);
        do {
            Node<E> obsTail = tail.get();
            Node<E> obsTailNext = obsTail.next.get();
            if (obsTail == tail.get()) {
                if (obsTailNext == null) {
                    if (obsTail.next.compareAndSet(null, newNode)) {
                        tail.compareAndSet(obsTail, newNode);
                        return;
                    }
                }
                else {
                    tail.compareAndSet(obsTail, obsTailNext);
                }
            }
        }
        while(true);
    }

    public E poll( ) {
        return null;
    }
}
