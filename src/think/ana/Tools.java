package think.ana;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
    Small miscellany.
 */
public final class Tools {

    // == List manipulation. ===========================================================

    public static <T> ArrayList<T> flatten(ArrayList<ArrayList<T>> lists) {
        final int size = lists
            .stream()
            .mapToInt(x -> x.size())
            .sum();
        final ArrayList<T> result = new ArrayList<>(size);
        for (ArrayList<T> list : lists) {
            result.addAll(list);
        }
        assert result.size() == size;
        return result;
    }

    public static <T> boolean rectangular(ArrayList<ArrayList<T>> lists) {
        return pairwiseStream(lists).allMatch(p -> p.a.size() == p.b.size());
    }

    public static <T> ArrayList<T> fill(T item, int size) {
        final ArrayList<T> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(item);
        }
        assert result.size() == size;
        return result;
    }

    // == Pairwise. ====================================================================

    public static record Pair<T>(T a, T b) {}

    /**
        Same as Python's `itertools.pairwise`.
     */
    public static <T> Iterable<Pair<T>> pairwise(final Iterable<T> items) {
        return () -> new Pairwise<>(items);
    }

    public static <T> Stream<Pair<T>> pairwiseStream(final Iterable<T> items) {
        return StreamSupport.stream(pairwise(items).spliterator(), false);
    }

    private static final class Pairwise<T> implements Iterator<Pair<T>> {

        private final Iterator<T> iterator;
        private T a; // Pair.a, the first of its two items.

        public Pairwise(final Iterable<T> items) {
            this.iterator = items.iterator();
            if (this.iterator.hasNext()) {
                this.a = this.iterator.next();
            }
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Pair<T> next() {
            final T b = iterator.next();
            final Pair<T> pair = new Pair<>(a, b);
            a = b;
            return pair;
        }
    }

    // == Ordering. ====================================================================

    public static record UniOrdered<T>(
        T item,
        int order1
    ) implements Comparable<UniOrdered<T>> {
        @Override
        public int compareTo(final UniOrdered<T> other) {
            return Integer.compare(order1, other.order1);
        }
    }

    public static record BiOrdered<T>(
        T item,
        int order1,
        int order2
    ) implements Comparable<BiOrdered<T>> {
        @Override
        public int compareTo(final BiOrdered<T> other) {
            final int cmpPriority = Integer.compare(order1, other.order1);
            if (cmpPriority != 0) {
                return cmpPriority;
            }
            return Integer.compare(order2, other.order2);
        }
    }

    // == AStarQueue. ==================================================================

    public static final class AStarQueue<T> {

        private final ArrayList<BiOrdered<T>> heap;
        private final HashMap<T, Integer> indices;
        private int tiebreaker;

        public AStarQueue() {
            this.heap = new ArrayList<>();
            this.indices = new HashMap<>();
            // LIFO tiebreaker causes DFS instead of BFS when multiple paths have equal
            // length, such as traveling diagonally with no obstacles. On a 10 by 10
            // grid, getting from (0, 0) to (9, 9) adds 35 cells to the frontier
            // instead of all 100 with a FIFO tiebreaker.
            this.tiebreaker = 0;
        }

        public void add(T item, int priority) {
            assert !indices.containsKey(item);
            assert tiebreaker != Integer.MIN_VALUE;
            heap.add(new BiOrdered<>(item, priority, tiebreaker--));
            final int index = heap.size() - 1;
            indices.put(item, index);
            bubbleUp(index);
        }

        public boolean contains(T item) {
            return indices.containsKey(item);
        }

        public int priority(T item) {
            assert contains(item);
            return heap.get(indices.get(item)).order1();
        }

        public void decrease(T item, int priority) {
            assert indices.containsKey(item);
            final int index = indices.get(item);
            assert priority < heap.get(index).order1();
            final int order2 = heap.get(index).order2();
            heap.set(index, new BiOrdered<>(item, priority, order2));
            bubbleUp(index);
        }

        public boolean isEmpty() {
            return heap.isEmpty();
        }

        public T remove() {
            assert !heap.isEmpty();
            final BiOrdered<T> min = heap.getFirst();
            final BiOrdered<T> last = heap.removeLast();
            indices.remove(min.item);
            if (heap.isEmpty()) {
                return min.item;
            }
            heap.set(0, last);
            indices.put(last.item(), 0);
            bubbleDown(0);
            return min.item;
        }

        private void bubbleUp(final int index) {
            int walk = index;
            while (walk > 0) {
                final int parent = (walk - 1) / 2;
                if (!lessThan(walk, parent)) {
                    break;
                }
                swap(walk, parent);
                walk = parent;
            }
        }

        private void bubbleDown(final int index) {
            int walk = index;
            while (true) {
                final int left = 2 * walk + 1;
                final int right = 2 * walk + 2;
                int smallest = walk;
                if (left < heap.size() && lessThan(left, smallest)) {
                    smallest = left;
                }
                if (right < heap.size() && lessThan(right, smallest)) {
                    smallest = right;
                }
                if (smallest == walk) {
                    break;
                }
                swap(walk, smallest);
                walk = smallest;
            }
        }

        private boolean lessThan(int i, int j) {
            final BiOrdered<T> left = heap.get(i);
            final BiOrdered<T> right = heap.get(j);
            return left.compareTo(right) < 0;
        }

        private void swap(int i, int j) {
            assert i != j;
            final BiOrdered<T> tmp = heap.get(i);
            heap.set(i, heap.get(j));
            heap.set(j, tmp);
            indices.put(heap.get(i).item(), i);
            indices.put(heap.get(j).item(), j);
        }
    }
}
