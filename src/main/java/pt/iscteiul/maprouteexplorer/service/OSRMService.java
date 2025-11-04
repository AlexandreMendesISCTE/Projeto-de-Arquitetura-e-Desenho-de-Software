package pt.iscteiul.maprouteexplorer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pt.iscteiul.maprouteexplorer.model.Location;
import pt.iscteiul.maprouteexplorer.model.Route;
import pt.iscteiul.maprouteexplorer.model.TransportMode;
import pt.iscteiul.maprouteexplorer.util.ConfigManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Serviço para integração com a API OSRM (Open Source Routing Machine).
 * 
 * Esta classe fornece métodos para calcular rotas, distâncias e tempos
 * de viagem utilizando a API OSRM. Suporta diferentes modos de transporte
 * e permite cálculos de rotas entre múltiplos pontos.
 * 
 * @author Alexandre Mendes
 * @version 1.0.0
 * @since 1.0.0
 */
public class OSRMService {

    /** Cliente HTTP para fazer requisições */
    private final HttpClientService httpClient;

    /**
     * Construtor que inicializa o serviço com um cliente HTTP.
     * 
     * @param httpClient cliente HTTP para requisições
     */
    public OSRMService(HttpClientService httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Obtém a URL base da API OSRM a partir da configuração.
     * Permite configurar servidor local via application.properties.
     * 
     * @return URL base da API OSRM
     */
    private String getOSRMBaseUrl() {
        return ConfigManager.getOSRMBaseUrl();
    }

    /**
     * Valida se as coordenadas estão dentro dos limites válidos.
     * 
     * @param location localização a validar
     * @throws IllegalArgumentException se as coordenadas forem inválidas
     */
    private void validateCoordinates(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Localização não pode ser nula");
        }

        double lat = location.getLatitude();
        double lon = location.getLongitude();

        if (lat < -90 || lat > 90) {
            throw new IllegalArgumentException(
                    String.format("Latitude inválida: %.6f (deve estar entre -90 e 90)", lat));
        }

        if (lon < -180 || lon > 180) {
            throw new IllegalArgumentException(
                    String.format("Longitude inválida: %.6f (deve estar entre -180 e 180)", lon));
        }
    }

