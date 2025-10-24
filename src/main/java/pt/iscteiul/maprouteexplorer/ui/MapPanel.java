package pt.iscteiul.maprouteexplorer.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

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
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }
    
    /**
     * Configura os listeners de mouse para interação com o mapa.
     */
    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
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
        Location clickedLocation = screenToLocation(x, y);
        
        if (clickedLocation != null) {
            selectedPoints.add(clickedLocation);
            
            if (pointSelectionListener != null) {
                pointSelectionListener.onPointSelected(clickedLocation);
            }
            
            repaint();
        }
    }
    
    /**
     * Converte coordenadas de tela para coordenadas geográficas.
     * 
     * @param screenX coordenada X da tela
     * @param screenY coordenada Y da tela
     * @return localização geográfica correspondente
     */
    private Location screenToLocation(int screenX, int screenY) {
        // TODO: Implementar conversão de coordenadas de tela para geográficas
        // Esta implementação dependeria da biblioteca de mapas utilizada
        
        // Implementação temporária - seria substituída pela conversão real
        double lat = mapCenter.getLatitude() + (getHeight() / 2 - screenY) * 0.001;
        double lon = mapCenter.getLongitude() + (screenX - getWidth() / 2) * 0.001;
        
        return new Location(lat, lon);
    }
    
    /**
     * Converte coordenadas geográficas para coordenadas de tela.
     * 
     * @param location localização geográfica
     * @return ponto na tela correspondente
     */
    private Point locationToScreen(Location location) {
        // TODO: Implementar conversão de coordenadas geográficas para tela
        // Esta implementação dependeria da biblioteca de mapas utilizada
        
        // Implementação temporária
        int x = (int) ((location.getLongitude() - mapCenter.getLongitude()) * 1000 + getWidth() / 2);
        int y = (int) (getHeight() / 2 - (location.getLatitude() - mapCenter.getLatitude()) * 1000);
        
        return new Point(x, y);
    }
    
    /**
     * Define a rota a ser exibida no mapa.
     * 
     * @param route rota para exibir
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
        repaint();
    }
    
    /**
     * Obtém o centro atual do mapa.
     * 
     * @return localização central do mapa
     */
    public Location getMapCenter() {
        return mapCenter;
    }
    
    /**
     * Define o nível de zoom do mapa.
     * 
     * @param zoomLevel novo nível de zoom
     */
    public void setZoomLevel(int zoomLevel) {
        this.zoomLevel = Math.max(1, Math.min(18, zoomLevel));
        repaint();
    }
    
    /**
     * Obtém o nível de zoom atual.
     * 
     * @return nível de zoom
     */
    public int getZoomLevel() {
        return zoomLevel;
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
     * Desenha o componente do mapa.
     * 
     * @param g contexto gráfico
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        try {
            // Configurar anti-aliasing para melhor qualidade
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Desenhar mapa base
            drawMapBase(g2d);
            
            // Desenhar pontos selecionados
            drawSelectedPoints(g2d);
            
            // Desenhar rota atual
            drawCurrentRoute(g2d);
            
        } finally {
            g2d.dispose();
        }
    }
    
    /**
     * Desenha o mapa base.
     * 
     * @param g2d contexto gráfico 2D
     */
    private void drawMapBase(Graphics2D g2d) {
        // TODO: Implementar renderização do mapa base
        // Esta implementação seria feita com uma biblioteca de mapas como JMapViewer
        
        // Implementação temporária - fundo simples
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Desenhar grade simples
        g2d.setColor(Color.GRAY);
        g2d.setStroke(new BasicStroke(1));
        for (int x = 0; x < getWidth(); x += 50) {
            g2d.drawLine(x, 0, x, getHeight());
        }
        for (int y = 0; y < getHeight(); y += 50) {
            g2d.drawLine(0, y, getWidth(), y);
        }
    }
    
    /**
     * Desenha os pontos selecionados no mapa.
     * 
     * @param g2d contexto gráfico 2D
     */
    private void drawSelectedPoints(Graphics2D g2d) {
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2));
        
        for (Location point : selectedPoints) {
            Point screenPoint = locationToScreen(point);
            int radius = 8;
            g2d.fillOval(screenPoint.x - radius, screenPoint.y - radius, radius * 2, radius * 2);
            g2d.setColor(new Color(139, 0, 0)); // DARK_RED
            g2d.drawOval(screenPoint.x - radius, screenPoint.y - radius, radius * 2, radius * 2);
            g2d.setColor(Color.RED);
        }
    }
    
    /**
     * Desenha a rota atual no mapa.
     * 
     * @param g2d contexto gráfico 2D
     */
    private void drawCurrentRoute(Graphics2D g2d) {
        if (currentRoute == null || currentRoute.isEmpty()) {
            return;
        }
        
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(3));
        
        List<Location> waypoints = currentRoute.getWaypoints();
        if (waypoints.size() >= 2) {
            Point prevPoint = locationToScreen(waypoints.get(0));
            
            for (int i = 1; i < waypoints.size(); i++) {
                Point currentPoint = locationToScreen(waypoints.get(i));
                g2d.drawLine(prevPoint.x, prevPoint.y, currentPoint.x, currentPoint.y);
                prevPoint = currentPoint;
            }
        }
    }
}


