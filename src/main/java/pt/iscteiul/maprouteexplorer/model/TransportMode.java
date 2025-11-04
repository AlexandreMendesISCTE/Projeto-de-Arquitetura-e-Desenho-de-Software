package pt.iscteiul.maprouteexplorer.model;

/**
 * Enumeração que define os modos de transporte disponíveis para cálculo de rotas.
 * 
 * Cada modo de transporte tem características específicas que afetam
 * o cálculo da rota, como velocidade máxima e restrições de acesso.
 * 
 * @author Alexandre Mendes
 * @version 1.0.0
 * @since 1.0.0
 */
public enum TransportMode {
    
    /**
     * Modo de transporte em automóvel.
     * Permite uso de todas as vias públicas e tem maior velocidade.
     * Perfil OSRM: "car" (conforme documentação oficial)
     */
    DRIVING("car", "Automóvel", 50.0),
    
    /**
     * Modo de transporte a pé.
     * Restrito a vias pedonais e tem menor velocidade.
     * Perfil OSRM: "foot" (conforme documentação oficial)
     */
    WALKING("foot", "A pé", 5.0),
    
    /**
     * Modo de transporte de bicicleta.
     * Permite ciclovias e vias com menor tráfego.
     * Perfil OSRM: "bike" (conforme documentação oficial)
     */
    CYCLING("bike", "Bicicleta", 15.0);
    
    /** Código utilizado pela API OSRM */
    private final String apiCode;
    
    /** Nome descritivo do modo de transporte */
    private final String displayName;
    
    /** Velocidade média em km/h */
    private final double averageSpeed;
    
    /**
     * Construtor do enum.
     * 
     * @param apiCode código da API
     * @param displayName nome para exibição
     * @param averageSpeed velocidade média em km/h
     */
    TransportMode(String apiCode, String displayName, double averageSpeed) {
        this.apiCode = apiCode;
        this.displayName = displayName;
        this.averageSpeed = averageSpeed;
    }
    
    /**
     * Obtém o código utilizado pela API OSRM.
     * 
     * @return código da API
     */
    public String getApiCode() {
        return apiCode;
    }
    
    /**
     * Obtém o nome descritivo do modo de transporte.
     * 
     * @return nome para exibição
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Obtém a velocidade média do modo de transporte.
     * 
     * @return velocidade média em km/h
     */
    public double getAverageSpeed() {
        return averageSpeed;
    }
    
    /**
     * Obtém o modo de transporte baseado no código da API.
     * 
     * @param apiCode código da API
     * @return modo de transporte correspondente
     * @throws IllegalArgumentException se o código não for válido
     */
    public static TransportMode fromApiCode(String apiCode) {
        for (TransportMode mode : values()) {
            if (mode.apiCode.equals(apiCode)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Código de API inválido: " + apiCode);
    }
    
    /**
     * Retorna uma representação em string do modo de transporte.
     * 
     * @return string com o nome descritivo
     */
    @Override
    public String toString() {
        return displayName;
    }
}


