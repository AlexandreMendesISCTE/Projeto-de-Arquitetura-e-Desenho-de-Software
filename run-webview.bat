@echo off
cd /d "%~dp0"

set JAVAFX_25=C:\Users\Utilizador\javafx-sdk-25.0.1
set JAVAFX_21=C:\Users\Utilizador\javafx-sdk-21.0.2
set JAVAFX_17=C:\Users\Utilizador\javafx-sdk-17.0.13

echo ==========================================
echo Running Map Route Explorer - WebView Mode
echo ==========================================
echo.

if exist "%JAVAFX_25%\lib\javafx.web.jar" (
    echo Using JavaFX SDK at %JAVAFX_25%
    "C:\Program Files\Java\jdk-23\bin\java" -Dmap.implementation=webview --module-path "%JAVAFX_25%\lib" --add-modules javafx.controls,javafx.web,javafx.swing -cp target\map-route-explorer-2.0.0-jar-with-dependencies.jar pt.iscteiul.maprouteexplorer.Main
) else if exist "%JAVAFX_21%\lib\javafx.web.jar" (
    echo Using JavaFX SDK at %JAVAFX_21%
    "C:\Program Files\Java\jdk-23\bin\java" -Dmap.implementation=webview --module-path "%JAVAFX_21%\lib" --add-modules javafx.controls,javafx.web,javafx.swing -cp target\map-route-explorer-2.0.0-jar-with-dependencies.jar pt.iscteiul.maprouteexplorer.Main
) else if exist "%JAVAFX_17%\lib\javafx.web.jar" (
    echo Using JavaFX SDK at %JAVAFX_17%
    "C:\Program Files\Java\jdk-23\bin\java" -Dmap.implementation=webview --module-path "%JAVAFX_17%\lib" --add-modules javafx.controls,javafx.web,javafx.swing -cp target\map-route-explorer-2.0.0-jar-with-dependencies.jar pt.iscteiul.maprouteexplorer.Main
) else (
    echo ==========================================
    echo ERROR: JavaFX SDK not found!
    echo ==========================================
    echo.
    echo WebView implementation requires JavaFX SDK.
    echo Please download JavaFX SDK from https://openjfx.io/
    echo and extract to: C:\Users\Utilizador\javafx-sdk-XX.X.X
    echo.
    pause
    exit /b 1
)

