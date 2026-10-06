@echo off
title Offline Payment System - S3 BTech Project
echo ========================================================
echo   OfflinePaymentSystem - Interoperable Offline Payment
echo   Educational Proof-of-Concept (S3 BTech Project)
echo ========================================================
echo.

REM Set local paths if not in system environment
if exist "C:\Users\LENOVO\AppData\Local\Programs\jdk\bin\java.exe" (
    set "JAVA_HOME=C:\Users\LENOVO\AppData\Local\Programs\jdk"
    set "PATH=C:\Users\LENOVO\AppData\Local\Programs\jdk\bin;C:\Users\LENOVO\AppData\Local\Programs\maven\bin;%PATH%"
)

REM Check if standalone jar exists
if exist "target\OfflinePaymentSystem-1.0.0.jar" (
    echo Launching packaged standalone application...
    java -jar "target\OfflinePaymentSystem-1.0.0.jar"
) else (
    echo Running via Maven exec:java...
    mvn exec:java
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with code %ERRORLEVEL%.
    pause
)
