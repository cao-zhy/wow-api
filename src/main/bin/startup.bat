@echo off

if "%JAVA_HOME%" == "" (
    echo JAVA_HOME environment variable is undefined, please set it.
    pause
) else (
    cd %~dp0
    "%JAVA_HOME%\bin\java" -jar ..\lib\wow-api-3.0.0.jar

    if errorlevel 1 (
        pause
    )
)

exit
