#!/bin/sh
# Gradle wrapper script
# https://gradle.org/install/

# Determine script location
SCRIPT_DIR=$(dirname "$0")
exec "$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.jar" "$@" 2>/dev/null || \
    gradle "$@"
