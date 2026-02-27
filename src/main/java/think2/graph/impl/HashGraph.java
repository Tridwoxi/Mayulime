package think2.graph.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SequencedMap;
import java.util.SequencedSet;
import think2.graph.Graph;
import think2.graph.Graph.MutableGraph;

/**
    HashMap-backed implementation of a graph. All operations have theoretically optimal time
    complexity in exchange for poor constant factor performance. This implementation is general and
    supports all graphs supported by {@link MutableGraph}. The iteration order of all collections
    returned by this implementation is order of first insertion.
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
        this.either = this.children;
    }

    public HashGraph(final Graph<K, V, E> other) {
        this(other.getNumVertices());
        for (final K vertexKey : other.getAllVertexKeys()) {
            putVertex(vertexKey, other.getVertexValue(vertexKey));
            for (final K destinationKey : other.getChildren(vertexKey)) {
                putEdge(vertexKey, destinationKey, other.getEdge(vertexKey, destinationKey));
            }
        }
    }

    private HashGraph(
        final SequencedMap<K, SequencedMap<K, E>> children,
        final SequencedMap<K, SequencedMap<K, E>> parents,
        final SequencedMap<K, V> values
    ) {
        this.children = shallowCopyNSM(children);
        this.parents = shallowCopyNSM(parents);
        this.values = new LinkedHashMap<>(values);
        this.either = this.children;
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
    public HashGraph<K, V, E> shallowCopy() {
        return new HashGraph<>(children, parents, values);
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

    private <O, I, D> SequencedMap<O, SequencedMap<I, D>> shallowCopyNSM(
        final SequencedMap<O, SequencedMap<I, D>> nestedSequencedMap
    ) {
        // Abbreviations: outer key "O"; inner key "I"; data "D"; nested SequencedMap "NSM".
        final SequencedMap<O, SequencedMap<I, D>> outerCopy = LinkedHashMap.newLinkedHashMap(
            nestedSequencedMap.size()
        );
        nestedSequencedMap.forEach((key, inner) -> {
            final SequencedMap<I, D> innerCopy = new LinkedHashMap<>(inner);
            outerCopy.put(key, innerCopy);
        });
        return outerCopy;
    }
}
