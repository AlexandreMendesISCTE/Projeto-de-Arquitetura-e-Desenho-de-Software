# Run Scripts Guide

## Overview

The project now has **separate run scripts** for each map implementation:
- **WebView implementation** (Leaflet.js) - requires JavaFX SDK
- **Pure Java implementation** - no JavaFX required

## Available Scripts

### WebView Implementation (Leaflet.js)

**Linux/Mac/Git Bash:**
```bash
./run-webview.sh
```

**Windows:**
```cmd
run-webview.bat
```

**Requirements:**
- JavaFX SDK installed (see `JAVAFX-INSTALL.md`)
- Java 23

**What it does:**
- Automatically detects JavaFX SDK (25.0.1, 21.0.2, or 17.0.13)
- Sets `map.implementation=webview` system property
- Loads JavaFX modules from SDK
- Runs the application with WebView map

### Pure Java Implementation

**Linux/Mac/Git Bash:**
```bash
./run-native.sh
```

**Windows:**
```cmd
run-native.bat
```

**Requirements:**
- Java 23
- No JavaFX SDK needed

**What it does:**
- Sets `map.implementation=native` system property
- Runs the application with Pure Java map
- Includes debug logging for troubleshooting

## Debug Logging

The **Pure Java implementation** includes extensive debug logging to help diagnose map loading issues. Logs include:

- Tile loading attempts
- Server selection for each tile
- Success/failure for each download
- Cache hits/misses
- Component size and coordinates
- Zoom level changes
- Map center changes

### Viewing Logs

Logs are output to:
- **Console** (standard output)
- **File**: `logs/maprouteexplorer.log` (if logback is configured)

### Log Levels

- **INFO**: Important operations (tile loading, zoom changes, etc.)
- **WARNING**: Failed tile downloads (tries next server)
- **SEVERE**: Complete failure (all servers failed, creating placeholder)
- **FINE**: Detailed information (tile coordinates, server selection)

## System Property

The implementation is selected via the `map.implementation` system property:

- `webview` (default) - Uses WebView with Leaflet.js
- `native` - Uses Pure Java tile-based implementation

## Troubleshooting

### Pure Java Map Not Loading

1. **Check console logs** - Look for:
   - "Loading tile: X/Y/Z" messages
   - "Successfully downloaded" or "Failed to download" messages
   - Error messages indicating which servers failed

2. **Verify Internet connection** - Tiles are downloaded from OSM servers

3. **Check HTTP client** - Verify OkHttpClientService is working

4. **Look for placeholders** - Grey tiles with X pattern indicate failed downloads

### WebView Not Working

1. Verify JavaFX SDK is installed
2. Check that the path in the script matches your installation
3. See `JAVAFX-INSTALL.md` for setup instructions

## Old Scripts

The original `run.sh` and `run.bat` scripts still exist but now default to WebView mode.
For clarity, use the new scripts:
- `run-webview.sh` / `run-webview.bat` - WebView mode
- `run-native.sh` / `run-native.bat` - Pure Java mode

