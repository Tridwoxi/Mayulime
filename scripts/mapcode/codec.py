"""Parse and encode mapcode, curse, and cursep formats."""

# ruff: noqa: TRY003 EM101 EM102 C901 PLR0912 PLR0915 PLR0913 PLR2004 S105 ISC003

from __future__ import annotations

from pathlib import Path

from .model import (
    BLANK,
    CHECKPOINT,
    CURSE_CELL_OFFSET,
    CURSE_CELL_STRIDE,
    CURSE_MAX_CHECKPOINT_LABEL,
    MAPCODE_MYSTERY_METADATA,
    PLAYER_WALL,
    SYSTEM_WALL,
    VALID_CURSE_CHECKPOINTS,
    VALID_CURSE_TOKENS,
    ConversionError,
    PuzzleState,
    clean_name,
)


def parse_mapcode(raw_text: str, fallback_name: str) -> PuzzleState:
    """Decode a mapcode string into a PuzzleState."""
    stripped = raw_text.strip()
    regions = stripped.split(":")
    if len(regions) != 2:
        raise ConversionError("Mapcode must contain exactly one ':' separator.")

    metadata = regions[0].split(".")
    if len(metadata) != 7:
        raise ConversionError("Mapcode metadata must contain 7 '.'-delimited fields.")

    try:
        cols = int(metadata[0])
        rows = int(metadata[1])
        blocking_budget = int(metadata[2])
    except ValueError as error:
        raise ConversionError(
            "Mapcode dimensions and budget must be integers.",
        ) from error

    if rows <= 0 or cols <= 0 or blocking_budget < 0:
        raise ConversionError(
            "Mapcode rows/cols must be positive and budget must be non-negative.",
        )

    features = [BLANK] * (rows * cols)
    consumed_budget = 0
    checkpoints_by_order: dict[int, int] = {}
    start_index: int | None = None
    finish_index: int | None = None

    traversing_index = 0
    for raw_token in regions[1].split(".")[:-1]:
        if "," not in raw_token:
            raise ConversionError(f"Malformed mapcode token: {raw_token!r}")
        skip_text, kind_order = raw_token.split(",", 1)
        try:
            skip = int(skip_text) if skip_text else 0
        except ValueError as error:
            raise ConversionError(f"Invalid skip value: {skip_text!r}") from error
        if skip < 0:
            raise ConversionError("Mapcode skips cannot be negative.")

        feature_index = traversing_index + skip
        if feature_index >= len(features):
            raise ConversionError("Mapcode feature index is out of bounds.")

        kind = kind_order[:1]
        order_text = kind_order[1:]
        try:
            order = int(order_text)
        except ValueError as error:
            raise ConversionError(f"Invalid token order: {kind_order!r}") from error

        if kind == "r":
            if order in (1, 3):
                features[feature_index] = SYSTEM_WALL
            elif order == 2:
                features[feature_index] = PLAYER_WALL
                consumed_budget += 1
            else:
                raise ConversionError(f"Unsupported wall order: r{order}.")
        elif kind == "s":
            if order != 1 or start_index is not None:
                raise ConversionError("Curse conversion supports exactly one start.")
            start_index = feature_index
            features[feature_index] = CHECKPOINT
        elif kind == "f":
            if order != 1 or finish_index is not None:
                raise ConversionError("Curse conversion supports exactly one finish.")
            finish_index = feature_index
            features[feature_index] = CHECKPOINT
        elif kind == "c":
            if order <= 0 or order in checkpoints_by_order:
                raise ConversionError(
                    "Checkpoint orders must be unique positive integers.",
                )
            checkpoints_by_order[order] = feature_index
            features[feature_index] = CHECKPOINT
        else:
            raise ConversionError(f"Unsupported mapcode token kind: {kind!r}")

        traversing_index = feature_index + 1

    if start_index is None or finish_index is None:
        raise ConversionError("Mapcode must contain exactly one start and one finish.")

    ordered_checkpoint_indexes = [start_index]
    for order in range(1, len(checkpoints_by_order) + 1):
        if order not in checkpoints_by_order:
            raise ConversionError("Checkpoint orders must be contiguous from c1.")
        ordered_checkpoint_indexes.append(checkpoints_by_order[order])
    ordered_checkpoint_indexes.append(finish_index)

    remaining_budget = blocking_budget - consumed_budget
    if remaining_budget < 0:
        raise ConversionError("Mapcode contains more player walls than budget allows.")

    puzzle_name = clean_name(metadata[3] or fallback_name)
    return PuzzleState(
        name=puzzle_name,
        rows=rows,
        cols=cols,
        blocking_budget=remaining_budget + consumed_budget,
        features=features,
        checkpoints=ordered_checkpoint_indexes,
    )


