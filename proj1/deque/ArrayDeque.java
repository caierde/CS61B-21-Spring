package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Iterable<T>{
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
        if (rear > front + 1) {
            System.arraycopy(items, front + 1, newArray, newSize - rear + front + 2, rear - front - 2);
            front = newSize - rear + front + 1;
            rear = 0;
        } else {
            System.arraycopy(items, front + 1, newArray, newSize - items.length + front + 1, items.length - front - 1);
            System.arraycopy(items, 0, newArray, 0, rear);
            front = newSize - items.length + front;
        }
        items = newArray;
    }

    public void addFirst(T item) {
        if (items.length == size) resize(items.length * 2);
        items[front] = item;
        front = (front - 1 + items.length) % items.length;
        size++;
    }

    public void addLast(T item) {
        if (items.length == size) resize(items.length * 2);
        items[rear] = item;
        rear = (rear + 1) % items.length;
        size++;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public T removeFirst() {
        if (size == 0) return null;
        if (size > 16 && (size - 1) / items.length < 0.25) resize(items.length / 2);
        front = (front + 1) % items.length;
        size--;
        return items[front];
    }

    public T removeLast() {
        if (size == 0) return null;
        if (size > 16 && (size - 1) / items.length < 0.25) resize(items.length / 2);
        rear = (rear - 1 + items.length) % items.length;
        size--;
        return items[rear];
    }

    public T get(int index) {
        if (index >= size || index < 0) return null;
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
        if (o == this) return true;
        if (!(o instanceof ArrayDeque)) return false;
        ArrayDeque<T> other = (ArrayDeque<T>) o;
        if (other.size != size) return false;
        int index = 0;
        for (T item : this) {
            if (item != other.get(index++)) return false;
        }
        return false;
    }

}
