package think.ints;

/**
    Ring buffer-backed queue of primitve ints. Designed and optimized solely for breath-first
    search. That means no error checking and fixed size. Illegal operations result in unspecified
    behavior.
 */
public final class IntQueue {

    private final int[] backing;
    private final int mask;
    private int head;
    private int tail;

    public IntQueue(final int finalCapacity) {
        // Ring buffers sacrifice one unit of capacity in the backing array to use the head == tail
        // trick, so we make up for it with an extra element here.
        int actualCapacity = Integer.highestOneBit(finalCapacity + 1);
        if (actualCapacity < finalCapacity + 1) {
            actualCapacity <<= 1;
        }
        this.backing = new int[actualCapacity];
        this.mask = actualCapacity - 1;
        this.head = 0;
        this.tail = 0;
    }

    public void add(final int value) {
        // PERF: Without -XX:+DebugNonSafepoints, async-profiler thinks BFS loop time is mostly this
        // method. It's actually only ~17% or so; time is spread pretty evenly in BFS. Also,
        // removing `& mask` below decreases performance for some reason.
        backing[tail] = value;
        tail = (tail + 1) & mask;
    }

    public int remove() {
        final int value = backing[head];
        head = (head + 1) & mask;
        return value;
    }

    public boolean isEmpty() {
        return head == tail;
    }

    public void clear() {
        head = 0;
        tail = 0;
    }
}
