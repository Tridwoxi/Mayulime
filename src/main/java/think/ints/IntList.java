package think.ints;

import java.util.Arrays;
import java.util.function.IntConsumer;

/**
    Array-backed list of primitive ints.
 */
public final class IntList {

    public static final int NOT_FOUND = -1;
    private int[] backing;
    private int size;

    public IntList(final int initialCapacity) {
        if (initialCapacity > 0) {
            this.backing = new int[initialCapacity];
        } else if (initialCapacity == 0) {
            this.backing = IntArrays.EMPTY;
        } else {
            throw new IllegalArgumentException("" + initialCapacity);
        }
    }

    public IntList(final int[] values) {
        this(values.length);
        extend(values);
    }

    public void add(final int value) {
        ensureCapacity(size + 1);
        backing[size] = value;
        size += 1;
    }

    public void insert(final int index, final int value) {
        if (index == size) {
            add(value);
            return;
        }
        requireInBounds(index);
        ensureCapacity(size + 1);
        System.arraycopy(backing, index, backing, index + 1, size - index);
        backing[index] = value;
        size += 1;
    }

    public void extend(final int[] values) {
        ensureCapacity(size + values.length);
        System.arraycopy(values, 0, backing, size, values.length);
        size += values.length;
    }

    public int get(final int index) {
        requireInBounds(index);
        return backing[index];
    }

    public int set(final int index, final int value) {
        requireInBounds(index);
        final int old = backing[index];
        backing[index] = value;
        return old;
    }

    public int removeElement(final int value) {
        final int index = indexOf(value);
        if (index == NOT_FOUND) {
            return NOT_FOUND;
        }
        removeAt(index);
        return index;
    }

    public int removeAt(final int index) {
        requireInBounds(index);
        final int value = backing[index];
        System.arraycopy(backing, index + 1, backing, index, size - index - 1);
        size -= 1;
        return value;
    }

    public void removeRange(final int startInclusive, final int endExclusive) {
        if (startInclusive >= endExclusive) {
            throw new IllegalArgumentException("" + startInclusive + " " + endExclusive);
        }
        requireInBounds(startInclusive);
        requireInBounds(endExclusive - 1);
        System.arraycopy(backing, endExclusive, backing, startInclusive, size - endExclusive);
        size -= endExclusive - startInclusive;
    }

    public void clear() {
        size = 0;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public boolean contains(final int value) {
        return indexOf(value) != NOT_FOUND;
    }

    public int indexOf(final int value) {
        for (int index = 0; index < size; index += 1) {
            if (backing[index] == value) {
                return index;
            }
        }
        return NOT_FOUND;
    }

    public void forEach(final IntConsumer action) {
        for (int index = 0; index < size; index += 1) {
            action.accept(backing[index]);
        }
    }

    public void ensureCapacity(final int minCapacity) {
        if (minCapacity <= backing.length) {
            return;
        }
        final int actualCapacity = Math.max(minCapacity, backing.length << 1);
        final int[] newBacking = new int[actualCapacity];
        System.arraycopy(backing, 0, newBacking, 0, size);
        backing = newBacking;
    }

    public int[] toArray() {
        return Arrays.copyOf(backing, size);
    }

    private void requireInBounds(final int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("" + index);
        }
    }
}
