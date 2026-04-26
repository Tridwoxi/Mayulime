package infra.bench;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Stream;
import think.domain.codec.Serializer;
import think.domain.model.Puzzle;
import think.domain.model.Tile;
import think.manager.Proposal;

// Might be nice to output a PCA instead?
public final class Cluster {

    public record Report(String representativeProposal, boolean meanInCluster) {}

    private Cluster() {}

    public static List<Report> createReports(final Stream<Proposal> proposals) {
        final List<Proposal> list = proposals.toList();
        if (list.isEmpty()) {
            return List.of();
        }

        final Puzzle puzzle = list.getFirst().getPuzzle();
        final List<Tile[]> states = list.stream().map(Proposal::getState).toList();
        final DisjointSets clusters = findClusters(states);

        final TreeMap<Integer, List<Integer>> indicesByCluster = new TreeMap<>();
        for (int index = 0; index < states.size(); index += 1) {
            indicesByCluster
                .computeIfAbsent(clusters.find(index), _ -> new ArrayList<>())
                .add(index);
        }

        return indicesByCluster
            .values()
            .stream()
            .map(indices -> createReport(puzzle, states, indices))
            .toList();
    }

    private static DisjointSets findClusters(final List<Tile[]> states) {
        final DisjointSets clusters = new DisjointSets(states.size());
        for (int left = 0; left < states.size(); left += 1) {
            for (int right = left + 1; right < states.size(); right += 1) {
                if (areConnected(states.get(left), states.get(right))) {
                    clusters.union(left, right);
                }
            }
        }
        return clusters;
    }

    private static boolean areConnected(final Tile[] left, final Tile[] right) {
        int leftOnly = 0;
        int rightOnly = 0;
        for (int index = 0; index < left.length; index += 1) {
            final boolean leftWall = left[index] == Tile.PLAYER_WALL;
            final boolean rightWall = right[index] == Tile.PLAYER_WALL;
            if (leftWall && !rightWall) {
                leftOnly += 1;
            } else if (!leftWall && rightWall) {
                rightOnly += 1;
            }
            if (leftOnly + rightOnly > 2) {
                return false;
            }
        }
        return leftOnly + rightOnly <= 1 || (leftOnly == 1 && rightOnly == 1);
    }

    private static Report createReport(
        final Puzzle puzzle,
        final List<Tile[]> states,
        final List<Integer> indices
    ) {
        final int[] wallCounts = wallCounts(states, indices);
        final int representative = representative(states, indices, wallCounts);
        return new Report(
            Serializer.serialize(puzzle, states.get(representative)),
            meanInCluster(puzzle, states, indices, wallCounts)
        );
    }

    private static int[] wallCounts(final List<Tile[]> states, final List<Integer> indices) {
        final int[] counts = new int[states.get(indices.getFirst()).length];
        for (final int stateIndex : indices) {
            final Tile[] state = states.get(stateIndex);
            for (int cell = 0; cell < state.length; cell += 1) {
                if (state[cell] == Tile.PLAYER_WALL) {
                    counts[cell] += 1;
                }
            }
        }
        return counts;
    }

    private static int representative(
        final List<Tile[]> states,
        final List<Integer> indices,
        final int[] wallCounts
    ) {
        int bestIndex = indices.getFirst();
        long bestDistance = distanceFromMean(states.get(bestIndex), wallCounts, indices.size());
        for (final int stateIndex : indices) {
            final long distance = distanceFromMean(
                states.get(stateIndex),
                wallCounts,
                indices.size()
            );
            if (distance < bestDistance) {
                bestIndex = stateIndex;
                bestDistance = distance;
            }
        }
        return bestIndex;
    }

    private static long distanceFromMean(
        final Tile[] state,
        final int[] wallCounts,
        final int clusterSize
    ) {
        long distance = 0L;
        for (int cell = 0; cell < state.length; cell += 1) {
            final int scaledValue = state[cell] == Tile.PLAYER_WALL ? clusterSize : 0;
            final long delta = scaledValue - wallCounts[cell];
            distance += delta * delta;
        }
        return distance;
    }

    private static boolean meanInCluster(
        final Puzzle puzzle,
        final List<Tile[]> states,
        final List<Integer> indices,
        final int[] wallCounts
    ) {
        final Tile[] meanState = roundedMean(
            states.get(indices.getFirst()),
            wallCounts,
            indices.size()
        );
        if (!puzzle.isValid(meanState)) {
            return false;
        }
        for (final int stateIndex : indices) {
            if (areConnected(meanState, states.get(stateIndex))) {
                return true;
            }
        }
        return false;
    }

    private static Tile[] roundedMean(
        final Tile[] prototype,
        final int[] wallCounts,
        final int clusterSize
    ) {
        final Tile[] state = prototype.clone();
        for (int cell = 0; cell < state.length; cell += 1) {
            if (wallCounts[cell] * 2 >= clusterSize) {
                state[cell] = Tile.PLAYER_WALL;
            } else if (state[cell] == Tile.PLAYER_WALL) {
                state[cell] = Tile.BLANK;
            }
        }
        return state;
    }

    private static final class DisjointSets {

        private final int[] parents;

        DisjointSets(final int size) {
            this.parents = new int[size];
            for (int index = 0; index < size; index += 1) {
                parents[index] = index;
            }
        }

        int find(final int index) {
            if (parents[index] != index) {
                parents[index] = find(parents[index]);
            }
            return parents[index];
        }

        void union(final int left, final int right) {
            final int leftRoot = find(left);
            final int rightRoot = find(right);
            if (leftRoot == rightRoot) {
                return;
            }
            if (leftRoot < rightRoot) {
                parents[rightRoot] = leftRoot;
            } else {
                parents[leftRoot] = rightRoot;
            }
        }
    }
}
