package pt.iscteiul.maprouteexplorer.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import pt.iscteiul.maprouteexplorer.model.Location;
import pt.iscteiul.maprouteexplorer.model.Route;

/**
 * Painel para exibição e interação com o mapa.
 * 
 * Este componente permite visualizar um mapa interativo, selecionar pontos
 * através de cliques, exibir rotas calculadas e fornecer funcionalidades
 * de zoom e pan. Integra-se com as APIs de mapas para renderização.
 * 
 * @author Alexandre Mendes
 * @version 1.0.0
 * @since 1.0.0
 */
public class MapPanel extends JPanel {
    
    /** Componente JMapViewer para renderização do mapa */
    private JMapViewer mapViewer;
    
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
        
        // Inicializar JMapViewer
        mapViewer = new JMapViewer();
        mapViewer.setTileSource(new OsmTileSource.Mapnik());
        mapViewer.setZoomControlsVisible(true);
        mapViewer.setScrollWrapEnabled(false);
        mapViewer.setDisplayPosition(new Coordinate(mapCenter.getLatitude(), mapCenter.getLongitude()), zoomLevel);
        
        add(mapViewer, java.awt.BorderLayout.CENTER);
    }
    
    /**
     * Configura os listeners de mouse para interação com o mapa.
     */
    private void setupMouseListeners() {
        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMapClick(e.getX(), e.getY());
            }
        });
    }
    
    /**
     * Processa cliques no mapa para seleção de pontos.
     * 
     * @param x coordenada X do clique
     * @param y coordenada Y do clique
     */
    private void handleMapClick(int x, int y) {
        Coordinate coord = mapViewer.getPosition(x, y);
        Location clickedLocation = new Location(coord.getLat(), coord.getLon());
        
        selectedPoints.add(clickedLocation);
        
        if (pointSelectionListener != null) {
            pointSelectionListener.onPointSelected(clickedLocation);
        }
        
        // Adicionar marcador no mapa
        addMarkerToMap(clickedLocation);
    }
    
    /**
     * Adiciona um marcador no mapa para uma localização específica.
     * 
     * @param location localização para adicionar marcador
     */
    private void addMarkerToMap(Location location) {
        Coordinate coord = new Coordinate(location.getLatitude(), location.getLongitude());
        MapMarker marker = new MapMarkerDot(coord);
        mapViewer.addMapMarker(marker);
    }
    
    /**
     * Converte coordenadas geográficas para coordenadas de tela.
     * 
     * @param location localização geográfica
     * @return ponto na tela correspondente
     */
    private Point locationToScreen(Location location) {
        Coordinate coord = new Coordinate(location.getLatitude(), location.getLongitude());
        return mapViewer.getPoint(coord);
    }
    
    /**
     * Define a rota a ser exibida no mapa.
     * 
     * @param route rota para exibir
     */
    public void setRoute(Route route) {
        this.currentRoute = route;
        drawRouteOnMap(route);
    }
    
    /**
     * Limpa a rota atual do mapa.
     */
    public void clearRoute() {
        this.currentRoute = null;
        // Remover polígonos de rota do mapa
        mapViewer.removeAllMapPolygons();
    }
    
    /**
     * Limpa todos os pontos selecionados.
     */
    public void clearSelectedPoints() {
        this.selectedPoints.clear();
        // Remover todos os marcadores do mapa
        mapViewer.removeAllMapMarkers();
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
        Coordinate coord = new Coordinate(center.getLatitude(), center.getLongitude());
        mapViewer.setDisplayPosition(coord, zoomLevel);
    }
    
    /**
     * Obtém o centro atual do mapa.
     * 
     * @return localização central do mapa
     */
    public Location getMapCenter() {
        Coordinate coord = mapViewer.getPosition(mapViewer.getWidth()/2, mapViewer.getHeight()/2);
        return new Location(coord.getLat(), coord.getLon());
    }
    
    /**
     * Define o nível de zoom do mapa.
     * 
     * @param zoomLevel novo nível de zoom
     */
    public void setZoomLevel(int zoomLevel) {
        this.zoomLevel = Math.max(1, Math.min(18, zoomLevel));
        mapViewer.setZoom(zoomLevel);
    }
    
    /**
     * Obtém o nível de zoom atual.
     * 
     * @return nível de zoom
     */
    public int getZoomLevel() {
        return mapViewer.getZoom();
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
     * Desenha uma rota no mapa.
     * 
     * @param route rota para desenhar
     */
    private void drawRouteOnMap(Route route) {
        if (route == null || route.isEmpty()) {
            return;
        }
        
        List<Location> waypoints = route.getWaypoints();
        if (waypoints.size() >= 2) {
            List<Coordinate> coordinates = new ArrayList<>();
            for (Location waypoint : waypoints) {
                coordinates.add(new Coordinate(waypoint.getLatitude(), waypoint.getLongitude()));
            }
            
            MapPolygon routePolygon = new MapPolygonImpl(coordinates);
            mapViewer.addMapPolygon(routePolygon);
        }
    }
    
}


