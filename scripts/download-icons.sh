#!/usr/bin/env bash
set -e
DEST="src/astrogeist/icons"
mkdir -p "$DEST"
BASE="https://raw.githubusercontent.com/tabler/tabler-icons/master/icons/outline"
for icon in telescope settings door-exit; do
    curl -fsSL "$BASE/$icon.svg" -o "$DEST/$icon.svg"
    echo "Downloaded $icon.svg"
done
