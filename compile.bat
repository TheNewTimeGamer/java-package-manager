@echo off
REM Set variables
set OUT_DIR=out
set LIB_DIR=lib
set JAR_NAME=PackageManager.jar
set MANIFEST_FILE=manifest.txt
set GSON_JAR=gson-2.8.9.jar

REM Create output directory if it doesn't exist
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

REM Compile the Java file
javac -d %OUT_DIR% -cp %LIB_DIR%\%GSON_JAR% PackageManager.java

REM Check if compilation was successful
if %errorlevel% neq 0 (
    echo Compilation failed.
    exit /b %errorlevel%
)

REM Create the JAR file in the out directory
jar cfm %OUT_DIR%\%JAR_NAME% %MANIFEST_FILE% -C %OUT_DIR% . -C %LIB_DIR% %GSON_JAR%

REM Check if JAR creation was successful
if %errorlevel% neq 0 (
    echo JAR creation failed.
    exit /b %errorlevel%
)

REM Clean up class files
for /R "%OUT_DIR%" %%f in (*.class) do (
    del "%%f"
)

echo Build completed successfully.