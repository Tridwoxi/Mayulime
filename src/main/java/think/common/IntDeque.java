package think.common;

import java.util.NoSuchElementException;

final class IntDeque {

    private int[] buffer;
    private int head;
    private int tail;
    private int size;

    IntDeque(final int initialCapacity) {
        this.buffer = new int[initialCapacity];
        this.head = 0;
        this.tail = 0;
        this.size = 0;
    }

    void addLast(final int value) {
        if (size == buffer.length) {
            grow();
        }
        buffer[tail] = value;
        tail = (tail + 1) % buffer.length;
        size += 1;
    }

    int removeFirst() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        final int value = buffer[head];
        head = (head + 1) % buffer.length;
        size -= 1;
        return value;
    }

    boolean isEmpty() {
        return size == 0;
    }

    int size() {
        return size;
    }

    void clear() {
        head = 0;
        tail = 0;
        size = 0;
    }

    private void grow() {
        final int[] next = new int[buffer.length * 2];
        if (head < tail) {
            System.arraycopy(buffer, head, next, 0, size);
        } else {
            final int tailLen = buffer.length - head;
            System.arraycopy(buffer, head, next, 0, tailLen);
            System.arraycopy(buffer, 0, next, tailLen, tail);
        }
        head = 0;
        tail = size;
        buffer = next;
    }
}
