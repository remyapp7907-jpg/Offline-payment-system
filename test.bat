@echo off
title Run Tests - OfflinePaymentSystem
echo ========================================================
echo   Running Tests for OfflinePaymentSystem
echo ========================================================
echo.

if exist "C:\Users\LENOVO\AppData\Local\Programs\jdk\bin\java.exe" (
    set "JAVA_HOME=C:\Users\LENOVO\AppData\Local\Programs\jdk"
    set "PATH=C:\Users\LENOVO\AppData\Local\Programs\jdk\bin;C:\Users\LENOVO\AppData\Local\Programs\maven\bin;%PATH%"
)

mvn test

echo.
pause
