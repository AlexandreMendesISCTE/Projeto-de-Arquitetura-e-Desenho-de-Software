package pt.iscteiul.maprouteexplorer.service;

import java.io.IOException;

/**
 * Serviço para realizar requisições HTTP.
 * 
 * Esta classe fornece uma interface simplificada para fazer requisições HTTP,
 * encapsulando a complexidade da comunicação com APIs externas e fornecendo
 * métodos convenientes para GET, POST e outras operações HTTP.
 * 
 * @author Alexandre Mendes
 * @version 1.0.0
 * @since 1.0.0
 */
public interface HttpClientService {
    
    /**
     * Realiza uma requisição HTTP GET para a URL especificada.
     * 
     * @param url URL para a qual fazer a requisição
     * @return resposta da requisição como string
     * @throws IOException se ocorrer erro na comunicação
     */
    String get(String url) throws IOException;
    
    /**
     * Realiza uma requisição HTTP POST para a URL especificada.
     * 
     * @param url URL para a qual fazer a requisição
     * @param body corpo da requisição
     * @return resposta da requisição como string
     * @throws IOException se ocorrer erro na comunicação
     */
    String post(String url, String body) throws IOException;
    
    /**
     * Realiza uma requisição HTTP GET com cabeçalhos personalizados.
     * 
     * @param url URL para a qual fazer a requisição
     * @param headers cabeçalhos HTTP personalizados
     * @return resposta da requisição como string
     * @throws IOException se ocorrer erro na comunicação
     */
    String get(String url, java.util.Map<String, String> headers) throws IOException;
    
    /**
     * Verifica se o serviço está disponível e configurado corretamente.
     * 
     * @return true se o serviço estiver disponível
     */
    boolean isAvailable();
}


