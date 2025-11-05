# 🌊 Fluxos de Dados e Processos - Map Route Explorer

**Versão**: 2.0.0  
**Data**: 5 de Novembro de 2025

## 👥 Autores

Este projeto foi desenvolvido por:

- **Alexandre Mendes** (111026)
- **Manuel Santos**
- **André Costa**
- **Ana Valente**

**Instituição**: Instituto Superior de Ciências do Trabalho e da Empresa (ISCTE-IUL)  
**Curso**: Engenharia Informática

---

## 🌐 Diagrama de Fluxo de Dados Completo

Este diagrama apresenta o fluxo completo de dados na aplicação, desde o início até o encerramento.

```mermaid
flowchart TD
    START([👤 Utilizador<br/>Inicia Aplicação]) --> INIT[Main.java<br/>Bootstrap]
    
    INIT --> LOAD_CONFIG[ConfigManager<br/>Carrega application.properties]
    LOAD_CONFIG --> INIT_LOG[Logback<br/>Configura Logging]
    INIT_LOG --> CREATE_SERVICES[Cria Serviços<br/>OSRMService NominatimService<br/>OkHttpClientService]
    CREATE_SERVICES --> LAUNCH_UI[Lança MainWindow<br/>Interface Gráfica Swing]
    
    LAUNCH_UI --> INIT_MAP[MapPanel<br/>Inicializa Mapa]
    INIT_MAP --> LOAD_TILES{Carregar Tiles<br/>do OpenStreetMap?}
    
    LOAD_TILES -->|Sim| DOWNLOAD_TILES[OkHttpClientService<br/>Download Tiles]
    DOWNLOAD_TILES --> CACHE_TILES[Cache LRU<br/>Armazena 100 Tiles]
    CACHE_TILES --> RENDER_MAP[Graphics2D<br/>Renderiza Mapa]
    
    LOAD_TILES -->|Cache Hit| RENDER_MAP
    
    RENDER_MAP --> USER_INTERACTION{Interação<br/>do Utilizador?}
    
    USER_INTERACTION -->|Pesquisar Local| SEARCH[NominatimService<br/>Geocodificação]
    SEARCH --> NOMINATIM_API[Nominatim API<br/>Requisição HTTP]
    NOMINATIM_API --> PARSE_LOCATION[Jackson<br/>Parse JSON Location]
    PARSE_LOCATION --> CENTER_MAP[Centralizar Mapa<br/>na Localização]
    CENTER_MAP --> USER_INTERACTION
    
    USER_INTERACTION -->|Zoom| ZOOM_MAP[MapPanel<br/>Ajusta Nível Zoom]
    ZOOM_MAP --> LOAD_TILES
    
    USER_INTERACTION -->|Pan| PAN_MAP[MapPanel<br/>Move Centro Mapa]
    PAN_MAP --> LOAD_TILES
    
    USER_INTERACTION -->|Clique Origem| SELECT_ORIGIN[PointSelectionListener<br/>onPointSelected origem]
    SELECT_ORIGIN --> MARK_ORIGIN[MapPanel<br/>Desenha Marcador Origem]
    MARK_ORIGIN --> USER_INTERACTION
    
    USER_INTERACTION -->|Clique Destino| SELECT_DEST[PointSelectionListener<br/>onPointSelected destino]
    SELECT_DEST --> MARK_DEST[MapPanel<br/>Desenha Marcador Destino]
    MARK_DEST --> CALC_ROUTE[OSRMService<br/>calculateRoute]
    
    CALC_ROUTE --> VALIDATE_COORDS{Validar<br/>Coordenadas?}
    VALIDATE_COORDS -->|Inválidas| ERROR_VALIDATION[Lançar<br/>OSRMException]
    ERROR_VALIDATION --> LOG_ERROR[Logback<br/>Log Erro]
    LOG_ERROR --> USER_INTERACTION
    
    VALIDATE_COORDS -->|Válidas| BUILD_URL[Construir URL<br/>OSRM API]
    BUILD_URL --> HTTP_REQUEST[OkHttpClientService<br/>Requisição HTTP GET]
    HTTP_REQUEST --> OSRM_API[OSRM API<br/>Cálculo Rota Otimizada]
    
    OSRM_API --> HTTP_RESPONSE{Status<br/>HTTP?}
    HTTP_RESPONSE -->|200 OK| PARSE_ROUTE[Jackson<br/>Parse JSON Route]
    HTTP_RESPONSE -->|429 Too Many| RETRY_BACKOFF[Retry com<br/>Exponential Backoff]
    RETRY_BACKOFF --> HTTP_REQUEST
    HTTP_RESPONSE -->|503 Service Unavailable| FALLBACK_SERVER[Fallback<br/>Servidor Alternativo]
    FALLBACK_SERVER --> HTTP_REQUEST
    HTTP_RESPONSE -->|Outros Erros| ERROR_HTTP[Lançar<br/>OSRMException]
    ERROR_HTTP --> LOG_ERROR
    
    PARSE_ROUTE --> CREATE_ROUTE[new Route<br/>waypoints distance duration mode]
    CREATE_ROUTE --> VALIDATE_ROUTE{Validar<br/>Route?}
    VALIDATE_ROUTE -->|Inválida| ERROR_VALIDATION
    VALIDATE_ROUTE -->|Válida| DRAW_ROUTE[MapPanel<br/>Desenha Polyline Graphics2D]
    
    DRAW_ROUTE --> FORMAT_INFO[RouteUtils<br/>Formata Distância Tempo]
    FORMAT_INFO --> DISPLAY_INFO[MainWindow<br/>Atualiza Painel Info]
    DISPLAY_INFO --> USER_INTERACTION
    
    USER_INTERACTION -->|Limpar| CLEAR[MainWindow<br/>Limpar Seleções]
    CLEAR --> CLEAR_MAP[MapPanel<br/>Remover Marcadores Rota]
    CLEAR_MAP --> RESET_UI[MainWindow<br/>Reset Painel Info]
    RESET_UI --> USER_INTERACTION
    
    USER_INTERACTION -->|Fechar| SHUTDOWN[Shutdown<br/>Aplicação]
    SHUTDOWN --> CLEANUP[Cleanup<br/>Thread Pool ExecutorService]
    CLEANUP --> SAVE_LOGS[Logback<br/>Flush Logs]
    SAVE_LOGS --> END([🛑 Fim<br/>Aplicação Encerrada])
    
    style START fill:#c8e6c9,stroke:#2e7d32,stroke-width:3px
    style END fill:#ffcdd2,stroke:#c62828,stroke-width:3px
    style CALC_ROUTE fill:#fff9c4,stroke:#f57f17,stroke-width:3px
    style OSRM_API fill:#e1bee7,stroke:#6a1b9a,stroke-width:3px
    style DOWNLOAD_TILES fill:#b3e5fc,stroke:#0277bd,stroke-width:3px
    style ERROR_VALIDATION fill:#ffccbc,stroke:#d84315,stroke-width:2px
    style ERROR_HTTP fill:#ffccbc,stroke:#d84315,stroke-width:2px
```

