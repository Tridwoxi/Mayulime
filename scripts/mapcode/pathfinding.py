"""BFS shortest-path computation and puzzle scoring."""

from __future__ import annotations

from collections import deque

from .model import PLAYER_WALL, SYSTEM_WALL, PuzzleState


def shortest_path_distance(
    features: list[str],
    rows: int,
    cols: int,
    start: int,
    finish: int,
) -> int:
    """Return the BFS shortest-path distance, or -1 if unreachable."""
    if features[start] in (SYSTEM_WALL, PLAYER_WALL) or features[finish] in (
        SYSTEM_WALL,
        PLAYER_WALL,
    ):
        return -1

    queue: deque[int] = deque([start])
    distances = [-1] * len(features)
    distances[start] = 0

    while queue:
        current = queue.popleft()
        current_row, current_col = divmod(current, cols)
        current_distance = distances[current]
        neighbors = (
            (current_row > 0, current - cols),
            (current_col < cols - 1, current + 1),
            (current_row < rows - 1, current + cols),
            (current_col > 0, current - 1),
        )
        for allowed, neighbor in neighbors:
            if not allowed:
                continue
            if (
                features[neighbor] in (SYSTEM_WALL, PLAYER_WALL)
                or distances[neighbor] >= 0
            ):
                continue
            next_distance = current_distance + 1
            if neighbor == finish:
                return next_distance
            distances[neighbor] = next_distance
            queue.append(neighbor)
    return -1


def evaluate_score(state: PuzzleState) -> int:
    """Sum BFS distances across checkpoint segments. Returns -1 if blocked."""
    score = 0
    for start, finish in zip(state.checkpoints, state.checkpoints[1:], strict=False):
        segment_distance = shortest_path_distance(
            state.features,
            state.rows,
            state.cols,
            start,
            finish,
        )
        if segment_distance < 0:
            return -1
        score += segment_distance
    return score
