package pt.iscteiul.maprouteexplorer.model;

/**
 * Representa uma localização geográfica com coordenadas de latitude e longitude.
 * 
 * Esta classe encapsula as informações básicas de uma posição geográfica,
 * incluindo coordenadas e informações opcionais como nome e endereço.
 * 
 * @author Alexandre Mendes
 * @version 1.0.0
 * @since 1.0.0
 */
public class Location {
    
    /** Latitude da localização em graus decimais */
    private double latitude;
    
    /** Longitude da localização em graus decimais */
    private double longitude;
    
    /** Nome ou descrição da localização (opcional) */
    private String name;
    
    /** Endereço completo da localização (opcional) */
    private String address;
    
    /**
     * Construtor padrão que cria uma localização sem informações adicionais.
     */
    public Location() {
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.name = "";
        this.address = "";
    }
    
    /**
     * Construtor que cria uma localização com coordenadas específicas.
     * 
     * @param latitude latitude da localização
     * @param longitude longitude da localização
     */
    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = "";
        this.address = "";
    }
    
    /**
     * Construtor completo que cria uma localização com todas as informações.
     * 
     * @param latitude latitude da localização
     * @param longitude longitude da localização
     * @param name nome da localização
     * @param address endereço da localização
     */
    public Location(double latitude, double longitude, String name, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.address = address;
    }
    
    /**
     * Obtém a latitude da localização.
     * 
     * @return latitude em graus decimais
     */
    public double getLatitude() {
        return latitude;
    }
    
    /**
     * Define a latitude da localização.
     * 
     * @param latitude latitude em graus decimais
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    /**
     * Obtém a longitude da localização.
     * 
     * @return longitude em graus decimais
     */
    public double getLongitude() {
        return longitude;
    }
    
    /**
     * Define a longitude da localização.
     * 
     * @param longitude longitude em graus decimais
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    /**
     * Obtém o nome da localização.
     * 
     * @return nome da localização
     */
    public String getName() {
        return name;
    }
    
    /**
     * Define o nome da localização.
     * 
     * @param name nome da localização
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Obtém o endereço da localização.
     * 
     * @return endereço da localização
     */
    public String getAddress() {
        return address;
    }
    
    /**
     * Define o endereço da localização.
     * 
     * @param address endereço da localização
     */
    public void setAddress(String address) {
        this.address = address;
    }
    
    /**
     * Calcula a distância em quilómetros entre esta localização e outra.
     * 
     * @param other outra localização
     * @return distância em quilómetros
     */
    public double distanceTo(Location other) {
        // Fórmula de Haversine para calcular distância entre dois pontos
        final int R = 6371; // Raio da Terra em quilómetros
        
        double latDistance = Math.toRadians(other.latitude - this.latitude);
        double lonDistance = Math.toRadians(other.longitude - this.longitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * Retorna uma representação em string da localização.
     * 
     * @return string com as coordenadas da localização
     */
    @Override
    public String toString() {
        return String.format("Location{lat=%.6f, lon=%.6f, name='%s'}", 
                           latitude, longitude, name);
    }
    
    /**
     * Verifica se duas localizações são iguais baseado nas coordenadas.
     * 
     * @param obj objeto a comparar
     * @return true se as localizações forem iguais
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Location location = (Location) obj;
        return Double.compare(location.latitude, latitude) == 0 &&
               Double.compare(location.longitude, longitude) == 0;
    }
    
    /**
     * Gera código hash baseado nas coordenadas.
     * 
     * @return código hash da localização
     */
    @Override
    public int hashCode() {
        return Double.hashCode(latitude) + Double.hashCode(longitude);
    }
}


