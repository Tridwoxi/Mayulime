package think.graph.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.function.Function;
import think.graph.Graph;
import think.graph.Graph.MutableGraph;

/**
    HashMap-backed implementation of a graph. All operations have theoretically optimal time
    complexity in exchange for poor constant factor performance. This implementation is general and
    supports all graphs supported by {@link MutableGraph}. The iteration order of all collections
    returned by this implementation is order of insertion.
 */
public final class HashGraph<K, V, E> implements MutableGraph<K, V, E> {

    private final SequencedMap<K, SequencedMap<K, E>> children;
    private final SequencedMap<K, SequencedMap<K, E>> parents;
    private final SequencedMap<K, V> values;
    private final SequencedMap<K, SequencedMap<K, E>> either; // Read-only view.

    public HashGraph(final int expectedSize) {
        this.children = LinkedHashMap.newLinkedHashMap(expectedSize);
        this.parents = LinkedHashMap.newLinkedHashMap(expectedSize);
        this.values = LinkedHashMap.newLinkedHashMap(expectedSize);
        this.either = children;
    }

    public HashGraph(final Graph<K, V, E> other) {
        this(16);
        throw new UnsupportedOperationException();
    }

    private HashGraph(
        final SequencedMap<K, SequencedMap<K, E>> children,
        final SequencedMap<K, SequencedMap<K, E>> parents,
        final SequencedMap<K, V> values
    ) {
        this.children = children;
        this.parents = parents;
        this.values = values;
        this.either = children;
    }

    @Override
    public boolean containsVertexKey(final K vertexKey) {
        return either.containsKey(vertexKey);
    }

    @Override
    public V getVertexValue(final K vertexKey) {
        throwIfNotContains(vertexKey);
        return values.get(vertexKey);
    }

    @Override
    public boolean containsEdge(final K sourceKey, final K destinationKey) {
        throwIfNotContains(sourceKey);
        throwIfNotContains(destinationKey);
        return children.get(sourceKey).containsKey(destinationKey);
    }

    @Override
    public E getEdge(final K sourceKey, final K destinationKey) {
        throwIfNotContains(sourceKey);
        throwIfNotContains(destinationKey);
        if (!children.get(sourceKey).containsKey(destinationKey)) {
            throw new NoSuchElementException();
        }
        return children.get(sourceKey).get(destinationKey);
    }

    @Override
    public SequencedSet<K> getChildren(final K parentKey) {
        throwIfNotContains(parentKey);
        return new LinkedHashSet<>(children.get(parentKey).keySet());
    }

    @Override
    public SequencedSet<K> getParents(final K childKey) {
        throwIfNotContains(childKey);
        return new LinkedHashSet<>(parents.get(childKey).keySet());
    }

    @Override
    public SequencedSet<K> getAllVertexKeys() {
        return new LinkedHashSet<>(either.keySet());
    }

    @Override
    public SequencedSet<V> getAllVertexValues() {
        return new LinkedHashSet<>(values.values());
    }

    @Override
    public HashGraph<K, V, E> shallowCopy() {
        final Function<
            SequencedMap<K, SequencedMap<K, E>>,
            SequencedMap<K, SequencedMap<K, E>>
        > copier = outer -> {
            final SequencedMap<K, SequencedMap<K, E>> outerCopy = LinkedHashMap.newLinkedHashMap(
                outer.size()
            );
            outer.forEach((key, inner) -> {
                final SequencedMap<K, E> innerCopy = new LinkedHashMap<>(inner);
                outerCopy.put(key, innerCopy);
            });
            return outerCopy;
        };
        final SequencedMap<K, SequencedMap<K, E>> childrenCopy = copier.apply(children);
        final SequencedMap<K, SequencedMap<K, E>> parentsCopy = copier.apply(parents);
        final SequencedMap<K, V> valuesCopy = new LinkedHashMap<>(values);
        return new HashGraph<>(childrenCopy, parentsCopy, valuesCopy);
    }

    @Override
    public int getNumVertices() {
        return either.size();
    }

    @Override
    public boolean putVertex(final K vertexKey, final V vertexValue) {
        if (!containsVertexKey(vertexKey)) {
            children.put(vertexKey, new LinkedHashMap<>());
            parents.put(vertexKey, new LinkedHashMap<>());
        }
        if (values.get(vertexKey) == null || !values.get(vertexKey).equals(vertexValue)) {
            values.put(vertexKey, vertexValue);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeVertex(final K vertexKey) {
        if (!containsVertexKey(vertexKey)) {
            return false;
        }
        final List<K> childrenCopy = new ArrayList<>(children.get(vertexKey).keySet());
        final List<K> parentsCopy = new ArrayList<>(parents.get(vertexKey).keySet());
        childrenCopy.forEach(other -> parents.get(other).remove(vertexKey));
        parentsCopy.forEach(other -> children.get(other).remove(vertexKey));
        children.remove(vertexKey);
        parents.remove(vertexKey);
        values.remove(vertexKey);
        return true;
    }

    @Override
    public boolean putEdge(final K sourceKey, final K destinationKey, final E edge) {
        throwIfNotContains(sourceKey);
        throwIfNotContains(destinationKey);
        if (
            children.get(sourceKey).containsKey(destinationKey) &&
            edge.equals(children.get(sourceKey).get(destinationKey))
        ) {
            return false;
        }
        children.get(sourceKey).put(destinationKey, edge);
        parents.get(destinationKey).put(sourceKey, edge);
        return true;
    }

    @Override
    public boolean removeEdge(final K sourceKey, final K destinationKey) {
        throwIfNotContains(sourceKey);
        throwIfNotContains(destinationKey);
        if (!children.get(sourceKey).containsKey(destinationKey)) {
            return false;
        }
        children.get(sourceKey).remove(destinationKey);
        parents.get(destinationKey).remove(sourceKey);
        return true;
    }

    private void throwIfNotContains(final K vertexKey) {
        if (!containsVertexKey(vertexKey)) {
            throw new NoSuchElementException();
        }
    }
}
