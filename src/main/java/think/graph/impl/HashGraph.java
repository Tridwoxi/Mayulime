package think.graph.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import think.graph.Graph.MutableGraph;

/**
    HashMap-backed implementation of a graph. All operations have theoretically optimal time
    complexity in exchange for poor constant factor performance.
 */
public final class HashGraph<V, E> implements MutableGraph<V, E> {

    private final HashMap<V, HashMap<V, E>> children;
    private final HashMap<V, HashMap<V, E>> parents;
    private final HashMap<V, HashMap<V, E>> either; // Read-only view.

    public HashGraph(final int expectedSize) {
        this.children = HashMap.newHashMap(expectedSize);
        this.parents = HashMap.newHashMap(expectedSize);
        this.either = children;
    }

    private HashGraph(
        final HashMap<V, HashMap<V, E>> children,
        final HashMap<V, HashMap<V, E>> parents
    ) {
        this.children = children;
        this.parents = parents;
        this.either = children;
    }

    @Override
    public boolean containsVertex(final V vertex) {
        return either.containsKey(vertex);
    }

    @Override
    public Optional<E> getEdge(final V source, final V destination) {
        throwIfNotContains(source);
        throwIfNotContains(destination);
        return children.get(source).containsKey(destination)
            ? Optional.of(children.get(source).get(destination))
            : Optional.empty();
    }

    @Override
    public ArrayList<V> getChildren(final V vertex) {
        throwIfNotContains(vertex);
        return new ArrayList<>(children.get(vertex).keySet());
    }

    @Override
    public ArrayList<V> getParents(final V vertex) {
        throwIfNotContains(vertex);
        return new ArrayList<>(parents.get(vertex).keySet());
    }

    @Override
    public ArrayList<V> getAllVertices() {
        return new ArrayList<>(either.keySet());
    }

    @Override
    public HashGraph<V, E> shallowCopy() {
        final Function<HashMap<V, HashMap<V, E>>, HashMap<V, HashMap<V, E>>> copier = outer -> {
            final HashMap<V, HashMap<V, E>> outerCopy = HashMap.newHashMap(outer.size());
            outer.forEach((key, inner) -> {
                final HashMap<V, E> innerCopy = new HashMap<>(inner);
                outerCopy.put(key, innerCopy);
            });
            return outerCopy;
        };
        final HashMap<V, HashMap<V, E>> childrenCopy = copier.apply(children);
        final HashMap<V, HashMap<V, E>> parentsCopy = copier.apply(parents);
        return new HashGraph<>(childrenCopy, parentsCopy);
    }

    @Override
    public boolean addVertex(final V vertex) {
        if (containsVertex(vertex)) {
            return false;
        }
        children.put(vertex, new HashMap<>());
        parents.put(vertex, new HashMap<>());
        return true;
    }

    @Override
    public boolean removeVertex(final V vertex) {
        if (!containsVertex(vertex)) {
            return false;
        }
        final ArrayList<V> childrenCopy = new ArrayList<>(children.get(vertex).keySet());
        final ArrayList<V> parentsCopy = new ArrayList<>(parents.get(vertex).keySet());
        childrenCopy.forEach(other -> parents.get(other).remove(vertex));
        parentsCopy.forEach(other -> children.get(other).remove(vertex));
        children.remove(vertex);
        parents.remove(vertex);
        return true;
    }

    @Override
    public boolean setEdge(final V source, final V destination, final E edge) {
        throwIfNotContains(source);
        throwIfNotContains(destination);
        final E previous = children.get(source).get(destination);
        if (children.get(source).containsKey(destination) && edge.equals(previous)) {
            return false;
        }
        children.get(source).put(destination, edge);
        parents.get(destination).put(source, edge);
        return true;
    }

    @Override
    public boolean removeEdge(final V source, final V destination) {
        throwIfNotContains(source);
        throwIfNotContains(destination);
        if (!children.get(source).containsKey(destination)) {
            return false;
        }
        children.get(source).remove(destination);
        parents.get(destination).remove(source);
        return true;
    }

    private void throwIfNotContains(final V vertex) {
        if (!containsVertex(vertex)) {
            throw new NoSuchElementException();
        }
    }
}
