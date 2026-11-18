@REM ----------------------------------------------------------------------------
@REM Maven Wrapper convenience script for e-comm backend
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set "IDEA_MAVEN=C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.1.1.1\plugins\maven\lib\maven3\bin\mvn.cmd"

where mvn >nul 2>&1
if %ERRORLEVEL% equ 0 (
    mvn %*
) else if exist "%IDEA_MAVEN%" (
    call "%IDEA_MAVEN%" %*
) else (
    echo Error: Maven executable not found on PATH or at "%IDEA_MAVEN%".
    exit /b 1
)
