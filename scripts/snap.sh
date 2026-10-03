#!/bin/sh
# Captures the current emulator screen to play-assets/screenshots/<name>.png
# Usage: scripts/snap.sh <name>
#   e.g. scripts/snap.sh 01-onboarding
set -e
NAME="${1:-snap}"
OUT_DIR="$(dirname "$0")/../play-assets/screenshots"
mkdir -p "$OUT_DIR"
~/Library/Android/sdk/platform-tools/adb exec-out screencap -p > "$OUT_DIR/$NAME.png"
echo "saved $OUT_DIR/$NAME.png"
