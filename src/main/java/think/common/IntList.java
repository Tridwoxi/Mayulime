package think.common;

import java.util.NoSuchElementException;
import java.util.function.IntConsumer;

/**
    Resizeable queue and list specialized for primitive ints. Optimized for breadth-first search.
 */
public final class IntList {

    public static final int NOT_FOUND = -1;
    private int[] backing;
    private int mask;
    private int head;
    private int tail;

    public IntList(final int initialCapacity) {
        if (initialCapacity > 0) {
            final int actualCapacity = roundUpToPowerOfTwo(initialCapacity);
            this.backing = new int[actualCapacity];
            this.mask = actualCapacity - 1;
        } else if (initialCapacity == 0) {
            this.backing = IntArrays.EMPTY;
            this.mask = 0;
        } else {
            throw new IllegalArgumentException("" + initialCapacity);
        }
        this.head = 0;
        this.tail = 0;
    }

    public void addRight(final int value) {
        // PERF: async-profiler says this method is 74% of thread runtime for ClimbSolver. But
        // threatening to increase capacity only hurts about 1% since the branch predictor is magic.
        if (backing.length == 0) {
            ensureCapacity(1);
        } else if (((tail + 1) & mask) == head) {
            ensureCapacity(getSize() + 1);
        }
        backing[tail] = value;
        tail = (tail + 1) & mask;
    }

    public int set(final int index, final int value) {
        requireInBounds(index);
        final int physical = (head + index) & mask;
        final int oldValue = backing[physical];
        backing[physical] = value;
        return oldValue;
    }

    public int removeLeft() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        final int value = backing[head];
        head = (head + 1) & mask;
        return value;
    }

    public int removeValue(final int value) {
        final int index = getIndex(value);
        if (index == NOT_FOUND) {
            throw new NoSuchElementException("" + value);
        }
        removeIndex(index);
        return index;
    }

    public int removeIndex(final int index) {
        requireInBounds(index);
        final int physical = (head + index) & mask;
        final int value = backing[physical];
        if (index < getSize() >>> 1) {
            if (head <= physical) {
                System.arraycopy(backing, head, backing, head + 1, physical - head);
            } else {
                System.arraycopy(backing, 0, backing, 1, physical);
                backing[0] = backing[mask];
                System.arraycopy(backing, head, backing, head + 1, mask - head);
            }
            head = (head + 1) & mask;
        } else {
            final int last = (tail - 1) & mask;
            if (physical <= last) {
                System.arraycopy(backing, physical + 1, backing, physical, last - physical);
            } else {
                System.arraycopy(backing, physical + 1, backing, physical, mask - physical);
                backing[mask] = backing[0];
                System.arraycopy(backing, 1, backing, 0, last);
            }
            tail = last;
        }
        return value;
    }

    public int getValue(final int index) {
        requireInBounds(index);
        return backing[(head + index) & mask];
    }

    public int getIndex(final int value) {
        for (int index = head; index != tail; index = (index + 1) & mask) {
            if (backing[index] == value) {
                return (index - head) & mask;
            }
        }
        return NOT_FOUND;
    }

    public void forEach(final IntConsumer action) {
        for (int index = head; index != tail; index = (index + 1) & mask) {
            action.accept(backing[index]);
        }
    }

    public boolean isEmpty() {
        return head == tail;
    }

    public int getSize() {
        return (tail - head) & mask;
    }

    public void clear() {
        head = 0;
        tail = 0;
    }

    public void ensureCapacity(final int desiredCapacity) {
        // Ring buffers can only be filled to `capacity - 1`, so we allocate an extra spot.
        final int incrementedCapacity = desiredCapacity + 1;
        if (incrementedCapacity <= backing.length) {
            return;
        }
        final int size = getSize();
        final int newCapacity = roundUpToPowerOfTwo(incrementedCapacity);
        final int[] newBacking = new int[newCapacity];
        dumpInto(newBacking, size);
        this.backing = newBacking;
        this.mask = newCapacity - 1;
        this.head = 0;
        this.tail = size;
    }

    public int[] extractBacking() {
        final int size = getSize();
        final int[] result = new int[size];
        dumpInto(result, size);
        return result;
    }

    private void requireInBounds(final int index) {
        if (index < 0 || index >= getSize()) {
            throw new IndexOutOfBoundsException("" + index);
        }
    }

    private void dumpInto(final int[] target, final int size) {
        if (head <= tail) {
            System.arraycopy(backing, head, target, 0, size);
        } else {
            final int headLen = backing.length - head;
            System.arraycopy(backing, head, target, 0, headLen);
            System.arraycopy(backing, 0, target, headLen, tail);
        }
    }

    private static int roundUpToPowerOfTwo(final int nonNegativeNumber) {
        if (nonNegativeNumber == 0) {
            return 0;
        }
        int result = Integer.highestOneBit(nonNegativeNumber);
        if (result < nonNegativeNumber) {
            result <<= 1;
        }
        return result;
    }
}