    /**
     * Calcula uma rota entre dois pontos.
     * 
     * @param origin        ponto de origem
     * @param destination   ponto de destino
     * @param transportMode modo de transporte
     * @return rota calculada
     * @throws IOException   se ocorrer erro na comunicação com a API
     * @throws OSRMException se a API retornar erro
     */
    public Route calculateRoute(Location origin, Location destination, TransportMode transportMode)
            throws IOException, OSRMException {

        if (origin == null || destination == null) {
            throw new IllegalArgumentException("Origem e destino não podem ser nulos");
        }

        // Validar coordenadas antes de enviar
        validateCoordinates(origin);
        validateCoordinates(destination);

        String coordinates = String.format(Locale.US, "%.6f,%.6f;%.6f,%.6f",
                origin.getLongitude(), origin.getLatitude(),
                destination.getLongitude(), destination.getLatitude());

        String url = String.format("%s/%s/%s?overview=full&steps=true",
                getOSRMBaseUrl(), transportMode.getApiCode(), coordinates);

        // Log para depuração: verificar se o modo de transporte está sendo usado
        // corretamente
        System.out.println("DEBUG: Calculando rota com modo de transporte: " + transportMode.getDisplayName() +
                " (código API: " + transportMode.getApiCode() + ")");
        System.out.println("DEBUG: URL da API: " + url);

        // NOTA: A API pública do OSRM (router.project-osrm.org) pode não suportar todos
        // os perfis
        // de transporte (walking, cycling) e pode retornar sempre valores de "driving".
        // Para suporte completo, considere usar um servidor OSRM local.

        String response = httpClient.get(url);
        Route route = parseRouteResponse(response, transportMode);

        // Log para depuração: verificar os valores retornados
        System.out.println("DEBUG: Distância obtida: " + route.getTotalDistance() + " m");
        System.out.println("DEBUG: Duração obtida: " + route.getTotalDuration() + " s");

        // Verificar se a duração parece ser de carro mesmo quando o modo é diferente
        // (heuristic para detectar problema da API pública)
        if (transportMode != TransportMode.DRIVING) {
            double distanceKm = route.getTotalDistance() / 1000.0;
            double expectedDuration = (distanceKm / transportMode.getAverageSpeed()) * 3600;
            double actualDuration = route.getTotalDuration();

            // Calcular duração esperada para carro (50 km/h) para comparação
            double expectedCarDuration = (distanceKm / 50.0) * 3600;
            double ratioToExpected = actualDuration / expectedDuration;
            double ratioToCar = actualDuration / expectedCarDuration;

            // Para bicicleta (15 km/h vs 50 km/h), a duração deveria ser ~3.3x maior que
            // carro
            // Para a pé (5 km/h vs 50 km/h), a duração deveria ser ~10x maior que carro
            // Se a duração retornada está muito próxima da duração de carro (dentro de
            // 50%),
            // mas deveria ser muito diferente, então a API provavelmente está retornando
            // valores de carro
            boolean tooCloseToCar = Math.abs(ratioToCar - 1.0) < 0.5;

            // Para bicicleta (15 km/h vs 50 km/h), a duração deveria ser ~3.3x maior que
            // carro
            // Se ratioToCar < 2.5, significa que está muito perto do carro (deveria ser
            // ~3.3x)
            // Para a pé (5 km/h vs 50 km/h), a duração deveria ser ~10x maior que carro
            // Se ratioToCar < 7.0, significa que está muito perto do carro (deveria ser
            // ~10x)
            boolean shouldBeMuchDifferent = (transportMode == TransportMode.CYCLING && ratioToCar < 2.5) ||
                    (transportMode == TransportMode.WALKING && ratioToCar < 7.0);

            // Se está muito próxima do carro OU muito longe do esperado, é problema
            if (tooCloseToCar || shouldBeMuchDifferent || ratioToExpected < 0.4 || ratioToExpected > 2.5) {
                System.out.println("WARNING: A duração retornada (" + actualDuration
                        + "s) pode não corresponder ao modo " +
                        transportMode.getDisplayName() + ". Esperado aproximadamente " + expectedDuration + "s.");
                System.out.println(
                        "WARNING: Duração de carro esperada: " + expectedCarDuration + "s. Ratio: " + ratioToCar);
                System.out.println(
                        "WARNING: Ratio esperado para " + transportMode.getDisplayName() + ": " + ratioToExpected);
                System.out.println("WARNING: shouldBeMuchDifferent: " + shouldBeMuchDifferent + ", tooCloseToCar: "
                        + tooCloseToCar);
            }
        }

        return route;
    }

    /**
     * Calcula uma rota entre múltiplos pontos (waypoints).
     * 
     * @param waypoints     lista de pontos da rota
     * @param transportMode modo de transporte
     * @return rota calculada
     * @throws IOException   se ocorrer erro na comunicação com a API
     * @throws OSRMException se a API retornar erro
     */
    public Route calculateRoute(List<Location> waypoints, TransportMode transportMode)
            throws IOException, OSRMException {

        if (waypoints == null || waypoints.size() < 2) {
            throw new IllegalArgumentException("É necessário pelo menos 2 pontos para calcular uma rota");
        }

        // Validar todas as coordenadas antes de enviar
        for (Location point : waypoints) {
            validateCoordinates(point);
        }

        StringBuilder coordinates = new StringBuilder();
        for (int i = 0; i < waypoints.size(); i++) {
            Location point = waypoints.get(i);
            String lon = String.format(Locale.US, "%.6f", point.getLongitude());
            String lat = String.format(Locale.US, "%.6f", point.getLatitude());

            coordinates.append(lon).append(",").append(lat);
            if (i < waypoints.size() - 1) {
                coordinates.append(";");
            }
        }

        String url = String.format("%s/%s/%s?overview=full&steps=true",
                getOSRMBaseUrl(), transportMode.getApiCode(), coordinates.toString());

        // Log para depuração: verificar se o modo de transporte está sendo usado
        // corretamente
        System.out.println("DEBUG: Calculando rota com múltiplos waypoints - modo: " + transportMode.getDisplayName() +
                " (código API: " + transportMode.getApiCode() + ")");
        System.out.println("DEBUG: URL da API: " + url);

        String response = httpClient.get(url);
        Route route = parseRouteResponse(response, transportMode);

        // Log para depuração: verificar os valores retornados
        System.out.println("DEBUG: Distância obtida: " + route.getTotalDistance() + " m");
        System.out.println("DEBUG: Duração obtida: " + route.getTotalDuration() + " s");

        return route;
    }

