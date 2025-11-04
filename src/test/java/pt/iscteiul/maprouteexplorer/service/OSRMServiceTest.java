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
        assertEquals(1000.0, route.getTotalDistance());
        assertEquals(300.0, route.getTotalDuration());
        assertFalse(route.getWaypoints().isEmpty());
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
        assertEquals(2000.0, route.getTotalDistance());
        assertEquals(600.0, route.getTotalDuration());
        assertFalse(route.getWaypoints().isEmpty());
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

    @Test
    void testInvalidCoordinates() {
        Location invalidLat = new Location(100.0, -9.1393); // Latitude inválida
        Location invalidLon = new Location(38.7223, 200.0);  // Longitude inválida
        Location valid = new Location(38.7223, -9.1393);

        assertThrows(IllegalArgumentException.class, () -> {
            osrmService.calculateRoute(invalidLat, valid, TransportMode.DRIVING);
        }, "Deve lançar exceção para latitude inválida");

        assertThrows(IllegalArgumentException.class, () -> {
            osrmService.calculateRoute(invalidLon, valid, TransportMode.DRIVING);
        }, "Deve lançar exceção para longitude inválida");
    }

    @Test
    void testNullCoordinates() {
        Location valid = new Location(38.7223, -9.1393);

        assertThrows(IllegalArgumentException.class, () -> {
            osrmService.calculateRoute(null, valid, TransportMode.DRIVING);
        }, "Deve lançar exceção para origem nula");

        assertThrows(IllegalArgumentException.class, () -> {
            osrmService.calculateRoute(valid, null, TransportMode.DRIVING);
        }, "Deve lançar exceção para destino nulo");
    }

    @Test
    void testMissingFieldsInResponse() throws IOException {
        // Teste com resposta sem campo "code"
        String responseWithoutCode = "{\"routes\":[{\"distance\":1000.0,\"duration\":300.0}]}";
        when(httpClient.get(anyString())).thenReturn(responseWithoutCode);

        Location origin = new Location(38.7223, -9.1393);
        Location destination = new Location(41.1579, -8.6291);

        assertThrows(OSRMException.class, () -> 
            osrmService.calculateRoute(origin, destination, TransportMode.DRIVING),
            "Deve lançar exceção quando campo 'code' está ausente");
    }

    @Test
    void testEmptyRoutesArray() throws IOException {
        // Teste com resposta com array de rotas vazio
        String responseEmptyRoutes = "{\"code\":\"Ok\",\"routes\":[]}";
        when(httpClient.get(anyString())).thenReturn(responseEmptyRoutes);

        Location origin = new Location(38.7223, -9.1393);
        Location destination = new Location(41.1579, -8.6291);

        assertThrows(OSRMException.class, () -> 
            osrmService.calculateRoute(origin, destination, TransportMode.DRIVING),
            "Deve lançar exceção quando array de rotas está vazio");
    }

    @Test
    void testMissingDistanceField() throws IOException {
        // Teste com resposta sem campo "distance"
        String responseWithoutDistance = "{\"code\":\"Ok\",\"routes\":[{\"duration\":300.0,\"geometry\":\"_p~iF~ps|U_ulLnnqC_mqNvxq`@\"}]}";
        when(httpClient.get(anyString())).thenReturn(responseWithoutDistance);

        Location origin = new Location(38.7223, -9.1393);
        Location destination = new Location(41.1579, -8.6291);

        assertThrows(OSRMException.class, () -> 
            osrmService.calculateRoute(origin, destination, TransportMode.DRIVING),
            "Deve lançar exceção quando campo 'distance' está ausente");
    }

    @Test
    void testNegativeDistance() throws IOException {
        // Teste com resposta com distância negativa
        String responseNegativeDistance = "{\"code\":\"Ok\",\"routes\":[{\"distance\":-1000.0,\"duration\":300.0,\"geometry\":\"_p~iF~ps|U_ulLnnqC_mqNvxq`@\"}]}";
        when(httpClient.get(anyString())).thenReturn(responseNegativeDistance);

        Location origin = new Location(38.7223, -9.1393);
        Location destination = new Location(41.1579, -8.6291);

        assertThrows(OSRMException.class, () -> 
            osrmService.calculateRoute(origin, destination, TransportMode.DRIVING),
            "Deve lançar exceção quando distância é negativa");
    }

    @Test
    void testErrorCodeHandling() throws IOException {
        // Teste com diferentes códigos de erro
        String responseNoRoute = "{\"code\":\"NoRoute\",\"message\":\"No route found\"}";
        when(httpClient.get(anyString())).thenReturn(responseNoRoute);

        Location origin = new Location(38.7223, -9.1393);
        Location destination = new Location(41.1579, -8.6291);

        OSRMException exception = assertThrows(OSRMException.class, () -> 
            osrmService.calculateRoute(origin, destination, TransportMode.DRIVING));
        
        String errorMessage = exception.getMessage();
        // A mensagem deve conter o código de erro ou a mensagem da API
        assertTrue(errorMessage.contains("NoRoute") || 
                   errorMessage.contains("No route found") || 
                   errorMessage.contains("Erro na resposta da API OSRM"),
            "Mensagem de erro deve conter informações sobre o erro: " + errorMessage);
    }
}