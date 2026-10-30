@echo off
pushd "%~dp0"
java -jar "target\oral-center-0.1.0-SNAPSHOT.jar"
if errorlevel 1 pause
popd
