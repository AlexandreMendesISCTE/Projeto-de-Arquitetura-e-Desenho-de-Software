# 🏗️ Arquitetura e Segurança - Map Route Explorer

## Índice

1. [Visão Geral da Arquitetura](#visão-geral-da-arquitetura)
2. [Diagrama de Contexto (C4 Level 1)](#diagrama-de-contexto-c4-level-1)
3. [Diagrama de Containers (C4 Level 2)](#diagrama-de-containers-c4-level-2)
4. [Diagrama de Componentes (C4 Level 3)](#diagrama-de-componentes-c4-level-3)
5. [Diagrama de Deployment](#diagrama-de-deployment)
6. [Arquitetura de Segurança](#arquitetura-de-segurança)
7. [Fluxo de Dados e Comunicação](#fluxo-de-dados-e-comunicação)
8. [Modelo de Ameaças](#modelo-de-ameaças)

---

## Visão Geral da Arquitetura

O Map Route Explorer é uma aplicação web moderna baseada em arquitetura **SPA (Single Page Application)** com deployment containerizado. A aplicação segue os princípios de:

- **Separação de Responsabilidades**: Frontend, APIs externas e proxy reverso
- **Stateless**: Não mantém estado no servidor
- **API-First**: Integração com múltiplas APIs externas
- **Containerização**: Deploy via Docker com Nginx

### Stack Tecnológico

| Camada           | Tecnologia              | Propósito                  |
| ---------------- | ----------------------- | -------------------------- |
| Frontend         | React 18 + TypeScript   | Interface do utilizador    |
| Build Tool       | Vite 5                  | Bundling e dev server      |
| State Management | Zustand                 | Estado global da aplicação |
| Data Fetching    | React Query             | Cache e sincronização      |
| Styling          | Tailwind CSS            | UI responsiva              |
| Maps             | Leaflet + React Leaflet | Renderização de mapas      |
| Web Server       | Nginx Alpine            | Servir assets e proxy      |
| Container        | Docker                  | Deployment e isolamento    |

---

## Diagrama de Contexto (C4 Level 1)

Este diagrama mostra o sistema Map Route Explorer e suas interações com utilizadores e sistemas externos.

```mermaid
C4Context
    title Diagrama de Contexto - Map Route Explorer

    Person(user, "Utilizador", "Pessoa que quer planear rotas e explorar mapas")
    Person(mobile_user, "Utilizador Mobile", "Acede via smartphone ou tablet")

    System(mre, "Map Route Explorer", "Aplicacao web para planeamento de rotas com multiplos modos de transporte")

    System_Ext(osm, "OpenStreetMap", "Fornece tiles de mapa e dados geograficos")
    System_Ext(google_places, "Google Places API", "Servico primario de geocodificacao e pesquisa")
    System_Ext(nominatim, "Nominatim API", "Servico de geocodificacao fallback")
    System_Ext(google_maps, "Google Maps Directions API", "Calculo de rotas para todos os modos de transporte")
    System_Ext(n8n, "n8n Webhook", "Chatbot assistente para ajuda com rotas")
    System_Ext(overpass, "Overpass API", "Consulta de POIs do OpenStreetMap")

    Rel(user, mre, "Utiliza", "HTTPS")
    Rel(mobile_user, mre, "Utiliza", "HTTPS")

    Rel(mre, osm, "Carrega tiles", "HTTPS")
    Rel(mre, google_places, "Pesquisa enderecos", "HTTPS")
    Rel(mre, nominatim, "Pesquisa enderecos fallback", "HTTPS")
    Rel(mre, google_maps, "Calcula rotas", "HTTPS")
    Rel(mre, n8n, "Envia mensagens", "HTTPS")
    Rel(mre, overpass, "Consulta POIs", "HTTPS")
```

---

## Diagrama de Containers (C4 Level 2)

Este diagrama detalha os containers que compõem o sistema.

```mermaid
C4Container
    title Diagrama de Containers - Map Route Explorer

    Person(user, "Utilizador", "Planeia rotas e explora mapas")

    Container_Boundary(app_boundary, "Map Route Explorer") {
        Container(spa, "Single Page Application", "React, TypeScript, Vite", "Interface interativa para visualizacao de mapas e planeamento de rotas")
        Container(nginx, "Nginx Proxy", "Nginx Alpine", "Serve assets estaticos e actua como proxy reverso para APIs externas")
    }

    System_Ext(osm_tiles, "OSM Tile Server", "Fornece tiles de mapa rasterizados")
    System_Ext(google_places_api, "Google Places API", "Geocodificacao primaria")
    System_Ext(nominatim_api, "Nominatim API", "Geocodificacao fallback")
    System_Ext(google_maps_api, "Google Maps Directions API", "Calculo de rotas para todos os modos")
    System_Ext(n8n_webhook, "n8n Workflow", "Chatbot backend")

    Rel(user, nginx, "Acede", "HTTPS:8082")
    Rel(nginx, spa, "Serve", "HTTP")

    Rel(spa, osm_tiles, "Carrega tiles", "HTTPS")
    Rel(spa, google_places_api, "Pesquisa enderecos", "HTTPS")
    Rel(spa, nginx, "Proxy Nominatim fallback", "HTTP")
    Rel(nginx, nominatim_api, "Proxy", "HTTPS")
    Rel(spa, google_maps_api, "Calcula rotas", "HTTPS")
    Rel(spa, nginx, "Proxy n8n", "HTTP")
    Rel(nginx, n8n_webhook, "Proxy", "HTTPS")
```

---

## Diagrama de Componentes (C4 Level 3)

Este diagrama mostra os componentes internos da aplicação React.

```mermaid
C4Component
    title Diagrama de Componentes - Frontend React

    Container_Boundary(spa, "Single Page Application") {

        Component(app, "App.tsx", "React Component", "Componente raiz com providers")
        Component(map_explorer, "MapRouteExplorer", "React Component", "Orchestrador principal da aplicacao")

        Component(map_container, "MapContainer", "React Leaflet", "Renderiza mapa interativo")
        Component(route_layer, "RouteLayer", "React Component", "Desenha polylines das rotas")
        Component(marker_layer, "MarkerLayer", "React Component", "Marcadores de origem destino waypoints")
        Component(poi_layer, "POILayer", "React Component", "Pontos de interesse no mapa")

        Component(location_search, "LocationSearch", "React Component", "Campo de pesquisa com autocomplete")
        Component(transport_selector, "TransportModeSelector", "React Component", "Seleccao do modo de transporte")
        Component(route_info, "RouteInfo", "React Component", "Informacoes de distancia e tempo")
        Component(chat_widget, "ChatWidget", "React Component", "Interface do chatbot")

        Component(route_store, "RouteStore", "Zustand Store", "Estado global de rotas")
        Component(map_store, "MapStore", "Zustand Store", "Estado do mapa")
        Component(search_store, "SearchStore", "Zustand Store", "Estado de pesquisa")
        Component(poi_store, "POIStore", "Zustand Store", "Estado de pontos de interesse")

        Component(google_places_svc, "GooglePlacesService", "Service", "Cliente API Google Places primario")
        Component(nominatim_svc, "NominatimService", "Service", "Cliente API Nominatim fallback")
        Component(google_maps_svc, "GoogleMapsService", "Service", "Cliente Google Maps Directions API")
        Component(n8n_svc, "N8nService", "Service", "Cliente webhook n8n")
    }

    Rel(app, map_explorer, "Renderiza")
    Rel(map_explorer, map_container, "Contem")
    Rel(map_explorer, location_search, "Contem")
    Rel(map_explorer, transport_selector, "Contem")
    Rel(map_explorer, route_info, "Contem")

    Rel(map_container, route_layer, "Renderiza")
    Rel(map_container, marker_layer, "Renderiza")
    Rel(map_container, poi_layer, "Renderiza")

    Rel(location_search, google_places_svc, "Usa primario")
    Rel(location_search, nominatim_svc, "Usa fallback")
    Rel(route_info, google_maps_svc, "Usa")
    Rel(chat_widget, n8n_svc, "Usa")

    Rel(map_explorer, route_store, "Le e escreve")
    Rel(map_explorer, map_store, "Le e escreve")
    Rel(location_search, search_store, "Le e escreve")
    Rel(poi_layer, poi_store, "Le e escreve")
```

---

## Diagrama de Deployment

```mermaid
C4Deployment
    title Diagrama de Deployment - Producao

    Deployment_Node(browser, "Browser", "Chrome Firefox Safari Edge") {
        Container(client_spa, "SPA React", "JavaScript Bundle", "Aplicacao cliente")
    }

    Deployment_Node(docker_host, "Docker Host", "Linux Windows") {
        Deployment_Node(docker_network, "Docker Network", "map-route-explorer-network") {
            Deployment_Node(container, "Container", "nginx alpine") {
                Container(nginx_server, "Nginx", "Web Server", "Serve ficheiros e proxy")
                Container(static_files, "Static Files", "/usr/share/nginx/html", "Bundle React")
            }
        }
    }

    Deployment_Node(external_apis, "APIs Externas", "Internet") {
        Container(google_places_ext, "Google Places", "places.googleapis.com", "Geocoding primario")
        Container(nominatim_ext, "Nominatim", "nominatim.openstreetmap.org", "Geocoding fallback")
        Container(google_maps_ext, "Google Maps", "maps.googleapis.com", "Routing API")
        Container(n8n_ext, "n8n", "yocomsn8n.duckdns.org", "Chatbot webhook")
    }

    Rel(browser, docker_host, "HTTPS", "8082")
    Rel(client_spa, nginx_server, "HTTP")
    Rel(nginx_server, static_files, "Serve")
    Rel(nginx_server, nominatim_ext, "Proxy", "HTTPS")
    Rel(nginx_server, n8n_ext, "Proxy", "HTTPS")
    Rel(client_spa, google_places_ext, "Direct", "HTTPS")
    Rel(client_spa, google_maps_ext, "Direct", "HTTPS")
```

---

## Arquitetura de Segurança

### Diagrama de Segurança - Fluxo de Dados

```mermaid
flowchart TB
    subgraph Internet["🌐 Internet - Zona Não Confiável"]
        User["👤 Utilizador"]
        Attacker["🏴‍☠️ Atacante"]
    end

    subgraph DMZ["🛡️ DMZ - Zona Desmilitarizada"]
        subgraph Docker["🐳 Container Docker"]
            Nginx["🔒 Nginx Proxy<br/>CORS Headers<br/>Security Headers"]
            SPA["📦 SPA Bundle<br/>Minificado<br/>Sem Secrets"]
        end
    end

    subgraph External["🌍 APIs Externas"]
        GooglePlaces["Google Places<br/>🔐 API Key"]
        Nominatim["Nominatim<br/>🔓 Público (Fallback)"]
        GoogleMaps["Google Maps Directions<br/>🔐 API Key"]
        N8N["n8n Webhook<br/>🔐 Autenticado"]
    end

    subgraph Secrets["🔑 Gestão de Secrets"]
        EnvFile[".env.local<br/>API Keys"]
        BuildTime["Build Time<br/>Injection"]
    end

    User -->|"HTTPS:8082"| Nginx
    Attacker -.->|"❌ Bloqueado"| Nginx

    Nginx -->|"Serve"| SPA
    Nginx -->|"Proxy fallback"| Nominatim
    Nginx -->|"Proxy + Auth"| N8N

    SPA -->|"API Key Header"| GooglePlaces
    SPA -->|"API Key Header"| GoogleMaps

    EnvFile -->|"Docker Build Args"| BuildTime
    BuildTime -->|"Embedded"| SPA

    style Attacker fill:#ff6b6b
    style Nginx fill:#51cf66
    style EnvFile fill:#ffd43b
```

### Controlos de Segurança Implementados

```mermaid
flowchart LR
    subgraph Layer1["Camada 1: Rede"]
        FW["🔥 Docker Network<br/>Isolamento"]
        DNS["🌐 DNS Resolver<br/>8.8.8.8"]
    end

    subgraph Layer2["Camada 2: Transporte"]
        TLS["🔒 HTTPS/TLS<br/>Todas as APIs"]
        HSTS["📜 HSTS Header<br/>🔜 Recomendado"]
    end

    subgraph Layer3["Camada 3: Aplicação"]
        CORS["🚫 CORS<br/>Whitelist Origins"]
        CSP["🛡️ CSP Headers<br/>🔜 Recomendado"]
        APIQuotas["⏱️ API Quotas<br/>Google Cloud"]
    end

    subgraph Layer4["Camada 4: Dados"]
        NoStore["💾 No Storage<br/>Stateless"]
        Sanitize["🧹 Input Sanitization<br/>XSS Prevention"]
        APIKeys["🔑 API Keys<br/>Build-time Only"]
    end

    Layer1 --> Layer2 --> Layer3 --> Layer4
```

---

## Fluxo de Dados e Comunicação

### Fluxo de Cálculo de Rota

```mermaid
sequenceDiagram
    autonumber
    participant U as 👤 Utilizador
    participant SPA as 📱 React SPA
    participant Store as 🗄️ Zustand Store
    participant Google as 🗺️ Google Maps Directions API
    participant Map as 🗺️ Leaflet Map

    U->>SPA: Define origem e destino
    SPA->>Store: updateOrigin() / updateDestination()
    Store-->>SPA: Estado atualizado

    U->>SPA: Seleciona modo transporte

    Note over SPA,Google: Todos os modos usam Google Maps Directions API
    SPA->>Google: DirectionsService.route()
    Note over SPA,Google: Modo: DRIVING, BICYCLING, WALKING, TRANSIT
    Google-->>SPA: Route com geometria, distância, tempo

    SPA->>Store: setRoute(routeData)
    Store-->>SPA: Notifica componentes
    SPA->>Map: Renderiza polyline
    Map-->>U: Visualiza rota no mapa
```

### Fluxo de Pesquisa de Localização

```mermaid
sequenceDiagram
    autonumber
    participant U as 👤 Utilizador
    participant Search as 🔍 LocationSearch
    participant GooglePlaces as 🗺️ Google Places API
    participant Nginx as 🔒 Nginx Proxy
    participant Nominatim as 🌍 Nominatim API

    U->>Search: Digita endereço
    Note over Search: Debounce 500ms

    Search->>GooglePlaces: AutocompleteService.getPlacePredictions()
    alt Google Places sucesso
        GooglePlaces-->>Search: Predictions
    else Google Places falha
        Search->>Nginx: GET /nominatim/search?q=...
        Note over Nginx: Adiciona User-Agent<br/>Proxy fallback
        Nginx->>Nominatim: GET /search?q=...
        Nominatim-->>Nginx: JSON results
        Nginx-->>Search: Passa resposta + CORS headers
    end

    Search-->>U: Mostra sugestões autocomplete
    U->>Search: Seleciona resultado
    Search->>Search: updateLocation(coords)
```

### Fluxo de Segurança - API Key Protection

```mermaid
flowchart TB
    subgraph Development["💻 Desenvolvimento"]
        EnvLocal[".env.local<br/>VITE_GOOGLE_MAPS_API_KEY=xxx"]
        GitIgnore[".gitignore<br/>Exclui .env.local"]
    end

    subgraph Build["🔨 Build Process"]
        DockerBuild["docker-compose build"]
        ViteBuild["Vite Build<br/>import.meta.env"]
        Bundle["bundle.js<br/>API Key embedded"]
    end

    subgraph Runtime["⚡ Runtime"]
        Browser["Browser"]
        APICall["fetch(url, {<br/>  headers: {<br/>    'X-Goog-Api-Key': key<br/>  }<br/>})"]
        Google["Google API"]
    end

    subgraph Protection["🛡️ Proteções"]
        Restrict["Google Cloud Console<br/>Restringir por domínio"]
        Quota["Quotas de API<br/>Limite de requests"]
        Monitor["Monitoring<br/>Alertas de uso"]
    end

    EnvLocal -->|"ARG"| DockerBuild
    GitIgnore -.->|"Previne commit"| EnvLocal
    DockerBuild --> ViteBuild
    ViteBuild --> Bundle

    Bundle --> Browser
    Browser --> APICall
    APICall --> Google

    Restrict --> Google
    Quota --> Google
    Monitor --> Google

    style EnvLocal fill:#ffd43b
    style GitIgnore fill:#51cf66
    style Restrict fill:#51cf66
```

---

## Modelo de Ameaças

### Matriz de Ameaças STRIDE

```mermaid
flowchart TB
    subgraph STRIDE["🎯 Modelo STRIDE"]
        S["<b>S</b>poofing<br/>Falsificação de Identidade"]
        T["<b>T</b>ampering<br/>Adulteração de Dados"]
        R["<b>R</b>epudiation<br/>Negação de Ações"]
        I["<b>I</b>nformation Disclosure<br/>Divulgação de Info"]
        D["<b>D</b>enial of Service<br/>Negação de Serviço"]
        E["<b>E</b>levation of Privilege<br/>Escalada de Privilégios"]
    end

    subgraph Mitigations["✅ Mitigações Implementadas"]
        M1["HTTPS + CORS<br/>Previne MITM"]
        M2["Input Validation<br/>Previne Injection"]
        M3["Logs do Nginx<br/>Auditoria"]
        M4["API Keys Protegidas<br/>Não expostas em repo"]
        M5["API Quotas<br/>Google Cloud Console"]
        M6["Stateless + No Auth<br/>Sem escalada possível"]
    end

    S --> M1
    T --> M2
    R --> M3
    I --> M4
    D --> M5
    E --> M6

    style S fill:#ff6b6b
    style T fill:#ffa94d
    style R fill:#ffd43b
    style I fill:#69db7c
    style D fill:#74c0fc
    style E fill:#b197fc
```

### Superfície de Ataque

```mermaid
flowchart LR
    subgraph Attack_Surface["🎯 Superfície de Ataque"]
        direction TB
        A1["🌐 Porta 8082<br/>Entrada HTTP"]
        A2["📡 APIs Externas<br/>Dependências"]
        A3["📦 Bundle JS<br/>Código Cliente"]
        A4["🔑 API Keys<br/>Credenciais"]
    end

    subgraph Risks["⚠️ Riscos"]
        R1["DDoS / Brute Force"]
        R2["API Downtime"]
        R3["XSS / Code Injection"]
        R4["Key Exposure"]
    end

    subgraph Controls["🛡️ Controlos"]
        C1["Health Check<br/>API Quotas"]
        C2["Google Places Fallback<br/>Cache React Query"]
        C3["Security Headers<br/>X-Frame-Options, etc"]
        C4["Domain Restriction<br/>Rotation Policy"]
    end

    A1 --> R1 --> C1
    A2 --> R2 --> C2
    A3 --> R3 --> C3
    A4 --> R4 --> C4
```

---

## Configuração de Segurança do Nginx

### Headers de Segurança Recomendados

```mermaid
flowchart TB
    subgraph Headers["📜 Security Headers"]
        H1["X-Frame-Options: DENY<br/>Previne Clickjacking"]
        H2["X-Content-Type-Options: nosniff<br/>Previne MIME Sniffing"]
        H3["X-XSS-Protection: 1; mode=block<br/>Filtro XSS Browser"]
        H4["Referrer-Policy: strict-origin<br/>Controla Referrer"]
        H5["Content-Security-Policy<br/>Whitelist de recursos"]
        H6["Strict-Transport-Security<br/>Força HTTPS"]
    end

    subgraph CSP["🔐 Content Security Policy"]
        CSP1["default-src 'self'"]
        CSP2["script-src 'self'"]
        CSP3["style-src 'self' 'unsafe-inline'"]
        CSP4["img-src 'self' data: https://*.tile.openstreetmap.org"]
        CSP5["connect-src 'self' https://*.googleapis.com https://*.openstreetmap.org"]
    end

    Headers --> CSP
```

---

## Checklist de Segurança

### Controlos Implementados ✅

| Controlo          | Status | Descrição                          |
| ----------------- | ------ | ---------------------------------- |
| HTTPS para APIs   | ✅     | Todas as comunicações encriptadas  |
| CORS configurado  | ✅     | Nginx adiciona headers apropriados |
| API Keys em .env  | ✅     | Não commitadas no repositório      |
| .gitignore        | ✅     | Exclui ficheiros sensíveis         |
| Rate Limiting     | ✅     | Nginx limita requests              |
| Health Check      | ✅     | Docker health check configurado    |
| Gzip Compression  | ✅     | Reduz bandwidth                    |
| Cache Headers     | ✅     | Assets com cache longo             |
| No Server Version | ✅     | Nginx não expõe versão             |
| DNS Resolver      | ✅     | Usa Google DNS (8.8.8.8)           |

### Controlos Recomendados 🔜

| Controlo            | Prioridade | Descrição                      |
| ------------------- | ---------- | ------------------------------ |
| Rate Limiting       | Alta       | Limitação de requests no Nginx |
| CSP Headers         | Alta       | Content Security Policy        |
| HSTS Header         | Alta       | Strict-Transport-Security      |
| WAF                 | Média      | Web Application Firewall       |
| API Key Rotation    | Média      | Rotação periódica de keys      |
| Audit Logs          | Média      | Centralização de logs          |
| Penetration Testing | Baixa      | Testes de segurança            |

---

## Resumo da Arquitetura

```mermaid
flowchart TB
    subgraph Summary["📊 Resumo Arquitetural"]
        direction LR

        subgraph Frontend["Frontend"]
            React["⚛️ React 18"]
            TS["📘 TypeScript"]
            Zustand["🐻 Zustand"]
            RQ["🔄 React Query"]
        end

        subgraph Infra["Infraestrutura"]
            Docker["🐳 Docker"]
            Nginx["🔒 Nginx"]
            Vite["⚡ Vite"]
        end

        subgraph APIs["APIs Externas"]
            OSM["🗺️ OpenStreetMap"]
            GooglePlaces2["📍 Google Places"]
            Nom["📍 Nominatim (Fallback)"]
            GoogleMaps2["🗺️ Google Maps Directions"]
            N8N2["🤖 n8n"]
        end
    end

    Frontend --> Infra
    Infra --> APIs

    style React fill:#61dafb
    style Docker fill:#2496ed
    style Nginx fill:#009639
```

---

_Documento de Arquitetura e Segurança - Map Route Explorer v3.0_  
_Gerado em 10/12/2025_
