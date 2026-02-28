package think2.graph.algs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import think2.graph.Graph;

/**
    Search algorithms upon a graph. The returned {@link Fill} parent maps of the search algorithms
    always reflects the order of children exploration. For example, on a graph with equal weights
    "Start -> A, B; A -> Finish; B -> Finish;", the path from Start to Finish will always be "Start
    -> A -> Finish" and never "Start -> B -> Finish".
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

        private final Map<K, K> parents; // get(K) -> parent of K.
        private final Map<K, E> edges; // get(K) -> edge from parent of K to K.
        private final Map<K, V> values; // get(K) -> value of K.
        private final K source;

        public Fill(
            final Map<K, K> parents,
            final Map<K, E> edges,
            final Map<K, V> values,
            final K source
        ) {
            final boolean properSourcePrescence =
                parents.get(source) == null &&
                edges.get(source) == null &&
                values.get(source) != null;
            final boolean keySetMatch =
                parents.keySet().equals(values.keySet()) &&
                parents.keySet().equals(edges.keySet());
            if (!properSourcePrescence || !keySetMatch) {
                throw new IllegalArgumentException();
            }
            this.parents = new HashMap<>(parents);
            this.edges = new HashMap<>(edges);
            this.values = new HashMap<>(values);
            this.source = source;
        }

        public K getSource() {
            return source;
        }

        public boolean isReachable(final K key) {
            return parents.containsKey(key);
        }

        public Path<K, V, E> getPathTo(final K reachableVertexKey) {
            if (!isReachable(reachableVertexKey)) {
                throw new IllegalArgumentException();
            }
            final List<K> pathKeys = new ArrayList<>();
            K walker = reachableVertexKey;
            while (!walker.equals(source)) {
                pathKeys.add(walker);
                walker = parents.get(walker);
            }
            pathKeys.add(source);
            Collections.reverse(pathKeys);

            final List<V> pathValues = pathKeys.stream().map(values::get).toList();
            final List<E> pathEdges = pathKeys.stream().skip(1L).map(edges::get).toList();
            return new Path<>(pathKeys, pathValues, pathEdges);
        }

        private static <K, V, E> Fill<K, V, E> fromParentsAndGraph(
            final Map<K, K> parents,
            final Graph<K, V, E> graph,
            final K source
        ) {
            final Map<K, E> edges = HashMap.newHashMap(parents.size());
            final Map<K, V> values = HashMap.newHashMap(parents.size());
            parents.forEach((child, parent) -> {
                values.put(child, graph.getVertexValue(child));
                edges.put(child, child.equals(source) ? null : graph.getEdge(parent, child));
            });
            return new Fill<K, V, E>(parents, edges, values, source);
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
        return Fill.fromParentsAndGraph(parents, graph, source);
    }
}
