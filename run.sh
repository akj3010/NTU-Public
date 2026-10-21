#!/usr/bin/env bash
# Builds (if needed) and runs the Stage 1 game.
#   ./run.sh                 interactive, default seed
#   ./run.sh 12345           interactive, fixed seed (reproducible)
#   ./run.sh --auto          scripted victory run
#   ./run.sh --auto-wait     scripted defeat run
set -euo pipefail
cd "$(dirname "$0")"

if [[ ! -d out || -z "$(find out -name '*.class' -print -quit)" ]]; then
  ./build.sh
fi

exec java -cp out restaurant.Main "$@"
