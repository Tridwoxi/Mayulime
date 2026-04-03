#!/usr/bin/env python3
"""Convert between Pathery mapcode, curse, and cursep formats."""

# ruff: noqa: T201 D103

from __future__ import annotations

import argparse
import sys
from pathlib import Path

from mapcode import ConversionError, infer_format, read_state, render_state


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description=(
            "Convert between mapcode, curse, and cursep using file extensions. "
            "When reading curse/cursep, trailing whitespace is preserved."
        ),
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )
    _ = parser.add_argument("input", help="Source file to convert.")
    _ = parser.add_argument("output", help="Destination file.")
    return parser


def main(argv: list[str] | None = None) -> int:
    args = build_parser().parse_args(argv)
    try:
        state = read_state(args.input)  # pyright: ignore[reportAny]
        target_format = infer_format(Path(args.output))  # pyright: ignore[reportAny]
        rendered = render_state(state, target_format)
        _ = Path(args.output).write_text(rendered)  # pyright: ignore[reportAny]
    except ConversionError as error:
        print(f"error: {error}", file=sys.stderr)
        return 1
    except OSError as error:
        print(f"error: {error}", file=sys.stderr)
        return 1

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
