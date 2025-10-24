package pt.iscteiul.maprouteexplorer.service;

import pt.iscteiul.maprouteexplorer.model.Location;

import java.io.IOException;
import java.util.List;

/**
 * Serviço para integração com a API Nominatim (OpenStreetMap).
 * 
 * Esta classe fornece métodos para geocodificação e pesquisa de locais
 * utilizando a API Nominatim, permitindo converter endereços em coordenadas
 * e vice-versa, além de pesquisar pontos de interesse.
 * 
 * @author Alexandre Mendes
 * @version 1.0.0
 * @since 1.0.0
 */
public class NominatimService {
    
    /** URL base da API Nominatim */
    private static final String NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org";
    
    /** Cliente HTTP para fazer requisições */
    private final HttpClientService httpClient;
    
    /** User-Agent para identificação nas requisições */
    private static final String USER_AGENT = "MapRouteExplorer/1.0.0";
    
    /**
     * Construtor que inicializa o serviço com um cliente HTTP.
     * 
     * @param httpClient cliente HTTP para requisições
     */
    public NominatimService(HttpClientService httpClient) {
        this.httpClient = httpClient;
    }
    
    /**
     * Converte um endereço em coordenadas geográficas (geocodificação).
     * 
     * @param address endereço a geocodificar
     * @return localização com coordenadas
     * @throws IOException se ocorrer erro na comunicação com a API
     * @throws NominatimException se a API retornar erro
     */
    public Location geocode(String address) throws IOException, NominatimException {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Endereço não pode ser nulo ou vazio");
        }
        
        String encodedAddress = java.net.URLEncoder.encode(address, "UTF-8");
        String url = String.format("%s/search?q=%s&format=json&limit=1", 
                                 NOMINATIM_BASE_URL, encodedAddress);
        
        String response = httpClient.get(url);
        return parseGeocodeResponse(response);
    }
    
    /**
     * Converte coordenadas geográficas em endereço (geocodificação reversa).
     * 
     * @param location localização com coordenadas
     * @return endereço formatado
     * @throws IOException se ocorrer erro na comunicação com a API
     * @throws NominatimException se a API retornar erro
     */
    public String reverseGeocode(Location location) throws IOException, NominatimException {
        if (location == null) {
            throw new IllegalArgumentException("Localização não pode ser nula");
        }
        
        String url = String.format("%s/reverse?lat=%.6f&lon=%.6f&format=json", 
                                 NOMINATIM_BASE_URL, location.getLatitude(), location.getLongitude());
        
        String response = httpClient.get(url);
        return parseReverseGeocodeResponse(response);
    }
    
    /**
     * Pesquisa locais por nome ou descrição.
     * 
     * @param query termo de pesquisa
     * @param limit número máximo de resultados
     * @return lista de localizações encontradas
     * @throws IOException se ocorrer erro na comunicação com a API
     * @throws NominatimException se a API retornar erro
     */
    public List<Location> searchPlaces(String query, int limit) throws IOException, NominatimException {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Termo de pesquisa não pode ser nulo ou vazio");
        }
        
        if (limit <= 0) {
            limit = 10; // Limite padrão
        }
        
        String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
        String url = String.format("%s/search?q=%s&format=json&limit=%d", 
                                 NOMINATIM_BASE_URL, encodedQuery, limit);
        
        String response = httpClient.get(url);
        return parseSearchResponse(response);
    }
    
    /**
     * Pesquisa pontos de interesse próximos a uma localização.
     * 
     * @param location localização de referência
     * @param radius raio de pesquisa em metros
     * @param amenity tipo de amenidade (opcional)
     * @return lista de pontos de interesse
     * @throws IOException se ocorrer erro na comunicação com a API
     * @throws NominatimException se a API retornar erro
     */
    public List<Location> searchNearbyPOIs(Location location, int radius, String amenity) 
            throws IOException, NominatimException {
        
        if (location == null) {
            throw new IllegalArgumentException("Localização não pode ser nula");
        }
        
        StringBuilder url = new StringBuilder();
        url.append(String.format("%s/search?lat=%.6f&lon=%.6f&radius=%d&format=json", 
                               NOMINATIM_BASE_URL, location.getLatitude(), location.getLongitude(), radius));
        
        if (amenity != null && !amenity.trim().isEmpty()) {
            url.append("&amenity=").append(java.net.URLEncoder.encode(amenity, "UTF-8"));
        }
        
        String response = httpClient.get(url.toString());
        return parseSearchResponse(response);
    }
    
    /**
     * Verifica se o serviço Nominatim está disponível.
     * 
     * @return true se o serviço estiver disponível
     */
    public boolean isServiceAvailable() {
        try {
            String testUrl = NOMINATIM_BASE_URL + "/search?q=test&format=json&limit=1";
            httpClient.get(testUrl);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Analisa a resposta JSON da geocodificação e converte para um objeto Location.
     * 
     * @param response resposta JSON da API
     * @return localização geocodificada
     * @throws NominatimException se a resposta não for válida
     */
    private Location parseGeocodeResponse(String response) throws NominatimException {
        // TODO: Implementar parsing do JSON da resposta Nominatim
        // Esta implementação seria feita com Jackson ou Gson
        
        if (response == null || response.trim().isEmpty()) {
            throw new NominatimException("Resposta vazia da API Nominatim");
        }
        
        // Implementação temporária
        return new Location(0.0, 0.0, "Localização não encontrada", "");
    }
    
    /**
     * Analisa a resposta JSON da geocodificação reversa.
     * 
     * @param response resposta JSON da API
     * @return endereço formatado
     * @throws NominatimException se a resposta não for válida
     */
    private String parseReverseGeocodeResponse(String response) throws NominatimException {
        // TODO: Implementar parsing do JSON para endereço
        // Esta implementação seria feita com Jackson ou Gson
        
        if (response == null || response.trim().isEmpty()) {
            throw new NominatimException("Resposta vazia da API Nominatim");
        }
        
        // Implementação temporária
        return "Endereço não encontrado";
    }
    
    /**
     * Analisa a resposta JSON da pesquisa de locais.
     * 
     * @param response resposta JSON da API
     * @return lista de localizações encontradas
     * @throws NominatimException se a resposta não for válida
     */
    private List<Location> parseSearchResponse(String response) throws NominatimException {
        // TODO: Implementar parsing do JSON para lista de localizações
        // Esta implementação seria feita com Jackson ou Gson
        
        if (response == null || response.trim().isEmpty()) {
            throw new NominatimException("Resposta vazia da API Nominatim");
        }
        
        // Implementação temporária
        return new java.util.ArrayList<>();
    }
}


