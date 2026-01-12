"""Generate a random Pathery problem that satisfies the parser.

The problem is solveable if the player does not place any rubbers. It may not be
solveable if the player is forced to place rubbers.
"""

from __future__ import annotations

import argparse
import random
import sys
from collections import deque
from itertools import pairwise

# ruff: noqa: T201 INP001 S311

__all__: list[str] = []

Cell = str
Grid = list[list[Cell]]
Position = tuple[int, int]

EMPTY: Cell = "."
BRICK: Cell = "#"


class CantGenerateError(Exception): ...


def is_connected(grid: Grid, start: Position, goal: Position) -> bool:  # Standard BFS.
    height = len(grid)
    width = len(grid[0])
    queue: deque[Position] = deque([start])
    visited: set[Position] = {start}
    while queue:
        row, col = queue.popleft()
        if (row, col) == goal:
            return True
        for drow, dcol in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
            nrow = row + drow
            ncol = col + dcol
            if (
                0 <= nrow < height
                and 0 <= ncol < width
                and grid[nrow][ncol] != BRICK
                and (nrow, ncol) not in visited
            ):
                visited.add((nrow, ncol))
                queue.append((nrow, ncol))
    return False


def all_connected(grid: Grid, checkpoints: list[Position]) -> bool:
    return all(is_connected(grid, a, b) for a, b in pairwise(checkpoints))


def generate_problem(
    height: int,
    width: int,
    brick_probability: float,
    checkpoint_count: int,
    max_attempts: int,
) -> Grid:
    def pick_cell(row: int, col: int) -> str:
        return str(checkpoint_map.get((row, col), "")) or (
            BRICK if random.random() < brick_probability else EMPTY
        )

    if any(x <= 0 for x in (height, width, max_attempts)):
        raise ValueError
    if checkpoint_count not in range(2, height * width):
        raise ValueError
    for _ in range(max_attempts):
        checkpoints = random.sample(range(height * width), checkpoint_count)
        checkpoints = [(i // width, i % width) for i in checkpoints]
        checkpoint_map = {v: i for i, v in enumerate(checkpoints)}
        grid = [[pick_cell(row, col) for col in range(width)] for row in range(height)]
        if all_connected(grid, checkpoints):
            return grid
    raise CantGenerateError


def format_problem(
    grid: Grid,
    rubber_count: int,
) -> str:
    section_delim = ";;;"
    line_delim = ";;"
    cell_delim = ";"
    return section_delim.join(
        (
            str(rubber_count),
            line_delim.join(cell_delim.join(line) for line in grid),
        ),
    )


def main() -> int:
    parser = argparse.ArgumentParser(
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )
    _ = parser.add_argument("--height", type=int, default=7)
    _ = parser.add_argument("--width", type=int, default=13)
    _ = parser.add_argument("--brick-probability", type=float, default=0.1)
    _ = parser.add_argument("--checkpoints", type=int, default=3)
    _ = parser.add_argument("--rubbers", type=int, default=7)
    _ = parser.add_argument("--max-attempts", type=int, default=100)
    args = parser.parse_args()

    try:
        grid = generate_problem(
            height=args.height,  # pyright: ignore[reportAny]
            width=args.width,  # pyright: ignore[reportAny]
            brick_probability=args.brick_probability,  # pyright: ignore[reportAny]
            checkpoint_count=args.checkpoints,  # pyright: ignore[reportAny]
            max_attempts=args.max_attempts,  # pyright: ignore[reportAny]
        )
    except CantGenerateError:
        print("Failed to generate problem.", file=sys.stderr)
        return 1
    except ValueError:
        print("Invalid input.", file=sys.stderr)
        return 1
    print(
        format_problem(grid, args.rubbers),  # pyright: ignore[reportAny]
        file=sys.stdout,
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
