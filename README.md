# Map Route Explorer

Sistema Interativo de Rotas e Exploração de Locais com OpenStreetMap

## Descrição

Aplicação interativa que permite aos utilizadores explorar mapas baseados em dados do OpenStreetMap, traçar rotas entre pontos de interesse e obter informações relevantes sobre o trajeto e a área circundante.

## Estrutura do Projeto

```
src/
├── main/
│   ├── java/pt/iscte_iul/maprouteexplorer/
│   │   ├── Main.java                          # Classe principal da aplicação
│   │   ├── model/                              # Modelos de dados
│   │   │   ├── Location.java                  # Representa uma localização geográfica
│   │   │   ├── Route.java                     # Representa uma rota calculada
│   │   │   └── TransportMode.java             # Enum para modos de transporte
│   │   ├── service/                            # Serviços de integração com APIs
│   │   │   ├── OSRMService.java               # Serviço para API OSRM
│   │   │   ├── OSRMException.java             # Exceção específica OSRM
│   │   │   ├── NominatimService.java          # Serviço para API Nominatim
│   │   │   ├── NominatimException.java        # Exceção específica Nominatim
│   │   │   ├── HttpClientService.java         # Interface para cliente HTTP
│   │   │   └── OkHttpClientService.java       # Implementação com OkHttp
│   │   ├── ui/                                 # Interface gráfica
│   │   │   ├── MainWindow.java                # Janela principal
│   │   │   ├── MapPanel.java                  # Painel do mapa
│   │   │   └── PointSelectionListener.java    # Interface para eventos
│   │   └── util/                               # Classes utilitárias
│   │       ├── RouteUtils.java                # Utilitários para rotas
│   │       └── ConfigManager.java             # Gestor de configurações
│   └── resources/
│       ├── application.properties              # Configurações da aplicação
│       └── logback.xml                         # Configuração de logging
└── test/
    └── java/pt/iscte_iul/maprouteexplorer/
        ├── model/
        │   └── LocationTest.java               # Testes para Location
        └── util/
            └── RouteUtilsTest.java             # Testes para RouteUtils
```

## Tecnologias Utilizadas

- **Java 17**: Linguagem de programação
- **Maven**: Gestão de dependências e build
- **Jackson**: Parsing de JSON
- **OkHttp**: Cliente HTTP para requisições
- **JUnit 5**: Framework de testes
- **Mockito**: Framework de mocking para testes
- **SLF4J + Logback**: Logging
- **Swing**: Interface gráfica

## APIs Utilizadas

- **OSRM**: Cálculo de rotas, distância e tempo
- **Nominatim**: Geocodificação e pesquisa de locais
- **Overpass API** (opcional): Pontos de interesse ao longo do trajeto

## Funcionalidades

### Obrigatórias
- ✅ Visualização de mapa interativo
- ✅ Seleção de rota entre origem e destino
- ✅ Cálculo de distância e tempo de viagem
- ✅ Limpeza e reinício de seleção

### Opcionais
- Pesquisa de localização (Nominatim)
- Modos de transporte (carro, bicicleta, a pé)
- Pontos de interesse (Overpass API)
- Múltiplos destinos
- Exportação de dados
- Estatísticas avançadas

## Como Executar

### Pré-requisitos
- Java 17 ou superior
- Maven 3.6 ou superior

### Compilar o projeto
```bash
mvn clean compile
```

### Executar testes
```bash
mvn test
```

### Executar a aplicação
```bash
mvn exec:java -Dexec.mainClass="pt.iscte_iul.maprouteexplorer.Main"
```

### Gerar JAR executável
```bash
mvn clean package
java -jar target/map-route-explorer-1.0.0-jar-with-dependencies.jar
```

### Gerar documentação Javadoc
```bash
mvn javadoc:javadoc
```
A documentação será gerada em `target/site/apidocs/`

## Configuração

As configurações da aplicação podem ser ajustadas no arquivo `src/main/resources/application.properties`:

- `osrm.base.url`: URL base da API OSRM
- `nominatim.base.url`: URL base da API Nominatim
- `http.timeout.seconds`: Timeout para requisições HTTP
- `map.center.latitude`: Latitude inicial do mapa
- `map.center.longitude`: Longitude inicial do mapa
- `map.default.zoom`: Nível de zoom padrão

## Arquitetura

O projeto segue uma arquitetura em camadas:

1. **Model**: Entidades de domínio (Location, Route, TransportMode)
2. **Service**: Integração com APIs externas (OSRM, Nominatim)
3. **UI**: Interface gráfica com Swing
4. **Util**: Classes utilitárias e configuração

## Autor

- Alexandre Mendes (111026)

## Licença

Este projeto está sob a licença especificada no arquivo LICENSE.

