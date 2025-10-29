package pt.iscteiul.maprouteexplorer.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.jxmapviewer.viewer.DefaultWaypointRenderer;

import pt.iscteiul.maprouteexplorer.model.Location;
import pt.iscteiul.maprouteexplorer.model.Route;

/**
 * Painel para exibição e interação com o mapa usando JXMapViewer2.
 * 
 * Este componente permite visualizar um mapa interativo baseado em
 * OpenStreetMap,
 * selecionar pontos através de cliques, exibir rotas calculadas e fornecer
 * funcionalidades de zoom e pan usando a biblioteca JXMapViewer2.
 * 
 * @author Alexandre Mendes
 * @version 1.0.0
 * @since 1.0.0
 */
public class MapPanel extends JPanel {

    /** Componente JXMapViewer para renderização do mapa */
    private JXMapViewer mapViewer;

    /** Lista de pontos selecionados no mapa */
    private List<Location> selectedPoints;

    /** Rota atual exibida no mapa */
    private Route currentRoute;

    /** Localização central do mapa */
    private Location mapCenter;

    /** Nível de zoom do mapa */
    private int zoomLevel;

    /** Listener para eventos de seleção de pontos */
    private PointSelectionListener pointSelectionListener;

    /**
     * Construtor que inicializa o painel do mapa.
     */
    public MapPanel() {
        this.selectedPoints = new ArrayList<>();
        this.currentRoute = null;
        this.mapCenter = new Location(38.7223, -9.1393); // Lisboa como centro padrão
        this.zoomLevel = 13;
        this.pointSelectionListener = null;

        initializeMap();
        setupMouseListeners();
    }

    /**
     * Inicializa o painel do mapa com configurações básicas.
     */
    private void initializeMap() {
        setLayout(new java.awt.BorderLayout());
        setPreferredSize(new Dimension(800, 600));
        setBorder(BorderFactory.createLineBorder(Color.BLACK));

        try {
            // Inicializar JXMapViewer
            mapViewer = new JXMapViewer();

            // Configurar tile factory para OpenStreetMap
            TileFactoryInfo info = new OSMTileFactoryInfo();
            DefaultTileFactory tileFactory = new DefaultTileFactory(info);
            mapViewer.setTileFactory(tileFactory);

            // Configurar o centro do mapa
            mapViewer.setAddressLocation(new GeoPosition(mapCenter.getLatitude(), mapCenter.getLongitude()));
            mapViewer.setZoom(zoomLevel);

            // Configurar controles de zoom
            // Note: JXMapViewer2 doesn't have these methods, they're handled automatically

            add(mapViewer, java.awt.BorderLayout.CENTER);

        } catch (Exception e) {
            // Se JXMapViewer falhar, usar implementação simplificada
            System.err.println("Erro ao inicializar JXMapViewer, usando implementação simplificada: " + e.getMessage());
            initializeSimpleMap();
        }
    }

    /**
     * Inicializa um mapa simplificado caso JXMapViewer falhe.
     */
    private void initializeSimpleMap() {
        setLayout(null);
        setBackground(Color.WHITE);
    }

