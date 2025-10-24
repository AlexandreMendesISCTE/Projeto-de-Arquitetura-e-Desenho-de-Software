package pt.iscteiul.maprouteexplorer.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe Location.
 * 
 * Esta classe contém testes para validar o comportamento da classe Location,
 * incluindo construtores, métodos de acesso, cálculos de distância e
 * operações de igualdade.
 * 
 * @author Alexandre Mendes
 * @version 1.0.0
 * @since 1.0.0
 */
class LocationTest {
    
    private Location location1;
    private Location location2;
    private Location location3;
    
    /**
     * Configuração inicial para cada teste.
     */
    @BeforeEach
    void setUp() {
        location1 = new Location(38.7223, -9.1393, "Lisboa", "Lisboa, Portugal");
        location2 = new Location(40.4168, -3.7038, "Madrid", "Madrid, Espanha");
        location3 = new Location(38.7223, -9.1393, "Lisboa Centro", "Centro de Lisboa");
    }
    
    /**
     * Testa o construtor padrão.
     */
    @Test
    void testDefaultConstructor() {
        Location location = new Location();
        
        assertEquals(0.0, location.getLatitude());
        assertEquals(0.0, location.getLongitude());
        assertEquals("", location.getName());
        assertEquals("", location.getAddress());
    }
    
    /**
     * Testa o construtor com coordenadas.
     */
    @Test
    void testConstructorWithCoordinates() {
        double lat = 40.0;
        double lon = -8.0;
        Location location = new Location(lat, lon);
        
        assertEquals(lat, location.getLatitude());
        assertEquals(lon, location.getLongitude());
        assertEquals("", location.getName());
        assertEquals("", location.getAddress());
    }
    
    /**
     * Testa o construtor completo.
     */
    @Test
    void testFullConstructor() {
        double lat = 40.0;
        double lon = -8.0;
        String name = "Teste";
        String address = "Endereço de teste";
        
        Location location = new Location(lat, lon, name, address);
        
        assertEquals(lat, location.getLatitude());
        assertEquals(lon, location.getLongitude());
        assertEquals(name, location.getName());
        assertEquals(address, location.getAddress());
    }
    
    /**
     * Testa os métodos getter e setter.
     */
    @Test
    void testGettersAndSetters() {
        Location location = new Location();
        
        // Testar setters e getters
        location.setLatitude(50.0);
        assertEquals(50.0, location.getLatitude());
        
        location.setLongitude(10.0);
        assertEquals(10.0, location.getLongitude());
        
        location.setName("Nome Teste");
        assertEquals("Nome Teste", location.getName());
        
        location.setAddress("Endereço Teste");
        assertEquals("Endereço Teste", location.getAddress());
    }
    
    /**
     * Testa o cálculo de distância entre duas localizações.
     */
    @Test
    void testDistanceTo() {
        // Distância aproximada entre Lisboa e Madrid (cerca de 500 km)
        double distance = location1.distanceTo(location2);
        
        assertTrue(distance > 400 && distance < 600, 
                  "Distância entre Lisboa e Madrid deve estar entre 400 e 600 km");
    }
    
    /**
     * Testa o cálculo de distância para o mesmo ponto.
     */
    @Test
    void testDistanceToSameLocation() {
        double distance = location1.distanceTo(location1);
        assertEquals(0.0, distance, 0.001, "Distância para o mesmo ponto deve ser 0");
    }
    
    /**
     * Testa o método toString.
     */
    @Test
    void testToString() {
        String str = location1.toString();
        
        assertTrue(str.contains("38.7223"));
        assertTrue(str.contains("-9.1393"));
        assertTrue(str.contains("Lisboa"));
    }
    
    /**
     * Testa o método equals para localizações iguais.
     */
    @Test
    void testEqualsSameLocation() {
        assertTrue(location1.equals(location1), "Localização deve ser igual a si mesma");
    }
    
    /**
     * Testa o método equals para localizações com as mesmas coordenadas.
     */
    @Test
    void testEqualsSameCoordinates() {
        assertTrue(location1.equals(location3), 
                  "Localizações com as mesmas coordenadas devem ser iguais");
    }
    
    /**
     * Testa o método equals para localizações diferentes.
     */
    @Test
    void testEqualsDifferentLocations() {
        assertFalse(location1.equals(location2), 
                   "Localizações diferentes devem ser diferentes");
    }
    
    /**
     * Testa o método equals com objeto nulo.
     */
    @Test
    void testEqualsWithNull() {
        assertFalse(location1.equals(null), "Localização não deve ser igual a null");
    }
    
    /**
     * Testa o método equals com objeto de tipo diferente.
     */
    @Test
    void testEqualsWithDifferentType() {
        assertFalse(location1.equals("string"), 
                   "Localização não deve ser igual a string");
    }
    
    /**
     * Testa o método hashCode.
     */
    @Test
    void testHashCode() {
        int hashCode1 = location1.hashCode();
        int hashCode2 = location1.hashCode();
        int hashCode3 = location3.hashCode();
        
        assertEquals(hashCode1, hashCode2, "HashCode deve ser consistente");
        assertEquals(hashCode1, hashCode3, "Localizações iguais devem ter o mesmo hashCode");
    }
    
    /**
     * Testa o método hashCode para localizações diferentes.
     */
    @Test
    void testHashCodeDifferentLocations() {
        int hashCode1 = location1.hashCode();
        int hashCode2 = location2.hashCode();
        
        assertNotEquals(hashCode1, hashCode2, 
                       "Localizações diferentes devem ter hashCode diferentes");
    }
}


