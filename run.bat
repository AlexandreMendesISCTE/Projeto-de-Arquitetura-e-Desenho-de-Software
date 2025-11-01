@echo off
cd /d "%~dp0"

set JAVAFX_25=C:\Users\Utilizador\javafx-sdk-25.0.1
set JAVAFX_21=C:\Users\Utilizador\javafx-sdk-21.0.2
set JAVAFX_17=C:\Users\Utilizador\javafx-sdk-17.0.13

if exist "%JAVAFX_25%\lib\javafx.web.jar" (
    echo Using JavaFX SDK at %JAVAFX_25%
    echo Note: If flickering persists, try JavaFX 21.0.2 or disable hardware acceleration with: -Dprism.order=sw
    "C:\Program Files\Java\jdk-23\bin\java" -Dprism.order=sw --module-path "%JAVAFX_25%\lib" --add-modules javafx.controls,javafx.web,javafx.swing -cp target\map-route-explorer-2.0.0-jar-with-dependencies.jar pt.iscteiul.maprouteexplorer.Main
) else if exist "%JAVAFX_21%\lib\javafx.web.jar" (
    echo Using JavaFX SDK at %JAVAFX_21%
    "C:\Program Files\Java\jdk-23\bin\java" -Dprism.order=sw --module-path "%JAVAFX_21%\lib" --add-modules javafx.controls,javafx.web,javafx.swing -cp target\map-route-explorer-2.0.0-jar-with-dependencies.jar pt.iscteiul.maprouteexplorer.Main
) else if exist "%JAVAFX_17%\lib\javafx.web.jar" (
    echo Using JavaFX SDK at %JAVAFX_17%
    "C:\Program Files\Java\jdk-23\bin\java" -Dprism.order=sw --module-path "%JAVAFX_17%\lib" --add-modules javafx.controls,javafx.web,javafx.swing -cp target\map-route-explorer-2.0.0-jar-with-dependencies.jar pt.iscteiul.maprouteexplorer.Main
) else (
    echo ==========================================
    echo WARNING: JavaFX SDK not found!
    echo ==========================================
    echo.
    echo The flickering issues you're experiencing are likely caused by
    echo JavaFX being loaded from the fat JAR instead of the module path.
    echo.
    echo To fix this, please download JavaFX SDK:
    echo.
    echo 1. Go to: https://openjfx.io/
    echo 2. Download: JavaFX SDK for Windows (any recent version)
    echo 3. Extract to: C:\Users\Utilizador\javafx-sdk-XX.X.X
    echo 4. Run this script again
    echo.
    echo The warning "Unsupported JavaFX configuration: classes were
    echo loaded from unnamed module" will disappear and rendering should
    echo improve significantly.
    echo.
    echo Attempting to run anyway (may have rendering issues)...
    echo.
    "C:\Program Files\Java\jdk-23\bin\java" -cp target\map-route-explorer-2.0.0-jar-with-dependencies.jar pt.iscteiul.maprouteexplorer.Main
    
    if %ERRORLEVEL% NEQ 0 (
        echo.
        echo Application failed. JavaFX SDK is required.
    )
)
