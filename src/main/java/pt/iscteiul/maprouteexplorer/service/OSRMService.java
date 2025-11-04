package pt.iscteiul.maprouteexplorer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pt.iscteiul.maprouteexplorer.model.Location;
import pt.iscteiul.maprouteexplorer.model.Route;
import pt.iscteiul.maprouteexplorer.model.TransportMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    
    /** URL base da API OSRM */
    private static final String OSRM_BASE_URL = "http://router.project-osrm.org/route/v1";
    
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
     * Calcula uma rota entre dois pontos.
     * 
     * @param origin ponto de origem
     * @param destination ponto de destino
     * @param transportMode modo de transporte
     * @return rota calculada
     * @throws IOException se ocorrer erro na comunicação com a API
     * @throws OSRMException se a API retornar erro
     */
    public Route calculateRoute(Location origin, Location destination, TransportMode transportMode) 
            throws IOException, OSRMException {
        
        if (origin == null || destination == null) {
            throw new IllegalArgumentException("Origem e destino não podem ser nulos");
        }
        
        String coordinates = String.format("%.6f,%.6f;%.6f,%.6f", 
                                         origin.getLongitude(), origin.getLatitude(),
                                         destination.getLongitude(), destination.getLatitude());
        
        String url = String.format("%s/%s/%s?overview=full&steps=true", 
                                 OSRM_BASE_URL, transportMode.getApiCode(), coordinates);
        
        String response = httpClient.get(url);
        return parseRouteResponse(response, transportMode);
    }
    
    /**
     * Calcula uma rota entre múltiplos pontos (waypoints).
     * 
     * @param waypoints lista de pontos da rota
     * @param transportMode modo de transporte
     * @return rota calculada
     * @throws IOException se ocorrer erro na comunicação com a API
     * @throws OSRMException se a API retornar erro
     */
    public Route calculateRoute(List<Location> waypoints, TransportMode transportMode) 
            throws IOException, OSRMException {
        
        if (waypoints == null || waypoints.size() < 2) {
            throw new IllegalArgumentException("É necessário pelo menos 2 pontos para calcular uma rota");
        }
        
        StringBuilder coordinates = new StringBuilder();
        for (int i = 0; i < waypoints.size(); i++) {
            Location point = waypoints.get(i);
            coordinates.append(String.format("%.6f,%.6f", point.getLongitude(), point.getLatitude()));
            if (i < waypoints.size() - 1) {
                coordinates.append(";");
            }
        }
        
        String url = String.format("%s/%s/%s?overview=full&steps=true", 
                                 OSRM_BASE_URL, transportMode.getApiCode(), coordinates.toString());
        
        String response = httpClient.get(url);
        return parseRouteResponse(response, transportMode);
    }
    
    /**
     * Calcula a distância e tempo entre dois pontos sem retornar a rota completa.
     * 
     * @param origin ponto de origem
     * @param destination ponto de destino
     * @param transportMode modo de transporte
     * @return array com [distância, tempo]
     * @throws IOException se ocorrer erro na comunicação com a API
     * @throws OSRMException se a API retornar erro
     */
    public double[] calculateDistanceAndDuration(Location origin, Location destination, TransportMode transportMode) 
            throws IOException, OSRMException {
        
        String coordinates = String.format("%.6f,%.6f;%.6f,%.6f", 
                                         origin.getLongitude(), origin.getLatitude(),
                                         destination.getLongitude(), destination.getLatitude());
        
        String url = String.format("%s/%s/%s?overview=false", 
                                 OSRM_BASE_URL, transportMode.getApiCode(), coordinates);
        
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
            String testUrl = OSRM_BASE_URL + "/driving/0,0;1,1?overview=false";
            httpClient.get(testUrl);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Analisa a resposta JSON da API OSRM e converte para um objeto Route.
     * 
     * @param response resposta JSON da API
     * @param transportMode modo de transporte utilizado
     * @return rota calculada
     * @throws OSRMException se a resposta não for válida
     */
    private Route parseRouteResponse(String response, TransportMode transportMode) throws OSRMException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            
            // Verifica o status da resposta
            String status = root.get("code").asText();
            if (!"Ok".equals(status)) {
                throw new OSRMException("Erro na resposta da API OSRM: " + status);
            }
            
            // Obtém o primeiro resultado da rota
            JsonNode route = root.get("routes").get(0);
            
            // Cria o objeto Route
            Route result = new Route();
            result.setTransportMode(transportMode);
            result.setDistance(route.get("distance").asDouble());
            result.setDuration(route.get("duration").asDouble());
            
            // Parse das coordenadas da rota
            String geometry = route.get("geometry").asText();
            List<Location> coordinates = decodePolyline(geometry);
            result.setCoordinates(coordinates);
            
            return result;
        } catch (IOException e) {
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
     * @param response resposta JSON da API
     * @return array com [distância em metros, tempo em segundos]
     * @throws OSRMException se a resposta não for válida
     */
    private double[] parseDistanceAndDurationResponse(String response) throws OSRMException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            
            // Verifica o status da resposta
            String status = root.get("code").asText();
            if (!"Ok".equals(status)) {
                throw new OSRMException("Erro na resposta da API OSRM: " + status);
            }
            
            // Obtém o primeiro resultado da rota
            JsonNode route = root.get("routes").get(0);
            
            // Retorna array com [distância em metros, tempo em segundos]
            return new double[]{
                route.get("distance").asDouble(),
                route.get("duration").asDouble()
            };
            
        } catch (IOException e) {
            throw new OSRMException("Erro ao processar resposta da API OSRM", e);
        }
    }
}


