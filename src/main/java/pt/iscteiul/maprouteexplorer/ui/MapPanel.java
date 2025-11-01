package pt.iscteiul.maprouteexplorer.ui;

import pt.iscteiul.maprouteexplorer.model.Location;
import pt.iscteiul.maprouteexplorer.model.Route;
import pt.iscteiul.maprouteexplorer.service.OkHttpClientService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Painel para exibição e interação com o mapa usando tiles OpenStreetMap.
 * 
 * Este componente permite visualizar um mapa interativo baseado em
 * OpenStreetMap, selecionar pontos através de cliques, exibir rotas calculadas
 * e fornecer funcionalidades de zoom e pan usando download direto de tiles.
 * 
 * @author Alexandre Mendes
 * @version 2.0.0
 * @since 2.0.0
 */
public class MapPanel extends JPanel implements MapPanelInterface {

    /** Logger for debug information */
    private static final Logger logger = Logger.getLogger(MapPanel.class.getName());

    /** OSM tile servers (fallback list) */
    private static final String[] TILE_SERVERS = {
            "https://a.tile.openstreetmap.org",
            "https://b.tile.openstreetmap.org",
            "https://c.tile.openstreetmap.org",
            "https://tile.openstreetmap.org"
    };

    /** Tamanho padrão dos tiles em pixels */
    private static final int TILE_SIZE = 256;
    private static final int MIN_ZOOM = 1;
    private static final int MAX_ZOOM = 18;

    /** Cliente HTTP para carregar tiles */
    private OkHttpClientService httpClient;

    /** Estado do mapa */
    private Location mapCenter;
    private int zoomLevel;
    private List<Location> selectedPoints;
    private Route currentRoute;
    private PointSelectionListener pointSelectionListener;

    /** Cache de tiles */
    private Map<String, BufferedImage> tileCache = new ConcurrentHashMap<>();
    private Set<String> tilesLoading = ConcurrentHashMap.newKeySet(); // Prevents duplicate requests
    private ExecutorService tileLoaderService;

    /** Estado de pan e zoom */
    private Point panStartPoint;
    private Point currentPanOffset = new Point(0, 0);
    private boolean isPanning = false;

    /**
     * Construtor que inicializa o painel do mapa.
     */
    public MapPanel() {
        // Enable debug logging for this class
        logger.setLevel(Level.INFO);
        Logger.getLogger(MapPanel.class.getPackage().getName()).setLevel(Level.INFO);

        logger.info("Initializing MapPanel (Pure Java implementation)");

        this.httpClient = new OkHttpClientService();
        this.selectedPoints = new ArrayList<>();
        this.currentRoute = null;
        this.mapCenter = new Location(38.7223, -9.1393); // Lisboa como centro padrão
        this.zoomLevel = 13;
        this.pointSelectionListener = null;

        logger.info(
                "Map center: " + mapCenter.getLatitude() + ", " + mapCenter.getLongitude() + " | Zoom: " + zoomLevel);

        // Thread pool para carregar tiles - increased for better performance when
        // zooming out
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(15);
        executor.setKeepAliveTime(60, TimeUnit.SECONDS);
        executor.allowCoreThreadTimeOut(false);
        this.tileLoaderService = executor;
        logger.info("Tile loader thread pool initialized with 15 threads");

        setLayout(null);
        setPreferredSize(new Dimension(800, 600));
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setBackground(Color.LIGHT_GRAY);

        setupMouseListeners();
        setFocusable(true);

        // Carregar tiles iniciais
        logger.info("Starting initial tile load...");
        SwingUtilities.invokeLater(() -> loadVisibleTiles());
    }

