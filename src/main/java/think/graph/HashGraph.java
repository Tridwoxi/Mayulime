package think.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

/**
    HashMap-backed implementation of a graph. All operations have theoretically optimal time
    complexity in exchange for poor constant factor performance.
 */
public final class HashGraph<V, E> implements Graph<V, E> {

    /*
       Let the first `V` in the following signatures be `V1` and the second `V` be `V2`. Outgoing
       stores `V1 -> V2`, so answers "where can I get from V1?". Incoming stores `V1 <- V2`, so
       answers "what vertices come into V1?"
    */
    private final HashMap<V, HashMap<V, E>> outgoing;
    private final HashMap<V, HashMap<V, E>> incoming;

    public HashGraph(final int expectedSize) {
        this.outgoing = HashMap.newHashMap(expectedSize);
        this.incoming = HashMap.newHashMap(expectedSize);
    }

    private HashGraph(
        final HashMap<V, HashMap<V, E>> outgoing,
        final HashMap<V, HashMap<V, E>> incoming
    ) {
        this.outgoing = outgoing;
        this.incoming = incoming;
    }

    @Override
    public boolean addVertex(final V vertex) {
        if (outgoing.containsKey(vertex)) {
            return false;
        }
        outgoing.put(vertex, new HashMap<>());
        incoming.put(vertex, new HashMap<>());
        return true;
    }

    @Override
    public boolean removeVertex(final V vertex) {
        final ArrayList<V> outgoingCopy = new ArrayList<>(outgoing.get(vertex).keySet());
        final ArrayList<V> incomingCopy = new ArrayList<>(incoming.get(vertex).keySet());
        outgoingCopy.forEach(other -> incoming.get(other).remove(vertex));
        incomingCopy.forEach(other -> outgoing.get(other).remove(vertex));
        outgoing.remove(vertex);
        incoming.remove(vertex);
        return false;
    }

    @Override
    public boolean containsVertex(final V vertex) {
        return outgoing.containsKey(vertex);
    }

    @Override
    public boolean setEdge(final V source, final V destination, final E edge) {
        final E previous = outgoing.get(source).get(destination);
        if (previous != null && previous.equals(edge)) {
            return false;
        }
        outgoing.get(source).put(destination, edge);
        incoming.get(destination).put(source, edge);
        return true;
    }

    @Override
    public Optional<E> getEdge(final V source, final V destination) {
        if (!outgoing.get(source).containsKey(destination)) {
            return Optional.empty();
        }
        return Optional.of(outgoing.get(source).get(destination));
    }

    @Override
    public boolean removeEdge(final V source, final V destination) {
        if (!outgoing.get(source).containsKey(destination)) {
            return false;
        }
        outgoing.get(source).remove(destination);
        incoming.get(destination).remove(source);
        return true;
    }

    @Override
    public ArrayList<V> getAllVertices() {
        return new ArrayList<>(outgoing.keySet());
    }

    @Override
    public ArrayList<V> getOutgoingNeighbors(final V vertex) {
        return new ArrayList<>(outgoing.get(vertex).keySet());
    }

    @Override
    public ArrayList<V> getIncomingNeighbors(final V vertex) {
        return new ArrayList<>(incoming.get(vertex).keySet());
    }

    @Override
    public Graph<V, E> copy() {
        final Function<HashMap<V, HashMap<V, E>>, HashMap<V, HashMap<V, E>>> copier = outer -> {
            final HashMap<V, HashMap<V, E>> outerCopy = HashMap.newHashMap(outer.size());
            outer.forEach((key, inner) -> {
                final HashMap<V, E> innerCopy = new HashMap<>(inner);
                outerCopy.put(key, innerCopy);
            });
            return outerCopy;
        };
        final HashMap<V, HashMap<V, E>> outgoingCopy = copier.apply(outgoing);
        final HashMap<V, HashMap<V, E>> incomingCopy = copier.apply(incoming);
        return new HashGraph<>(outgoingCopy, incomingCopy);
    }
}
