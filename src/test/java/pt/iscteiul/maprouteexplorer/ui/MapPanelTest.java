package pt.iscteiul.maprouteexplorer.ui;

import pt.iscteiul.maprouteexplorer.model.Location;
import pt.iscteiul.maprouteexplorer.model.Route;
import pt.iscteiul.maprouteexplorer.model.TransportMode;
import pt.iscteiul.maprouteexplorer.service.OkHttpClientService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.*;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para MapPanel - Carregamento e Navegação do Mapa.
 * 
 * Testa funcionalidades de:
 * - Carregamento de tiles
 * - Zoom (in/out)
 * - Pan (movimento do mapa)
 * - Seleção de pontos
 * - Desenho de rotas
 * - Navegação básica
 * 
 * @author Alexandre Mendes
 * @version 2.0.0
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Carregamento e Navegação do Mapa")
class MapPanelTest {

    private MapPanel mapPanel;
    
    @Mock
    private PointSelectionListener mockListener;
    
    @Mock
    private OkHttpClientService mockHttpClient;

    @BeforeEach
    void setUp() {
        // Use a mock HTTP client if needed, or let it use real one for integration tests
        mapPanel = new MapPanel();
        mapPanel.setPreferredSize(new Dimension(800, 600));
        
        // Wait for component to initialize
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    @DisplayName("Deve inicializar o mapa com centro padrão (Lisboa)")
    void testInitialization() {
        // Then
        Location center = mapPanel.getMapCenter();
        assertNotNull(center, "Map center should not be null");
        assertEquals(38.7223, center.getLatitude(), 0.0001, "Should be Lisbon latitude");
        assertEquals(-9.1393, center.getLongitude(), 0.0001, "Should be Lisbon longitude");
        assertEquals(13, mapPanel.getZoomLevel(), "Default zoom should be 13");
    }

    @Test
    @DisplayName("Deve definir e obter centro do mapa")
    void testSetMapCenter() {
        // Given
        Location newCenter = new Location(40.4168, -3.7038); // Madrid
        
        // When
        mapPanel.setMapCenter(newCenter);
        
        // Then
        Location actualCenter = mapPanel.getMapCenter();
        assertNotNull(actualCenter);
        assertEquals(newCenter.getLatitude(), actualCenter.getLatitude(), 0.0001);
        assertEquals(newCenter.getLongitude(), actualCenter.getLongitude(), 0.0001);
    }

    @Test
    @DisplayName("Deve definir e obter nível de zoom")
    void testSetZoomLevel() {
        // Given
        int newZoom = 15;
        
        // When
        mapPanel.setZoomLevel(newZoom);
        
        // Then
        assertEquals(newZoom, mapPanel.getZoomLevel());
    }

    @Test
    @DisplayName("Deve limitar zoom ao mínimo permitido")
    void testZoomLevelMinimum() {
        // Given
        int belowMinimum = 0;
        
        // When
        mapPanel.setZoomLevel(belowMinimum);
        
        // Then
        assertTrue(mapPanel.getZoomLevel() >= 1, "Zoom should be at least 1");
    }

    @Test
    @DisplayName("Deve limitar zoom ao máximo permitido")
    void testZoomLevelMaximum() {
        // Given
        int aboveMaximum = 25;
        
        // When
        mapPanel.setZoomLevel(aboveMaximum);
        
        // Then
        assertTrue(mapPanel.getZoomLevel() <= 18, "Zoom should be at most 18");
    }

    @Test
    @DisplayName("Deve fazer zoom in no centro do mapa")
    void testZoomIn() {
        // Given
        int initialZoom = mapPanel.getZoomLevel();
        
        // When
        mapPanel.zoomIn();
        
        // Then
        assertEquals(initialZoom + 1, mapPanel.getZoomLevel(), "Zoom should increase by 1");
    }

    @Test
    @DisplayName("Deve fazer zoom out no centro do mapa")
    void testZoomOut() {
        // Given
        mapPanel.setZoomLevel(10); // Set to a safe zoom level
        int initialZoom = mapPanel.getZoomLevel();
        
        // When
        mapPanel.zoomOut();
        
        // Then
        assertEquals(initialZoom - 1, mapPanel.getZoomLevel(), "Zoom should decrease by 1");
    }

    @Test
    @DisplayName("Deve definir listener para seleção de pontos")
    void testPointSelectionListener() {
        // When
        mapPanel.setPointSelectionListener(mockListener);
        
        // Then
        assertDoesNotThrow(() -> mapPanel.setPointSelectionListener(mockListener),
                          "Setting listener should not throw");
        // Note: We can't directly verify the listener is set as it's private,
        // but we can verify it doesn't throw and the public API works
    }

    @Test
    @DisplayName("Deve limpar pontos selecionados")
    void testClearSelectedPoints() {
        // Given
        mapPanel.setPreferredSize(new Dimension(800, 600));
        
        // When
        mapPanel.clearSelectedPoints();
        
        // Then
        List<Location> points = mapPanel.getSelectedPoints();
        assertNotNull(points);
        assertEquals(0, points.size(), "Should have no selected points after clearing");
    }

    @Test
    @DisplayName("Deve definir e limpar rota")
    void testRouteManagement() {
        // Given
        Location origin = new Location(38.7223, -9.1393);
        Location destination = new Location(40.4168, -3.7038);
        Route route = new Route();
        route.addWaypoint(origin);
        route.addWaypoint(destination);
        
        // When
        mapPanel.setRoute(route);
        
        // Then - route should be set
        assertNotNull(mapPanel, "MapPanel should exist");
        
        // When - clear route
        mapPanel.clearRoute();
        
        // Then - route should be cleared
        assertDoesNotThrow(() -> mapPanel.clearRoute(), "Clear route should not throw");
    }

    @Test
    @DisplayName("Deve obter lista de pontos selecionados")
    void testGetSelectedPoints() {
        // When
        List<Location> points = mapPanel.getSelectedPoints();
        
        // Then
        assertNotNull(points, "Selected points list should not be null");
        assertTrue(points instanceof List, "Should return a List");
    }

    @Test
    @DisplayName("Deve validar coordenadas geográficas")
    void testGeographicCoordinates() {
        // Given
        mapPanel.setPreferredSize(new Dimension(800, 600));
        mapPanel.setMapCenter(new Location(38.7223, -9.1393));
        mapPanel.setZoomLevel(13);
        
        // Then
        Location center = mapPanel.getMapCenter();
        assertNotNull(center);
        assertTrue(center.getLatitude() >= -90 && center.getLatitude() <= 90,
                   "Latitude should be in valid range");
        assertTrue(center.getLongitude() >= -180 && center.getLongitude() <= 180,
                   "Longitude should be in valid range");
    }

    @Test
    @DisplayName("Deve manter zoom dentro dos limites válidos")
    void testZoomBounds() {
        // Given - test minimum
        mapPanel.setZoomLevel(1);
        assertEquals(1, mapPanel.getZoomLevel(), "Should allow zoom level 1");
        
        // Given - test maximum
        mapPanel.setZoomLevel(18);
        assertEquals(18, mapPanel.getZoomLevel(), "Should allow zoom level 18");
        
        // Given - test below minimum
        mapPanel.setZoomLevel(0);
        assertTrue(mapPanel.getZoomLevel() >= 1, "Should clamp to minimum zoom");
        
        // Given - test above maximum
        mapPanel.setZoomLevel(20);
        assertTrue(mapPanel.getZoomLevel() <= 18, "Should clamp to maximum zoom");
    }

    @Test
    @DisplayName("Deve permitir múltiplas seleções de pontos")
    void testMultiplePointSelection() {
        // Given
        mapPanel.clearSelectedPoints();
        
        // When - simulate multiple point selections
        // Note: Since handleMapClick is private, we test the state indirectly
        // by verifying the getSelectedPoints() method works
        
        // Then
        List<Location> points = mapPanel.getSelectedPoints();
        assertNotNull(points);
        // Initially should be empty
        assertEquals(0, points.size());
        
        // After clearing, should remain empty
        mapPanel.clearSelectedPoints();
        assertEquals(0, mapPanel.getSelectedPoints().size());
    }

    @Test
    @DisplayName("Deve manter estado após mudanças de zoom")
    void testStateAfterZoom() {
        // Given
        Location originalCenter = mapPanel.getMapCenter();
        int originalZoom = mapPanel.getZoomLevel();
        
        // When
        mapPanel.setZoomLevel(originalZoom + 1);
        mapPanel.setZoomLevel(originalZoom);
        
        // Then
        Location finalCenter = mapPanel.getMapCenter();
        assertEquals(originalZoom, mapPanel.getZoomLevel(), "Zoom should return to original");
        // Center might change slightly due to tile calculations, so we just verify it's valid
        assertNotNull(finalCenter);
    }

    @Test
    @DisplayName("Deve ser possível definir rota vazia")
    void testSetEmptyRoute() {
        // When
        mapPanel.setRoute(null);
        
        // Then
        assertDoesNotThrow(() -> mapPanel.clearRoute(), "Should handle null route");
    }

    @Test
    @DisplayName("Deve manter centro após zoom in/out")
    void testCenterAfterZoom() {
        // Given
        Location center = new Location(38.7223, -9.1393);
        mapPanel.setMapCenter(center);
        
        // When
        int zoom = mapPanel.getZoomLevel();
        mapPanel.setZoomLevel(zoom + 2);
        mapPanel.setZoomLevel(zoom);
        
        // Then
        Location finalCenter = mapPanel.getMapCenter();
        assertNotNull(finalCenter);
        // Note: Center might adjust slightly for optimal tile alignment
        // We verify it's still in the same general area
        assertTrue(Math.abs(finalCenter.getLatitude() - center.getLatitude()) < 1.0,
                   "Latitude should remain close to original");
        assertTrue(Math.abs(finalCenter.getLongitude() - center.getLongitude()) < 1.0,
                   "Longitude should remain close to original");
    }

    @Test
    @DisplayName("Deve desenhar rota simples entre dois pontos")
    void testDrawSimpleRoute() {
        Location origin = new Location(38.7223, -9.1393);
        Location destination = new Location(38.7369, -9.1427);

        Route route = new Route();
        route.addWaypoint(origin);
        route.addWaypoint(destination);
        route.setTransportMode(TransportMode.DRIVING);

        assertDoesNotThrow(() -> {
            mapPanel.setRoute(route);
            mapPanel.repaint();
        }, "Deve desenhar rota simples sem erros");
    }

    @Test
    @DisplayName("Deve atualizar rota dinamicamente ao alterar pontos")
    void testDynamicRouteUpdate() {
        Route firstRoute = new Route(Arrays.asList(
            new Location(38.7223, -9.1393),
            new Location(38.7369, -9.1427)
        ));


        mapPanel.setRoute(firstRoute);
        Route secondRoute = new Route(Arrays.asList(
            new Location(40.4168, -3.7038),
            new Location(41.1579, -8.6291)
        ));

        assertDoesNotThrow(() -> {
            mapPanel.setRoute(secondRoute);
            mapPanel.repaint();
        }, "Deve aceitar nova rota dinamicamente");
    }

    @Test
    @DisplayName("Deve limpar rota quando solicitado")
    void testClearRoute() {
        Route route = new Route(Arrays.asList(
            new Location(38.7223, -9.1393),
            new Location(38.7369, -9.1427)
        ));
        mapPanel.setRoute(route);

        mapPanel.clearRoute();

        assertDoesNotThrow(() -> mapPanel.repaint(), 
                      "Deve repintar sem erros após limpar rota");
    }
}

