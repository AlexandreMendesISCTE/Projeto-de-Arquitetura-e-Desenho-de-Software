# Map Implementations Guide

## Overview

The application supports **two different map implementations** that you can switch between at runtime:

1. **WebView (Leaflet.js)** - Uses JavaFX WebView with Leaflet.js for rendering
2. **Pure Java** - Native Java implementation using direct tile loading

Both implementations provide the same functionality:
- Map display with OpenStreetMap tiles
- Zoom and pan
- Point marking/selection
- Route drawing
- Search and geocoding

## Switching Between Implementations

### Using the Menu Bar

1. Run the application using `./run.sh` or `run.bat`
2. Look for the **"Mapa"** menu in the menu bar at the top
3. Click on **"Mapa"** to see the options:
   - **"WebView (Leaflet.js)"** - Switch to WebView implementation
   - **"Pure Java"** - Switch to Pure Java implementation

### State Preservation

When switching between implementations, the application **preserves the current state**:
- ✅ Map center location
- ✅ Zoom level
- ✅ Current route (if calculated)
- ⚠️ Selected points (cleared - you'll need to click again)

## Implementation Details

### WebView (Leaflet.js)

**File:** `MapPanelWebView.java`

- **Technology:** JavaFX WebView + Leaflet.js 0.7.7
- **Advantages:**
  - Smooth rendering
  - Better tile caching handled by browser engine
  - More visual features available through Leaflet plugins
  
- **Requirements:**
  - JavaFX SDK (see `JAVAFX-INSTALL.md`)
  - Internet connection for Leaflet.js CDN

**Note:** Uses Leaflet 0.7.7 which is more compatible with JavaFX WebView and eliminates flickering issues.

### Pure Java

**File:** `MapPanel.java`

- **Technology:** Pure Java Swing + OkHttp for tile loading
- **Advantages:**
  - No external dependencies (besides OkHttp)
  - Full control over tile caching and loading
  - No JavaFX required
  
- **Requirements:**
  - OkHttp library (already included)
  - Internet connection for OSM tiles

**Note:** Uses direct tile downloading and rendering with custom caching logic.

## Code Architecture

Both implementations implement the `MapPanelInterface` interface, which ensures:
- Same public API
- Seamless switching without code changes
- Consistent behavior

**Interface:** `MapPanelInterface.java`

The `MainWindow` class uses this interface to work with either implementation:

```java
private MapPanelInterface mapPanel;
```

This allows the switching logic to work regardless of which implementation is active.

## Default Implementation

The application starts with **WebView (Leaflet.js)** by default, as it provides better visual quality and smoother interaction.

## Troubleshooting

### WebView Not Working

- Ensure JavaFX SDK is installed (see `JAVAFX-INSTALL.md`)
- Check that JavaFX modules are loaded from module path
- Verify Internet connection for Leaflet.js CDN

### Pure Java Tiles Not Loading

- Check Internet connection
- Verify OSM tile servers are accessible
- Check console for HTTP errors

### Switching Doesn't Work

- Ensure both implementations are compiled
- Check for exceptions in the console
- Verify the menu bar is visible

## Performance Comparison

| Feature | WebView | Pure Java |
|---------|---------|-----------|
| Initial Load | Faster | Slower (downloads tiles) |
| Pan Performance | Smooth | Good (after cache warmup) |
| Zoom Performance | Very Smooth | Good |
| Memory Usage | Higher (WebView overhead) | Lower |
| CPU Usage | Lower (browser engine) | Higher (Java rendering) |

## When to Use Which?

**Use WebView when:**
- You want the smoothest experience
- You have JavaFX available
- You want browser-like rendering

**Use Pure Java when:**
- You can't use JavaFX
- You want full control over tile caching
- You prefer native Java rendering
- You're developing for environments without WebView support

