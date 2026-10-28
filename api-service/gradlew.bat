@echo off
setlocal
set DIR=%~dp0
if not defined JAVA_HOME goto findJavaFromPath
set JAVA_EXE=%JAVA_HOME%\bin\java.exe
if exist "%JAVA_EXE%" goto runWrapper
:findJavaFromPath
set JAVA_EXE=java.exe
where %JAVA_EXE% >nul 2>nul
if %ERRORLEVEL% neq 0 (
  echo ERROR: JAVA_HOME is not set and no java command could be found in your PATH. 1>&2
  exit /b 1
)
:runWrapper
"%JAVA_EXE%" -classpath "%DIR%\gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
endlocal
