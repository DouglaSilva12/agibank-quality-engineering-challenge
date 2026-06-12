@echo off
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-web-allure-local.ps1" %*
