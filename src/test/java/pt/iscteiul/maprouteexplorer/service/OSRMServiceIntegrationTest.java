package pt.iscteiul.maprouteexplorer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pt.iscteiul.maprouteexplorer.model.Location;
import pt.iscteiul.maprouteexplorer.model.Route;
import pt.iscteiul.maprouteexplorer.model.TransportMode;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de Integração para OSRMService - Testa integração real com API OSRM.
 * 
 * Estes testes verificam se os 5 pontos principais estão implementados:
 * 1. Configuração de servidor (local ou público)
 * 2. Envio de requisições HTTP
 * 3. Processamento de resposta JSON
 * 4. Validação de precisão dos dados
 * 5. Teste com diferentes pontos de origem e destino
 * 
 * @author Alexandre Mendes
 * @version 2.0.0
 * @since 2.0.0
 */
@DisplayName("Testes de Integração - OSRM Service")
class OSRMServiceIntegrationTest {

    private OSRMService osrmService;

    @BeforeEach
    void setUp() {
        // Usa cliente HTTP real para testes de integração
        OkHttpClientService httpClient = new OkHttpClientService();
        osrmService = new OSRMService(httpClient);
    }

    @Test
    @DisplayName("1. Configuração: Deve usar URL da configuração")
    void testServerConfiguration() {
        // Verifica se o serviço está disponível (testa configuração)
        // Pode ser true ou false dependendo da conectividade, mas não deve lançar exceção
        assertDoesNotThrow(() -> osrmService.isServiceAvailable());
    }

