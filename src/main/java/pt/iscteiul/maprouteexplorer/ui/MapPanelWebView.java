package pt.iscteiul.maprouteexplorer.ui;

import pt.iscteiul.maprouteexplorer.model.Location;
import pt.iscteiul.maprouteexplorer.model.Route;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Painel para exibição e interação com o mapa usando JavaFX WebView e
 * Leaflet.js.
 * 
 * Versão minimalista sem features extras que possam afetar o render do mapa.
 * 
 * @author Alexandre Mendes
 * @version 2.0.0
 * @since 2.0.0
 */
public class MapPanelWebView extends JPanel implements MapPanelInterface {

    private JFXPanel fxPanel;
    private WebEngine webEngine;
    private List<Location> selectedPoints;
    private Route currentRoute;
    private Location mapCenter;
    private int zoomLevel;
    private PointSelectionListener pointSelectionListener;

    /**
     * Construtor que inicializa o painel do mapa.
     */
    public MapPanelWebView() {
        this.selectedPoints = new ArrayList<>();
        this.currentRoute = null;
        this.mapCenter = new Location(38.7223, -9.1393); // Lisboa
        this.zoomLevel = 13;
        this.pointSelectionListener = null;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));
        setBorder(BorderFactory.createLineBorder(Color.BLACK));

        initializeJavaFX();
    }

    /**
     * Inicializa JavaFX e carrega o mapa HTML.
     */
    private void initializeJavaFX() {
        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);

        // Disable automatic repainting that could cause flickering
        fxPanel.setPreferredSize(new Dimension(800, 600));

        Platform.runLater(() -> {
            try {
                WebView webView = new WebView();
                webEngine = webView.getEngine();
                webEngine.setJavaScriptEnabled(true);

                // Set solid background to prevent transparency flickering
                webView.setStyle("-fx-background-color: white;");

                // Set User-Agent to ensure proper tile loading
                webEngine.setUserAgent(
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 MapRouteExplorer/2.0");

                // Disable excessive logging that could cause performance issues
                // Only log critical errors
                webEngine.getLoadWorker().exceptionProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        System.err.println("WebEngine exception: " + newVal.getMessage());
                    }
                });

                String html = loadMapHTML();
                webEngine.loadContent(html);

                // Set up JavaScript bridge for point selection
                setupJavaScriptBridge();

                Scene scene = new Scene(webView);
                scene.setFill(javafx.scene.paint.Color.WHITE);

                // Set scene before setting to panel to prevent initial flicker
                fxPanel.setScene(scene);

                // Wait a moment for initial load
                Platform.runLater(() -> {
                    // Force initial size to prevent resize flickering
                    fxPanel.setSize(getWidth(), getHeight());
                });

            } catch (Exception e) {
                System.err.println("Erro ao inicializar WebView: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        if (fxPanel != null) {
            fxPanel.setSize(width, height);
        }
    }

    /**
     * Carrega o conteúdo HTML do mapa.
     */
    private String loadMapHTML() {
        try (InputStream is = getClass().getResourceAsStream("/map.html")) {
            if (is == null) {
                return generateInlineHTML();
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Erro ao carregar map.html, usando HTML inline: " + e.getMessage());
            return generateInlineHTML();
        }
    }

    /**
     * Gera HTML inline caso o ficheiro não seja encontrado - versão minimalista.
     */
    private String generateInlineHTML() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <!-- Use older Leaflet version that's more compatible with JavaFX WebView -->
                    <link rel="stylesheet" href="https://unpkg.com/leaflet@0.7.7/dist/leaflet.css" />
                    <script src="https://unpkg.com/leaflet@0.7.7/dist/leaflet.js"></script>
                    <style>
                        body { margin: 0; padding: 0; background-color: white; }
                        #map { width: 100vw; height: 100vh; background-color: white; }
                    </style>
                </head>
                <body>
                    <div id="map"></div>
                    <script>
                        var map = L.map('map').setView([38.7223, -9.1393], 13);
                        var osmLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            maxZoom: 19,
                            minZoom: 1,
                            attribution: '© OpenStreetMap contributors',
                            subdomains: ['a', 'b', 'c'],
                            tileSize: 256,
                            zoomOffset: 0
                        });
                        osmLayer.on('tileerror', function(error, tile) {
                            // Silent error handling
                        });
                        osmLayer.addTo(map);

                        // Enable point marking on map click
                        map.on('click', function(e) {
                            var lat = e.latlng.lat;
                            var lon = e.latlng.lng;
                            var marker = L.marker([lat, lon], {
                                icon: L.icon({
                                    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png',
                                    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
                                    iconSize: [25, 41],
                                    iconAnchor: [12, 41],
                                    popupAnchor: [1, -34],
                                    shadowSize: [41, 41]
                                })
                            }).addTo(map);
                            markers.push(marker);
                            if (window.javaBridge && window.javaBridge.onMapClick) {
                                window.javaBridge.onMapClick(lat, lon);
                            } else if (window.javaBridgeCallbacks) {
                                window.javaBridgeCallbacks.push({type: 'click', lat: lat, lon: lon});
                            }
                        });

                        var markers = [];
                        var routePolyline = null;

                        window.addMarker = function(lat, lon, title) {
                            var marker = L.marker([lat, lon]).addTo(map);
                            if (title) marker.bindPopup(title);
                            markers.push(marker);
                            return markers.length - 1;
                        };

                        window.setMapCenter = function(lat, lon, zoom) {
                            if (zoom !== undefined && zoom !== null) {
                                map.setView([lat, lon], zoom);
                            } else {
                                map.setView([lat, lon], map.getZoom());
                            }
                        };

                        window.drawRoute = function(waypoints) {
                            if (routePolyline) map.removeLayer(routePolyline);
                            if (!waypoints || waypoints.length < 2) return;
                            var latlngs = waypoints.map(function(wp) { return [wp.lat, wp.lon]; });
                            routePolyline = L.polyline(latlngs, {
                                color: '#3388ff',
                                weight: 5,
                                opacity: 0.7
                            }).addTo(map);
                            map.fitBounds(routePolyline.getBounds(), { padding: [50, 50] });
                        };

                        window.clearMarkers = function() {
                            markers.forEach(function(m) { map.removeLayer(m); });
                            markers = [];
                            if (routePolyline) { map.removeLayer(routePolyline); routePolyline = null; }
                        };
                    </script>
                </body>
                </html>
                """;
    }

    /**
     * Configura a ponte Java-JavaScript para comunicação bidirecional.
     */
    private void setupJavaScriptBridge() {
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                Platform.runLater(() -> {
                    try {
                        // Create JavaScript bridge using executeScript
                        String bridgeScript = """
                                window.javaBridgeCallbacks = [];
                                window.javaBridge = {
                                    onMapClick: function(lat, lon) {
                                        if (!window.javaBridgeCallbacks) window.javaBridgeCallbacks = [];
                                        window.javaBridgeCallbacks.push({type: 'click', lat: lat, lon: lon});
                                    }
                                };
                                """;
                        webEngine.executeScript(bridgeScript);

                        // Start polling for callbacks
                        startCallbackPoller();
                    } catch (Exception e) {
                        System.err.println("Erro ao configurar bridge JavaScript: " + e.getMessage());
                    }
                });
            }
        });
    }

    /**
     * Inicia polling para callbacks JavaScript (para comunicação JavaScript ->
     * Java).
     */
    private void startCallbackPoller() {
        javax.swing.Timer timer = new javax.swing.Timer(200, e -> { // Poll every 200ms
            Platform.runLater(() -> {
                try {
                    String callbackCheck = """
                            (function() {
                                if (!window.javaBridgeCallbacks || window.javaBridgeCallbacks.length === 0) {
                                    return JSON.stringify(null);
                                }
                                var callback = window.javaBridgeCallbacks.shift();
                                return JSON.stringify(callback);
                            })();
                            """;
                    String resultStr = (String) webEngine.executeScript(callbackCheck);

                    if (resultStr != null && !resultStr.equals("null") && !resultStr.isEmpty()) {
                        resultStr = resultStr.trim();
                        if (resultStr.startsWith("{") && resultStr.contains("\"type\":\"click\"")) {
                            // Parse click event
                            int latStart = resultStr.indexOf("\"lat\":") + 6;
                            int latEnd = resultStr.indexOf(",", latStart);
                            if (latEnd == -1)
                                latEnd = resultStr.indexOf("}", latStart);

                            int lonStart = resultStr.indexOf("\"lon\":") + 6;
                            int lonEnd = resultStr.indexOf("}", lonStart);

                            if (latStart > 5 && lonStart > 5) {
                                try {
                                    double lat = Double.parseDouble(resultStr.substring(latStart, latEnd).trim());
                                    double lon = Double.parseDouble(resultStr.substring(lonStart, lonEnd).trim());
                                    handleMapClick(lat, lon);
                                } catch (NumberFormatException ex) {
                                    // Ignore parsing errors
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    // Ignore errors in polling
                }
            });
        });
        timer.setRepeats(true);
        timer.start();
    }

    /**
     * Processa cliques no mapa recebidos do JavaScript.
     */
    private void handleMapClick(double lat, double lon) {
        SwingUtilities.invokeLater(() -> {
            Location location = new Location(lat, lon);
            selectedPoints.add(location);

            if (pointSelectionListener != null) {
                pointSelectionListener.onPointSelected(location);
            }
        });
    }

    /**
     * Executa JavaScript na página.
     */
    private void executeJS(String script) {
        if (webEngine == null)
            return;

        Platform.runLater(() -> {
            try {
                webEngine.executeScript(script);
            } catch (Exception e) {
                System.err.println("Erro ao executar JavaScript: " + e.getMessage());
            }
        });
    }

    // ========== API Pública ==========

    public void setMapCenter(Location center) {
        this.mapCenter = center;
        String js = String.format("window.setMapCenter(%f, %f, %d);",
                center.getLatitude(), center.getLongitude(), zoomLevel);
        executeJS(js);
    }

    public Location getMapCenter() {
        return mapCenter;
    }

    public void setZoomLevel(int zoom) {
        this.zoomLevel = Math.max(1, Math.min(19, zoom));
        if (mapCenter != null) {
            String js = String.format("window.setMapCenter(%f, %f, %d);",
                    mapCenter.getLatitude(), mapCenter.getLongitude(), zoomLevel);
            executeJS(js);
        }
    }

    public int getZoomLevel() {
        return zoomLevel;
    }

    public void setRoute(Route route) {
        this.currentRoute = route;

        if (route != null && !route.isEmpty()) {
            List<Location> waypoints = route.getWaypoints();
            if (waypoints.size() >= 2) {
                StringBuilder js = new StringBuilder("window.drawRoute([");
                for (int i = 0; i < waypoints.size(); i++) {
                    Location wp = waypoints.get(i);
                    js.append(String.format("{lat: %f, lon: %f}",
                            wp.getLatitude(), wp.getLongitude()));
                    if (i < waypoints.size() - 1) {
                        js.append(", ");
                    }
                }
                js.append("]);");
                executeJS(js.toString());
            }
        }
    }

    public void clearRoute() {
        this.currentRoute = null;
        executeJS("window.drawRoute([]);");
    }

    public void clearSelectedPoints() {
        this.selectedPoints.clear();
        executeJS("window.clearMarkers();");
    }

    public List<Location> getSelectedPoints() {
        return new ArrayList<>(selectedPoints);
    }

    public void setPointSelectionListener(PointSelectionListener listener) {
        this.pointSelectionListener = listener;
    }
}
