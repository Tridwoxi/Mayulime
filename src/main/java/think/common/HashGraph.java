package think.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
    A graph is a collection of vertex keys (K) which hold the vertex values (V) of the vertices
    they represent and are connected by edges (E). Graphs do not support duplicate keys or edges.

    All methods that return a boolean indicate if the graph changed after the operation. All
    methods that accept a vertex key except for {@link #containsVertexKey(V)}, {@link #putVertex(V,
    V)}, {@link #removeVertex(V)} throw a {@link NoSuchElementException} if the key is not
    contained in this graph.

    This implementation is HashMap-backed. Its operations have theoretically optimal time
    complexity in exchange for poor constant factor performance. If different implementations are
    desired, you may find the Graph interface (look for it in git history) useful.
 */
public final class HashGraph<K, V, E> {

    private final Map<K, Map<K, E>> children;
    private final Map<K, Map<K, E>> parents;
    private final Map<K, V> values;
    private final Map<K, Map<K, E>> either; // Alias, for when either map would do.

    public HashGraph(final int expectedSize) {
        this.children = HashMap.newHashMap(expectedSize);
        this.parents = HashMap.newHashMap(expectedSize);
        this.values = HashMap.newHashMap(expectedSize);
        this.either = this.children;
    }

    public HashGraph(final HashGraph<K, V, E> other) {
        this(other.getNumVertices());
        for (final K vertexKey : other.getAllVertexKeys()) {
            putVertex(vertexKey, other.getVertexValue(vertexKey));
            for (final K destinationKey : other.getChildren(vertexKey)) {
                putEdge(vertexKey, destinationKey, other.getEdge(vertexKey, destinationKey));
            }
        }
    }

    // == Querying. ==

    public boolean containsVertexKey(final K vertexKey) {
        return either.containsKey(vertexKey);
    }

    public V getVertexValue(final K vertexKey) {
        throwIfNotContains(vertexKey);
        return values.get(vertexKey);
    }

    public boolean containsEdge(final K sourceKey, final K destinationKey) {
        throwIfNotContains(sourceKey);
        throwIfNotContains(destinationKey);
        return children.get(sourceKey).containsKey(destinationKey);
    }

    public E getEdge(final K sourceKey, final K destinationKey) {
        throwIfNotContains(sourceKey);
        throwIfNotContains(destinationKey);
        if (!children.get(sourceKey).containsKey(destinationKey)) {
            throw new NoSuchElementException();
        }
        return children.get(sourceKey).get(destinationKey);
    }

    public Set<K> getChildren(final K parentKey) {
        throwIfNotContains(parentKey);
        return new HashSet<>(children.get(parentKey).keySet());
    }

    public Set<K> getParents(final K childKey) {
        throwIfNotContains(childKey);
        return new HashSet<>(parents.get(childKey).keySet());
    }

    public Set<K> getAllVertexKeys() {
        return new HashSet<>(either.keySet());
    }

    public int getNumVertices() {
        return either.size();
    }

    // == Mutation. ==

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

    // == Helpers. ==

    private void throwIfNotContains(final K vertexKey) {
        if (!containsVertexKey(vertexKey)) {
            throw new NoSuchElementException();
        }
    }
}
