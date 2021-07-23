@echo off

if "%JAVA_HOME%" == "" (
  echo JAVA_HOME environment variable is undefined, please set it.
  pause
) else (
  cd %~dp0
    
  %JAVA_HOME%\bin\java -Dfile.encoding=utf-8 -jar ..\lib\wow-api-3.3.2.jar

  if errorlevel 1 (
    pause
  )
)

exit
