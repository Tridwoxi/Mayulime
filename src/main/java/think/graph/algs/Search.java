package think.graph.algs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import think.graph.Graph;

/**
    Search algorithms upon a graph. The returned {@link Fill} parent maps of the search algorithms
    always reflects the order of children exploration. For example, on a graph with equal weights,
    "Start -> A, B; A -> Finish; B -> Finish;" then the path from Start to Finish will always be
    "Start -> A -> Finish" and never "Start -> B -> Finish".
 */
public final class Search {

    /**
        A Path is an ordered sequence of vertices on a graph. Vertices need not be unique. A Path
        contains N vertices and N-1 edges between them, with N >= 1. The path components are
        mutable, so please take care to mutate only in a prudent fashion.
     */
    public record Path<K, V, E>(List<K> vertexKeys, List<V> vertexValues, List<E> edges) {
        public Path {
            // Although it doesn't look like it does anything, this defensive copying works.
            vertexKeys = new ArrayList<>(vertexKeys);
            vertexValues = new ArrayList<>(vertexValues);
            edges = new ArrayList<>(edges);
            if (vertexKeys.size() != vertexValues.size() || vertexKeys.size() != edges.size() + 1) {
                throw new IllegalArgumentException();
            }
        }

        public Path<K, V, E> shallowCopy() {
            return new Path<>(vertexKeys, vertexValues, edges);
        }
    }

    /**
        A Fill is a result of a search operation from a source. It resembles a directed tree with
        `source` as its root.
     */
    public static final class Fill<K, V, E> {

        private final Map<K, K> toParent; // get(K) -> parent of K.
        private final Map<K, E> fromParent; // get(K) -> edge from parent of K to K.
        private final Map<K, V> toValue; // get(K) -> value of K.
        private final K source;

        public Fill(
            final Map<K, K> toParent,
            final Map<K, E> fromParent,
            final Map<K, V> toValue,
            final K source
        ) {
            this.toParent = new HashMap<>(toParent);
            this.fromParent = new HashMap<>(fromParent);
            this.toValue = new HashMap<>(toValue);
            this.source = source;
            validate(() -> {
                throw new IllegalArgumentException();
            });
        }

        public K getSource() {
            return source;
        }

        public boolean isReachable(final K key) {
            return toParent.containsKey(key);
        }

        public Path<K, V, E> getPathTo(final K reachableVertexKey) {
            if (!isReachable(reachableVertexKey)) {
                throw new IllegalArgumentException();
            }
            final List<K> keys = new ArrayList<>();
            K walker = reachableVertexKey;
            while (!walker.equals(source)) {
                keys.add(walker);
                walker = toParent.get(walker);
            }
            keys.add(source);
            Collections.reverse(keys);

            final List<V> values = keys.stream().map(toValue::get).toList();
            final List<E> edges = keys.stream().skip(1L).map(fromParent::get).toList();
            return new Path<>(keys, values, edges);
        }

        private static <K, V, E> Fill<K, V, E> fromToParentsAndGraph(
            final Map<K, K> toParent,
            final Graph<K, V, E> graph,
            final K source
        ) {
            final Map<K, E> fromParent = HashMap.newHashMap(toParent.size());
            final Map<K, V> toValue = HashMap.newHashMap(toParent.size());
            toParent.forEach((child, parent) -> {
                toValue.put(child, graph.getVertexValue(child));
                if (!child.equals(source)) {
                    fromParent.put(child, graph.getEdge(parent, child));
                }
            });
            return new Fill<K, V, E>(toParent, fromParent, toValue, source);
        }

        private void validate(final Runnable uponFailure) {
            if (toParent.size() != toValue.size() || toParent.size() != fromParent.size() + 1) {
                uponFailure.run();
            }
            final Predicate<K> okay = vertexKey ->
                !vertexKey.equals(source) &&
                (!toParent.containsKey(vertexKey) || !toValue.containsKey(vertexKey));
            if (!toParent.keySet().stream().allMatch(okay)) {
                uponFailure.run();
            }
        }
    }

    private Search() {}

    public static <K, V, E> Fill<K, V, E> breadthFirst(final Graph<K, V, E> graph, final K source) {
        final Map<K, K> parents = new HashMap<>();
        final Deque<K> frontier = new ArrayDeque<>();
        // I'm pretty sure this null does no harm. HashMap permits nulls, and the parents map
        // escapes only to Fill::fromToParentsAndGraph, which checks child against source instead of
        // treating null like a sentinel.
        parents.put(source, null);
        frontier.addLast(source);
        while (!frontier.isEmpty()) {
            final K parent = frontier.removeFirst();
            for (final K child : graph.getChildren(parent)) {
                if (!parents.containsKey(child)) {
                    parents.put(child, parent);
                    frontier.addLast(child);
                }
            }
        }
        return Fill.fromToParentsAndGraph(parents, graph, source);
    }
}
