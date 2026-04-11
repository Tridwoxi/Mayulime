#!/usr/bin/env python3
"""Score a mapcode, curse, or cursep file by computing shortest-path length."""

# ruff: noqa: T201 D103

from __future__ import annotations

import argparse
import sys
from pathlib import Path

from mapcode import PLAYER_WALL, ConversionError, evaluate_score, read_state


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Evaluate the shortest-path score of a puzzle file.",
    )
    _ = parser.add_argument(
        "input",
        help="Puzzle file to score (.mapcode, .curse, or .cursep).",
    )
    return parser


def main(argv: list[str] | None = None) -> int:
    args = build_parser().parse_args(argv)
    input_path: str = args.input  # pyright: ignore[reportAny]

    path = Path(input_path)
    if not path.exists():
        print(f"error: {path} not found", file=sys.stderr)
        return 1

    try:
        state = read_state(input_path)
    except ConversionError as error:
        print(f"error: {error}", file=sys.stderr)
        return 1
    except OSError as error:
        print(f"error: {error}", file=sys.stderr)
        return 1

    score = evaluate_score(state)

    walls = sum(1 for tile in state.state if tile == PLAYER_WALL)

    print(f"puzzle : {state.name}")
    print(f"size   : {state.cols}x{state.rows}")
    print(f"budget : {state.blocking_budget}")
    print(f"walls  : {walls} placed")
    print(f"score  : {score}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
