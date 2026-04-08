#!/usr/bin/env bash
set -euo pipefail

# Profile a bench run with async-profiler showing only methods after `Solver.run`. Typically
# completes in under 10 seconds including build time. Results are printed to stdout. Safe to run
# while other benchmarks are in progress, but not concurrently with other profile.sh invocations,
# or for other benchmarks to begin while this script is starting up.

if ! command -v asprof &>/dev/null; then
    echo "asprof not found. Install async-profiler first." >&2
    exit 1
fi

if [ $# -ne 1 ]; then
    echo "Usage: $0 <solver>" >&2
    echo "Example: $0 CLIMB" >&2
    exit 1
fi

SOLVER="$1"

cleanup() {
    [ -n "${BENCH_PID:-}" ] && kill "$BENCH_PID" 2>/dev/null
    [ -n "${GRADLE_PID:-}" ] && kill "$GRADLE_PID" 2>/dev/null
}
trap cleanup EXIT

./gradlew build -q 2>/dev/null

EXISTING_PIDS=" $(jps -l 2>/dev/null | grep 'infra.launch.Bench' | awk '{print $1}' | tr '\n' ' ' || true) "

./gradlew bench --args="THROUGHPUT $SOLVER examples/huge1.mapcode 999999 1" --console=plain -q 2>/dev/null &
GRADLE_PID=$!

while true; do
    if ! kill -0 "$GRADLE_PID" 2>/dev/null; then
        echo "Gradle died?" >&2
        exit 1
    fi
    for pid in $(jps -l 2>/dev/null | grep 'infra.launch.Bench' | awk '{print $1}'); do
        if [[ "$EXISTING_PIDS" != *" $pid "* ]]; then
            BENCH_PID="$pid"
            break 2
        fi
    done
    sleep 0.2
done

asprof -d 3 -e cpu -o collapsed -s -I 'Solver.run' "$BENCH_PID" \
    | sed 's/.*Solver\.run;//' \
    | awk '{a[$1]+=$2} END {for(k in a) print k,a[k]}' \
    | sort -t' ' -k2 -rn
