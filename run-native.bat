@echo off
cd /d "%~dp0"

echo ==========================================
echo Running Map Route Explorer - Pure Java Mode
echo ==========================================
echo.
echo Note: This implementation doesn't require JavaFX SDK
echo.

"C:\Program Files\Java\jdk-23\bin\java" -Dmap.implementation=native -cp target\map-route-explorer-2.0.0-jar-with-dependencies.jar pt.iscteiul.maprouteexplorer.Main