def parse_curse(
    raw_text: str,
    fallback_name: str,
    blank_char: str,
    rows_override: int | None,
    cols_override: int | None,
    budget_override: int | None,
    name_override: str | None,
) -> PuzzleState:
    """Decode a curse or cursep string into a PuzzleState."""
    lines = raw_text.splitlines()
    if not lines:
        raise ConversionError("Curse input must contain at least one row.")

    rows = rows_override if rows_override is not None else len(lines)
    if rows != len(lines):
        raise ConversionError(
            f"Expected {rows} rows but found {len(lines)} rows in the curse file.",
        )

    inferred_cols = max((len(line) + 1) // CURSE_CELL_STRIDE for line in lines)
    cols = cols_override if cols_override is not None else inferred_cols
    if cols <= 0:
        raise ConversionError("Curse input must have at least one column.")

    features = [BLANK] * (rows * cols)
    start_index: int | None = None
    finish_index: int | None = None
    checkpoint_indexes: dict[int, int] = {}
    player_wall_count = 0

    for row, line in enumerate(lines):
        for char_index, char in enumerate(line):
            if char == "\r":
                continue
            if char == blank_char:
                continue
            if char == " " and blank_char == ".":
                raise ConversionError(
                    "Cursep input may not use spaces for blank cells.",
                )
            if char not in VALID_CURSE_TOKENS:
                raise ConversionError(
                    f"Unsupported curse token {char!r} at row {row + 1}.",
                )
            if char_index % CURSE_CELL_STRIDE != CURSE_CELL_OFFSET:
                raise ConversionError(
                    f"Token {char!r} at row {row + 1},"
                    + f" col {char_index + 1} is off-grid.",
                )

            col = char_index // CURSE_CELL_STRIDE
            if col >= cols:
                raise ConversionError(
                    "Curse token lies outside the configured column count.",
                )

            feature_index = row * cols + col
            if char == "#":
                features[feature_index] = SYSTEM_WALL
            elif char == "@":
                features[feature_index] = PLAYER_WALL
                player_wall_count += 1
            elif char == "S":
                if start_index is not None:
                    raise ConversionError("Curse input may contain only one S.")
                start_index = feature_index
                features[feature_index] = CHECKPOINT
            elif char == "X":
                if finish_index is not None:
                    raise ConversionError("Curse input may contain only one X.")
                finish_index = feature_index
                features[feature_index] = CHECKPOINT
            elif char in VALID_CURSE_CHECKPOINTS:
                checkpoint_order = ord(char) - ord("A") + 1
                if checkpoint_order in checkpoint_indexes:
                    raise ConversionError(f"Duplicate checkpoint label {char}.")
                checkpoint_indexes[checkpoint_order] = feature_index
                features[feature_index] = CHECKPOINT
            else:
                raise AssertionError(char)

    if start_index is None or finish_index is None:
        raise ConversionError("Curse input must contain one S and one X.")

    for checkpoint_order in range(1, len(checkpoint_indexes) + 1):
        if checkpoint_order not in checkpoint_indexes:
            raise ConversionError("Curse checkpoints must be contiguous from A.")

    puzzle_name = clean_name(name_override or fallback_name)
    blocking_budget = (
        budget_override if budget_override is not None else player_wall_count
    )
    checkpoints = [start_index]
    checkpoints.extend(
        checkpoint_indexes[index] for index in range(1, len(checkpoint_indexes) + 1)
    )
    checkpoints.append(finish_index)
    return PuzzleState(
        name=puzzle_name,
        rows=rows,
        cols=cols,
        blocking_budget=blocking_budget,
        features=features,
        checkpoints=checkpoints,
    )


def _checkpoint_token(index: int, checkpoints: list[int]) -> str:
    checkpoint_position = checkpoints.index(index)
    if checkpoint_position == 0:
        return "s1"
    if checkpoint_position == len(checkpoints) - 1:
        return "f1"
    return f"c{checkpoint_position}"


def encode_mapcode(state: PuzzleState) -> str:
    """Encode a PuzzleState into a mapcode string."""
    metadata = (
        f"{state.cols}.{state.rows}.{state.blocking_budget}."
        f"{clean_name(state.name)}{MAPCODE_MYSTERY_METADATA}"
    )
    tokens: list[str] = []
    traversing_index = 0
    for index, feature in enumerate(state.features):
        token: str | None
        if feature == BLANK:
            token = None
        elif feature == SYSTEM_WALL:
            token = "r1"
        elif feature == PLAYER_WALL:
            token = "r2"
        elif feature == CHECKPOINT:
            token = _checkpoint_token(index, state.checkpoints)
        else:
            raise AssertionError(feature)

        if token is None:
            continue
        skips = index - traversing_index
        skip_prefix = "" if skips == 0 else str(skips)
        tokens.append(f"{skip_prefix},{token}")
        traversing_index = index + 1
    return f"{metadata}:{'.'.join(tokens)}."


def _checkpoint_label(checkpoint_position: int) -> str:
    if checkpoint_position == 0:
        return "S"
    if checkpoint_position == -1:
        return "X"
    if checkpoint_position <= len(VALID_CURSE_CHECKPOINTS):
        return VALID_CURSE_CHECKPOINTS[checkpoint_position - 1]
    raise ConversionError(
        "Curse format supports at most "
        + f"{CURSE_MAX_CHECKPOINT_LABEL} as the last intermediate checkpoint.",
    )


def encode_curse(state: PuzzleState, blank_char: str) -> str:
    """Encode a PuzzleState into a curse or cursep string."""
    checkpoint_labels = {
        state.checkpoints[0]: "S",
        state.checkpoints[-1]: "X",
    }
    for checkpoint_position, checkpoint_index in enumerate(
        state.checkpoints[1:-1],
        start=1,
    ):
        checkpoint_labels[checkpoint_index] = _checkpoint_label(checkpoint_position)

    row_width = state.cols * CURSE_CELL_STRIDE - 1
    lines: list[str] = []
    for row in range(state.rows):
        chars = [blank_char] * row_width
        if blank_char == ".":
            for slot in range(0, row_width, CURSE_CELL_STRIDE):
                chars[slot] = "."
            for slot in range(2, row_width, CURSE_CELL_STRIDE):
                chars[slot] = "."
        for col in range(state.cols):
            index = row * state.cols + col
            char_index = col * CURSE_CELL_STRIDE + CURSE_CELL_OFFSET
            feature = state.features[index]
            if feature == BLANK:
                chars[char_index] = blank_char
            elif feature == SYSTEM_WALL:
                chars[char_index] = "#"
            elif feature == PLAYER_WALL:
                chars[char_index] = "@"
            elif feature == CHECKPOINT:
                chars[char_index] = checkpoint_labels[index]
            else:
                raise AssertionError(feature)
        lines.append("".join(chars))
    return "\n".join(lines) + "\n"


def infer_format(path: Path) -> str:
    """Determine format from file extension."""
    suffix = path.suffix.lower()
    if suffix == ".mapcode":
        return "mapcode"
    if suffix == ".curse":
        return "curse"
    if suffix == ".cursep":
        return "cursep"
    raise ConversionError(f"Unable to infer format from {path.name!r}.")


def read_state(input_path: str) -> PuzzleState:
    """Read and parse a mapcode/curse/cursep file into a PuzzleState."""
    source_path = Path(input_path)
    raw_text = source_path.read_text()
    fallback_name = source_path.stem
    source_format = infer_format(source_path)

    if source_format == "mapcode":
        return parse_mapcode(raw_text, fallback_name=fallback_name)
    if source_format == "curse":
        return parse_curse(
            raw_text,
            fallback_name=fallback_name,
            blank_char=" ",
            rows_override=None,
            cols_override=None,
            budget_override=None,
            name_override=None,
        )
    if source_format == "cursep":
        return parse_curse(
            raw_text,
            fallback_name=fallback_name,
            blank_char=".",
            rows_override=None,
            cols_override=None,
            budget_override=None,
            name_override=None,
        )
    raise AssertionError(source_format)


def render_state(state: PuzzleState, target_format: str) -> str:
    """Encode a PuzzleState into the requested format string."""
    if target_format == "mapcode":
        return encode_mapcode(state)
    if target_format == "curse":
        return encode_curse(state, blank_char=" ")
    if target_format == "cursep":
        return encode_curse(state, blank_char=".")
    raise AssertionError(target_format)
