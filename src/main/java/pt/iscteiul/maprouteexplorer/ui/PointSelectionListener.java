package pt.iscteiul.maprouteexplorer.ui;

import pt.iscteiul.maprouteexplorer.model.Location;

/**
 * Interface para receber eventos de seleção de pontos no mapa.
 * 
 * Esta interface define o contrato para componentes que precisam
 * ser notificados quando o utilizador seleciona pontos no mapa,
 * permitindo implementar lógica de resposta adequada.
 * 
 * @author Alexandre Mendes
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PointSelectionListener {
    
    /**
     * Chamado quando um ponto é selecionado no mapa.
     * 
     * @param location localização selecionada
     */
    void onPointSelected(Location location);
    
    /**
     * Chamado quando um ponto é removido do mapa.
     * 
     * @param location localização removida
     */
    default void onPointRemoved(Location location) {
        // Implementação padrão vazia - pode ser sobrescrita se necessário
    }
    
    /**
     * Chamado quando todos os pontos são limpos do mapa.
     */
    default void onAllPointsCleared() {
        // Implementação padrão vazia - pode ser sobrescrita se necessário
    }
}


