@echo off

REM -----------------------------------------------------------------------------
REM Gradle start up script for Windows
REM -----------------------------------------------------------------------------

set DIR=%~dp0
set JAVA_EXE=

if defined JAVA_HOME (
  set JAVA_EXE=%JAVA_HOME%\bin\java.exe
) else (
  set JAVA_EXE=java
)

"%JAVA_EXE%" ^
  -Xmx64m -Xms64m ^
  -classpath "%DIR%\gradle\wrapper\gradle-wrapper.jar" ^
  org.gradle.wrapper.GradleWrapperMain %*

exit /b %ERRORLEVEL%
