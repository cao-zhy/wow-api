@echo off

if "%JAVA_HOME%" == "" (
  echo JAVA_HOME environment variable is undefined, please set it.
  pause
) else (
  cd %~dp0
    
  for %%f in (..\lib\wow-api-*.jar) do (
    %JAVA_HOME%\bin\java -Dfile.encoding=utf-8 -jar %%f
  )

  if errorlevel 1 (
    pause
  )
)

exit
