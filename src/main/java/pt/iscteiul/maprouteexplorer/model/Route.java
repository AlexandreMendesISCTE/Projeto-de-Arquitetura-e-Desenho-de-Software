package pt.iscteiul.maprouteexplorer.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Representa uma rota calculada entre dois ou mais pontos.
 * 
 * Esta classe encapsula todas as informações de uma rota, incluindo
 * os pontos de passagem, distância total, tempo estimado e instruções de navegação.
 * 
 * @author Alexandre Mendes
 * @version 1.0.0
 * @since 1.0.0
 */
public class Route {
    
    /** Lista de pontos que compõem a rota */
    private List<Location> waypoints;
    
    /** Distância total da rota em metros */
    private double totalDistance;
    
    /** Tempo estimado de viagem em segundos */
    private double totalDuration;
    
    /** Instruções de navegação para a rota */
    private List<String> instructions;
    
    /** Modo de transporte utilizado para calcular a rota */
    private TransportMode transportMode;
    
    /**
     * Construtor padrão que cria uma rota vazia.
     */
    public Route() {
        this.waypoints = new ArrayList<>();
        this.totalDistance = 0.0;
        this.totalDuration = 0.0;
        this.instructions = new ArrayList<>();
        this.transportMode = TransportMode.DRIVING;
    }
    
    /**
     * Construtor que cria uma rota com pontos específicos.
     * 
     * @param waypoints lista de pontos da rota
     */
    public Route(List<Location> waypoints) {
        this.waypoints = new ArrayList<>(waypoints);
        this.totalDistance = 0.0;
        this.totalDuration = 0.0;
        this.instructions = new ArrayList<>();
        this.transportMode = TransportMode.DRIVING;
    }
    
    /**
     * Obtém a lista de pontos da rota.
     * 
     * @return lista de pontos da rota
     */
    public List<Location> getWaypoints() {
        return new ArrayList<>(waypoints);
    }
    
    /**
     * Define a lista de pontos da rota.
     * 
     * @param waypoints lista de pontos da rota
     */
    public void setWaypoints(List<Location> waypoints) {
        this.waypoints = new ArrayList<>(waypoints);
    }
    
    /**
     * Adiciona um ponto à rota.
     * 
     * @param location ponto a adicionar
     */
    public void addWaypoint(Location location) {
        this.waypoints.add(location);
    }
    
    /**
     * Obtém a distância total da rota.
     * 
     * @return distância total em metros
     */
    public double getTotalDistance() {
        return totalDistance;
    }
    
    /**
     * Define a distância total da rota.
     * 
     * @param totalDistance distância total em metros
     */
    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }
    
    /**
     * Obtém o tempo estimado de viagem.
     * 
     * @return tempo estimado em segundos
     */
    public double getTotalDuration() {
        return totalDuration;
    }
    
    /**
     * Define o tempo estimado de viagem.
     * 
     * @param totalDuration tempo estimado em segundos
     */
    public void setTotalDuration(double totalDuration) {
        this.totalDuration = totalDuration;
    }
    
    /**
     * Obtém as instruções de navegação.
     * 
     * @return lista de instruções
     */
    public List<String> getInstructions() {
        return new ArrayList<>(instructions);
    }
    
    /**
     * Define as instruções de navegação.
     * 
     * @param instructions lista de instruções
     */
    public void setInstructions(List<String> instructions) {
        this.instructions = new ArrayList<>(instructions);
    }
    
    /**
     * Adiciona uma instrução de navegação.
     * 
     * @param instruction instrução a adicionar
     */
    public void addInstruction(String instruction) {
        this.instructions.add(instruction);
    }
    
    /**
     * Obtém o modo de transporte da rota.
     * 
     * @return modo de transporte
     */
    public TransportMode getTransportMode() {
        return transportMode;
    }
    
    /**
     * Define o modo de transporte da rota.
     * 
     * @param transportMode modo de transporte
     */
    public void setTransportMode(TransportMode transportMode) {
        this.transportMode = transportMode;
    }
    
    /**
     * Obtém a distância total formatada em quilómetros.
     * 
     * @return distância formatada
     */
    public String getFormattedDistance() {
        if (totalDistance < 1000) {
            return String.format("%.0f m", totalDistance);
        } else {
            return String.format("%.2f km", totalDistance / 1000);
        }
    }
    
    /**
     * Obtém o tempo estimado formatado em horas e minutos.
     * 
     * @return tempo formatado
     */
    public String getFormattedDuration() {
        int hours = (int) (totalDuration / 3600);
        int minutes = (int) ((totalDuration % 3600) / 60);
        
        if (hours > 0) {
            return String.format("%d h %d min", hours, minutes);
        } else {
            return String.format("%d min", minutes);
        }
    }
    
    /**
     * Verifica se a rota está vazia.
     * 
     * @return true se a rota não tem pontos
     */
    public boolean isEmpty() {
        return waypoints.isEmpty();
    }
    
    /**
     * Obtém o número de pontos na rota.
     * 
     * @return número de pontos
     */
    public int getWaypointCount() {
        return waypoints.size();
    }
    
    /**
     * Limpa todos os pontos e informações da rota.
     */
    public void clear() {
        waypoints.clear();
        instructions.clear();
        totalDistance = 0.0;
        totalDuration = 0.0;
    }
    
    /**
     * Retorna uma representação em string da rota.
     * 
     * @return string com informações da rota
     */
    @Override
    public String toString() {
        return String.format("Route{waypoints=%d, distance=%.2f km, duration=%.0f min, mode=%s}", 
                           waypoints.size(), totalDistance / 1000, totalDuration / 60, transportMode);
    }
}


