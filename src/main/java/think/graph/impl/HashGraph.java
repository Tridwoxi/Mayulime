package think.graph.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import think.graph.Graph.MutableGraph;

/**
    HashMap-backed implementation of a graph. All operations have theoretically optimal time
    complexity in exchange for poor constant factor performance. This implementation is general and
    supports all graphs supported by {@link MutableGraph}.
 */
public final class HashGraph<K, V, E> implements MutableGraph<K, V, E> {

    private final Map<K, Map<K, E>> children;
    private final Map<K, Map<K, E>> parents;
    private final Map<K, V> values;
    private final Map<K, Map<K, E>> either; // Read-only view.

    public HashGraph(final int expectedSize) {
        this.children = HashMap.newHashMap(expectedSize);
        this.parents = HashMap.newHashMap(expectedSize);
        this.values = HashMap.newHashMap(expectedSize);
        this.either = children;
    }

    private HashGraph(
        final Map<K, Map<K, E>> children,
        final Map<K, Map<K, E>> parents,
        final Map<K, V> values
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
    public Set<K> getChildren(final K parentKey) {
        throwIfNotContains(parentKey);
        return new HashSet<>(children.get(parentKey).keySet());
    }

    @Override
    public Set<K> getParents(final K childKey) {
        throwIfNotContains(childKey);
        return new HashSet<>(parents.get(childKey).keySet());
    }

    @Override
    public Set<K> getAllVertexKeys() {
        return new HashSet<>(either.keySet());
    }

    @Override
    public Set<V> getAllVertexValues() {
        return new HashSet<>(values.values());
    }

    @Override
    public HashGraph<K, V, E> shallowCopy() {
        final Function<Map<K, Map<K, E>>, Map<K, Map<K, E>>> copier = outer -> {
            final Map<K, Map<K, E>> outerCopy = HashMap.newHashMap(outer.size());
            outer.forEach((key, inner) -> {
                final Map<K, E> innerCopy = new HashMap<>(inner);
                outerCopy.put(key, innerCopy);
            });
            return outerCopy;
        };
        final Map<K, Map<K, E>> childrenCopy = copier.apply(children);
        final Map<K, Map<K, E>> parentsCopy = copier.apply(parents);
        final Map<K, V> valuesCopy = new HashMap<>(values);
        return new HashGraph<>(childrenCopy, parentsCopy, valuesCopy);
    }

    @Override
    public boolean putVertex(final K vertexKey, final V vertexValue) {
        if (!containsVertexKey(vertexKey)) {
            children.put(vertexKey, new HashMap<>());
            parents.put(vertexKey, new HashMap<>());
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