---

## 🎬 Ciclo de Vida da Aplicação

```mermaid
flowchart TB
    Start(("🚀 Início"))
    
    subgraph INIT["🔧 Inicialização"]
        Main["Main.main()"]
        Config["ConfigManager.load()"]
        Log["Logback.init()"]
        Services["Inicializar Serviços"]
    end
    
    subgraph UI_INIT["🎨 UI Initialization"]
        Window["Criar MainWindow"]
        Map["Criar MapPanel"]
        Listeners["Registrar Listeners"]
    end
    
    subgraph RUNTIME["▶️ Runtime"]
        Event["Aguardar Eventos"]
        
        subgraph EVENTS["Eventos Possíveis"]
            Click["Clique no Mapa"]
            Search["Pesquisa"]
            Zoom["Zoom In/Out"]
            Pan["Pan (Arrastar)"]
            Clear["Limpar"]
        end
        
        Process["Processar Evento"]
        Update["Atualizar UI"]
    end
    
    subgraph SHUTDOWN["🛑 Encerramento"]
        Close["Fechar Janela"]
        Cleanup["Cleanup Recursos"]
        SaveLogs["Salvar Logs"]
    end
    
    End(("⏹️ Fim"))
    
    Start --> Main
    Main --> Config
    Config --> Log
    Log --> Services
    Services --> Window
    Window --> Map
    Map --> Listeners
    Listeners --> Event
    
    Event --> Click
    Event --> Search
    Event --> Zoom
    Event --> Pan
    Event --> Clear
    
    Click --> Process
    Search --> Process
    Zoom --> Process
    Pan --> Process
    Clear --> Process
    
    Process --> Update
    Update --> Event
    
    Event --> Close
    Close --> Cleanup
    Cleanup --> SaveLogs
    SaveLogs --> End

    classDef startClass fill:#4caf50,stroke:#fff,stroke-width:3px,color:#fff
    classDef initClass fill:#2196f3,stroke:#fff,stroke-width:2px,color:#fff
    classDef runtimeClass fill:#ff9800,stroke:#fff,stroke-width:2px,color:#fff
    classDef shutdownClass fill:#f44336,stroke:#fff,stroke-width:2px,color:#fff
    classDef endClass fill:#9e9e9e,stroke:#fff,stroke-width:3px,color:#fff

    class Start startClass
    class Main,Config,Log,Services,Window,Map,Listeners initClass
    class Event,Click,Search,Zoom,Pan,Clear,Process,Update runtimeClass
    class Close,Cleanup,SaveLogs shutdownClass
    class End endClass
```

