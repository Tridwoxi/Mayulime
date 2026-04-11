#!/usr/bin/env python3
"""Generate random parser-compatible Pathery MapCodes."""

# ruff: noqa: T201 D103 S311

from __future__ import annotations

import argparse
import random
import re
import sys
from collections import deque
from dataclasses import dataclass

from mapcode import BLANK, SYSTEM_WALL, WAYPOINT, PuzzleState, encode_mapcode

MAX_NAME_LENGTH = 100
MIN_WAYPOINTS = 2

type Position = tuple[int, int]


class CantGenerateError(Exception):
    """Map generation exceeds the allowed retries."""


class InvalidInputError(Exception):
    """User input violates generator constraints."""


@dataclass(frozen=True)
class GeneratorConfig:
    """Parameters for random puzzle generation."""

    height: int
    width: int
    player_walls: int
    system_wall_probability: float
    waypoints: int
    name: str
    max_attempts: int
    seed: int | None


def clean_generator_name(name: str) -> str:
    """Sanitise a generator name, replacing delimiters with underscores."""
    cleaned = re.sub(r"\s+", " ", name.strip())
    cleaned = cleaned.replace(".", "_").replace(":", "_")
    if len(cleaned) > MAX_NAME_LENGTH:
        return cleaned[:MAX_NAME_LENGTH] + "..."
    return cleaned


def position_to_index(position: Position, width: int) -> int:
    row, col = position
    return row * width + col


def reachable_from_start(
    height: int,
    width: int,
    wall_indexes: set[int],
    start: Position,
) -> set[Position]:
    if position_to_index(start, width) in wall_indexes:
        return set()

    queue: deque[Position] = deque([start])
    visited: set[Position] = {start}
    while queue:
        row, col = queue.popleft()
        for drow, dcol in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
            nrow = row + drow
            ncol = col + dcol
            if not (0 <= nrow < height and 0 <= ncol < width):
                continue
            neighbor = (nrow, ncol)
            if position_to_index(neighbor, width) in wall_indexes:
                continue
            if neighbor in visited:
                continue
            visited.add(neighbor)
            queue.append(neighbor)
    return visited


def all_waypoints_reachable(
    height: int,
    width: int,
    wall_indexes: set[int],
    waypoint_positions: list[Position],
) -> bool:
    reachable = reachable_from_start(
        height=height,
        width=width,
        wall_indexes=wall_indexes,
        start=waypoint_positions[0],
    )
    return all(position in reachable for position in waypoint_positions)


def generate_problem(
    config: GeneratorConfig,
    rng: random.Random,
) -> tuple[list[int], set[int]]:
    num_cells = config.height * config.width
    cell_indexes = list(range(num_cells))

    for _ in range(config.max_attempts):
        waypoint_indexes = rng.sample(cell_indexes, config.waypoints)
        waypoint_set = set(waypoint_indexes)
        wall_indexes = {
            index
            for index in cell_indexes
            if index not in waypoint_set
            and rng.random() < config.system_wall_probability
        }
        waypoint_positions = [
            (index // config.width, index % config.width) for index in waypoint_indexes
        ]
        if all_waypoints_reachable(
            height=config.height,
            width=config.width,
            wall_indexes=wall_indexes,
            waypoint_positions=waypoint_positions,
        ):
            return waypoint_indexes, wall_indexes
    raise CantGenerateError


def build_puzzle_state(
    config: GeneratorConfig,
    waypoint_indexes: list[int],
    wall_indexes: set[int],
) -> PuzzleState:
    """Convert generator output into a PuzzleState for encoding."""
    num_cells = config.height * config.width
    state = [BLANK] * num_cells
    for index in wall_indexes:
        state[index] = SYSTEM_WALL
    for index in waypoint_indexes:
        state[index] = WAYPOINT
    return PuzzleState(
        name=clean_generator_name(config.name),
        rows=config.height,
        cols=config.width,
        blocking_budget=config.player_walls,
        state=state,
        waypoints=waypoint_indexes,
    )


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Generate a random Pathery MapCode.",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )
    _ = parser.add_argument(
        "height",
        type=int,
        help="Number of rows.",
    )
    _ = parser.add_argument(
        "width",
        type=int,
        help="Number of columns.",
    )
    _ = parser.add_argument(
        "player_walls",
        type=int,
        help="Blocking budget for the player.",
    )
    _ = parser.add_argument(
        "--system-wall-probability",
        type=float,
        default=0.1,
        help="Probability each non-waypoint cell is a wall.",
    )
    _ = parser.add_argument(
        "--waypoints",
        type=int,
        default=3,
        help="Total waypoint count (including start and finish).",
    )
    _ = parser.add_argument(
        "--name",
        type=str,
        default="Generated",
        help="Puzzle name embedded in the mapcode.",
    )
    _ = parser.add_argument(
        "--max-attempts",
        type=int,
        default=100,
        help="Retry limit for generating a connected board.",
    )
    _ = parser.add_argument(
        "--seed",
        type=int,
        default=None,
        help="RNG seed for reproducibility.",
    )
    return parser


def validate_config(config: GeneratorConfig) -> None:
    if config.height <= 0 or config.width <= 0:
        msg = "height and width must be positive integers."
        raise InvalidInputError(msg)
    if config.max_attempts <= 0:
        msg = "max-attempts must be a positive integer."
        raise InvalidInputError(msg)
    if config.player_walls < 0:
        msg = "player-walls must be zero or greater."
        raise InvalidInputError(msg)
    if not 0.0 <= config.system_wall_probability <= 1.0:
        msg = "system-wall-probability must be between 0 and 1."
        raise InvalidInputError(msg)

    total_cells = config.height * config.width
    if not MIN_WAYPOINTS <= config.waypoints < total_cells:
        msg = "waypoints must be at least 2 and less than height*width."
        raise InvalidInputError(msg)


def parse_config(argv: list[str] | None = None) -> GeneratorConfig:
    args = build_parser().parse_args(argv)
    config = GeneratorConfig(
        height=args.height,  # pyright: ignore[reportAny]
        width=args.width,  # pyright: ignore[reportAny]
        player_walls=args.player_walls,  # pyright: ignore[reportAny]
        system_wall_probability=args.system_wall_probability,  # pyright: ignore[reportAny]
        waypoints=args.waypoints,  # pyright: ignore[reportAny]
        name=args.name,  # pyright: ignore[reportAny]
        max_attempts=args.max_attempts,  # pyright: ignore[reportAny]
        seed=args.seed,  # pyright: ignore[reportAny]
    )
    validate_config(config)
    return config


def main() -> int:
    try:
        config = parse_config()
        rng = random.Random(config.seed)
        waypoint_indexes, wall_indexes = generate_problem(
            config=config,
            rng=rng,
        )
        state = build_puzzle_state(config, waypoint_indexes, wall_indexes)
        mapcode = encode_mapcode(state)
    except CantGenerateError:
        print(
            "Failed to generate a connected problem within max-attempts.",
            file=sys.stderr,
        )
        return 1
    except InvalidInputError as exception:
        print(f"Invalid input: {exception}", file=sys.stderr)
        return 1

    print(mapcode, file=sys.stdout)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
