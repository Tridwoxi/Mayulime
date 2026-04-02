#!/usr/bin/env bash
set -euo pipefail

# Profile a bench run with async-profiler showing only methods after `Solver.run`. Typically
# completes in under 10 seconds including build time. Results are printed to stdout.

if ! command -v asprof &>/dev/null; then
    echo "asprof not found. Install async-profiler first." >&2
    exit 1
fi

if [ $# -eq 0 ]; then
    echo "Usage: $0 <bench args...>" >&2
    echo "Example: $0 THROUGHPUT CLIMB examples/huge1.mapcode 999999 1" >&2
    exit 1
fi

cleanup() {
    [ -n "${BENCH_PID:-}" ] && kill "$BENCH_PID" 2>/dev/null
    [ -n "${GRADLE_PID:-}" ] && kill "$GRADLE_PID" 2>/dev/null
}
trap cleanup EXIT

./gradlew build -q 2>/dev/null

./gradlew bench --args="$*" --console=plain -q 2>/dev/null &
GRADLE_PID=$!

while true; do
    BENCH_PID=$(jps -l 2>/dev/null | grep 'infra.launch.Bench' | awk '{print $1}') || true
    if [ -n "$BENCH_PID" ]; then
        break
    fi
    sleep 0.2
done

asprof -d 3 -e cpu -o collapsed -s -I 'Solver.run' "$BENCH_PID" \
    | sed 's/.*Solver\.run;//' \
    | sort -t' ' -k2 -rn
