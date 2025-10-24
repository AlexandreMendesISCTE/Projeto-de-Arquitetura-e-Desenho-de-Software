package pt.iscteiul.maprouteexplorer.util;

import pt.iscteiul.maprouteexplorer.model.Location;
import pt.iscteiul.maprouteexplorer.model.Route;
import pt.iscteiul.maprouteexplorer.model.TransportMode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Testes unitários para a classe RouteUtils.
 * 
 * Esta classe contém testes para validar o comportamento dos métodos
 * utilitários da classe RouteUtils, incluindo validações, cálculos
 * de distância e formatações.
 * 
 * @author Alexandre Mendes
 * @version 1.0.0
 * @since 1.0.0
 */
class RouteUtilsTest {
    
    private List<Location> validWaypoints;
    private List<Location> invalidWaypoints;
    private Location validLocation;
    private Location invalidLocation;
    
    /**
     * Configuração inicial para cada teste.
     */
    @BeforeEach
    void setUp() {
        validLocation = new Location(38.7223, -9.1393, "Lisboa", "Lisboa, Portugal");
        invalidLocation = new Location(200.0, 200.0, "Inválida", "Coordenadas inválidas");
        
        validWaypoints = Arrays.asList(
            new Location(38.7223, -9.1393, "Lisboa", "Lisboa, Portugal"),
            new Location(40.4168, -3.7038, "Madrid", "Madrid, Espanha"),
            new Location(41.9028, 12.4964, "Roma", "Roma, Itália")
        );
        
        invalidWaypoints = Arrays.asList(
            new Location(200.0, 200.0, "Inválida 1", "Coordenadas inválidas"),
            new Location(38.7223, -9.1393, "Válida", "Coordenadas válidas")
        );
    }
    
    /**
     * Testa a validação de rotas válidas.
     */
    @Test
    void testIsValidRouteWithValidRoute() {
        assertTrue(RouteUtils.isValidRoute(validWaypoints), 
                  "Rota com pontos válidos deve ser válida");
    }
    
    /**
     * Testa a validação de rotas com lista nula.
     */
    @Test
    void testIsValidRouteWithNullList() {
        assertFalse(RouteUtils.isValidRoute(null), 
                   "Lista nula deve ser inválida");
    }
    
    /**
     * Testa a validação de rotas com lista vazia.
     */
    @Test
    void testIsValidRouteWithEmptyList() {
        assertFalse(RouteUtils.isValidRoute(new ArrayList<>()), 
                   "Lista vazia deve ser inválida");
    }
    
    /**
     * Testa a validação de rotas com apenas um ponto.
     */
    @Test
    void testIsValidRouteWithSinglePoint() {
        List<Location> singlePoint = Arrays.asList(validLocation);
        assertFalse(RouteUtils.isValidRoute(singlePoint), 
                   "Rota com apenas um ponto deve ser inválida");
    }
    
    /**
     * Testa a validação de rotas com pontos inválidos.
     */
    @Test
    void testIsValidRouteWithInvalidPoints() {
        assertFalse(RouteUtils.isValidRoute(invalidWaypoints), 
                   "Rota com pontos inválidos deve ser inválida");
    }
    
    /**
     * Testa a validação de localizações válidas.
     */
    @Test
    void testIsValidLocationWithValidLocation() {
        assertTrue(RouteUtils.isValidLocation(validLocation), 
                  "Localização válida deve ser válida");
    }
    
    /**
     * Testa a validação de localizações nulas.
     */
    @Test
    void testIsValidLocationWithNullLocation() {
        assertFalse(RouteUtils.isValidLocation(null), 
                   "Localização nula deve ser inválida");
    }
    
    /**
     * Testa a validação de localizações com coordenadas inválidas.
     */
    @Test
    void testIsValidLocationWithInvalidCoordinates() {
        assertFalse(RouteUtils.isValidLocation(invalidLocation), 
                   "Localização com coordenadas inválidas deve ser inválida");
    }
    
    /**
     * Testa o cálculo de distância total.
     */
    @Test
    void testCalculateTotalDistance() {
        double distance = RouteUtils.calculateTotalDistance(validWaypoints);
        
        assertTrue(distance > 0, "Distância total deve ser positiva");
        assertTrue(distance < 10000, "Distância total deve ser razoável");
    }
    
    /**
     * Testa o cálculo de distância com lista nula.
     */
    @Test
    void testCalculateTotalDistanceWithNullList() {
        double distance = RouteUtils.calculateTotalDistance(null);
        assertEquals(0.0, distance, "Distância com lista nula deve ser 0");
    }
    
    /**
     * Testa o cálculo de distância com lista vazia.
     */
    @Test
    void testCalculateTotalDistanceWithEmptyList() {
        double distance = RouteUtils.calculateTotalDistance(new ArrayList<>());
        assertEquals(0.0, distance, "Distância com lista vazia deve ser 0");
    }
    
