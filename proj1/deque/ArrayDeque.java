package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Iterable<T>, Deque<T> {
    private int size;
    private T[] items;
    private int front;
    private int rear;

    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
        front = 0;
        rear = 1;
    }

    private void resize(int newSize) {
        T[] newArray = (T[]) new Object[newSize];
        if (size == 0) {
            items = newArray;
            return;
        }
        int startIndex = (front + 1) % items.length;
        int endIndex = (rear - 1 + items.length) % items.length;
        if (endIndex >= startIndex) {
            // this case is elements in a contiguous block
            System.arraycopy(items, startIndex, newArray,
                    0, size);
            front = newSize - 1;
            rear = size;
        } else {
            // elements are wrapped around the end of the array
            System.arraycopy(items, startIndex, newArray,
                    newSize - items.length + startIndex, items.length - startIndex);
            System.arraycopy(items, 0, newArray, 0, endIndex + 1);
            front = newSize - items.length + startIndex -1;
        }
        items = newArray;
    }

    public void addFirst(T item) {
        if (items.length == size) {
            resize(items.length * 2);
        }
        items[front] = item;
        front = (front - 1 + items.length) % items.length;
        size++;
    }

    public void addLast(T item) {
        if (items.length == size) {
            resize(items.length * 2);
        }
        items[rear] = item;
        rear = (rear + 1) % items.length;
        size++;
    }

    public int size() {
        return size;
    }

    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        if (size > 16 && (size - 1) * 1.0 / items.length < 0.25) {
            resize(items.length / 2);
        }
        front = (front + 1) % items.length;
        size--;
        return items[front];
    }

    public T removeLast() {
        if (size == 0) {
            return null;
        }
        // !!直接(size - 1) / items.length会丢弃小数
        if (size > 16 && (size - 1) * 1.0 / items.length < 0.25) {
            resize(items.length / 2);
        }
        rear = (rear - 1 + items.length) % items.length;
        size--;
        return items[rear];
    }

    public T get(int index) {
        if (index >= size || index < 0) {
            return null;
        }
        return items[(front + 1 + index) % items.length];
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int index;

        public ArrayDequeIterator() {
            index = 0;
        }

        public boolean hasNext() {
            return index < size;
        }

        public T next() {
            T item = items[(front + 1 + index) % items.length];
            index++;
            return item;
        }
    }

    public void printDeque() {
        for (T item : this) {
            System.out.print(item + " ");
        }
        System.out.println();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ArrayDeque)) {
            return false;
        }
        ArrayDeque<?> other = (ArrayDeque<?>) o;
        if (other.size != size) {
            return false;
        }
        Iterator<T> it1 = this.iterator();
        Iterator<?> it2 = other.iterator();
        while (it1.hasNext()) {
            T item1 = it1.next();
            Object item2 = it2.next();
            if (!item1.equals(item2)) {
                return false;
            }
        }
        return true;
    }

}