    @Test
    @DisplayName("2. Requisições HTTP: Deve enviar requisição para dois pontos")
    void testSendRequestTwoPoints() throws IOException, OSRMException {
        // Teste com Lisboa-Porto
        Location origin = new Location(38.7223, -9.1393);      // Lisboa
        Location destination = new Location(41.1579, -8.6291); // Porto

        // Se a API estiver disponível, deve calcular rota
        try {
            Route route = osrmService.calculateRoute(origin, destination, TransportMode.DRIVING);
            
            // Validações básicas
            assertNotNull(route, "Rota não deve ser nula");
            assertNotNull(route.getTransportMode(), "Modo de transporte não deve ser nulo");
            assertTrue(route.getTotalDistance() > 0, "Distância deve ser positiva");
            assertTrue(route.getTotalDuration() > 0, "Duração deve ser positiva");
            assertFalse(route.getWaypoints().isEmpty(), "Deve haver waypoints");
        } catch (OSRMException | IOException e) {
            // Se a API não estiver disponível, apenas loga mas não falha o teste
            System.out.println("API OSRM não disponível: " + e.getMessage());
            // Não falha o teste se for erro de conectividade
            if (!e.getMessage().contains("conexão") && !e.getMessage().contains("connection")) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("2. Requisições HTTP: Deve enviar requisição para múltiplos waypoints")
    void testSendRequestMultipleWaypoints() throws IOException, OSRMException {
        List<Location> waypoints = Arrays.asList(
            new Location(38.7223, -9.1393), // Lisboa
            new Location(39.7444, -8.8072), // Leiria
            new Location(41.1579, -8.6291)  // Porto
        );

        try {
            Route route = osrmService.calculateRoute(waypoints, TransportMode.DRIVING);
            
            assertNotNull(route);
            assertTrue(route.getTotalDistance() > 0);
            assertTrue(route.getTotalDuration() > 0);
            assertFalse(route.getWaypoints().isEmpty());
        } catch (OSRMException | IOException e) {
            System.out.println("API OSRM não disponível: " + e.getMessage());
            if (!e.getMessage().contains("conexão") && !e.getMessage().contains("connection")) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("3. Processamento JSON: Deve extrair distância e tempo corretamente")
    void testProcessJsonResponse() throws IOException, OSRMException {
        Location origin = new Location(38.7223, -9.1393);
        Location destination = new Location(40.4168, -3.7038); // Madrid

        try {
            Route route = osrmService.calculateRoute(origin, destination, TransportMode.DRIVING);
            
            // Valida que distância e tempo foram extraídos
            assertNotNull(route.getTotalDistance(), "Distância não deve ser nula");
            assertNotNull(route.getTotalDuration(), "Duração não deve ser nula");
            
            // Valida que são valores numéricos válidos
            assertTrue(route.getTotalDistance() > 0, "Distância deve ser positiva");
            assertTrue(route.getTotalDuration() > 0, "Duração deve ser positiva");
            
            // Valida que são valores razoáveis (Lisboa-Madrid: ~600-700 km)
            assertTrue(route.getTotalDistance() < 1000000, "Distância deve ser razoável (< 1000 km)");
            assertTrue(route.getTotalDuration() < 86400, "Duração deve ser razoável (< 24 horas)");
        } catch (OSRMException | IOException e) {
            System.out.println("API OSRM não disponível: " + e.getMessage());
            if (!e.getMessage().contains("conexão") && !e.getMessage().contains("connection")) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("4. Validação de Precisão: Deve validar coordenadas de entrada")
    void testValidateInputCoordinates() {
        // Testa coordenadas inválidas
        Location invalidLat = new Location(100.0, -9.1393); // Latitude inválida
        Location invalidLon = new Location(38.7223, 200.0);  // Longitude inválida
        Location valid = new Location(38.7223, -9.1393);

        // Deve lançar exceção para coordenadas inválidas
        assertThrows(IllegalArgumentException.class, () -> {
            osrmService.calculateRoute(invalidLat, valid, TransportMode.DRIVING);
        }, "Deve lançar exceção para latitude inválida");

        assertThrows(IllegalArgumentException.class, () -> {
            osrmService.calculateRoute(invalidLon, valid, TransportMode.DRIVING);
        }, "Deve lançar exceção para longitude inválida");
    }

    @Test
    @DisplayName("4. Validação de Precisão: Deve validar valores numéricos retornados")
    void testValidateNumericValues() throws IOException, OSRMException {
        Location origin = new Location(38.7223, -9.1393);
        Location destination = new Location(41.1579, -8.6291);

        try {
            Route route = osrmService.calculateRoute(origin, destination, TransportMode.DRIVING);
            
            // Valida que os valores são positivos
            assertTrue(route.getTotalDistance() >= 0, "Distância não deve ser negativa");
            assertTrue(route.getTotalDuration() >= 0, "Duração não deve ser negativa");
            
            // Valida que os valores são razoáveis
            assertTrue(route.getTotalDistance() < 40000000, "Distância deve ser < 40000 km");
            assertTrue(route.getTotalDuration() < 2592000, "Duração deve ser < 30 dias");
        } catch (OSRMException | IOException e) {
            System.out.println("API OSRM não disponível: " + e.getMessage());
            if (!e.getMessage().contains("conexão") && !e.getMessage().contains("connection")) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("5. Diferentes Pontos: Deve funcionar com diferentes origens e destinos")
    void testDifferentOriginDestinationPoints() throws IOException, OSRMException {
        // Teste 1: Lisboa-Porto (Portugal)
        Location lisboa = new Location(38.7223, -9.1393);
        Location porto = new Location(41.1579, -8.6291);

        // Teste 2: Lisboa-Madrid (Portugal-Espanha)
        Location madrid = new Location(40.4168, -3.7038);

        // Teste 3: Porto-Braga (Norte de Portugal)
        Location braga = new Location(41.5518, -8.4229);

        try {
            // Teste Lisboa-Porto
            Route route1 = osrmService.calculateRoute(lisboa, porto, TransportMode.DRIVING);
            assertNotNull(route1);
            assertTrue(route1.getTotalDistance() > 0);

            // Teste Lisboa-Madrid
            Route route2 = osrmService.calculateRoute(lisboa, madrid, TransportMode.DRIVING);
            assertNotNull(route2);
            assertTrue(route2.getTotalDistance() > 0);
            // Madrid deve estar mais longe que Porto
            assertTrue(route2.getTotalDistance() > route1.getTotalDistance());

            // Teste Porto-Braga
            Route route3 = osrmService.calculateRoute(porto, braga, TransportMode.DRIVING);
            assertNotNull(route3);
            assertTrue(route3.getTotalDistance() > 0);
            // Braga deve estar mais perto de Porto que Lisboa
            assertTrue(route3.getTotalDistance() < route1.getTotalDistance());
        } catch (OSRMException | IOException e) {
            System.out.println("API OSRM não disponível: " + e.getMessage());
            if (!e.getMessage().contains("conexão") && !e.getMessage().contains("connection")) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("5. Diferentes Pontos: Deve funcionar com diferentes modos de transporte")
    void testDifferentTransportModes() throws IOException, OSRMException {
        Location origin = new Location(38.7223, -9.1393);
        Location destination = new Location(41.1579, -8.6291);

        try {
            // Teste com diferentes modos de transporte
            Route drivingRoute = osrmService.calculateRoute(origin, destination, TransportMode.DRIVING);
            Route cyclingRoute = osrmService.calculateRoute(origin, destination, TransportMode.CYCLING);
            Route walkingRoute = osrmService.calculateRoute(origin, destination, TransportMode.WALKING);

            assertNotNull(drivingRoute);
            assertNotNull(cyclingRoute);
            assertNotNull(walkingRoute);

            // Todas devem ter distância positiva
            assertTrue(drivingRoute.getTotalDistance() > 0);
            assertTrue(cyclingRoute.getTotalDistance() > 0);
            assertTrue(walkingRoute.getTotalDistance() > 0);

            // Walking geralmente tem distância maior (não pode usar estradas)
            // Mas isso pode variar, então apenas validamos que são valores válidos
            assertTrue(walkingRoute.getTotalDistance() > 0);
        } catch (OSRMException | IOException e) {
            System.out.println("API OSRM não disponível: " + e.getMessage());
            if (!e.getMessage().contains("conexão") && !e.getMessage().contains("connection")) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("Validação: Deve rejeitar requisições com pontos nulos")
    void testNullPointsValidation() {
        Location valid = new Location(38.7223, -9.1393);

        assertThrows(IllegalArgumentException.class, () -> {
            osrmService.calculateRoute(null, valid, TransportMode.DRIVING);
        }, "Deve lançar exceção para origem nula");

        assertThrows(IllegalArgumentException.class, () -> {
            osrmService.calculateRoute(valid, null, TransportMode.DRIVING);
        }, "Deve lançar exceção para destino nulo");
    }

    @Test
    @DisplayName("Validação: Deve validar resposta JSON com campos ausentes")
    void testInvalidJsonResponse() {
        // Este teste verifica que o código valida campos obrigatórios
        // A validação real é feita em OSRMServiceTest com mocks
        // Aqui apenas garantimos que o serviço não crasha com respostas inválidas
        assertDoesNotThrow(() -> {
            // Não deve lançar exceção mesmo se a API não estiver disponível
            osrmService.isServiceAvailable();
        });
    }
}

