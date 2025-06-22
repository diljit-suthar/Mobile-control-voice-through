#!/usr/bin/env sh
##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################
if [ -z "$JAVA_HOME" ]; then
  echo "ERROR: JAVA_HOME is not set and no 'java' command could be found."
  exit 1
fi

exec "$JAVA_HOME/bin/java" -Xmx64m -Xms64m -cp gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain "$@"