    /**
     * Configura os listeners de mouse para interação com o mapa.
     */
    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    isPanning = true;
                    panStartPoint = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && isPanning) {
                    Point releasePoint = e.getPoint();
                    // Check if mouse moved significantly (more than 5 pixels = pan, otherwise =
                    // click)
                    int dx = Math.abs(releasePoint.x - panStartPoint.x);
                    int dy = Math.abs(releasePoint.y - panStartPoint.y);

                    if (dx > 5 || dy > 5) {
                        // This was a pan operation
                        isPanning = false;
                        // After panning, reload visible tiles to fill any gaps
                        SwingUtilities.invokeLater(() -> {
                            loadVisibleTiles();
                            repaint();
                        });
                    } else {
                        // This was a click (minimal movement) - select point
                        isPanning = false;
                        logger.info("Point selected at screen coordinates: " + releasePoint.x + ", " + releasePoint.y);
                        handleMapClick(releasePoint.x, releasePoint.y);
                    }
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    // Duplo clique para zoom in
                    zoomIn();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isPanning && panStartPoint != null) {
                    int dx = e.getX() - panStartPoint.x;
                    int dy = e.getY() - panStartPoint.y;
                    currentPanOffset.translate(dx, dy);
                    panStartPoint = e.getPoint();
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                // Stop panning if mouse leaves component
                isPanning = false;
            }
        });

        addMouseWheelListener(e -> {
            int notches = e.getWheelRotation();
            Point mousePoint = e.getPoint();
            if (notches < 0) {
                zoomInAt(mousePoint.x, mousePoint.y);
            } else {
                zoomOutAt(mousePoint.x, mousePoint.y);
            }
        });
    }

    /**
     * Aumenta o zoom do mapa no ponto especificado.
     */
    private void zoomInAt(int x, int y) {
        if (zoomLevel < MAX_ZOOM) {
            logger.info("Zoom in at (" + x + ", " + y + ") from zoom level " + zoomLevel);

            // Get location at mouse position before zoom - this is what we want to keep
            // under the mouse
            Location mouseLocation = screenToLocation(x, y);
            logger.info("Mouse location: " + mouseLocation.getLatitude() + ", " + mouseLocation.getLongitude());

            int oldZoom = zoomLevel;
            zoomLevel++;

            // The mouse location should stay fixed - make it the new center
            // Calculate the offset needed to center the mouse location
            mapCenter = mouseLocation;
            currentPanOffset.setLocation(0, 0);

            logger.info("New zoom level: " + zoomLevel + ", new center: " + mapCenter.getLatitude() + ", "
                    + mapCenter.getLongitude());

            // Clear loading queue for old zoom level, but keep cached tiles that might be
            // useful
            // Only clear tiles from the old zoom level
            Map<String, BufferedImage> newCache = new ConcurrentHashMap<>();
            for (Map.Entry<String, BufferedImage> entry : tileCache.entrySet()) {
                if (!entry.getKey().startsWith(oldZoom + "/")) {
                    // Keep tiles from other zoom levels (they might be useful)
                    newCache.put(entry.getKey(), entry.getValue());
                }
            }
            tileCache = newCache;
            tilesLoading.clear();

            logger.info("Cleared old zoom tiles, cache now has " + tileCache.size() + " tiles");

            loadVisibleTiles();
            repaint();
        }
    }

    /**
     * Diminui o zoom do mapa no ponto especificado.
     */
    private void zoomOutAt(int x, int y) {
        if (zoomLevel > MIN_ZOOM) {
            logger.info("Zoom out at (" + x + ", " + y + ") from zoom level " + zoomLevel);

            // Get location at mouse position before zoom - this is what we want to keep
            // under the mouse
            Location mouseLocation = screenToLocation(x, y);
            logger.info("Mouse location: " + mouseLocation.getLatitude() + ", " + mouseLocation.getLongitude());

            int oldZoom = zoomLevel;
            zoomLevel--;

            // The mouse location should stay fixed - make it the new center
            mapCenter = mouseLocation;
            currentPanOffset.setLocation(0, 0);

            logger.info("New zoom level: " + zoomLevel + ", new center: " + mapCenter.getLatitude() + ", "
                    + mapCenter.getLongitude());

            // Clear loading queue for old zoom level, but keep cached tiles that might be
            // useful
            // Only clear tiles from the old zoom level
            Map<String, BufferedImage> newCache = new ConcurrentHashMap<>();
            for (Map.Entry<String, BufferedImage> entry : tileCache.entrySet()) {
                if (!entry.getKey().startsWith(oldZoom + "/")) {
                    // Keep tiles from other zoom levels (they might be useful)
                    newCache.put(entry.getKey(), entry.getValue());
                }
            }
            tileCache = newCache;
            tilesLoading.clear();

            logger.info("Cleared old zoom tiles, cache now has " + tileCache.size() + " tiles");

            loadVisibleTiles();
            repaint();
        }
    }

    /**
     * Aumenta o zoom do mapa (método público para compatibilidade).
     */
    public void zoomIn() {
        zoomInAt(getWidth() / 2, getHeight() / 2);
    }

    /**
     * Diminui o zoom do mapa (método público para compatibilidade).
     */
    public void zoomOut() {
        zoomOutAt(getWidth() / 2, getHeight() / 2);
    }

    /**
     * Desenha o componente do mapa.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Limpar fundo com cor neutra (não azul)
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Calcular limites dos tiles visíveis
        int centerX = getWidth() / 2 + currentPanOffset.x;
        int centerY = getHeight() / 2 + currentPanOffset.y;

        // Converter lat/lon do centro para coordenadas de tile
        double[] centerTile = latLonToTileXY(mapCenter.getLatitude(), mapCenter.getLongitude(), zoomLevel);

        // Desenhar tiles visíveis com buffer aumentado
        int tilesX = (int) Math.ceil(getWidth() / (double) TILE_SIZE) + 3;
        int tilesY = (int) Math.ceil(getHeight() / (double) TILE_SIZE) + 3;

        for (int x = -tilesX / 2; x <= tilesX / 2; x++) {
            for (int y = -tilesY / 2; y <= tilesY / 2; y++) {
                int tileX = (int) Math.floor(centerTile[0]) + x;
                int tileY = (int) Math.floor(centerTile[1]) + y;

                // Normalize tile coordinates (handle wrapping at boundaries)
                int maxTile = (int) Math.pow(2, zoomLevel);
                tileX = ((tileX % maxTile) + maxTile) % maxTile; // Wrap X coordinate
                // Y coordinate should not wrap (clamp to valid range)
                if (tileY < 0)
                    tileY = 0;
                if (tileY >= maxTile)
                    tileY = maxTile - 1;

                String tileKey = zoomLevel + "/" + tileX + "/" + tileY;
                BufferedImage tile = tileCache.get(tileKey);

                if (tile != null) {
                    int pixelX = centerX + (tileX - (int) Math.floor(centerTile[0])) * TILE_SIZE
                            - (int) ((centerTile[0] % 1) * TILE_SIZE);
                    int pixelY = centerY + (tileY - (int) Math.floor(centerTile[1])) * TILE_SIZE
                            - (int) ((centerTile[1] % 1) * TILE_SIZE);

                    g2d.drawImage(tile, pixelX, pixelY, TILE_SIZE, TILE_SIZE, null);

                    // If this is a placeholder, still try to load the real tile
                    if (isPlaceholderTile(tile)) {
                        loadTile(tileX, tileY, zoomLevel);
                    }
                } else {
                    // Show placeholder while loading
                    BufferedImage placeholder = createPlaceholderTile();
                    int pixelX = centerX + (tileX - (int) Math.floor(centerTile[0])) * TILE_SIZE
                            - (int) ((centerTile[0] % 1) * TILE_SIZE);
                    int pixelY = centerY + (tileY - (int) Math.floor(centerTile[1])) * TILE_SIZE
                            - (int) ((centerTile[1] % 1) * TILE_SIZE);
                    g2d.drawImage(placeholder, pixelX, pixelY, TILE_SIZE, TILE_SIZE, null);

                    // Carregar tile assincronamente
                    loadTile(tileX, tileY, zoomLevel);
                }
            }
        }

        // Desenhar rota
        if (currentRoute != null && !currentRoute.isEmpty()) {
            drawRoute(g2d);
        }

        // Desenhar pontos selecionados
        drawSelectedPoints(g2d);

        g2d.dispose();
    }

    /**
     * Carrega os tiles visíveis na tela com buffer adicional para melhor
     * performance.
     */
    private void loadVisibleTiles() {
        logger.info("Loading visible tiles for center: " + mapCenter.getLatitude() + ", " + mapCenter.getLongitude()
                + " zoom: " + zoomLevel);
        logger.info("Component size: " + getWidth() + "x" + getHeight());

        double[] centerTile = latLonToTileXY(mapCenter.getLatitude(), mapCenter.getLongitude(), zoomLevel);
        logger.fine("Center tile coordinates: " + centerTile[0] + ", " + centerTile[1]);

        // Increased buffer to 4 tiles in each direction for smoother zoom/pan
        int tilesX = (int) Math.ceil(getWidth() / (double) TILE_SIZE) + 4;
        int tilesY = (int) Math.ceil(getHeight() / (double) TILE_SIZE) + 4;
        logger.info("Loading " + tilesX + "x" + tilesY + " tiles around center");

        // Load visible tiles first (priority)
        List<String> priorityTiles = new ArrayList<>();
        List<String> backgroundTiles = new ArrayList<>();

        for (int x = -tilesX / 2; x <= tilesX / 2; x++) {
            for (int y = -tilesY / 2; y <= tilesY / 2; y++) {
                int tileX = (int) Math.floor(centerTile[0]) + x;
                int tileY = (int) Math.floor(centerTile[1]) + y;

                // Normalize tile coordinates (handle wrapping at boundaries)
                int maxTile = (int) Math.pow(2, zoomLevel);
                tileX = ((tileX % maxTile) + maxTile) % maxTile; // Wrap X coordinate
                // Y coordinate should not wrap (clamp to valid range)
                if (tileY < 0)
                    tileY = 0;
                if (tileY >= maxTile)
                    tileY = maxTile - 1;

                String tileKey = zoomLevel + "/" + tileX + "/" + tileY;

                // Prioritize visible area tiles
                if (Math.abs(x) <= tilesX / 4 && Math.abs(y) <= tilesY / 4) {
                    priorityTiles.add(tileKey);
                } else {
                    backgroundTiles.add(tileKey);
                }
            }
        }

        logger.info("Priority tiles to load: " + priorityTiles.size());
        logger.info("Background tiles to load: " + backgroundTiles.size());

        // Load priority tiles first
        for (String tileKey : priorityTiles) {
            String[] parts = tileKey.split("/");
            loadTile(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[0]));
        }

        // Load background tiles after a short delay
        tileLoaderService.submit(() -> {
            try {
                Thread.sleep(100); // Small delay to prioritize visible tiles
                logger.fine("Loading " + backgroundTiles.size() + " background tiles");
                for (String tileKey : backgroundTiles) {
                    String[] parts = tileKey.split("/");
                    loadTile(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[0]));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Carrega um tile específico com prevenção de requisições duplicadas.
     */
    private void loadTile(int tileX, int tileY, int zoom) {
        // Validate tile coordinates - prevent invalid requests
        int maxTile = (int) Math.pow(2, zoom);
        if (tileX < 0 || tileX >= maxTile || tileY < 0 || tileY >= maxTile || zoom < MIN_ZOOM || zoom > MAX_ZOOM) {
            // Invalid tile coordinates - create placeholder
            String tileKey = zoom + "/" + tileX + "/" + tileY;
            logger.warning("Invalid tile coordinates: " + tileKey + " (maxTile: " + maxTile + ")");
            BufferedImage placeholder = createPlaceholderTile();
            tileCache.put(tileKey, placeholder);
            return;
        }

        String tileKey = zoom + "/" + tileX + "/" + tileY;

        // Check if tile exists and if it's a placeholder
        BufferedImage existingTile = tileCache.get(tileKey);

        // Skip if already cached AND it's not a placeholder
        if (existingTile != null && !isPlaceholderTile(existingTile)) {
            logger.fine("Tile already cached (non-placeholder): " + tileKey);
            return;
        }

        // If there's a placeholder, we'll replace it - continue to load the real tile
        if (existingTile != null && isPlaceholderTile(existingTile)) {
            logger.fine("Tile has placeholder, will replace: " + tileKey);
        }

        // Skip if already loading (prevent duplicate requests)
        if (!tilesLoading.add(tileKey)) {
            logger.fine("Tile already loading: " + tileKey);
            return;
        }

        logger.info("Loading tile: " + tileKey + (existingTile != null ? " (replacing placeholder)" : ""));

        tileLoaderService.submit(() -> {
            try {
                logger.info("Attempting to download tile: " + tileKey);
                // Try each tile server
                for (String server : TILE_SERVERS) {
                    String url = server + "/" + zoom + "/" + tileX + "/" + tileY + ".png";
                    logger.fine("Trying server: " + server + " for tile " + tileKey);

                    try {
                        Image tileImage = httpClient.downloadTileImage(url);
                        if (tileImage != null) {
                            logger.info("Successfully downloaded tile: " + tileKey + " from " + server);
                            BufferedImage bufferedTile = new BufferedImage(
                                    TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_RGB);
                            Graphics2D g = bufferedTile.createGraphics();
                            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                            g.drawImage(tileImage, 0, 0, TILE_SIZE, TILE_SIZE, null);
                            g.dispose();

                            tileCache.put(tileKey, bufferedTile);
                            tilesLoading.remove(tileKey);
                            logger.info("Tile cached successfully. Total cached tiles: " + tileCache.size());

                            SwingUtilities.invokeLater(() -> {
                                repaint();
                            });
                            return; // Success
                        } else {
                            logger.warning("Tile image is null from server: " + server);
                        }
                    } catch (Exception e) {
                        logger.warning("Failed to download from " + server + ": " + e.getMessage());
                        // Try next server
                        continue;
                    }
                }
                // All servers failed - create placeholder
                logger.severe("All servers failed for tile: " + tileKey + ". Creating placeholder.");
                BufferedImage placeholder = createPlaceholderTile();
                tileCache.put(tileKey, placeholder);
                tilesLoading.remove(tileKey);
                SwingUtilities.invokeLater(() -> repaint());
            } catch (Exception e) {
                tilesLoading.remove(tileKey);
                logger.severe("Error loading tile " + tileKey + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Cria um tile placeholder quando o carregamento falha.
     */
    private BufferedImage createPlaceholderTile() {
        BufferedImage img = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
        g.setColor(Color.DARK_GRAY);
        g.drawLine(0, 0, TILE_SIZE, TILE_SIZE);
        g.drawLine(TILE_SIZE, 0, 0, TILE_SIZE);
        g.dispose();
        return img;
    }

    /**
     * Verifica se um tile é um placeholder (baseado no padrão visual).
     * 
     * @param tile tile para verificar
     * @return true se for placeholder
     */
    private boolean isPlaceholderTile(BufferedImage tile) {
        if (tile == null || tile.getWidth() != TILE_SIZE || tile.getHeight() != TILE_SIZE) {
            return false;
        }
        // Check if the center pixel is gray (placeholder characteristic)
        // Placeholders are filled with gray color
        int rgb = tile.getRGB(TILE_SIZE / 2, TILE_SIZE / 2);
        Color pixelColor = new Color(rgb);
        // Placeholder has gray background (RGB around 128, 128, 128)
        // Check if it's close to gray
        int grayValue = (pixelColor.getRed() + pixelColor.getGreen() + pixelColor.getBlue()) / 3;
        return grayValue >= 110 && grayValue <= 145; // Gray range
    }

    /**
     * Converte coordenadas lat/lon para coordenadas de tile (Mercator).
     */
    private double[] latLonToTileXY(double lat, double lon, int zoom) {
        double n = Math.pow(2, zoom);
        double tileX = (lon + 180.0) / 360.0 * n;
        double latRad = Math.toRadians(lat);
        double tileY = (1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI) / 2.0 * n;
        return new double[] { tileX, tileY };
    }

    /**
     * Converte coordenadas de tile para lat/lon.
     */
    private double[] tileXYToLatLon(double tileX, double tileY, int zoom) {
        double n = Math.pow(2, zoom);
        double lon = tileX / n * 360.0 - 180.0;
        double latRad = Math.atan(Math.sinh(Math.PI * (1 - 2 * tileY / n)));
        double lat = Math.toDegrees(latRad);
        return new double[] { lat, lon };
    }

    /**
     * Converte localização geográfica para coordenadas de tela.
     */
    private Point locationToScreen(Location location) {
        double[] centerTile = latLonToTileXY(mapCenter.getLatitude(), mapCenter.getLongitude(), zoomLevel);
        double[] locTile = latLonToTileXY(location.getLatitude(), location.getLongitude(), zoomLevel);

        int centerX = getWidth() / 2 + currentPanOffset.x;
        int centerY = getHeight() / 2 + currentPanOffset.y;

        int pixelX = centerX + (int) ((locTile[0] - centerTile[0]) * TILE_SIZE);
        int pixelY = centerY + (int) ((locTile[1] - centerTile[1]) * TILE_SIZE);

        return new Point(pixelX, pixelY);
    }

    /**
     * Converte coordenadas de tela para localização geográfica.
     */
    private Location screenToLocation(int x, int y) {
        double[] centerTile = latLonToTileXY(mapCenter.getLatitude(), mapCenter.getLongitude(), zoomLevel);

        int centerX = getWidth() / 2 + currentPanOffset.x;
        int centerY = getHeight() / 2 + currentPanOffset.y;

        double deltaX = (x - centerX) / (double) TILE_SIZE;
        double deltaY = (y - centerY) / (double) TILE_SIZE;

        double[] tileXY = { centerTile[0] + deltaX, centerTile[1] + deltaY };
        double[] latLon = tileXYToLatLon(tileXY[0], tileXY[1], zoomLevel);

        return new Location(latLon[0], latLon[1]);
    }

    /**
     * Processa cliques no mapa para seleção de pontos.
     */
    private void handleMapClick(int x, int y) {
        Location location = screenToLocation(x, y);
        selectedPoints.add(location);

        logger.info("Point selected: " + location.getLatitude() + ", " + location.getLongitude() +
                " (Total points: " + selectedPoints.size() + ")");

        if (pointSelectionListener != null) {
            pointSelectionListener.onPointSelected(location);
        }

        repaint();
    }

    /**
     * Desenha a rota atual no mapa.
     */
    private void drawRoute(Graphics2D g2d) {
        if (currentRoute == null || currentRoute.isEmpty()) {
            return;
        }

        List<Location> waypoints = currentRoute.getWaypoints();
        if (waypoints.size() < 2) {
            return;
        }

        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        Point prevPoint = locationToScreen(waypoints.get(0));
        for (int i = 1; i < waypoints.size(); i++) {
            Point currentPoint = locationToScreen(waypoints.get(i));
            g2d.drawLine(prevPoint.x, prevPoint.y, currentPoint.x, currentPoint.y);
            prevPoint = currentPoint;
        }
    }

    /**
     * Desenha os pontos selecionados no mapa.
     */
    private void drawSelectedPoints(Graphics2D g2d) {
        g2d.setColor(Color.RED);

        for (Location point : selectedPoints) {
            Point screenPoint = locationToScreen(point);
            g2d.fillOval(screenPoint.x - 6, screenPoint.y - 6, 12, 12);
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(screenPoint.x - 6, screenPoint.y - 6, 12, 12);
            g2d.setColor(Color.RED);
        }
    }

    // ========== API Pública ==========

    /**
     * Define o centro do mapa.
     */
    public void setMapCenter(Location center) {
        logger.info("Setting map center to: " + center.getLatitude() + ", " + center.getLongitude());
        this.mapCenter = center;
        currentPanOffset.setLocation(0, 0);
        loadVisibleTiles();
        repaint();
    }

    /**
     * Obtém o centro atual do mapa.
     */
    public Location getMapCenter() {
        return mapCenter;
    }

    /**
     * Define o nível de zoom do mapa.
     */
    public void setZoomLevel(int zoom) {
        int oldZoom = this.zoomLevel;
        this.zoomLevel = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));
        logger.info("Zoom level changed from " + oldZoom + " to " + this.zoomLevel);
        currentPanOffset.setLocation(0, 0);

        // Only clear tiles from the old zoom level, keep others
        Map<String, BufferedImage> newCache = new ConcurrentHashMap<>();
        for (Map.Entry<String, BufferedImage> entry : tileCache.entrySet()) {
            if (!entry.getKey().startsWith(oldZoom + "/")) {
                newCache.put(entry.getKey(), entry.getValue());
            }
        }
        tileCache = newCache;
        tilesLoading.clear();

        logger.info("Cleared old zoom tiles, cache now has " + tileCache.size() + " tiles");

        loadVisibleTiles();
        repaint();
    }

    /**
     * Obtém o nível de zoom atual.
     */
    public int getZoomLevel() {
        return zoomLevel;
    }

    /**
     * Define a rota a ser exibida no mapa.
     */
    public void setRoute(Route route) {
        this.currentRoute = route;
        repaint();
    }

    /**
     * Limpa a rota atual do mapa.
     */
    public void clearRoute() {
        this.currentRoute = null;
        repaint();
    }

    /**
     * Limpa todos os pontos selecionados.
     */
    public void clearSelectedPoints() {
        this.selectedPoints.clear();
        repaint();
    }

    /**
     * Obtém os pontos selecionados no mapa.
     */
    public List<Location> getSelectedPoints() {
        return new ArrayList<>(selectedPoints);
    }

    /**
     * Define o listener para eventos de seleção de pontos.
     */
    public void setPointSelectionListener(PointSelectionListener listener) {
        this.pointSelectionListener = listener;
    }
}
