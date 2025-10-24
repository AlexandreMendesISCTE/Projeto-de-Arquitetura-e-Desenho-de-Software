package pt.iscteiul.maprouteexplorer.util;

import pt.iscteiul.maprouteexplorer.model.Location;
import pt.iscteiul.maprouteexplorer.model.Route;
import pt.iscteiul.maprouteexplorer.model.TransportMode;

import java.util.List;
import java.util.ArrayList;

/**
 * Classe utilitária para operações relacionadas com rotas.
 * 
 * Esta classe fornece métodos estáticos para cálculos e manipulações
 * de rotas, incluindo validações, conversões de unidades e formatações.
 * 
 * @author Alexandre Mendes
 * @version 1.0.0
 * @since 1.0.0
 */
public class RouteUtils {
    
    /** Fator de conversão de metros para quilómetros */
    private static final double METERS_TO_KM = 0.001;
    
    /** Fator de conversão de quilómetros para milhas */
    private static final double KM_TO_MILES = 0.621371;
    
    /** Fator de conversão de segundos para horas */
    private static final double SECONDS_TO_HOURS = 1.0 / 3600.0;
    
    /**
     * Construtor privado para evitar instanciação da classe utilitária.
     */
    private RouteUtils() {
        throw new UnsupportedOperationException("Classe utilitária não pode ser instanciada");
    }
    