---

## 🌐 Sistema de Cache de Tiles

```mermaid
flowchart TB
    Request["🖱️ Utilizador<br/>navega no mapa"]
    
    Check{"Tile existe<br/>no cache?"}
    
    subgraph CACHE["💾 Cache LRU"]
        Hit["Cache Hit<br/>Retorna tile"]
        Miss["Cache Miss<br/>Requisita tile"]
    end
    
    subgraph DOWNLOAD["⬇️ Download"]
        Try1["Servidor 1<br/>(a.tile.osm.org)"]
        Try2["Servidor 2<br/>(b.tile.osm.org)"]
        Try3["Servidor 3<br/>(c.tile.osm.org)"]
        Error["Placeholder<br/>(Cinza)"]
    end
    
    subgraph STORE["📦 Armazenamento"]
        Add["Adiciona ao cache"]
        Evict{"Cache cheio?<br/>(>100 tiles)"}
        Remove["Remove LRU<br/>(Least Recently Used)"]
    end
    
    Render["🎨 Renderiza<br/>no MapPanel"]
    
    Request --> Check
    Check -->|Sim| Hit
    Check -->|Não| Miss
    
    Hit --> Render
    Miss --> Try1
    Try1 -->|200 OK| Add
    Try1 -->|429/503| Try2
    Try2 -->|200 OK| Add
    Try2 -->|429/503| Try3
    Try3 -->|200 OK| Add
    Try3 -->|Erro| Error
    Error --> Render
    
    Add --> Evict
    Evict -->|Sim| Remove
    Evict -->|Não| Render
    Remove --> Render

    classDef userClass fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    classDef cacheClass fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px
    classDef downloadClass fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
    classDef storeClass fill:#f3e5f5,stroke:#6a1b9a,stroke-width:2px
    classDef renderClass fill:#fce4ec,stroke:#880e4f,stroke-width:2px

    class Request userClass
    class Check,Hit,Miss cacheClass
    class Try1,Try2,Try3,Error downloadClass
    class Add,Evict,Remove storeClass
    class Render renderClass
```

---

## 🔄 Processo de Cálculo de Rota (Detalhado)

```mermaid
sequenceDiagram
    actor User as 👤 Utilizador
    participant UI as MainWindow
    participant MAP as MapPanel
    participant OSRM as OSRMService
    participant HTTP as OkHttpClient
    participant API as OSRM API
    participant Model as Route
    participant Utils as RouteUtils
    
    Note over User,Utils: Fase 1: Seleção de Origem
    User->>MAP: Clica no mapa (origem)
    activate MAP
    MAP->>MAP: pixelToLatLon()
    MAP->>UI: onPointSelected(origin)
    deactivate MAP
    activate UI
    UI->>UI: selectedOrigin = location
    UI->>MAP: drawMarker(origin, GREEN, "A")
    UI->>User: Exibe marcador verde
    deactivate UI
    
    Note over User,Utils: Fase 2: Seleção de Destino
    User->>MAP: Clica no mapa (destino)
    activate MAP
    MAP->>MAP: pixelToLatLon()
    MAP->>UI: onPointSelected(destination)
    deactivate MAP
    activate UI
    UI->>UI: selectedDestination = location
    UI->>MAP: drawMarker(destination, RED, "B")
    UI->>User: Exibe marcador vermelho
    
    Note over User,Utils: Fase 3: Cálculo de Rota
    UI->>OSRM: calculateRoute(origin, dest, CAR)
    deactivate UI
    activate OSRM
    
    OSRM->>OSRM: validateCoordinates()
    alt Coordenadas Inválidas
        OSRM-->>UI: throw OSRMException
        UI-->>User: Exibe erro
    end
    
    OSRM->>OSRM: buildRequestUrl()
    Note right of OSRM: URL: /route/v1/driving/<br/>lat1,lon1;lat2,lon2
    
    OSRM->>HTTP: get(url)
    deactivate OSRM
    activate HTTP
    
    HTTP->>HTTP: new Request.Builder()
    HTTP->>HTTP: addHeader("User-Agent")
    HTTP->>API: HTTP GET
    deactivate HTTP
    activate API
    
    API->>API: Calcula rota otimizada
    Note right of API: Algoritmo de Dijkstra<br/>sobre grafo de ruas
    API->>HTTP: JSON Response
    deactivate API
    activate HTTP
    
    alt Status 429 (Too Many Requests)
        HTTP->>HTTP: wait(exponentialBackoff)
        HTTP->>API: Retry request
    else Status 503 (Service Unavailable)
        HTTP->>HTTP: switchToFallbackServer()
        HTTP->>API: Retry request
    else Status 200 (OK)
        HTTP->>OSRM: Return JSON string
    end
    deactivate HTTP
    activate OSRM
    
    Note over User,Utils: Fase 4: Parsing e Validação
    OSRM->>OSRM: ObjectMapper.readValue()
    OSRM->>OSRM: extractWaypoints()
    OSRM->>OSRM: extractDistance()
    OSRM->>OSRM: extractDuration()
    OSRM->>OSRM: decodePolyline()
    
    OSRM->>Model: new Route(waypoints, distance, duration)
    activate Model
    Model->>Model: validateData()
    Model->>OSRM: return route
    deactivate Model
    
    OSRM->>UI: return route
    deactivate OSRM
    activate UI
    
    Note over User,Utils: Fase 5: Visualização
    UI->>Utils: formatDistance(route.distance)
    activate Utils
    Utils->>UI: "5.2 km"
    deactivate Utils
    
    UI->>Utils: formatDuration(route.duration)
    activate Utils
    Utils->>UI: "15 min"
    deactivate Utils
    
    UI->>UI: distanceLabel.setText("5.2 km")
    UI->>UI: durationLabel.setText("15 min")
    UI->>MAP: drawRoute(route)
    deactivate UI
    
    activate MAP
    MAP->>MAP: drawPolyline(waypoints, BLUE)
    MAP->>User: Exibe rota no mapa
    deactivate MAP
```

