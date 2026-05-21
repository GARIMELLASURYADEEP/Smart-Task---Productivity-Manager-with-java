#!/bin/bash
# Smart Task Manager - Linux/Mac run script

echo "Compiling Java backend..."
mkdir -p backend/out

javac -d backend/out -cp backend/lib/sqlite-jdbc.jar \
  backend/src/com/smarttask/model/*.java \
  backend/src/com/smarttask/db/*.java \
  backend/src/com/smarttask/util/*.java \
  backend/src/com/smarttask/dao/*.java \
  backend/src/com/smarttask/handler/*.java \
  backend/src/com/smarttask/MainServer.java

if [ $? -ne 0 ]; then
  echo "Compilation failed."
  exit 1
fi

echo "Starting server..."
java -cp "backend/lib/sqlite-jdbc.jar:backend/out" com.smarttask.MainServer
