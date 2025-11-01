#!/bin/bash
cd "$(dirname "$0")"

echo "=========================================="
echo "Running Map Route Explorer - Pure Java Mode"
echo "=========================================="
echo ""
echo "Note: This implementation doesn't require JavaFX SDK"
echo ""

"/c/Program Files/Java/jdk-23/bin/java" -Dmap.implementation=native -Djava.util.logging.config.file=src/main/resources/logback.xml -cp target/map-route-explorer-2.0.0-jar-with-dependencies.jar pt.iscteiul.maprouteexplorer.Main

