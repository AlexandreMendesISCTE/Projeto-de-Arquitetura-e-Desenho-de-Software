package pt.iscteiul.maprouteexplorer.ui;

import pt.iscteiul.maprouteexplorer.model.Location;
import pt.iscteiul.maprouteexplorer.model.Route;

import java.util.List;

/**
 * Interface comum para todos os painéis de mapa.
 * 
 * Define a API pública que todos os painéis de mapa devem implementar,
 * permitindo trocar entre diferentes implementações sem alterar o código cliente.
 * 
 * @author Alexandre Mendes
 * @version 2.0.0
 * @since 2.0.0
 */
public interface MapPanelInterface {
    
    /**
     * Define o centro do mapa.
     * 
     * @param center localização central do mapa
     */
    void setMapCenter(Location center);
    
    /**
     * Obtém o centro atual do mapa.
     * 
     * @return localização central do mapa
     */
    Location getMapCenter();
    
    /**
     * Define o nível de zoom do mapa.
     * 
     * @param zoom nível de zoom (geralmente entre 1 e 19)
     */
    void setZoomLevel(int zoom);
    
    /**
     * Obtém o nível de zoom atual.
     * 
     * @return nível de zoom atual
     */
    int getZoomLevel();
    
    /**
     * Define a rota a ser exibida no mapa.
     * 
     * @param route rota a ser desenhada
     */
    void setRoute(Route route);
    
    /**
     * Limpa a rota atual do mapa.
     */
    void clearRoute();
    
    /**
     * Limpa todos os pontos selecionados.
     */
    void clearSelectedPoints();
    
    /**
     * Obtém os pontos selecionados no mapa.
     * 
     * @return lista de pontos selecionados
     */
    List<Location> getSelectedPoints();
    
    /**
     * Define o listener para eventos de seleção de pontos.
     * 
     * @param listener listener para eventos de seleção
     */
    void setPointSelectionListener(PointSelectionListener listener);
}

