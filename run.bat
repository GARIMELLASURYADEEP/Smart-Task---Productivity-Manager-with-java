@echo off
REM Smart Task Manager - Windows run script
echo Compiling Java backend...

if not exist backend\out mkdir backend\out

javac -d backend/out -cp backend/lib/sqlite-jdbc.jar backend/src/com/smarttask/model/*.java backend/src/com/smarttask/db/*.java backend/src/com/smarttask/util/*.java backend/src/com/smarttask/dao/*.java backend/src/com/smarttask/handler/*.java backend/src/com/smarttask/MainServer.java

if errorlevel 1 (
    echo Compilation failed. Check Java installation.
    pause
    exit /b 1
)

echo Starting server...
cd /d %~dp0
java -cp "backend/lib/sqlite-jdbc.jar;backend/out" com.smarttask.MainServer

pause
