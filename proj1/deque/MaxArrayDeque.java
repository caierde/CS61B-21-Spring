package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> c) {
        super();
        this.comparator = c;
    }

    public T max() {
        if (isEmpty()) return null;
        T maxItem = get(0);
        for (T item : this) {
            maxItem = comparator.compare(maxItem, item) > 0 ? maxItem : item;
        }
        return maxItem;
    }

    public T max(Comparator<T> c) {
        if (isEmpty()) return null;
        T maxItem = get(0);
        for (T item : this) {
            maxItem = c.compare(maxItem, item) > 0 ? maxItem : item;
        }
        return maxItem;
    }
}