---

## 🔍 Processo de Pesquisa de Localização

```mermaid
flowchart TD
    Start([Utilizador digita<br/>no campo de pesquisa])
    
    Input[Captura texto<br/>TextField.getText]
    
    Validate{Texto<br/>válido?}
    
    NominatimService[NominatimService<br/>searchLocation query]
    
    RateLimit[Respeita Rate Limit<br/>1 req/seg]
    
    BuildURL[Constrói URL<br/>search?q=query&format=json]
    
    HTTPRequest[OkHttpClientService<br/>GET request]
    
    NominatimAPI[Nominatim API<br/>Processa pesquisa]
    
    ParseJSON[Jackson<br/>Parse JSON array]
    
    CreateLocations[Criar lista de<br/>Location objects]
    
    DisplayResults[Exibe resultados<br/>em lista dropdown]
    
    UserSelects{Utilizador<br/>seleciona?}
    
    CenterMap[Centraliza mapa<br/>na localização]
    
    AddMarker[Adiciona marcador<br/>no mapa]
    
    End([Fim])
    
    Start --> Input
    Input --> Validate
    Validate -->|Não vazio| NominatimService
    Validate -->|Vazio| End
    NominatimService --> RateLimit
    RateLimit --> BuildURL
    BuildURL --> HTTPRequest
    HTTPRequest --> NominatimAPI
    NominatimAPI --> ParseJSON
    ParseJSON --> CreateLocations
    CreateLocations --> DisplayResults
    DisplayResults --> UserSelects
    UserSelects -->|Sim| CenterMap
    UserSelects -->|Não| End
    CenterMap --> AddMarker
    AddMarker --> End
    
    style Start fill:#c8e6c9,stroke:#2e7d32
    style End fill:#ffcdd2,stroke:#c62828
    style NominatimService fill:#fff9c4,stroke:#f57f17
    style NominatimAPI fill:#e1bee7,stroke:#6a1b9a
```

---

## ⚙️ Gestão de Threads e Concorrência

