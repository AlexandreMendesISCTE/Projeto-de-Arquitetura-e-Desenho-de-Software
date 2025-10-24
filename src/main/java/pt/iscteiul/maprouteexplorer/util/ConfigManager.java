package pt.iscteiul.maprouteexplorer.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Gestor de configurações da aplicação.
 * 
 * Esta classe fornece métodos para carregar e aceder a configurações
 * da aplicação a partir de ficheiros de propriedades, com valores
 * padrão para configurações essenciais.
 * 
 * @author Alexandre Mendes
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConfigManager {
    
    /** Logger para esta classe */
    private static final Logger logger = Logger.getLogger(ConfigManager.class.getName());
    
    /** Nome do ficheiro de configuração */
    private static final String CONFIG_FILE = "application.properties";
    
    /** Propriedades carregadas */
    private static Properties properties;
    
    /** Flag indicando se as configurações foram carregadas */
    private static boolean loaded = false;
    
    // Chaves de configuração
    private static final String OSRM_BASE_URL_KEY = "osrm.base.url";
    private static final String NOMINATIM_BASE_URL_KEY = "nominatim.base.url";
    private static final String HTTP_TIMEOUT_KEY = "http.timeout.seconds";
    private static final String MAP_CENTER_LAT_KEY = "map.center.latitude";
    private static final String MAP_CENTER_LON_KEY = "map.center.longitude";
    private static final String DEFAULT_ZOOM_KEY = "map.default.zoom";
    private static final String USER_AGENT_KEY = "http.user.agent";
    
    // Valores padrão
    private static final String DEFAULT_OSRM_URL = "http://router.project-osrm.org/route/v1";
    private static final String DEFAULT_NOMINATIM_URL = "https://nominatim.openstreetmap.org";
    private static final String DEFAULT_HTTP_TIMEOUT = "30";
    private static final String DEFAULT_MAP_CENTER_LAT = "38.7223";
    private static final String DEFAULT_MAP_CENTER_LON = "-9.1393";
    private static final String DEFAULT_ZOOM = "13";
    private static final String DEFAULT_USER_AGENT = "MapRouteExplorer/1.0.0";
    
    /**
     * Construtor privado para evitar instanciação da classe utilitária.
     */
    private ConfigManager() {
        throw new UnsupportedOperationException("Classe utilitária não pode ser instanciada");
    }
    
    /**
     * Carrega as configurações do ficheiro de propriedades.
     * 
     * @throws IOException se ocorrer erro ao carregar o ficheiro
     */
    public static synchronized void loadConfig() throws IOException {
        if (loaded) {
            return;
        }
        
        properties = new Properties();
        
        try (InputStream input = ConfigManager.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
                logger.info("Configurações carregadas do ficheiro: " + CONFIG_FILE);
            } else {
                logger.warning("Ficheiro de configuração não encontrado: " + CONFIG_FILE + 
                              ". Utilizando valores padrão.");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erro ao carregar configurações", e);
            throw e;
        }
        
        loaded = true;
    }
    
    /**
     * Obtém uma propriedade de configuração como string.
     * 
     * @param key chave da propriedade
     * @param defaultValue valor padrão se a chave não existir
     * @return valor da propriedade
     */
    public static String getProperty(String key, String defaultValue) {
        if (!loaded) {
            try {
                loadConfig();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Erro ao carregar configurações, utilizando valor padrão", e);
                return defaultValue;
            }
        }
        
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Obtém uma propriedade de configuração como inteiro.
     * 
     * @param key chave da propriedade
     * @param defaultValue valor padrão se a chave não existir ou for inválida
     * @return valor da propriedade
     */
    public static int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warning("Valor inválido para propriedade " + key + ": " + value + 
                          ". Utilizando valor padrão: " + defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Obtém uma propriedade de configuração como double.
     * 
     * @param key chave da propriedade
     * @param defaultValue valor padrão se a chave não existir ou for inválida
     * @return valor da propriedade
     */
    public static double getDoubleProperty(String key, double defaultValue) {
        String value = getProperty(key, String.valueOf(defaultValue));
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            logger.warning("Valor inválido para propriedade " + key + ": " + value + 
                          ". Utilizando valor padrão: " + defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Obtém uma propriedade de configuração como boolean.
     * 
     * @param key chave da propriedade
     * @param defaultValue valor padrão se a chave não existir
     * @return valor da propriedade
     */
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }
    
    /**
     * Obtém a URL base da API OSRM.
     * 
     * @return URL base da API OSRM
     */
    public static String getOSRMBaseUrl() {
        return getProperty(OSRM_BASE_URL_KEY, DEFAULT_OSRM_URL);
    }
    
    /**
     * Obtém a URL base da API Nominatim.
     * 
     * @return URL base da API Nominatim
     */
    public static String getNominatimBaseUrl() {
        return getProperty(NOMINATIM_BASE_URL_KEY, DEFAULT_NOMINATIM_URL);
    }
    
    /**
     * Obtém o timeout HTTP em segundos.
     * 
     * @return timeout HTTP em segundos
     */
    public static int getHttpTimeout() {
        return getIntProperty(HTTP_TIMEOUT_KEY, Integer.parseInt(DEFAULT_HTTP_TIMEOUT));
    }
    
    /**
     * Obtém a latitude do centro do mapa.
     * 
     * @return latitude do centro do mapa
     */
    public static double getMapCenterLatitude() {
        return getDoubleProperty(MAP_CENTER_LAT_KEY, Double.parseDouble(DEFAULT_MAP_CENTER_LAT));
    }
    
    /**
     * Obtém a longitude do centro do mapa.
     * 
     * @return longitude do centro do mapa
     */
    public static double getMapCenterLongitude() {
        return getDoubleProperty(MAP_CENTER_LON_KEY, Double.parseDouble(DEFAULT_MAP_CENTER_LON));
    }
    
    /**
     * Obtém o nível de zoom padrão do mapa.
     * 
     * @return nível de zoom padrão
     */
    public static int getDefaultZoom() {
        return getIntProperty(DEFAULT_ZOOM_KEY, Integer.parseInt(DEFAULT_ZOOM));
    }
    
    /**
     * Obtém o User-Agent para requisições HTTP.
     * 
     * @return User-Agent
     */
    public static String getUserAgent() {
        return getProperty(USER_AGENT_KEY, DEFAULT_USER_AGENT);
    }
    
    /**
     * Verifica se as configurações foram carregadas.
     * 
     * @return true se as configurações foram carregadas
     */
    public static boolean isLoaded() {
        return loaded;
    }
    
    /**
     * Força o recarregamento das configurações.
     * 
     * @throws IOException se ocorrer erro ao carregar o ficheiro
     */
    public static synchronized void reloadConfig() throws IOException {
        loaded = false;
        loadConfig();
    }
    
    /**
     * Obtém todas as propriedades carregadas.
     * 
     * @return cópia das propriedades
     */
    public static Properties getAllProperties() {
        if (!loaded) {
            try {
                loadConfig();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Erro ao carregar configurações", e);
                return new Properties();
            }
        }
        
        Properties copy = new Properties();
        copy.putAll(properties);
        return copy;
    }
}


