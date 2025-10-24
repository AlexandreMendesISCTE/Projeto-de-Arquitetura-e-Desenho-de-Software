package pt.iscteiul.maprouteexplorer.service;

/**
 * Exceção específica para erros relacionados com a API OSRM.
 * 
 * Esta exceção é lançada quando ocorrem problemas na comunicação
 * ou processamento de dados da API OSRM, como respostas inválidas,
 * erros de rede ou problemas de parsing.
 * 
 * @author Alexandre Mendes
 * @version 1.0.0
 * @since 1.0.0
 */
public class OSRMException extends Exception {
    
    /**
     * Construtor que cria uma exceção com uma mensagem específica.
     * 
     * @param message mensagem de erro
     */
    public OSRMException(String message) {
        super(message);
    }
    
    /**
     * Construtor que cria uma exceção com uma mensagem e causa específicas.
     * 
     * @param message mensagem de erro
     * @param cause causa da exceção
     */
    public OSRMException(String message, Throwable cause) {
        super(message, cause);
    }
}