    /**
     * Testa o cálculo de distância com apenas um ponto.
     */
    @Test
    void testCalculateTotalDistanceWithSinglePoint() {
        List<Location> singlePoint = Arrays.asList(validLocation);
        double distance = RouteUtils.calculateTotalDistance(singlePoint);
        assertEquals(0.0, distance, "Distância com um ponto deve ser 0");
    }
    
    /**
     * Testa a estimativa de tempo de viagem.
     */
    @Test
    void testEstimateTravelTime() {
        double distance = 1000; // 1 km
        double time = RouteUtils.estimateTravelTime(distance, TransportMode.DRIVING);
        
        assertTrue(time > 0, "Tempo estimado deve ser positivo");
        assertTrue(time < 3600, "Tempo estimado deve ser razoável");
    }
    
    /**
     * Testa a estimativa de tempo com distância zero.
     */
    @Test
    void testEstimateTravelTimeWithZeroDistance() {
        double time = RouteUtils.estimateTravelTime(0, TransportMode.DRIVING);
        assertEquals(0.0, time, "Tempo com distância zero deve ser 0");
    }
    
    /**
     * Testa a estimativa de tempo com modo de transporte nulo.
     */
    @Test
    void testEstimateTravelTimeWithNullTransportMode() {
        double time = RouteUtils.estimateTravelTime(1000, null);
        assertEquals(0.0, time, "Tempo com modo nulo deve ser 0");
    }
    
    /**
     * Testa a conversão de metros para quilómetros.
     */
    @Test
    void testMetersToKilometers() {
        double meters = 1000;
        double kilometers = RouteUtils.metersToKilometers(meters);
        assertEquals(1.0, kilometers, 0.001, "1000 metros devem ser 1 km");
    }
    
    /**
     * Testa a conversão de quilómetros para milhas.
     */
    @Test
    void testKilometersToMiles() {
        double kilometers = 1.0;
        double miles = RouteUtils.kilometersToMiles(kilometers);
        assertTrue(miles > 0.6 && miles < 0.7, "1 km deve ser aproximadamente 0.62 milhas");
    }
    
    /**
     * Testa a conversão de segundos para horas.
     */
    @Test
    void testSecondsToHours() {
        double seconds = 3600;
        double hours = RouteUtils.secondsToHours(seconds);
        assertEquals(1.0, hours, 0.001, "3600 segundos devem ser 1 hora");
    }
    
    /**
     * Testa a formatação de distância em metros.
     */
    @Test
    void testFormatDistanceInMeters() {
        String formatted = RouteUtils.formatDistance(500);
        assertTrue(formatted.contains("500 m"), "Distância de 500m deve conter '500 m'");
    }
    
    /**
     * Testa a formatação de distância em quilómetros.
     */
    @Test
    void testFormatDistanceInKilometers() {
        String formatted = RouteUtils.formatDistance(1500);
        assertTrue(formatted.contains("1.50 km"), "Distância de 1500m deve conter '1.50 km'");
    }
    
    /**
     * Testa a formatação de duração em segundos.
     */
    @Test
    void testFormatDurationInSeconds() {
        String formatted = RouteUtils.formatDuration(30);
        assertTrue(formatted.contains("30 s"), "Duração de 30s deve conter '30 s'");
    }
    
    /**
     * Testa a formatação de duração em minutos.
     */
    @Test
    void testFormatDurationInMinutes() {
        String formatted = RouteUtils.formatDuration(90);
        assertTrue(formatted.contains("1 min"), "Duração de 90s deve conter '1 min'");
    }
    
    /**
     * Testa a formatação de duração em horas.
     */
    @Test
    void testFormatDurationInHours() {
        String formatted = RouteUtils.formatDuration(3661);
        assertTrue(formatted.contains("1 h"), "Duração de 3661s deve conter '1 h'");
    }
    
    /**
     * Testa o cálculo do centroide.
     */
    @Test
    void testCalculateCentroid() {
        Location centroid = RouteUtils.calculateCentroid(validWaypoints);
        
        assertNotNull(centroid, "Centroide não deve ser nulo");
        assertTrue(centroid.getLatitude() > 38 && centroid.getLatitude() < 42, 
                  "Latitude do centroide deve estar no intervalo esperado");
        assertTrue(centroid.getLongitude() > -10 && centroid.getLongitude() < 15, 
                  "Longitude do centroide deve estar no intervalo esperado");
    }
    
    /**
     * Testa o cálculo do centroide com lista nula.
     */
    @Test
    void testCalculateCentroidWithNullList() {
        Location centroid = RouteUtils.calculateCentroid(null);
        assertEquals(0.0, centroid.getLatitude());
        assertEquals(0.0, centroid.getLongitude());
    }
    
    /**
     * Testa o cálculo do centroide com lista vazia.
     */
    @Test
    void testCalculateCentroidWithEmptyList() {
        Location centroid = RouteUtils.calculateCentroid(new ArrayList<>());
        assertEquals(0.0, centroid.getLatitude());
        assertEquals(0.0, centroid.getLongitude());
    }
}


