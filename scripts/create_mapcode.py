#!/usr/bin/env python3
"""Generate random parser-compatible Pathery MapCodes."""

from __future__ import annotations

import argparse
import random
import re
import sys
from collections import deque
from dataclasses import dataclass

# basedpyright: strict
# ruff: noqa: D103 T201 S311 D101 ERA001

type Position = tuple[int, int]

MAX_NAME_LENGTH = 100
MIN_CHECKPOINTS = 2


class CantGenerateError(Exception):
    """Map generation exceeds the allowed retries."""


class InvalidInputError(Exception):
    """User input violates generator constraints."""


@dataclass(frozen=True)
class GeneratorConfig:
    height: int
    width: int
    system_wall_probability: float
    checkpoints: int
    player_walls: int
    name: str
    max_attempts: int
    seed: int | None


def clean_name(name: str) -> str:
    cleaned = re.sub(r"\s+", " ", name.strip())
    cleaned = cleaned.replace(".", "_").replace(":", "_")
    if len(cleaned) > MAX_NAME_LENGTH:
        return cleaned[:MAX_NAME_LENGTH] + "..."
    return cleaned


def position_to_index(position: Position, width: int) -> int:
    row, col = position
    return row * width + col


def index_to_position(index: int, width: int) -> Position:
    return (index // width, index % width)


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
            neighbor_index = position_to_index(neighbor, width)
            if neighbor_index in wall_indexes:
                continue
            if neighbor in visited:
                continue
            visited.add(neighbor)
            queue.append(neighbor)
    return visited


def all_checkpoints_reachable_from_start(
    height: int,
    width: int,
    wall_indexes: set[int],
    checkpoint_positions: list[Position],
) -> bool:
    reachable = reachable_from_start(
        height=height,
        width=width,
        wall_indexes=wall_indexes,
        start=checkpoint_positions[0],
    )
    return all(position in reachable for position in checkpoint_positions)


def build_feature_map(
    checkpoint_indexes: list[int],
    wall_indexes: set[int],
) -> dict[int, str]:
    feature_map: dict[int, str] = {}

    for index in wall_indexes:
        feature_map[index] = "r1"

    feature_map[checkpoint_indexes[0]] = "s1"
    feature_map[checkpoint_indexes[-1]] = "f1"

    for checkpoint_order, checkpoint_index in enumerate(
        checkpoint_indexes[1:-1],
        start=1,
    ):
        feature_map[checkpoint_index] = f"c{checkpoint_order}"

    return feature_map


def encode_board(feature_map: dict[int, str], num_cells: int) -> str:
    ordered_indexes = sorted(feature_map.keys())
    tokens: list[str] = []
    traversing_index = 0

    for feature_index in ordered_indexes:
        if not (0 <= feature_index < num_cells):
            message = "Feature index out of board bounds."
            raise ValueError(message)
        skips = feature_index - traversing_index
        skip_prefix = "" if skips == 0 else str(skips)
        tokens.append(f"{skip_prefix},{feature_map[feature_index]}")
        traversing_index = feature_index + 1

    return ".".join(tokens) + "."


def encode_mapcode(
    config: GeneratorConfig,
    checkpoint_indexes: list[int],
    wall_indexes: set[int],
) -> str:
    metadata = (
        f"{config.width}.{config.height}."
        f"{config.player_walls}.{clean_name(config.name)}..."
    )
    feature_map = build_feature_map(checkpoint_indexes, wall_indexes)
    board = encode_board(feature_map, config.height * config.width)
    return f"{metadata}:{board}"


def generate_problem(
    config: GeneratorConfig,
    rng: random.Random,
) -> tuple[list[int], set[int]]:
    num_cells = config.height * config.width
    cell_indexes = list(range(num_cells))

    for _ in range(config.max_attempts):
        checkpoint_indexes = rng.sample(cell_indexes, config.checkpoints)
        checkpoint_set = set(checkpoint_indexes)
        wall_indexes = {
            index
            for index in cell_indexes
            if index not in checkpoint_set
            and rng.random() < config.system_wall_probability
        }
        checkpoint_positions = [
            index_to_position(index, config.width) for index in checkpoint_indexes
        ]
        if all_checkpoints_reachable_from_start(
            height=config.height,
            width=config.width,
            wall_indexes=wall_indexes,
            checkpoint_positions=checkpoint_positions,
        ):
            return checkpoint_indexes, wall_indexes
    raise CantGenerateError


def parse_config(argv: list[str] | None = None) -> GeneratorConfig:
    args = build_parser().parse_args(argv)
    config = GeneratorConfig(
        height=args.height,  # pyright: ignore[reportAny]
        width=args.width,  # pyright: ignore[reportAny]
        system_wall_probability=args.system_wall_probability,  # pyright: ignore[reportAny]
        checkpoints=args.checkpoints,  # pyright: ignore[reportAny]
        player_walls=args.player_walls,  # pyright: ignore[reportAny]
        name=args.name,  # pyright: ignore[reportAny]
        max_attempts=args.max_attempts,  # pyright: ignore[reportAny]
        seed=args.seed,  # pyright: ignore[reportAny]
    )
    validate_config(config)
    return config


def validate_config(config: GeneratorConfig) -> None:
    if config.height <= 0 or config.width <= 0 or config.max_attempts <= 0:
        message = "height, width, and max-attempts must be positive integers."
        raise InvalidInputError(message)
    if config.player_walls < 0:
        message = "player-walls must be zero or greater."
        raise InvalidInputError(message)
    if not 0.0 <= config.system_wall_probability <= 1.0:
        message = "system-wall-probability must be between 0 and 1 inclusive."
        raise InvalidInputError(message)

    total_cells = config.height * config.width
    if not MIN_CHECKPOINTS <= config.checkpoints < total_cells:
        message = "checkpoints must be at least 2 and less than height*width."
        raise InvalidInputError(message)


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )
    _ = parser.add_argument("--height", type=int, default=7)
    _ = parser.add_argument("--width", type=int, default=13)
    _ = parser.add_argument("--system-wall-probability", type=float, default=0.1)
    _ = parser.add_argument("--checkpoints", type=int, default=3)
    _ = parser.add_argument("--player-walls", type=int, default=7)
    _ = parser.add_argument("--name", type=str, default="Generated")
    _ = parser.add_argument("--max-attempts", type=int, default=100)
    _ = parser.add_argument("--seed", type=int, default=None)
    return parser


def main() -> int:
    try:
        config = parse_config()
        rng = random.Random(config.seed)
        checkpoint_indexes, wall_indexes = generate_problem(config=config, rng=rng)
        mapcode = encode_mapcode(
            config=config,
            checkpoint_indexes=checkpoint_indexes,
            wall_indexes=wall_indexes,
        )
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
    sys.exit(main())