    /**
     * Calcula a distância e tempo entre dois pontos sem retornar a rota completa.
     * 
     * @param origin        ponto de origem
     * @param destination   ponto de destino
     * @param transportMode modo de transporte
     * @return array com [distância, tempo]
     * @throws IOException   se ocorrer erro na comunicação com a API
     * @throws OSRMException se a API retornar erro
     */
    public double[] calculateDistanceAndDuration(Location origin, Location destination, TransportMode transportMode)
            throws IOException, OSRMException {

        if (origin == null || destination == null) {
            throw new IllegalArgumentException("Origem e destino não podem ser nulos");
        }

        // Validar coordenadas antes de enviar
        validateCoordinates(origin);
        validateCoordinates(destination);

        String coordinates = String.format(Locale.US, "%.6f,%.6f;%.6f,%.6f",
                origin.getLongitude(), origin.getLatitude(),
                destination.getLongitude(), destination.getLatitude());

        String url = String.format("%s/%s/%s?overview=false",
                getOSRMBaseUrl(), transportMode.getApiCode(), coordinates);

        String response = httpClient.get(url);
        return parseDistanceAndDurationResponse(response);
    }

    /**
     * Verifica se o serviço OSRM está disponível.
     * 
     * @return true se o serviço estiver disponível
     */
    public boolean isServiceAvailable() {
        try {
            // Usar perfil "car" conforme documentação oficial do OSRM
            String testUrl = getOSRMBaseUrl() + "/car/0,0;1,1?overview=false";
            httpClient.get(testUrl);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Analisa a resposta JSON da API OSRM e converte para um objeto Route.
     * 
     * Este método extrai os dados de distância e tempo diretamente da resposta JSON
     * da API OSRM:
     * - Extrai o campo "distance" do objeto "route" dentro do array "routes"
     * (distância total em metros)
     * - Extrai o campo "duration" do objeto "route" dentro do array "routes" (tempo
     * estimado em segundos)
     * - Valida os valores extraídos (não negativos, dentro de limites razoáveis)
     * - Atribui os valores ao objeto Route através dos métodos setTotalDistance() e
     * setTotalDuration()
     * 
     * @param response      resposta JSON da API OSRM
     * @param transportMode modo de transporte utilizado
     * @return rota calculada com distância e tempo extraídos da resposta da API
     * @throws OSRMException se a resposta não for válida ou se os campos de
     *                       distância/tempo estiverem ausentes
     */
    private Route parseRouteResponse(String response, TransportMode transportMode) throws OSRMException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            // Validação: Verificar se o campo "code" existe
            if (!root.has("code")) {
                throw new OSRMException("Resposta da API OSRM inválida: campo 'code' ausente");
            }

            // Verifica o status da resposta
            String status = root.get("code").asText();
            if (!"Ok".equals(status)) {
                // Melhor tratamento de códigos de erro
                String errorMessage = root.has("message") ? root.get("message").asText() : "Erro desconhecido";
                throw new OSRMException("Erro na resposta da API OSRM [" + status + "]: " + errorMessage);
            }

            // Validação: Verificar se o campo "routes" existe e não está vazio
            if (!root.has("routes")) {
                throw new OSRMException("Resposta da API OSRM inválida: campo 'routes' ausente");
            }

            JsonNode routesNode = root.get("routes");
            if (!routesNode.isArray() || routesNode.size() == 0) {
                throw new OSRMException("Resposta da API OSRM inválida: nenhuma rota encontrada");
            }

            // Obtém o primeiro resultado da rota
            JsonNode route = routesNode.get(0);

            // Validação: Verificar campos obrigatórios da rota
            if (!route.has("distance")) {
                throw new OSRMException("Resposta da API OSRM inválida: campo 'distance' ausente");
            }
            if (!route.has("duration")) {
                throw new OSRMException("Resposta da API OSRM inválida: campo 'duration' ausente");
            }
            if (!route.has("geometry")) {
                throw new OSRMException("Resposta da API OSRM inválida: campo 'geometry' ausente");
            }

            // ============================================
            // EXTRAÇÃO DE DADOS DE DISTÂNCIA E TEMPO
            // ============================================
            // Obtém os dados de distância e tempo diretamente da resposta JSON da API OSRM
            // - distance: distância total da rota em metros (double)
            // - duration: tempo estimado de viagem em segundos (double)
            // Estes valores são extraídos do campo "route" dentro do array "routes"
            double distance = route.get("distance").asDouble();
            double duration = route.get("duration").asDouble();

            if (distance < 0) {
                throw new OSRMException("Resposta da API OSRM inválida: distância negativa (" + distance + ")");
            }
            if (duration < 0) {
                throw new OSRMException("Resposta da API OSRM inválida: duração negativa (" + duration + ")");
            }

            // Validação: Verificar se os valores são razoáveis (distância máxima: 40000 km,
            // tempo máximo: 30 dias)
            if (distance > 40000000) {
                throw new OSRMException("Resposta da API OSRM inválida: distância muito grande (" + distance + " m)");
            }
            if (duration > 2592000) { // 30 dias em segundos
                throw new OSRMException("Resposta da API OSRM inválida: duração muito grande (" + duration + " s)");
            }

            // ============================================
            // ATRIBUIÇÃO DOS DADOS EXTRAÍDOS
            // ============================================
            // Cria o objeto Route e atribui os dados extraídos da API
            // - totalDistance: distância em metros obtida da API
            // - totalDuration: duração em segundos obtida da API
            Route result = new Route();
            result.setTransportMode(transportMode);
            result.setTotalDistance(distance); // Atribui distância extraída da resposta
            result.setTotalDuration(duration); // Atribui tempo extraído da resposta

            // Parse das coordenadas da rota
            String geometry = route.get("geometry").asText();
            if (geometry == null || geometry.isEmpty()) {
                throw new OSRMException("Resposta da API OSRM inválida: geometria vazia");
            }

            List<Location> coordinates = decodePolyline(geometry);

            // Validação: Verificar se as coordenadas decodificadas são válidas
            for (Location loc : coordinates) {
                if (loc.getLatitude() < -90 || loc.getLatitude() > 90 ||
                        loc.getLongitude() < -180 || loc.getLongitude() > 180) {
                    throw new OSRMException("Resposta da API OSRM inválida: coordenadas fora dos limites válidos");
                }
            }

            result.setWaypoints(coordinates);

            return result;
        } catch (OSRMException e) {
            // Re-lança OSRMException sem alterar
            throw e;
        } catch (JsonProcessingException e) {
            throw new OSRMException("Erro ao processar JSON da resposta da API OSRM", e);
        } catch (Exception e) {
            throw new OSRMException("Erro ao processar resposta da API OSRM", e);
        }
    }

