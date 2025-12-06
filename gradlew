#!/usr/bin/env sh

APP_HOME=$(cd "$(dirname "$0")"; pwd)
JAVA_HOME="${JAVA_HOME}"

if [ -z "$JAVA_HOME" ]; then
  JAVA_EXE="java"
else
  JAVA_EXE="$JAVA_HOME/bin/java"
fi

WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_PROPERTIES="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Gradle wrapper JAR not found, bootstrapping..."
  PROPS_URL=$(grep distributionUrl "$WRAPPER_PROPERTIES" | cut -d= -f2)
  DIST_VERSION=$(basename "$PROPS_URL" | sed 's/gradle-\(.*\)-bin.zip/\1/')
  mkdir -p "$APP_HOME/gradle/wrapper"
  curl -sL "https://raw.githubusercontent.com/gradle/gradle/v${DIST_VERSION}/subprojects/wrapper/src/main/resources/gradle-wrapper.jar" -o "$WRAPPER_JAR"
fi

exec "$JAVA_EXE" -jar "$WRAPPER_JAR" "$@"
