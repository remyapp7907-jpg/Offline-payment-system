@echo off
title Build Offline Payment System
echo ========================================================
echo   Building OfflinePaymentSystem (Maven Package)
echo ========================================================
echo.

if exist "C:\Users\LENOVO\AppData\Local\Programs\jdk\bin\java.exe" (
    set "JAVA_HOME=C:\Users\LENOVO\AppData\Local\Programs\jdk"
    set "PATH=C:\Users\LENOVO\AppData\Local\Programs\jdk\bin;C:\Users\LENOVO\AppData\Local\Programs\maven\bin;%PATH%"
)

mvn clean package

echo.
if %ERRORLEVEL% EQU 0 (
    echo ========================================================
    echo   BUILD SUCCESSFUL!
    echo   Target JAR: target\OfflinePaymentSystem-1.0.0.jar
    echo ========================================================
) else (
    echo BUILD FAILED with error code %ERRORLEVEL%.
)
pause
