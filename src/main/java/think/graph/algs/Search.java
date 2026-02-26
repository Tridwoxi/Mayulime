package think.graph.algs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import think.graph.Graph;

public final class Search {

    /**
        A Path is an ordered sequence of vertices on a graph. Vertices need not be unique. A Path
        contains N vertices and N-1 edges between them, with N >= 1.
     */
    public record Path<V, E>(List<V> vertices, List<E> edges) {
        public Path {
            // This defensive copy is effective.
            vertices = new ArrayList<>(vertices);
            edges = new ArrayList<>(edges);
            if (vertices.size() != edges.size() + 1) {
                throw new IllegalArgumentException();
            }
        }

        public V getStart() {
            return vertices.getFirst();
        }

        public List<V> getTrailingVertices() {
            return new ArrayList<>(vertices.subList(0, vertices.size() - 1));
        }

        public V getFinish() {
            return vertices.getLast();
        }

        public List<V> getLeadingVertices() {
            return new ArrayList<>(vertices.subList(1, vertices.size()));
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

    /**
        A Fill is a result of a search operation from a source. `toParent.get(V)` returns the
        parent of a vertex; `fromParent.get(V)` represents the edge taken from parent to the
        vertex. Together, these form a tree.
     */
    public record Fill<V, E>(Map<V, V> toParent, Map<V, E> fromParent, V source) {
        public Fill {
            toParent = new HashMap<>(toParent);
            fromParent = new HashMap<>(fromParent);
            if (toParent.size() != fromParent().size() + 1) {
                throw new IllegalArgumentException();
            }
        }

        public boolean isReachable(final V vertex) {
            return toParent.containsKey(vertex);
        }

        public Path<V, E> getPathTo(final V vertex) {
            if (!isReachable(vertex)) {
                throw new IllegalArgumentException();
            }
            final List<V> vertices = new ArrayList<>();
            V walker = vertex;
            while (walker != null) {
                vertices.add(walker);
                walker = toParent.get(walker);
            }
            Collections.reverse(vertices);

            final List<E> edges = new ArrayList<>();
            for (int index = 1; index < vertices.size(); index++) {
                edges.add(fromParent.get(vertices.get(index)));
            }
            return new Path<>(vertices, edges);
        }
    }

    private Search() {}

    public static <V, E> Fill<V, E> breadthFirst(final Graph<V, E> graph, final V source) {
        final Map<V, V> parents = new HashMap<>();
        final Deque<V> frontier = new ArrayDeque<>();
        // I'm pretty sure this null does no harm. HashMap permits nulls, and the parents map
        // escapes only to makeFill, which checks child against source instead of treating null
        // like a sentinel.
        parents.put(source, null);
        frontier.addLast(source);
        while (!frontier.isEmpty()) {
            final V parent = frontier.removeFirst();
            for (final V child : graph.getChildren(parent)) {
                if (!parents.containsKey(child)) {
                    parents.put(child, parent);
                    frontier.addLast(child);
                }
            }
        }
        return makeFill(graph, parents, source);
    }

    private static <V, E> Fill<V, E> makeFill(
        final Graph<V, E> graph,
        final Map<V, V> toParent,
        final V source
    ) {
        final Map<V, E> fromParent = HashMap.newHashMap(toParent.size());
        toParent.forEach((child, parent) -> {
            if (!child.equals(source)) {
                fromParent.put(child, graph.getEdge(parent, child).orElseThrow());
            }
        });
        return new Fill<V, E>(toParent, fromParent, source);
    }
}
