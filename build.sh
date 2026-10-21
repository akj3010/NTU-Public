#!/usr/bin/env bash
# Compiles the Stage 1 game to ./out.
# Prefers a real 'javac'; if only a JDK runtime is installed (no javac binary),
# falls back to the in-runtime compiler via tools/CompileAll.java.
set -euo pipefail
cd "$(dirname "$0")"

rm -rf out
mkdir -p out

if command -v "${JAVAC:-javac}" >/dev/null 2>&1; then
  JAVAC="${JAVAC:-javac}"
  find src/main/java -name '*.java' > out/.sources
  "$JAVAC" -d out @"out/.sources"
  echo "Build OK -> out/ (javac)"
else
  echo "javac not found; using tools/CompileAll.java (JDK runtime compiler)."
  java tools/CompileAll.java
fi
