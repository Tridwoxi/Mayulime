"""Benchmark the Java bench entrypoint repeatedly in parallel."""

from __future__ import annotations

import argparse
import re
import statistics
import subprocess
import sys
from collections.abc import Sequence
from concurrent.futures import Future, ThreadPoolExecutor, as_completed
from dataclasses import dataclass
from pathlib import Path
from typing import cast

# pyright: strict, reportAny=false
# ruff: noqa: D103 T201 INP001 S603 TC003

SOLUTION_PATTERN = re.compile(r"Solution:\s*(.+)")
SCORE_PATTERN = re.compile(r"Score:\s*(-?\d+)")
TIME_PATTERN = re.compile(r"Found after:\s*(\d+)\s+ms")


class BenchRunError(Exception):
    """One bench subprocess did not produce a usable result."""


class InvalidInputError(Exception):
    """User input violates runner constraints."""


@dataclass(frozen=True)
class BenchConfig:
    """Validated command-line settings for repeated bench runs."""

    map_path: Path
    solver_name: str
    timeout_ms: int
    runs: int
    parallelism: int
    java_command: str
    jar_path: Path


@dataclass(frozen=True)
class BenchResult:
    """Parsed result from one successful bench subprocess."""

    run_number: int
    score: int
    time_ms: int
    mapcode: str


def require_int(value: object, field_name: str) -> int:
    if isinstance(value, bool) or not isinstance(value, int):
        message = f"{field_name} must be an integer."
        raise InvalidInputError(message)
    return value


def require_str(value: object, field_name: str) -> str:
    if not isinstance(value, str):
        message = f"{field_name} must be a string."
        raise InvalidInputError(message)
    return value


def require_path(value: object, field_name: str) -> Path:
    if not isinstance(value, str):
        message = f"{field_name} must be a path string."
        raise InvalidInputError(message)
    return Path(value)


def parse_config(argv: Sequence[str] | None = None) -> BenchConfig:
    raw_values = cast("dict[str, object]", vars(build_parser().parse_args(argv)))
    config = BenchConfig(
        map_path=require_path(raw_values["map_path"], "map-path"),
        solver_name=require_str(raw_values["solver_name"], "solver-name").strip(),
        timeout_ms=require_int(raw_values["timeout_ms"], "timeout-ms"),
        runs=require_int(raw_values["runs"], "runs"),
        parallelism=require_int(raw_values["parallelism"], "parallelism"),
        java_command=require_str(raw_values["java"], "java").strip(),
        jar_path=require_path(raw_values["jar_path"], "jar-path"),
    )
    validate_config(config)
    return config


def validate_config(config: BenchConfig) -> None:
    if config.timeout_ms <= 0:
        message = "timeout-ms must be a positive integer."
        raise InvalidInputError(message)
    if config.runs <= 0:
        message = "runs must be a positive integer."
        raise InvalidInputError(message)
    if config.parallelism <= 0:
        message = "parallelism must be a positive integer."
        raise InvalidInputError(message)
    if config.solver_name == "":
        message = "solver-name must not be blank."
        raise InvalidInputError(message)
    if config.java_command == "":
        message = "java must not be blank."
        raise InvalidInputError(message)
    if not config.map_path.is_file():
        message = f"MapCode file does not exist: {config.map_path}"
        raise InvalidInputError(message)
    if not config.jar_path.is_file():
        message = (
            f"Bench jar does not exist: {config.jar_path}. "
            "Run ./gradlew build first."
        )
        raise InvalidInputError(message)


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )
    _ = parser.add_argument("map_path", type=str)
    _ = parser.add_argument("solver_name", type=str)
    _ = parser.add_argument("timeout_ms", type=int)
    _ = parser.add_argument("--runs", type=int, default=100)
    _ = parser.add_argument("--parallelism", type=int, default=10)
    _ = parser.add_argument("--java", type=str, default="java")
    _ = parser.add_argument("--jar-path", type=str, default="build/libs/Mayulime.jar")
    return parser


def parse_bench_stdout(stdout: str, run_number: int) -> BenchResult:
    if "Nothing found." in stdout:
        message = f"Run {run_number} did not find a solution."
        raise BenchRunError(message)

    solution_match = SOLUTION_PATTERN.search(stdout)
    score_match = SCORE_PATTERN.search(stdout)
    time_match = TIME_PATTERN.search(stdout)
    if solution_match is None or score_match is None or time_match is None:
        message = f"Run {run_number} produced unparseable output."
        raise BenchRunError(message)

    return BenchResult(
        run_number=run_number,
        score=int(score_match.group(1)),
        time_ms=int(time_match.group(1)),
        mapcode=solution_match.group(1).strip(),
    )


def run_single_bench(config: BenchConfig, run_number: int) -> BenchResult:
    command = [
        config.java_command,
        "-cp",
        str(config.jar_path),
        "infra.launch.Bench",
        str(config.map_path),
        config.solver_name,
        str(config.timeout_ms),
    ]
    completed = subprocess.run(
        command,
        check=False,
        capture_output=True,
        text=True,
    )
    if completed.returncode != 0:
        message = (
            f"Run {run_number} exited with code {completed.returncode}. "
            f"stderr: {completed.stderr.strip()}"
        )
        raise BenchRunError(message)
    return parse_bench_stdout(completed.stdout, run_number)


def choose_best_result(results: Sequence[BenchResult]) -> BenchResult:
    return max(results, key=lambda result: (result.score, -result.time_ms))


def summarize_scores(results: Sequence[BenchResult], requested_runs: int) -> str:
    scores = [result.score for result in results]
    count = len(scores)
    mean_score = statistics.fmean(scores)
    median_score = statistics.median(scores)
    minimum_score = min(scores)
    maximum_score = max(scores)
    standard_deviation = 0.0 if count == 1 else statistics.stdev(scores)

    return "\n".join(
        [
            f"Runs: {count}/{requested_runs}",
            f"Min score: {minimum_score}",
            f"Max score: {maximum_score}",
            f"Mean score: {mean_score:.2f}",
            f"Median score: {median_score:.2f}",
            f"Stddev score: {standard_deviation:.2f}",
        ],
    )


def run_many(config: BenchConfig) -> list[BenchResult]:
    worker_count = min(config.runs, config.parallelism)
    results: list[BenchResult] = []

    with ThreadPoolExecutor(max_workers=worker_count) as executor:
        futures: dict[Future[BenchResult], int] = {
            executor.submit(run_single_bench, config, run_number): run_number
            for run_number in range(1, config.runs + 1)
        }
        for future in as_completed(futures):
            try:
                result = future.result()
            except BenchRunError:
                continue
            results.append(result)

    return results


def main(argv: Sequence[str] | None = None) -> int:
    try:
        config = parse_config(argv)
        results = run_many(config)
    except InvalidInputError as error:
        print(error, file=sys.stderr)
        return 1

    if not results:
        print("All bench runs failed.", file=sys.stderr)
        return 1

    print(summarize_scores(results, config.runs))
    best_result = choose_best_result(results)
    print(f"Best mapcode: {best_result.mapcode}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
