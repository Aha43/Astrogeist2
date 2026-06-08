#!/usr/bin/env bash
set -e

FLATLAF_VERSION="3.4.1"
JSVG_VERSION="1.7.2"
JUNIT_VERSION="1.10.2"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
LIB_DIR="$ROOT_DIR/lib"
TEST_LIB_DIR="$ROOT_DIR/lib/test"

mkdir -p "$LIB_DIR" "$TEST_LIB_DIR"

MAVEN="https://repo1.maven.org/maven2"

download() {
    local file="$1"
    local url="$2"
    local dir="$3"
    local target="$dir/$file"
    if [ -f "$target" ]; then
        echo "Already exists: $file"
    else
        echo "Downloading $file..."
        curl -fsSL -o "$target" "$url"
    fi
}

download "flatlaf-${FLATLAF_VERSION}.jar" \
    "$MAVEN/com/formdev/flatlaf/${FLATLAF_VERSION}/flatlaf-${FLATLAF_VERSION}.jar" \
    "$LIB_DIR"

download "flatlaf-extras-${FLATLAF_VERSION}.jar" \
    "$MAVEN/com/formdev/flatlaf-extras/${FLATLAF_VERSION}/flatlaf-extras-${FLATLAF_VERSION}.jar" \
    "$LIB_DIR"

download "jsvg-${JSVG_VERSION}.jar" \
    "$MAVEN/com/github/weisj/jsvg/${JSVG_VERSION}/jsvg-${JSVG_VERSION}.jar" \
    "$LIB_DIR"

download "junit-platform-console-standalone-${JUNIT_VERSION}.jar" \
    "$MAVEN/org/junit/platform/junit-platform-console-standalone/${JUNIT_VERSION}/junit-platform-console-standalone-${JUNIT_VERSION}.jar" \
    "$TEST_LIB_DIR"

echo ""
echo "Done."
echo "  Runtime libs : $LIB_DIR"
echo "  Test libs    : $TEST_LIB_DIR"