    /**
     * Decodifica a string de geometria polyline para uma lista de coordenadas.
     * 
     * @param encoded string encoded polyline
     * @return lista de localizações
     */
    private List<Location> decodePolyline(String encoded) {
        List<Location> poly = new ArrayList<>();
        int index = 0;
        int len = encoded.length();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;

            // Decodifica latitude
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            // Decodifica longitude
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            Location location = new Location();
            location.setLatitude(lat * 1e-5);
            location.setLongitude(lng * 1e-5);
            poly.add(location);
        }

        return poly;
    }

    /**
     * Analisa a resposta JSON para obter apenas distância e tempo.
     * 
     * Este método extrai os dados de distância e tempo diretamente da resposta JSON
     * da API OSRM:
     * - Extrai o campo "distance" (distância total em metros)
     * - Extrai o campo "duration" (tempo estimado em segundos)
     * - Valida os valores extraídos
     * - Retorna um array com os valores [distância, tempo]
     * 
     * @param response resposta JSON da API OSRM
     * @return array com [distância em metros, tempo em segundos] extraídos da
     *         resposta
     * @throws OSRMException se a resposta não for válida
     */
    private double[] parseDistanceAndDurationResponse(String response) throws OSRMException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            // Validação: Verificar se o campo "code" existe
            if (!root.has("code")) {
                throw new OSRMException("Resposta da API OSRM inválida: campo 'code' ausente");
            }

            // Verifica o status da resposta
            String status = root.get("code").asText();
            if (!"Ok".equals(status)) {
                // Melhor tratamento de códigos de erro
                String errorMessage = root.has("message") ? root.get("message").asText() : "Erro desconhecido";
                throw new OSRMException("Erro na resposta da API OSRM [" + status + "]: " + errorMessage);
            }

            // Validação: Verificar se o campo "routes" existe e não está vazio
            if (!root.has("routes")) {
                throw new OSRMException("Resposta da API OSRM inválida: campo 'routes' ausente");
            }

            JsonNode routesNode = root.get("routes");
            if (!routesNode.isArray() || routesNode.size() == 0) {
                throw new OSRMException("Resposta da API OSRM inválida: nenhuma rota encontrada");
            }

            // Obtém o primeiro resultado da rota
            JsonNode route = routesNode.get(0);

            // Validação: Verificar campos obrigatórios
            if (!route.has("distance")) {
                throw new OSRMException("Resposta da API OSRM inválida: campo 'distance' ausente");
            }
            if (!route.has("duration")) {
                throw new OSRMException("Resposta da API OSRM inválida: campo 'duration' ausente");
            }

            // ============================================
            // EXTRAÇÃO DE DADOS DE DISTÂNCIA E TEMPO
            // ============================================
            // Obtém os dados de distância e tempo diretamente da resposta JSON da API OSRM
            // - distance: distância total da rota em metros (double)
            // - duration: tempo estimado de viagem em segundos (double)
            // Estes valores são extraídos do campo "route" dentro do array "routes"
            double distance = route.get("distance").asDouble();
            double duration = route.get("duration").asDouble();

            if (distance < 0) {
                throw new OSRMException("Resposta da API OSRM inválida: distância negativa (" + distance + ")");
            }
            if (duration < 0) {
                throw new OSRMException("Resposta da API OSRM inválida: duração negativa (" + duration + ")");
            }

            // Validação: Verificar se os valores são razoáveis
            if (distance > 40000000) {
                throw new OSRMException("Resposta da API OSRM inválida: distância muito grande (" + distance + " m)");
            }
            if (duration > 2592000) { // 30 dias em segundos
                throw new OSRMException("Resposta da API OSRM inválida: duração muito grande (" + duration + " s)");
            }

            // ============================================
            // RETORNO DOS DADOS EXTRAÍDOS
            // ============================================
            // Retorna array com [distância em metros, tempo em segundos]
            // Os dados foram extraídos diretamente da resposta JSON da API OSRM
            return new double[] { distance, duration };

        } catch (OSRMException e) {
            // Re-lança OSRMException sem alterar
            throw e;
        } catch (JsonProcessingException e) {
            throw new OSRMException("Erro ao processar JSON da resposta da API OSRM", e);
        } catch (Exception e) {
            throw new OSRMException("Erro ao processar resposta da API OSRM", e);
        }
    }
}
