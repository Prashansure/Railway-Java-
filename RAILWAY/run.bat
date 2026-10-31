@echo off
cd /d "%~dp0"
if not exist target\classes mkdir target\classes
dir /s /b src\main\java\*.java > target\sources.txt
javac -encoding UTF-8 -d target\classes @target\sources.txt
if errorlevel 1 (
  pause
  exit /b 1
)
java -cp "target\classes;lib\sqlite-jdbc-3.43.0.0.jar" railway.SmartRailwaySystem
