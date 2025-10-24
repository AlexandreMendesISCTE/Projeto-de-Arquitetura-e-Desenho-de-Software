package pt.iscteiul.maprouteexplorer.service;

/**
 * Exceção específica para erros relacionados com a API Nominatim.
 * 
 * Esta exceção é lançada quando ocorrem problemas na comunicação
 * ou processamento de dados da API Nominatim, como respostas inválidas,
 * erros de rede ou problemas de parsing.
 * 
 * @author Alexandre Mendes
 * @version 1.0.0
 * @since 1.0.0
 */
public class NominatimException extends Exception {
    
    /**
     * Construtor que cria uma exceção com uma mensagem específica.
     * 
     * @param message mensagem de erro
     */
    public NominatimException(String message) {
        super(message);
    }
    
    /**
     * Construtor que cria uma exceção com uma mensagem e causa específicas.
     * 
     * @param message mensagem de erro
     * @param cause causa da exceção
     */
    public NominatimException(String message, Throwable cause) {
        super(message, cause);
    }
}


