@echo off
setlocal

set APP_HOME=%~dp0
set WRAPPER_JAR=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
set WRAPPER_PROPERTIES=%APP_HOME%\gradle\wrapper\gradle-wrapper.properties

if not defined JAVA_HOME (
  set JAVA_EXE=java
) else (
  set JAVA_EXE=%JAVA_HOME%\bin\java.exe
)

if not exist "%WRAPPER_JAR%" (
  for /f "tokens=2 delims==" %%A in ('findstr /r /c:"^distributionUrl" "%WRAPPER_PROPERTIES%"') do set DIST_URL=%%A
  for /f "tokens=2 delims=-" %%B in ("%DIST_URL%") do set GRADLE_VER=%%B
  if not exist "%APP_HOME%\gradle\wrapper" mkdir "%APP_HOME%\gradle\wrapper"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri https://raw.githubusercontent.com/gradle/gradle/v%GRADLE_VER%/subprojects/wrapper/src/main/resources/gradle-wrapper.jar -OutFile '%WRAPPER_JAR%'"
)

"%JAVA_EXE%" -jar "%WRAPPER_JAR%" %*

endlocal
