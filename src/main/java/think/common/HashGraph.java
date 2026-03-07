package think.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
    A graph is a collection of vertex keys (K) which hold the vertex values (V) of the vertices
    they represent and are connected by edges (E). Graphs do not support duplicate keys or edges.

    All methods that return a boolean indicate if the graph changed after the operation. All
    methods that accept a vertex key except for {@link #containsVertexKey(V)}, {@link #putVertex(V,
    V)}, {@link #removeVertex(V)} throw a {@link NoSuchElementException} if the key is not
    contained in this graph.

    Set-returning query methods expose unmodifiable live views rather than defensive copies because
    it is more performant. This means callers may need to copy the returned sets.

    This implementation is HashMap-backed. Its operations have theoretically optimal time
    complexity in exchange for poor constant factor performance. If different implementations are
    desired, you may find reviving the Graph interface (look for it in git history) useful.
 */
public final class HashGraph<K, V, E> {

    private final Map<K, Map<K, E>> children;
    private final Map<K, Map<K, E>> parents;
    private final Map<K, Set<K>> childKeyViews;
    private final Map<K, Set<K>> parentKeyViews;
    private final Map<K, V> values;
    private final Set<K> allVertexKeysView;

    public HashGraph(final int expectedSize) {
        this.children = HashMap.newHashMap(expectedSize);
        this.parents = HashMap.newHashMap(expectedSize);
        this.childKeyViews = HashMap.newHashMap(expectedSize);
        this.parentKeyViews = HashMap.newHashMap(expectedSize);
        this.values = HashMap.newHashMap(expectedSize);
        this.allVertexKeysView = Collections.unmodifiableSet(this.children.keySet());
    }

    public HashGraph(final HashGraph<K, V, E> other) {
        this(other.getNumVertices());
        for (final Entry<K, Map<K, E>> entry : other.children.entrySet()) {
            final Map<K, E> childMap = new HashMap<>(entry.getValue());
            this.children.put(entry.getKey(), childMap);
            this.childKeyViews.put(entry.getKey(), Collections.unmodifiableSet(childMap.keySet()));
        }
        for (final Entry<K, Map<K, E>> entry : other.parents.entrySet()) {
            final Map<K, E> parentMap = new HashMap<>(entry.getValue());
            this.parents.put(entry.getKey(), parentMap);
            this.parentKeyViews.put(
                entry.getKey(),
                Collections.unmodifiableSet(parentMap.keySet())
            );
        }
        this.values.putAll(other.values);
    }

    // == Querying. ==

    public boolean containsVertexKey(final K vertexKey) {
        return children.containsKey(vertexKey);
    }

    public V getVertexValue(final K vertexKey) {
        throwIfNotContains(vertexKey);
        return values.get(vertexKey);
    }

    public boolean containsEdge(final K sourceKey, final K destinationKey) {
        throwIfNotContains(destinationKey);
        return getConnections(children, sourceKey).containsKey(destinationKey);
    }

    public E getEdge(final K sourceKey, final K destinationKey) {
        throwIfNotContains(destinationKey);
        final Map<K, E> sourceChildren = getConnections(children, sourceKey);
        final E edge = sourceChildren.get(destinationKey);
        if (edge == null && !sourceChildren.containsKey(destinationKey)) {
            throw new NoSuchElementException();
        }
        return edge;
    }

    public Set<K> getChildren(final K parentKey) {
        return getKeyView(childKeyViews, parentKey);
    }

    public Set<K> getParents(final K childKey) {
        return getKeyView(parentKeyViews, childKey);
    }

    public Set<K> getAllVertexKeys() {
        return allVertexKeysView;
    }

    public int getNumVertices() {
        return children.size();
    }

    // == Mutation. ==

    public boolean putVertex(final K vertexKey, final V vertexValue) {
        final boolean hadVertex = children.containsKey(vertexKey);
        if (!hadVertex) {
            final Map<K, E> childMap = new HashMap<>();
            final Map<K, E> parentMap = new HashMap<>();
            children.put(vertexKey, childMap);
            parents.put(vertexKey, parentMap);
            childKeyViews.put(vertexKey, Collections.unmodifiableSet(childMap.keySet()));
            parentKeyViews.put(vertexKey, Collections.unmodifiableSet(parentMap.keySet()));
        }
        final V previousValue = values.get(vertexKey);
        if (hadVertex && Objects.equals(previousValue, vertexValue)) {
            return false;
        }
        values.put(vertexKey, vertexValue);
        return true;
    }

    public boolean removeVertex(final K vertexKey) {
        final Map<K, E> vertexChildren = children.get(vertexKey);
        if (vertexChildren == null) {
            return false;
        }
        final Map<K, E> vertexParents = parents.get(vertexKey);
        final List<K> childrenCopy = new ArrayList<>(vertexChildren.keySet());
        final List<K> parentsCopy = new ArrayList<>(vertexParents.keySet());
        for (final K childKey : childrenCopy) {
            parents.get(childKey).remove(vertexKey);
        }
        for (final K parentKey : parentsCopy) {
            children.get(parentKey).remove(vertexKey);
        }
        children.remove(vertexKey);
        parents.remove(vertexKey);
        childKeyViews.remove(vertexKey);
        parentKeyViews.remove(vertexKey);
        values.remove(vertexKey);
        return true;
    }

    public boolean putEdge(final K sourceKey, final K destinationKey, final E edge) {
        throwIfNotContains(destinationKey);
        final Map<K, E> sourceChildren = getConnections(children, sourceKey);
        final E previousEdge = sourceChildren.get(destinationKey);
        if (sourceChildren.containsKey(destinationKey) && Objects.equals(previousEdge, edge)) {
            return false;
        }
        sourceChildren.put(destinationKey, edge);
        parents.get(destinationKey).put(sourceKey, edge);
        return true;
    }

    public boolean removeEdge(final K sourceKey, final K destinationKey) {
        throwIfNotContains(destinationKey);
        final Map<K, E> sourceChildren = getConnections(children, sourceKey);
        if (!sourceChildren.containsKey(destinationKey)) {
            return false;
        }
        sourceChildren.remove(destinationKey);
        parents.get(destinationKey).remove(sourceKey);
        return true;
    }

    // == Helpers. ==

    private void throwIfNotContains(final K vertexKey) {
        if (!children.containsKey(vertexKey)) {
            throw new NoSuchElementException();
        }
    }

    private Map<K, E> getConnections(final Map<K, Map<K, E>> direction, final K vertexKey) {
        final Map<K, E> connections = direction.get(vertexKey);
        if (connections == null) {
            throw new NoSuchElementException();
        }
        return connections;
    }

    private Set<K> getKeyView(final Map<K, Set<K>> direction, final K vertexKey) {
        final Set<K> connections = direction.get(vertexKey);
        if (connections == null) {
            throw new NoSuchElementException();
        }
        return connections;
    }
}
