"""Puzzle model, constants, and shared utilities."""

# ruff: noqa: D101 D103

from __future__ import annotations

import re
from dataclasses import dataclass

type Position = tuple[int, int]

BLANK = "blank"
SYSTEM_WALL = "system_wall"
PLAYER_WALL = "player_wall"
CHECKPOINT = "checkpoint"

CURSE_MAX_CHECKPOINT_LABEL = "N"
CURSE_CELL_STRIDE = 3
CURSE_CELL_OFFSET = 1
MAPCODE_MYSTERY_METADATA = "..."
MAPCODE_UNNAMED_PUZZLE = "Unnamed Puzzle"
VALID_CURSE_CHECKPOINTS = "ABCDEFGHIJKLMN"
VALID_CURSE_TOKENS = set("#@SX." + VALID_CURSE_CHECKPOINTS)


class ConversionError(Exception):
    """A file cannot be converted safely."""


@dataclass(frozen=True)
class PuzzleState:
    name: str
    rows: int
    cols: int
    blocking_budget: int
    features: list[str]
    checkpoints: list[int]


def clean_name(name: str) -> str:
    cleaned = re.sub(r"\s+", " ", name.strip())
    cleaned = cleaned.replace(".", " ").replace(":", " ")
    return cleaned or MAPCODE_UNNAMED_PUZZLE
