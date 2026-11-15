@echo off

set DATACLEANER_HOME=%~dp0

echo Using DATACLEANER_HOME: %DATACLEANER_HOME%

set DATACLEANER_LIB_HOME=%DATACLEANER_HOME%
echo Using DATACLEANER_LIB_HOME: %DATACLEANER_LIB_HOME%

cd /d %DATACLEANER_HOME%

rem verify that a Java 21 (or later) runtime is available
set "JAVA_MAJOR_VERSION="
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
	for /f "delims=. tokens=1" %%m in ("%%~v") do set JAVA_MAJOR_VERSION=%%m
)
if "%JAVA_MAJOR_VERSION%"=="" set JAVA_MAJOR_VERSION=0
if %JAVA_MAJOR_VERSION% LSS 21 (
	echo ERROR: DataCleaner requires Java 21 or later.
	java -version
	echo Please install a Java 21+ runtime and make sure it is on the PATH.
	exit /b 1
)

set DATACLEANER_JAVA_OPTS=%JAVA_OPTS% -Xmx1024m
echo Using DATACLEANER_JAVA_OPTS=%DATACLEANER_JAVA_OPTS%

call java %DATACLEANER_JAVA_OPTS% -jar "%DATACLEANER_LIB_HOME%\DataCleaner.jar"
