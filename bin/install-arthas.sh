#!/usr/bin/env bash
set -euo pipefail

# Installs a runnable `arthas` command into ~/.local/bin (already on PATH in most shells),
# and copies arthas-boot.jar into ~/.local/share/arthas.

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SRC_JAR="$REPO_ROOT/arthas-boot.jar"
SRC_WRAPPER="$REPO_ROOT/bin/arthas"

DEST_BIN_DIR="${DEST_BIN_DIR:-"$HOME/.local/bin"}"
DEST_SHARE_DIR="${DEST_SHARE_DIR:-"$HOME/.local/share/arthas"}"
DEST_JAR="$DEST_SHARE_DIR/arthas-boot.jar"
DEST_WRAPPER="$DEST_BIN_DIR/arthas"

if [[ ! -f "$SRC_JAR" ]]; then
  echo "ERROR: missing $SRC_JAR" >&2
  exit 1
fi
if [[ ! -f "$SRC_WRAPPER" ]]; then
  echo "ERROR: missing $SRC_WRAPPER" >&2
  exit 1
fi

mkdir -p "$DEST_BIN_DIR" "$DEST_SHARE_DIR"

# Keep jar readable, wrapper executable.
install -m 0644 "$SRC_JAR" "$DEST_JAR"
install -m 0755 "$SRC_WRAPPER" "$DEST_WRAPPER"

echo "Installed:"
echo "  $DEST_WRAPPER"
echo "  $DEST_JAR"
echo
echo "Verify:"
echo "  arthas --help"

