@echo off
REM Create output directory if it doesn't exist
if not exist "out" mkdir "out"

REM Compile the Java file
javac -d out -cp lib\gson-2.8.9.jar PackageManager.java

REM Check if compilation was successful
if %errorlevel% neq 0 (
    echo Compilation failed.
    exit /b %errorlevel%
)

REM Create the JAR file
jar cfm PackageManager.jar manifest.txt -C out . -C lib gson-2.8.9.jar

REM Check if JAR creation was successful
if %errorlevel% neq 0 (
    echo JAR Creation failed.
    exit /b %errorlevel%
)

echo Build completed successfully.