    /**
     * Valida se uma lista de pontos é válida para cálculo de rota.
     * 
     * @param waypoints lista de pontos
     * @return true se a lista for válida
     */
    public static boolean isValidRoute(List<Location> waypoints) {
        if (waypoints == null || waypoints.size() < 2) {
            return false;
        }
        
        // Verificar se todos os pontos são válidos
        for (Location point : waypoints) {
            if (!isValidLocation(point)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Valida se uma localização tem coordenadas válidas.
     * 
     * @param location localização a validar
     * @return true se a localização for válida
     */
    public static boolean isValidLocation(Location location) {
        if (location == null) {
            return false;
        }
        
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        
        return lat >= -90.0 && lat <= 90.0 && lon >= -180.0 && lon <= 180.0;
    }
    
    /**
     * Calcula a distância total de uma rota somando as distâncias entre pontos consecutivos.
     * 
     * @param waypoints lista de pontos da rota
     * @return distância total em metros
     */
    public static double calculateTotalDistance(List<Location> waypoints) {
        if (waypoints == null || waypoints.size() < 2) {
            return 0.0;
        }
        
        double totalDistance = 0.0;
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Location current = waypoints.get(i);
            Location next = waypoints.get(i + 1);
            totalDistance += current.distanceTo(next) * 1000; // Converter para metros
        }
        
        return totalDistance;
    }
    
    /**
     * Estima o tempo de viagem baseado na distância e modo de transporte.
     * 
     * @param distance distância em metros
     * @param transportMode modo de transporte
     * @return tempo estimado em segundos
     */
    public static double estimateTravelTime(double distance, TransportMode transportMode) {
        if (distance <= 0 || transportMode == null) {
            return 0.0;
        }
        
        double distanceKm = distance * METERS_TO_KM;
        double averageSpeedKmh = transportMode.getAverageSpeed();
        
        return (distanceKm / averageSpeedKmh) * 3600; // Converter para segundos
    }
    
    /**
     * Converte metros para quilómetros.
     * 
     * @param meters distância em metros
     * @return distância em quilómetros
     */
    public static double metersToKilometers(double meters) {
        return meters * METERS_TO_KM;
    }
    
    /**
     * Converte quilómetros para milhas.
     * 
     * @param kilometers distância em quilómetros
     * @return distância em milhas
     */
    public static double kilometersToMiles(double kilometers) {
        return kilometers * KM_TO_MILES;
    }
    
    /**
     * Converte segundos para horas.
     * 
     * @param seconds tempo em segundos
     * @return tempo em horas
     */
    public static double secondsToHours(double seconds) {
        return seconds * SECONDS_TO_HOURS;
    }
    
    /**
     * Formata uma distância em metros para uma string legível.
     * 
     * @param distanceMeters distância em metros
     * @return string formatada
     */
    public static String formatDistance(double distanceMeters) {
        if (distanceMeters < 1000) {
            return String.format("%.0f m", distanceMeters);
        } else {
            double km = metersToKilometers(distanceMeters);
            return String.format("%.2f km", km);
        }
    }
    
    /**
     * Formata um tempo em segundos para uma string legível.
     * 
     * @param durationSeconds tempo em segundos
     * @return string formatada
     */
    public static String formatDuration(double durationSeconds) {
        int hours = (int) (durationSeconds / 3600);
        int minutes = (int) ((durationSeconds % 3600) / 60);
        int seconds = (int) (durationSeconds % 60);
        
        if (hours > 0) {
            return String.format("%d h %d min", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%d min %d s", minutes, seconds);
        } else {
            return String.format("%d s", seconds);
        }
    }
    
    /**
     * Cria uma rota otimizada removendo pontos redundantes.
     * 
     * @param originalRoute rota original
     * @param minDistance distância mínima entre pontos em metros
     * @return rota otimizada
     */
    public static Route optimizeRoute(Route originalRoute, double minDistance) {
        if (originalRoute == null || originalRoute.isEmpty()) {
            return new Route();
        }
        
        List<Location> waypoints = originalRoute.getWaypoints();
        List<Location> optimizedWaypoints = new ArrayList<>();
        
        if (!waypoints.isEmpty()) {
            optimizedWaypoints.add(waypoints.get(0)); // Sempre incluir o primeiro ponto
            
            Location lastAdded = waypoints.get(0);
            
            for (int i = 1; i < waypoints.size() - 1; i++) {
                Location current = waypoints.get(i);
                if (lastAdded.distanceTo(current) * 1000 >= minDistance) {
                    optimizedWaypoints.add(current);
                    lastAdded = current;
                }
            }
            
            // Sempre incluir o último ponto
            if (waypoints.size() > 1) {
                optimizedWaypoints.add(waypoints.get(waypoints.size() - 1));
            }
        }
        
        Route optimizedRoute = new Route(optimizedWaypoints);
        optimizedRoute.setTransportMode(originalRoute.getTransportMode());
        optimizedRoute.setTotalDistance(calculateTotalDistance(optimizedWaypoints));
        optimizedRoute.setTotalDuration(estimateTravelTime(optimizedRoute.getTotalDistance(), 
                                                         optimizedRoute.getTransportMode()));
        
        return optimizedRoute;
    }
    
    /**
     * Verifica se duas rotas são similares baseado na distância e tempo.
     * 
     * @param route1 primeira rota
     * @param route2 segunda rota
     * @param tolerancePercent tolerância em percentagem
     * @return true se as rotas forem similares
     */
    public static boolean areRoutesSimilar(Route route1, Route route2, double tolerancePercent) {
        if (route1 == null || route2 == null) {
            return false;
        }
        
        double distanceDiff = Math.abs(route1.getTotalDistance() - route2.getTotalDistance());
        double avgDistance = (route1.getTotalDistance() + route2.getTotalDistance()) / 2;
        
        double durationDiff = Math.abs(route1.getTotalDuration() - route2.getTotalDuration());
        double avgDuration = (route1.getTotalDuration() + route2.getTotalDuration()) / 2;
        
        double distanceTolerance = (avgDistance * tolerancePercent) / 100;
        double durationTolerance = (avgDuration * tolerancePercent) / 100;
        
        return distanceDiff <= distanceTolerance && durationDiff <= durationTolerance;
    }
    
    /**
     * Calcula o centro geográfico de uma lista de pontos.
     * 
     * @param locations lista de localizações
     * @return centro geográfico
     */
    public static Location calculateCentroid(List<Location> locations) {
        if (locations == null || locations.isEmpty()) {
            return new Location(0.0, 0.0);
        }
        
        double totalLat = 0.0;
        double totalLon = 0.0;
        
        for (Location location : locations) {
            totalLat += location.getLatitude();
            totalLon += location.getLongitude();
        }
        
        double avgLat = totalLat / locations.size();
        double avgLon = totalLon / locations.size();
        
        return new Location(avgLat, avgLon);
    }
}