    /**
     * Configura os listeners de mouse para interação com o mapa.
     */
    private void setupMouseListeners() {
        if (mapViewer != null) {
            mapViewer.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleMapClick(e.getX(), e.getY());
                }
            });
        } else {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleMapClick(e.getX(), e.getY());
                }
            });
        }
    }

    /**
     * Processa cliques no mapa para seleção de pontos.
     * 
     * @param x coordenada X do clique
     * @param y coordenada Y do clique
     */
    private void handleMapClick(int x, int y) {
        Location clickedLocation;

        if (mapViewer != null) {
            // Usar JXMapViewer para conversão de coordenadas
            clickedLocation = convertScreenToLocation(x, y);
        } else {
            // Usar conversão simplificada
            clickedLocation = screenToLocationSimple(x, y);
        }

        selectedPoints.add(clickedLocation);

        if (pointSelectionListener != null) {
            pointSelectionListener.onPointSelected(clickedLocation);
        }

        // Atualizar marcadores no mapa
        updateMapMarkers();
    }

    /**
     * Converte coordenadas de tela para coordenadas geográficas usando JXMapViewer.
     * 
     * @param x coordenada X da tela
     * @param y coordenada Y da tela
     * @return localização geográfica
     */
    private Location convertScreenToLocation(int x, int y) {
        if (mapViewer != null) {
            GeoPosition geoPosition = mapViewer.convertPointToGeoPosition(new Point(x, y));
            return new Location(geoPosition.getLatitude(), geoPosition.getLongitude());
        } else {
            return screenToLocationSimple(x, y);
        }
    }

    /**
     * Converte coordenadas de tela para coordenadas geográficas (método
     * simplificado).
     * 
     * @param x coordenada X da tela
     * @param y coordenada Y da tela
     * @return localização geográfica
     */
    private Location screenToLocationSimple(int x, int y) {
        // Conversão simplificada - em uma implementação real usaria projeção
        // cartográfica
        double lat = mapCenter.getLatitude() + (getHeight() / 2 - y) * 0.001;
        double lon = mapCenter.getLongitude() + (x - getWidth() / 2) * 0.001;
        return new Location(lat, lon);
    }

    /**
     * Converte coordenadas geográficas para coordenadas de tela.
     * 
     * @param location localização geográfica
     * @return ponto na tela correspondente
     */
    private Point locationToScreen(Location location) {
        if (mapViewer != null) {
            java.awt.geom.Point2D point2D = mapViewer
                    .convertGeoPositionToPoint(new GeoPosition(location.getLatitude(), location.getLongitude()));
            return new Point((int) point2D.getX(), (int) point2D.getY());
        } else {
            return locationToScreenSimple(location);
        }
    }

    /**
     * Converte coordenadas geográficas para coordenadas de tela (método
     * simplificado).
     * 
     * @param location localização geográfica
     * @return ponto na tela correspondente
     */
    private java.awt.Point locationToScreenSimple(Location location) {
        // Conversão simplificada - em uma implementação real usaria projeção
        // cartográfica
        int x = (int) ((location.getLongitude() - mapCenter.getLongitude()) / 0.001) + getWidth() / 2;
        int y = (int) ((mapCenter.getLatitude() - location.getLatitude()) / 0.001) + getHeight() / 2;
        return new java.awt.Point(x, y);
    }

    /**
     * Atualiza os marcadores no mapa.
     */
    private void updateMapMarkers() {
        if (mapViewer == null) {
            repaint();
            return;
        }

        // Criar conjunto de waypoints
        Set<Waypoint> waypoints = new HashSet<>();
        for (Location location : selectedPoints) {
            waypoints.add(new DefaultWaypoint(location.getLatitude(), location.getLongitude()));
        }

        // Configurar waypoint painter
        WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();
        waypointPainter.setWaypoints(waypoints);
        waypointPainter.setRenderer(new DefaultWaypointRenderer());

        mapViewer.setOverlayPainter(waypointPainter);
    }

    /**
     * Define a rota a ser exibida no mapa.
     * 
     * @param route rota para exibir
     */
    public void setRoute(Route route) {
        this.currentRoute = route;
        // TODO: Implementar desenho de rota no JXMapViewer
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
        updateMapMarkers();
    }

    /**
     * Obtém os pontos selecionados no mapa.
     * 
     * @return lista de pontos selecionados
     */
    public List<Location> getSelectedPoints() {
        return new ArrayList<>(selectedPoints);
    }

    /**
     * Define o centro do mapa.
     * 
     * @param center nova localização central
     */
    public void setMapCenter(Location center) {
        this.mapCenter = center;
        if (mapViewer != null) {
            mapViewer.setAddressLocation(new GeoPosition(center.getLatitude(), center.getLongitude()));
        }
        repaint();
    }

    /**
     * Obtém o centro atual do mapa.
     * 
     * @return localização central do mapa
     */
    public Location getMapCenter() {
        if (mapViewer != null) {
            GeoPosition center = mapViewer.getAddressLocation();
            return new Location(center.getLatitude(), center.getLongitude());
        } else {
            return mapCenter;
        }
    }

    /**
     * Define o nível de zoom do mapa.
     * 
     * @param zoomLevel novo nível de zoom
     */
    public void setZoomLevel(int zoomLevel) {
        this.zoomLevel = Math.max(1, Math.min(18, zoomLevel));
        if (mapViewer != null) {
            mapViewer.setZoom(zoomLevel);
        }
        repaint();
    }

    /**
     * Obtém o nível de zoom atual.
     * 
     * @return nível de zoom
     */
    public int getZoomLevel() {
        if (mapViewer != null) {
            return mapViewer.getZoom();
        } else {
            return zoomLevel;
        }
    }

    /**
     * Define o listener para eventos de seleção de pontos.
     * 
     * @param listener listener para eventos
     */
    public void setPointSelectionListener(PointSelectionListener listener) {
        this.pointSelectionListener = listener;
    }

    /**
     * Desenha o componente do mapa (apenas para modo simplificado).
     * 
     * @param g contexto gráfico
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Se JXMapViewer não estiver disponível, desenhar mapa simplificado
        if (mapViewer == null) {
            Graphics2D g2d = (Graphics2D) g;

            // Desenhar fundo do mapa
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // Desenhar grade de coordenadas
            drawGrid(g2d);

            // Desenhar pontos selecionados
            drawSelectedPoints(g2d);

            // Desenhar rota
            drawRoute(g2d);

            // Desenhar informações do mapa
            drawMapInfo(g2d);
        }
    }

    /**
     * Desenha a grade de coordenadas.
     * 
     * @param g2d contexto gráfico
     */
    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(Color.GRAY);

        // Linhas verticais
        for (int x = 0; x < getWidth(); x += 50) {
            g2d.drawLine(x, 0, x, getHeight());
        }

        // Linhas horizontais
        for (int y = 0; y < getHeight(); y += 50) {
            g2d.drawLine(0, y, getWidth(), y);
        }
    }

    /**
     * Desenha os pontos selecionados.
     * 
     * @param g2d contexto gráfico
     */
    private void drawSelectedPoints(Graphics2D g2d) {
        g2d.setColor(Color.RED);

        for (Location point : selectedPoints) {
            Point screenPoint = locationToScreen(point);
            g2d.fillOval(screenPoint.x - 5, screenPoint.y - 5, 10, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(screenPoint.x - 5, screenPoint.y - 5, 10, 10);
            g2d.setColor(Color.RED);
        }
    }

    /**
     * Desenha a rota atual.
     * 
     * @param g2d contexto gráfico
     */
    private void drawRoute(Graphics2D g2d) {
        if (currentRoute == null || currentRoute.isEmpty()) {
            return;
        }

        List<Location> waypoints = currentRoute.getWaypoints();
        if (waypoints.size() >= 2) {
            g2d.setColor(Color.BLUE);
            g2d.setStroke(new java.awt.BasicStroke(3));

            Point prevPoint = locationToScreen(waypoints.get(0));
            for (int i = 1; i < waypoints.size(); i++) {
                Point currentPoint = locationToScreen(waypoints.get(i));
                g2d.drawLine(prevPoint.x, prevPoint.y, currentPoint.x, currentPoint.y);
                prevPoint = currentPoint;
            }
        }
    }

    /**
     * Desenha informações do mapa.
     * 
     * @param g2d contexto gráfico
     */
    private void drawMapInfo(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.drawString("Centro: " + String.format("%.4f, %.4f",
                mapCenter.getLatitude(), mapCenter.getLongitude()), 10, 20);
        g2d.drawString("Zoom: " + zoomLevel, 10, 40);
        g2d.drawString("Pontos: " + selectedPoints.size(), 10, 60);
        g2d.drawString("JXMapViewer: " + (mapViewer != null ? "Ativo" : "Desabilitado"), 10, 80);
    }
}