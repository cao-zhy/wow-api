@echo off

cd %~dp0
java -jar ..\lib\wow-api-2.0.0.jar

if errorlevel 1 (
    pause
)
exit
