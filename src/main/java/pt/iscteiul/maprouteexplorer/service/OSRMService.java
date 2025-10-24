package pt.iscteiul.maprouteexplorer.service;

import pt.iscteiul.maprouteexplorer.model.Location;
import pt.iscteiul.maprouteexplorer.model.Route;
import pt.iscteiul.maprouteexplorer.model.TransportMode;

import java.io.IOException;
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
        // TODO: Implementar parsing do JSON da resposta OSRM
        // Esta implementação seria feita com Jackson ou Gson
        
        Route route = new Route();
        route.setTransportMode(transportMode);
        
        // Implementação temporária - seria substituída pelo parsing real
        if (response == null || response.trim().isEmpty()) {
            throw new OSRMException("Resposta vazia da API OSRM");
        }
        
        return route;
    }
    
    /**
     * Analisa a resposta JSON para obter apenas distância e tempo.
     * 
     * @param response resposta JSON da API
     * @return array com [distância em metros, tempo em segundos]
     * @throws OSRMException se a resposta não for válida
     */
    private double[] parseDistanceAndDurationResponse(String response) throws OSRMException {
        // TODO: Implementar parsing do JSON para distância e tempo
        // Esta implementação seria feita com Jackson ou Gson
        
        if (response == null || response.trim().isEmpty()) {
            throw new OSRMException("Resposta vazia da API OSRM");
        }
        
        // Implementação temporária
        return new double[]{0.0, 0.0};
    }
}


