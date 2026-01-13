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

    public static <T> ArrayList<T> fill(T elem, int size) {
        final ArrayList<T> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(elem);
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

    // == AStarQueue. ==================================================================

    public static record Ordered<T>(T item, int priority) {}

    public static final class AStarQueue<T> {

        private final ArrayList<Ordered<T>> heap;
        private final HashMap<T, Integer> indices;

        public AStarQueue() {
            this.heap = new ArrayList<>();
            this.indices = new HashMap<>();
        }

        public void add(T item, int priority) {
            assert !indices.containsKey(item);
            heap.add(new Ordered<>(item, priority));
            final int index = heap.size() - 1;
            indices.put(item, index);
            bubbleUp(index);
        }

        public boolean contains(T item) {
            return indices.containsKey(item);
        }

        public int priority(T item) {
            assert contains(item);
            return heap.get(indices.get(item)).priority();
        }

        public void decrease(T item, int priority) {
            assert indices.containsKey(item);
            assert priority < heap.get(indices.get(item)).priority();
            final int index = indices.get(item);
            heap.set(index, new Ordered<>(item, priority));
            bubbleUp(index);
        }

        public boolean isEmpty() {
            return heap.isEmpty();
        }

        public T remove() {
            assert !heap.isEmpty();
            final Ordered<T> min = heap.getFirst();
            final Ordered<T> last = heap.removeLast();
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
                if (heap.get(walk).priority() >= heap.get(parent).priority()) {
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
                if (
                    left < heap.size() &&
                    heap.get(left).priority() < heap.get(smallest).priority()
                ) {
                    smallest = left;
                }
                if (
                    right < heap.size() &&
                    heap.get(right).priority() < heap.get(smallest).priority()
                ) {
                    smallest = right;
                }
                if (smallest == walk) {
                    break;
                }
                swap(walk, smallest);
                walk = smallest;
            }
        }

        private void swap(int i, int j) {
            assert i != j;
            final Ordered<T> tmp = heap.get(i);
            heap.set(i, heap.get(j));
            heap.set(j, tmp);
            indices.put(heap.get(i).item(), i);
            indices.put(heap.get(j).item(), j);
        }
    }
}
