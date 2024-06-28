package deque;

import java.util.Iterator;

// 循环双链表版本
public class LinkedListDeque<T> implements Iterable<T>, Deque<T> {
    // 哨兵节点
    private Node sentinal;
    private int size;

    private class Node {
        T item;
        Node prev;
        Node next;

        Node(T item, Node prev, Node next) {
            this.item = item;
            this.prev = prev;
            this.next = next;
        }
    }

    // 构造方法:this is the second method which is recommended by Teacher
    public LinkedListDeque() {
        sentinal = new Node(null, null, null);
        size = 0;
        sentinal.next = sentinal;
        sentinal.prev = sentinal;
    }

    public void addFirst(T item) {
        Node newNode = new Node(item, sentinal, sentinal.next);
        // what amazing, like a  coincidence,
        // no longer need to consider special cases like if(sentinal.next == null)
        sentinal.next.prev = newNode;
        sentinal.next = newNode;
        size++;
    }

    public void addLast(T item) {
        Node newNode = new Node(item, sentinal.prev, sentinal);
        sentinal.prev.next = newNode;
        sentinal.prev = newNode;
        size++;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        Node curNode = sentinal.next;
        while (curNode != sentinal) {
            System.out.print(curNode.item + " ");
            curNode = curNode.next;
        }
        System.out.println();
    }

    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        Node removeNode = sentinal.next;
        removeNode.next.prev = sentinal;
        sentinal.next = removeNode.next;
        size--;
        return removeNode.item;
    }

    public T removeLast() {
        if (size == 0) {
            return null;
        }
        Node removeNode = sentinal.prev;
        removeNode.prev.next = sentinal;
        sentinal.prev = removeNode.prev;
        size--;
        return removeNode.item;
    }

    @Override
    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private int index;
        private Node curNode;

        LinkedListDequeIterator() {
            index = 0;
            curNode = sentinal.next;
        }

        public boolean hasNext() {
            // index = 0, 1, 2...; size = 1, 2, 3...; so not has equal
            return index < size;
        }

        public T next() {
            T temp = curNode.item;
            curNode = curNode.next;
            index++;
            return temp;
        }
    }

    public T get(int index) {
        if (index >= size || index < 0) {
            return null;
        }
        int curIndex = 0;
        // this syntax is novel to me, it is a new concept
        for (T i : this) {
            if (curIndex == index) {
                return i;
            } else {
                curIndex++;
            }
        }
        return null;
    }

    private T getRecursiveHelp(int index, Node curNode) {
        if (index > 0) {
            return getRecursiveHelp(index - 1, curNode.next);
        }
        return curNode.item;
    }

    public T getRecursive(int index) {
        if (index >= size || index < 0) {
            return null;
        }
        return getRecursiveHelp(index, sentinal.next);
    }

    @Override
    public boolean equals(Object o) {
        // l realize why chatgpt is avoidance, it is quite useful to who learn by themselves
        if (o == this) {
            return true;
        }
        if (!(o instanceof Deque)) {
            return false;
        }
        Deque<?> other = (Deque<?>) o;
        if (other.size() != size) {
            return false;
        }
        int index = 0;
        Iterator<T> it1 = this.iterator();
        for (T item : this) {
            if (!item.equals(other.get(index++))) {
                return false;
            }
        }
        return true;
    }

}
