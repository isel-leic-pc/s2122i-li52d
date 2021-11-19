package pc.li52d.lockfree;


import java.util.concurrent.atomic.AtomicReference;

public class StackLF<E> {

    private static class Node<E> {
        final E value;
        Node<E> next;

        Node(E value) {
            this.value = value;
            this.next = null;
        }

        Node() {
            this(null);
        }
    }

    private AtomicReference<Node<E>> head =
        new AtomicReference<>();

    public void push(E elem) {
        Node<E> newNode = new Node<>(elem);
        Node<E>  obsHead = null;
        do {
           obsHead = head.get();
           newNode.next = obsHead;
        }
        while(!head.compareAndSet(obsHead, newNode));
    }

    public E pop() {
        do {
            Node<E> obsHead = head.get();
            if (obsHead == null)
                return null;
            if (head.compareAndSet(obsHead, obsHead.next)) {
                return obsHead.value;
            }
        }
        while(true);
    }
}
