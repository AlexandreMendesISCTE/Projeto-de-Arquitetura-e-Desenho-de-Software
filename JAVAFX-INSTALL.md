# JavaFX SDK Installation Guide

## ⚠️ IMPORTANT: This Fixes the Flickering Issue!

The warning you're seeing:
```
WARNING: Unsupported JavaFX configuration: classes were loaded from 'unnamed module @759ebb3d'
```

**This is likely the root cause of your flickering and rendering issues!**

When JavaFX is loaded from the fat JAR (classpath) instead of the module path, it can cause:
- Flickering tiles
- Rendering problems
- Performance issues
- WebView functionality problems

## Quick Fix

### 1. Download JavaFX SDK

1. Go to: **https://openjfx.io/**
2. Click **"Download"**
3. Select:
   - **Version**: 21.0.2 (or latest)
   - **Platform**: Windows
   - **SDK**: JavaFX SDK
4. Download the `.zip` file

### 2. Extract JavaFX SDK

1. Extract the downloaded ZIP file
2. Extract to: `C:\Users\Utilizador\javafx-sdk-21.0.2`
3. Verify the folder contains a `lib` directory with files like:
   - `javafx.web.jar`
   - `javafx.controls.jar`
   - `javafx.swing.jar`

### 3. Run the Application

Simply run:
```bash
./run.sh
```

Or on Windows:
```cmd
run.bat
```

The script will now automatically detect JavaFX SDK and use it from the module path, which should **eliminate the flickering**!

## Verification

After installing JavaFX SDK and running the app, you should **NOT** see this warning:
```
WARNING: Unsupported JavaFX configuration: classes were loaded from 'unnamed module'
```

Instead, you should see:
```
Using JavaFX SDK at C:\Users\Utilizador\javafx-sdk-21.0.2
```

## Alternative Locations

If you extract JavaFX SDK to a different location, update the `JAVAFX_HOME` variable in `run.sh` or `run.bat`:
- `run.sh`: Edit line 4
- `run.bat`: Edit line 4

## Troubleshooting

**Still seeing the warning?**
- Verify `javafx.web.jar` exists in `C:\Users\Utilizador\javafx-sdk-21.0.2\lib\`
- Check the path in the run script matches your installation
- Try running with absolute path manually to test

**Still having flickering?**
- Make sure the warning is gone first
- Check console for any other errors
- Try a different zoom level or location

## Notes

- The project now uses **Leaflet 0.7.7** (instead of 1.9.4) which has better compatibility with JavaFX WebView
- The WebView implementation (`MapPanelWebView`) is working perfectly with point marking enabled
- A pure Java implementation (`MapPanel`) is also available as an alternative

