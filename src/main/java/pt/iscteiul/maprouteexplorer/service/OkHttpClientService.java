package pt.iscteiul.maprouteexplorer.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Implementação do HttpClientService utilizando OkHttp.
 * 
 * Esta classe fornece uma implementação concreta do HttpClientService
 * utilizando a biblioteca OkHttp para realizar requisições HTTP de forma
 * eficiente e com suporte a recursos avançados como timeouts e retry.
 * 
 * @author Alexandre Mendes
 * @version 1.0.0
 * @since 1.0.0
 */
public class OkHttpClientService implements HttpClientService {
    
    /** Cliente HTTP OkHttp */
    private final OkHttpClient client;
    
    /** Timeout padrão para requisições em segundos */
    private static final int DEFAULT_TIMEOUT = 30;
    
    /**
     * Construtor que inicializa o cliente HTTP com configurações padrão.
     */
    public OkHttpClientService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * Construtor que permite configurar o cliente HTTP.
     * 
     * @param client cliente HTTP personalizado
     */
    public OkHttpClientService(OkHttpClient client) {
        this.client = client;
    }
    
    /**
     * Realiza uma requisição HTTP GET para a URL especificada.
     * 
     * @param url URL para a qual fazer a requisição
     * @return resposta da requisição como string
     * @throws IOException se ocorrer erro na comunicação
     */
    @Override
    public String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erro HTTP: " + response.code() + " - " + response.message());
            }
            
            if (response.body() == null) {
                throw new IOException("Resposta vazia do servidor");
            }
            
            return response.body().string();
        }
    }
    
    /**
     * Realiza uma requisição HTTP POST para a URL especificada.
     * 
     * @param url URL para a qual fazer a requisição
     * @param body corpo da requisição
     * @return resposta da requisição como string
     * @throws IOException se ocorrer erro na comunicação
     */
    @Override
    public String post(String url, String body) throws IOException {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(body, mediaType);
        
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erro HTTP: " + response.code() + " - " + response.message());
            }
            
            if (response.body() == null) {
                throw new IOException("Resposta vazia do servidor");
            }
            
            return response.body().string();
        }
    }
    
    /**
     * Realiza uma requisição HTTP GET com cabeçalhos personalizados.
     * 
     * @param url URL para a qual fazer a requisição
     * @param headers cabeçalhos HTTP personalizados
     * @return resposta da requisição como string
     * @throws IOException se ocorrer erro na comunicação
     */
    @Override
    public String get(String url, Map<String, String> headers) throws IOException {
        Request.Builder requestBuilder = new Request.Builder().url(url);
        
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        
        Request request = requestBuilder.build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erro HTTP: " + response.code() + " - " + response.message());
            }
            
            if (response.body() == null) {
                throw new IOException("Resposta vazia do servidor");
            }
            
            return response.body().string();
        }
    }
    
    /**
     * Verifica se o serviço está disponível e configurado corretamente.
     * 
     * @return true se o serviço estiver disponível
     */
    @Override
    public boolean isAvailable() {
        return client != null;
    }
    
    /**
     * Obtém o cliente HTTP subjacente.
     * 
     * @return cliente OkHttp
     */
    public OkHttpClient getClient() {
        return client;
    }
}


