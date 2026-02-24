package think.graph.algs;

import java.util.ArrayList;
import java.util.function.BiFunction;

public final class Search {

    public record Path<V, E>(ArrayList<V> vertices, ArrayList<E> edges) {
        public Path {
            vertices = new ArrayList<>(vertices);
            edges = new ArrayList<>(edges);
            if (vertices.size() != edges.size() + 1) {
                throw new IllegalArgumentException();
            }
        }

        public V getStart() {
            return vertices.getFirst();
        }

        public ArrayList<V> getTrailingVertices() {
            return new ArrayList<>(vertices.subList(0, vertices.size() - 2));
        }

        public V getFinish() {
            return vertices.getLast();
        }

        public ArrayList<V> getLeadingVertices() {
            return new ArrayList<>(vertices.subList(1, vertices.size() - 1));
        }

        public <R> R reduceEdges(final R initial, final BiFunction<R, E, R> reducer) {
            // This implementation has the reduction run sequentially in exchange for callers not
            // needing to pass a combiner or identity as an initial, unlike Stream::reduce.
            R accumulator = initial;
            for (final E edge : edges) {
                accumulator = reducer.apply(accumulator, edge);
            }
            return accumulator;
        }
    }

    private Search() {}
}
