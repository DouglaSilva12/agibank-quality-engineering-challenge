@echo off
setlocal

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-performance-jmeter.ps1" %*
exit /b %ERRORLEVEL%
