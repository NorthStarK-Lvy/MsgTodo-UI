@echo off
rem MsgTodo Gradle wrapper (Windows).
rem Comments are ASCII-only: cmd parses .bat files in the OEM code page (GBK
rem on zh-CN systems), and UTF-8 Chinese comments can swallow a newline and
rem get executed as commands.
rem
rem Does not require JAVA_HOME. GRADLE_USER_HOME / GRADLE_RO_DEP_CACHE can be
rem pinned per-machine in gradle-home.local (not committed; see .gitignore).
rem gradle-home.local WINS over an inherited GRADLE_USER_HOME: build hosts may
rem inject a stale/shared cache path, which caused lock fights between builds.
setlocal
set "DIR=%~dp0"

if exist "%DIR%gradle-home.local" (
  for /f "usebackq tokens=1,* delims==" %%a in ("%DIR%gradle-home.local") do (
    if /i "%%a"=="GRADLE_USER_HOME" set "GRADLE_USER_HOME=%%b"
    if /i "%%a"=="GRADLE_RO_DEP_CACHE" set "GRADLE_RO_DEP_CACHE=%%b"
  )
)

if defined JAVA_HOME (
  set "JAVAEXE=%JAVA_HOME%\bin\java.exe"
) else (
  set "JAVAEXE=java"
)

"%JAVAEXE%" -classpath "%DIR%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
set "RC=%ERRORLEVEL%"
endlocal & exit /b %RC%
