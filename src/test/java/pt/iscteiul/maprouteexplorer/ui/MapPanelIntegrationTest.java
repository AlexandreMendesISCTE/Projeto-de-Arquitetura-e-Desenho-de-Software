package pt.iscteiul.maprouteexplorer.ui;

import pt.iscteiul.maprouteexplorer.model.Location;
import pt.iscteiul.maprouteexplorer.model.Route;
import pt.iscteiul.maprouteexplorer.model.TransportMode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;

import java.awt.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de Integração para MapPanel - Carregamento e Navegação.
 * 
 * Testa funcionalidades de forma integrada:
 * - Carregamento real de tiles
 * - Navegação completa (zoom + pan)
 * - Interação com componentes reais
 * 
 * @author Alexandre Mendes
 * @version 2.0.0
 * @since 2.0.0
 */
@DisplayName("Testes de Integração - Carregamento e Navegação do Mapa")
class MapPanelIntegrationTest {

    private MapPanel mapPanel;

    @BeforeEach
    void setUp() {
        mapPanel = new MapPanel();
        mapPanel.setPreferredSize(new Dimension(800, 600));
        
        // Initialize component in EDT
        try {
            EventQueue.invokeAndWait(() -> {
                mapPanel.setVisible(true);
            });
            // Wait for initial tile loading to start
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Deve carregar tiles inicialmente")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testInitialTileLoading() {
        // Given - map is initialized
        
        // When - wait for initial load
        try {
            Thread.sleep(2000); // Give time for tiles to load
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Then
        assertNotNull(mapPanel.getMapCenter(), "Map center should be set");
        assertTrue(mapPanel.getZoomLevel() >= 1 && mapPanel.getZoomLevel() <= 18,
                   "Zoom should be in valid range");
        
        // Component should be ready
        assertTrue(mapPanel.getWidth() > 0 || mapPanel.getPreferredSize().width > 0,
                   "Component should have valid size");
    }

    @Test
    @DisplayName("Deve fazer navegação completa (zoom + pan)")
    void testCompleteNavigation() {
        // Given
        int initialZoom = mapPanel.getZoomLevel();
        
        // When - zoom in
        mapPanel.zoomIn();
        int afterZoomIn = mapPanel.getZoomLevel();
        
        // Then
        assertEquals(initialZoom + 1, afterZoomIn, "Zoom should increase");
        
        // When - zoom out
        mapPanel.zoomOut();
        int afterZoomOut = mapPanel.getZoomLevel();
        
        // Then
        assertEquals(initialZoom, afterZoomOut, "Zoom should return to original");
        
        // When - change center
        Location newCenter = new Location(40.4168, -3.7038); // Madrid
        mapPanel.setMapCenter(newCenter);
        
        // Then
        Location finalCenter = mapPanel.getMapCenter();
        assertNotNull(finalCenter);
        assertEquals(newCenter.getLatitude(), finalCenter.getLatitude(), 0.001);
        assertEquals(newCenter.getLongitude(), finalCenter.getLongitude(), 0.001);
    }

    @Test
    @DisplayName("Deve carregar tiles após mudança de zoom")
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void testTileLoadingAfterZoom() {
        // Given
        int initialZoom = mapPanel.getZoomLevel();
        
        // When - change zoom
        mapPanel.setZoomLevel(initialZoom + 2);
        
        // Wait for tiles to load
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Then
        assertEquals(initialZoom + 2, mapPanel.getZoomLevel(), "Zoom should be changed");
        
        // When - zoom back
        mapPanel.setZoomLevel(initialZoom);
        
        // Wait for tiles
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Then
        assertEquals(initialZoom, mapPanel.getZoomLevel(), "Zoom should return to original");
    }

    @Test
    @DisplayName("Deve carregar tiles após mudança de centro")
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void testTileLoadingAfterCenterChange() {
        // When - change to different city
        Location newCenter = new Location(40.4168, -3.7038); // Madrid
        mapPanel.setMapCenter(newCenter);
        
        // Wait for tiles to load
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Then
        Location actualCenter = mapPanel.getMapCenter();
        assertNotNull(actualCenter);
        assertEquals(newCenter.getLatitude(), actualCenter.getLatitude(), 0.01);
        assertEquals(newCenter.getLongitude(), actualCenter.getLongitude(), 0.01);
    }

    @Test
    @DisplayName("Deve manter funcionalidade após múltiplas operações de navegação")
    void testMultipleNavigationOperations() {
        // Given
        Location center1 = new Location(38.7223, -9.1393); // Lisboa
        Location center2 = new Location(40.4168, -3.7038); // Madrid
        Location center3 = new Location(41.1579, -8.6291); // Porto
        
        // When - perform multiple navigation operations
        mapPanel.setMapCenter(center1);
        mapPanel.setZoomLevel(13);
        
        mapPanel.setMapCenter(center2);
        mapPanel.setZoomLevel(15);
        
        mapPanel.setMapCenter(center3);
        mapPanel.setZoomLevel(12);
        
        // Then
        Location finalCenter = mapPanel.getMapCenter();
        assertNotNull(finalCenter);
        assertEquals(12, mapPanel.getZoomLevel());
        
        // Verify all centers are valid
        assertTrue(finalCenter.getLatitude() >= -90 && finalCenter.getLatitude() <= 90);
        assertTrue(finalCenter.getLongitude() >= -180 && finalCenter.getLongitude() <= 180);
    }

    @Test
    @DisplayName("Deve desenhar rota no mapa")
    void testRouteDrawing() {
        // Given
        Location origin = new Location(38.7223, -9.1393); // Lisboa
        Location destination = new Location(40.4168, -3.7038); // Madrid
        
        Route route = new Route();
        route.addWaypoint(origin);
        route.addWaypoint(destination);
        route.setTransportMode(TransportMode.DRIVING);
        
        // When
        mapPanel.setRoute(route);
        
        // Then
        assertDoesNotThrow(() -> mapPanel.repaint(), "Should be able to repaint with route");
        
        // When - clear route
        mapPanel.clearRoute();
        
        // Then
        assertDoesNotThrow(() -> mapPanel.repaint(), "Should be able to repaint after clearing route");
    }

    @Test
    @DisplayName("Deve funcionar corretamente com diferentes níveis de zoom")
    void testDifferentZoomLevels() {
        // Test minimum zoom
        mapPanel.setZoomLevel(1);
        assertEquals(1, mapPanel.getZoomLevel());
        
        // Test medium zoom
        mapPanel.setZoomLevel(10);
        assertEquals(10, mapPanel.getZoomLevel());
        
        // Test maximum zoom
        mapPanel.setZoomLevel(18);
        assertEquals(18, mapPanel.getZoomLevel());
        
        // Test zoom progression
        for (int zoom = 5; zoom <= 15; zoom++) {
            mapPanel.setZoomLevel(zoom);
            assertEquals(zoom, mapPanel.getZoomLevel(), "Zoom should be set to " + zoom);
        }
    }
}

