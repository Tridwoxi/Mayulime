package think.common;

/**
    Fixed-size circular buffer-backed deque of ints. It is unspecified behaviour to fill
    this implementation to full capacity.
 */
final class IntDeque {

    private final int[] buffer;
    private final int mask;
    private int head;
    private int tail;

    IntDeque(final int finalCapacity) {
        int actualCapacity = Integer.highestOneBit(finalCapacity);
        if (actualCapacity < finalCapacity) {
            actualCapacity <<= 1;
        }
        this.buffer = new int[actualCapacity];
        this.mask = actualCapacity - 1;
        this.head = 0;
        this.tail = 0;
    }

    void addLast(final int value) {
        // PERF: Async profiler says this method is 74% of thread runtime for ClimbV1Solver.
        buffer[tail] = value;
        tail = (tail + 1) & mask;
    }

    int removeFirst() {
        final int value = buffer[head];
        head = (head + 1) & mask;
        return value;
    }

    boolean isEmpty() {
        return head == tail;
    }

    void clear() {
        head = 0;
        tail = 0;
    }
}
