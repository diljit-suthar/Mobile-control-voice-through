#!/usr/bin/env sh
APP_HOME=`dirname "$0"`
JAVA="java"
exec "$JAVA" -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