```mermaid
flowchart LR
    subgraph "🎯 Main Thread (EDT - Event Dispatch Thread)"
        MainWindow[MainWindow<br/>UI Components]
        MapPanel[MapPanel<br/>Rendering]
        EventHandlers[Event Handlers<br/>Mouse/Keyboard]
    end
    
    subgraph "⚡ Background Threads (ExecutorService)"
        TileLoader1[Tile Loader 1<br/>Download tile a.tile.osm.org]
        TileLoader2[Tile Loader 2<br/>Download tile b.tile.osm.org]
        TileLoader3[Tile Loader 3<br/>Download tile c.tile.osm.org]
        TileLoader4[Tile Loader 4<br/>Download tile a.tile.osm.org]
        TileLoader5[Tile Loader 5<br/>Download tile b.tile.osm.org]
        TileLoader6[Tile Loader 6<br/>Download tile c.tile.osm.org]
    end
    
    subgraph "💾 Shared Resources"
        Cache[Tile Cache<br/>LinkedHashMap<br/>Thread-safe via<br/>synchronized]
        Queue[Download Queue<br/>BlockingQueue<br/>Thread-safe]
    end
    
    EventHandlers -->|Pan/Zoom Event| Queue
    Queue -->|Take| TileLoader1
    Queue -->|Take| TileLoader2
    Queue -->|Take| TileLoader3
    Queue -->|Take| TileLoader4
    Queue -->|Take| TileLoader5
    Queue -->|Take| TileLoader6
    
    TileLoader1 -->|Put| Cache
    TileLoader2 -->|Put| Cache
    TileLoader3 -->|Put| Cache
    TileLoader4 -->|Put| Cache
    TileLoader5 -->|Put| Cache
    TileLoader6 -->|Put| Cache
    
    Cache -->|SwingUtilities.invokeLater| MapPanel
    MapPanel -->|paintComponent| MainWindow
    
    style MainWindow fill:#e3f2fd,stroke:#1565c0
    style Queue fill:#fff3e0,stroke:#e65100
    style Cache fill:#f3e5f5,stroke:#6a1b9a
```

---

## 🔄 Tratamento de Erros e Retry

```mermaid
flowchart TD
    Request[Requisição HTTP<br/>à API Externa]
    
    Execute[OkHttpClient<br/>execute request]
    
    Response{Status<br/>Code?}
    
    Success[200 OK<br/>Processa resposta]
    
    TooMany[429 Too Many Requests<br/>Rate limit excedido]
    
    Unavailable[503 Service Unavailable<br/>Servidor temporariamente down]
    
    OtherError[4xx/5xx<br/>Outros erros]
    
    Backoff[Exponential Backoff<br/>wait = 2^attempt * 100ms]
    
    Fallback[Switch Fallback Server<br/>a.tile → b.tile → c.tile]
    
    MaxRetries{Tentativas<br/>< maxRetries?}
    
    LogError[Log erro<br/>Logback ERROR level]
    
    ThrowException[Lançar Exception<br/>OSRMException ou<br/>NominatimException]
    
    Return[Retorna resultado<br/>ao chamador]
    
    Request --> Execute
    Execute --> Response
    
    Response -->|200| Success
    Response -->|429| TooMany
    Response -->|503| Unavailable
    Response -->|Outros| OtherError
    
    Success --> Return
    
    TooMany --> Backoff
    Backoff --> MaxRetries
    
    Unavailable --> Fallback
    Fallback --> MaxRetries
    
    OtherError --> LogError
    LogError --> ThrowException
    
    MaxRetries -->|Sim| Execute
    MaxRetries -->|Não| LogError
    
    style Success fill:#c8e6c9,stroke:#2e7d32
    style TooMany fill:#fff9c4,stroke:#f57f17
    style Unavailable fill:#ffecb3,stroke:#ff6f00
    style OtherError fill:#ffccbc,stroke:#d84315
    style ThrowException fill:#ffcdd2,stroke:#c62828
```

---

## 📊 Métricas de Performance

### Tempos Médios de Operações

| Operação | Tempo Médio | Tempo Máximo | Meta |
|----------|-------------|--------------|------|
| Carregamento de tile | 250ms | 500ms | < 500ms |
| Cálculo de rota | 800ms | 2000ms | < 2s |
| Parsing JSON | 50ms | 100ms | < 200ms |
| Renderização frame | 16ms | 33ms | 60 FPS |
| Geocodificação | 600ms | 1500ms | < 2s |
| Cache lookup | 1ms | 5ms | < 10ms |

### Utilização de Recursos

| Recurso | Utilização Média | Pico | Limite |
|---------|------------------|------|--------|
| Memória RAM | 400MB | 800MB | 1GB |
| CPU (idle) | 2% | 5% | - |
| CPU (cálculo rota) | 15% | 30% | - |
| Threads ativas | 8 | 12 | 20 |
| Cache size | 50 tiles | 100 tiles | 100 tiles |
| Network bandwidth | 500KB/s | 2MB/s | - |

---

## 📖 Referências

- [Diagrama de Arquitetura](./DIAGRAMA_ARQUITETURA_COMPLETA.md)
- [Diagrama de Classes](./DIAGRAMA_CLASSES.md)
- [Requisitos](./REQUISITOS.md)
- [Padrões de Projeto](./PADROES_E_BOAS_PRATICAS.md)

---

**Documento criado em**: 5 de Novembro de 2025
