package pt.iscteiul.maprouteexplorer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pt.iscteiul.maprouteexplorer.model.Location;
import pt.iscteiul.maprouteexplorer.model.Route;
import pt.iscteiul.maprouteexplorer.model.TransportMode;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class OSRMServiceTest {

    @Mock
    private HttpClientService httpClient;

    private OSRMService osrmService;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        osrmService = new OSRMService(httpClient);
        mapper = new ObjectMapper();
    }

    @Test
    void testCalculateRouteSuccess() throws IOException, OSRMException {
        // Simula uma resposta JSON válida da API OSRM
        String mockResponse = "{"
            + "\"code\":\"Ok\","
            + "\"routes\":[{"
            + "\"distance\":1000.0,"
            + "\"duration\":300.0,"
            + "\"geometry\":\"_p~iF~ps|U_ulLnnqC_mqNvxq`@\""
            + "}]"
            + "}";

        when(httpClient.get(anyString())).thenReturn(mockResponse);

        Location origin = new Location(38.7223, -9.1393);      // Lisboa
        Location destination = new Location(41.1579, -8.6291); // Porto
        
        Route route = osrmService.calculateRoute(origin, destination, TransportMode.DRIVING);
        
        assertNotNull(route);
        assertEquals(TransportMode.DRIVING, route.getTransportMode());
        assertEquals(1000.0, route.getDistance());
        assertEquals(300.0, route.getDuration());
        assertFalse(route.getCoordinates().isEmpty());
    }

    @Test
    void testCalculateRouteWithMultipleWaypoints() throws IOException, OSRMException {
        String mockResponse = "{"
            + "\"code\":\"Ok\","
            + "\"routes\":[{"
            + "\"distance\":2000.0,"
            + "\"duration\":600.0,"
            + "\"geometry\":\"_p~iF~ps|U_ulLnnqC_mqNvxq`@\""
            + "}]"
            + "}";

        when(httpClient.get(anyString())).thenReturn(mockResponse);

        List<Location> waypoints = Arrays.asList(
            new Location(38.7223, -9.1393), // Lisboa
            new Location(39.7444, -8.8072), // Leiria
            new Location(41.1579, -8.6291)  // Porto
        );

        Route route = osrmService.calculateRoute(waypoints, TransportMode.DRIVING);
        
        assertNotNull(route);
        assertEquals(2000.0, route.getDistance());
        assertEquals(600.0, route.getDuration());
        assertFalse(route.getCoordinates().isEmpty());
    }

    @Test
    void testCalculateDistanceAndDuration() throws IOException, OSRMException {
        String mockResponse = "{"
            + "\"code\":\"Ok\","
            + "\"routes\":[{"
            + "\"distance\":1000.0,"
            + "\"duration\":300.0"
            + "}]"
            + "}";

        when(httpClient.get(anyString())).thenReturn(mockResponse);

        Location origin = new Location(38.7223, -9.1393);
        Location destination = new Location(41.1579, -8.6291);
        
        double[] result = osrmService.calculateDistanceAndDuration(origin, destination, TransportMode.DRIVING);
        
        assertEquals(2, result.length);
        assertEquals(1000.0, result[0]); // distância
        assertEquals(300.0, result[1]);  // duração
    }

    @Test
    void testDecodePolyline() throws Exception {
        // Testa o método decodePolyline usando reflexão já que é privado
        java.lang.reflect.Method method = OSRMService.class.getDeclaredMethod("decodePolyline", String.class);
        method.setAccessible(true);
        
        // Este é um exemplo real de polyline que representa uma rota pequena
        String encoded = "_p~iF~ps|U_ulLnnqC_mqNvxq`@";
        
        @SuppressWarnings("unchecked")
        List<Location> locations = (List<Location>) method.invoke(osrmService, encoded);
        
        assertNotNull(locations);
        assertFalse(locations.isEmpty());
        
        // Verifica se as coordenadas decodificadas estão dentro de limites razoáveis
        for (Location loc : locations) {
            assertTrue(loc.getLatitude() >= -90 && loc.getLatitude() <= 90);
            assertTrue(loc.getLongitude() >= -180 && loc.getLongitude() <= 180);
        }
    }

    @Test
    void testServiceAvailability() throws IOException {
        when(httpClient.get(anyString())).thenReturn("{}");
        assertTrue(osrmService.isServiceAvailable());

        when(httpClient.get(anyString())).thenThrow(new IOException("Simulando erro de conexão"));
        assertFalse(osrmService.isServiceAvailable());
    }

    @Test
    void testInvalidResponse() throws IOException {
        String invalidResponse = "{\"code\":\"Error\",\"message\":\"Invalid coordinates\"}";
        when(httpClient.get(anyString())).thenReturn(invalidResponse);

        Location origin = new Location(38.7223, -9.1393);
        Location destination = new Location(41.1579, -8.6291);

        assertThrows(OSRMException.class, () -> 
            osrmService.calculateRoute(origin, destination, TransportMode.DRIVING));
    }
}