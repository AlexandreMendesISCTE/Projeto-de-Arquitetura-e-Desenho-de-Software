# ðŸ—ºï¸ Map Route Explorer

> **Sistema Interativo de Rotas e ExploraÃ§Ã£o de Locais com OpenStreetMap**

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.java.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-VNC_Ready-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

Uma aplicaÃ§Ã£o desktop interativa desenvolvida em Java que permite aos utilizadores explorar mapas baseados em dados do OpenStreetMap, traÃ§ar rotas entre pontos de interesse e obter informaÃ§Ãµes relevantes sobre o trajeto e a Ã¡rea circundante.

**VersÃ£o**: 2.0.0 | **Status**: Em desenvolvimento ativo

## ï¿½ Autores

Este projeto foi desenvolvido por:

- **Alexandre Mendes** (111026)
- **Manuel Santos**
- **AndrÃ© Costa**
- **Ana Valente**

**InstituiÃ§Ã£o**: Instituto Superior de CiÃªncias do Trabalho e da Empresa (ISCTE-IUL)  
**Curso**: Engenharia InformÃ¡tica

## ï¿½ðŸš€ InÃ­cio RÃ¡pido

### **ExecuÃ§Ã£o via Docker (Recomendado)** ðŸ³

```bash
# Clone o repositÃ³rio
git clone https://github.com/AlexandreMendesISCTE/Projeto-de-Arquitetura-e-Desenho-de-Software.git
cd Projeto-de-Arquitetura-e-Desenho-de-Software

# Inicie com Docker Compose
docker compose up -d

# Acesse via Browser (noVNC) - http://localhost:6080
```

**Acesso Ã  AplicaÃ§Ã£o:**
- ðŸŒ **Browser (noVNC)**: http://localhost:6080 (sem senha)
- ðŸ–¥ï¸ **VNC Viewer**: `localhost:5901` | Senha: `maproute123`

### **ExecuÃ§Ã£o Local**

A aplicaÃ§Ã£o utiliza uma **implementaÃ§Ã£o nativa em Java puro** para renderizaÃ§Ã£o de mapas com Swing e Graphics2D.

**PrÃ©-requisitos:** Java 17+ | Maven 3.6+

```bash
# Usando scripts (recomendado)
./run-native.sh          # Linux/Mac/Git Bash
run-native.bat           # Windows

# Ou manualmente
mvn clean package -DskipTests
java -jar target/map-route-explorer-2.0.0-jar-with-dependencies.jar
```

## ðŸ"‹ Ãndice

- [VisÃ£o Geral](#-visÃ£o-geral)
- [Diagramas do Sistema](#-diagramas-do-sistema)
  - [Fluxo de Dados e Requisitos](#-fluxo-de-dados-e-requisitos)
  - [Arquitetura em Camadas](#ï¸-arquitetura-em-camadas)
- [Funcionalidades](#-funcionalidades)
- [Tecnologias](#ï¸-tecnologias)
- [UtilizaÃ§Ã£o](#-utilizaÃ§Ã£o)
- [Arquitetura](#ï¸-arquitetura)
- [Desenvolvimento](#-desenvolvimento)
- [DocumentaÃ§Ã£o](#-documentaÃ§Ã£o)
- [LicenÃ§a](#-licenÃ§a)

## ðŸŽ¯ VisÃ£o Geral

O **Map Route Explorer** Ã© um projeto acadÃ©mico desenvolvido no Ã¢mbito da disciplina de Arquitetura e Desenho de Software, que demonstra a integraÃ§Ã£o de mÃºltiplas APIs REST para criar uma experiÃªncia de navegaÃ§Ã£o e exploraÃ§Ã£o geogrÃ¡fica completa.

### Objetivos

- **ExploraÃ§Ã£o GeogrÃ¡fica**: Mapas interativos baseados no OpenStreetMap
- **CÃ¡lculo de Rotas**: IntegraÃ§Ã£o com API OSRM para rotas otimizadas
- **GeocodificaÃ§Ã£o**: API Nominatim para conversÃ£o de endereÃ§os
- **VisualizaÃ§Ã£o de Dados**: ApresentaÃ§Ã£o clara de informaÃ§Ãµes de rota

### Contexto AcadÃ©mico

Desenvolvido com metodologia **SCRUM**, utilizando Trello para gestÃ£o de projeto e documentaÃ§Ã£o com notaÃ§Ãµes **UML** e **BPMN**. Todo o cÃ³digo estÃ¡ disponÃ­vel no GitHub com documentaÃ§Ã£o completa.

### Destaques TÃ©cnicos

- âœ… **RenderizaÃ§Ã£o Nativa**: ImplementaÃ§Ã£o 100% Java sem dependÃªncias externas (JMapViewer, JavaFX)
- âœ… **Performance Otimizada**: Cache LRU, thread pool de 6 threads, download concorrente
- âœ… **DetecÃ§Ã£o Inteligente**: DiferenciaÃ§Ã£o automÃ¡tica entre arrastar (drag) e clicar (click)
- âœ… **Testes Completos**: UnitÃ¡rios e de integraÃ§Ã£o com cobertura >80%

## ðŸ"Š Diagramas do Sistema

### ðŸ"„ Fluxo de Dados e Requisitos

```mermaid
flowchart TB
    subgraph USER["ðŸ'¤ Utilizador"]
        UI[Interface Swing]
    end
    
    subgraph CORE["ðŸŽ¯ Sistema Core"]
        CTRL[Controller/Main]
        MAP[MapPanel - RenderizaÃ§Ã£o]
        CACHE[Cache LRU<br/>100 tiles]
    end
    
    subgraph SERVICES["ðŸ"§ ServiÃ§os"]
        HTTP[HttpClientService]
        OSRM[OSRMService]
        NOM[NominatimService]
    end
    
    subgraph APIS["ðŸŒ APIs Externas"]
        OSM[(OpenStreetMap<br/>Tiles)]
        OSRM_API[(OSRM API<br/>Rotas)]
        NOM_API[(Nominatim API<br/>Geocoding)]
    end
    
    subgraph RF["ðŸ"‹ Requisitos Funcionais"]
        RF1[RF01: Visualizar Mapa]
        RF2[RF02: Calcular Rota]
        RF3[RF03: Pesquisar Local]
        RF4[RF04: Selecionar Pontos]
        RF5[RF05: Modos Transporte]
    end
    
    subgraph RNF["âš¡ Requisitos NÃ£o Funcionais"]
        RNF1[RNF01: Performance<br/>Cache + Threads]
        RNF2[RNF02: Usabilidade<br/>DetecÃ§Ã£o Drag/Click]
        RNF3[RNF03: Escalabilidade<br/>Thread Pool]
        RNF4[RNF04: Manutenibilidade<br/>PadrÃµes MVC]
    end
    
    UI -->|1. AÃ§Ã£o Utilizador| CTRL
    CTRL -->|2. Atualizar UI| MAP
    MAP -->|3. Requisitar Tiles| CACHE
    CACHE -->|Cache Miss| HTTP
    HTTP -->|4. Download| OSM
    
    CTRL -->|5. Pesquisar| NOM
    NOM --> HTTP
    HTTP --> NOM_API
    
    CTRL -->|6. Calcular Rota| OSRM
    OSRM --> HTTP
    HTTP --> OSRM_API
    
    MAP -.->|Implementa| RF1
    MAP -.->|Implementa| RF4
    OSRM -.->|Implementa| RF2
    OSRM -.->|Implementa| RF5
    NOM -.->|Implementa| RF3
    
    CACHE -.->|Garante| RNF1
    MAP -.->|Garante| RNF2
    HTTP -.->|Garante| RNF3
    CORE -.->|Garante| RNF4
    
    style USER fill:#e1f5ff
    style CORE fill:#fff4e1
    style SERVICES fill:#f0f0f0
    style APIS fill:#e8f5e9
    style RF fill:#fff3e0
    style RNF fill:#f3e5f5
```

**Legenda:**
- ðŸ"µ **Fluxo de Dados**: Linha sÃ³lida mostra o caminho dos dados
- ðŸ"— **ImplementaÃ§Ã£o**: Linha tracejada conecta componentes aos requisitos
- âš¡ **5 RF Principais**: VisualizaÃ§Ã£o, Rotas, Pesquisa, SeleÃ§Ã£o, Modos
- ðŸ"Š **4 RNF Chave**: Performance, Usabilidade, Escalabilidade, Manutenibilidade

### ðŸ—ï¸ Arquitetura em Camadas

```mermaid
graph TB
    subgraph PRESENTATION["ðŸŽ¨ Camada de ApresentaÃ§Ã£o"]
        MW[MainWindow<br/>JFrame Principal]
        MP[MapPanel<br/>RenderizaÃ§Ã£o Mapa]
        CP[ControlPanel<br/>BotÃµes e Inputs]
    end
    
    subgraph CONTROLLER["ðŸŽ® Camada de Controlo"]
        MAIN[Main<br/>Bootstrap]
        CFG[ConfigManager<br/>Singleton]
        PSL[PointSelectionListener<br/>Observer Pattern]
    end
    
    subgraph SERVICE["ðŸ"§ Camada de ServiÃ§os"]
        HTTP[HttpClientService<br/>Adapter Pattern]
        OSRM[OSRMService<br/>Facade Pattern]
        NOM[NominatimService<br/>Facade Pattern]
    end
    
    subgraph MODEL["ðŸ"Š Camada de Modelo"]
        LOC[Location<br/>Coordenadas]
        ROUTE[Route<br/>Dados Rota]
        TM[TransportMode<br/>Strategy Pattern]
    end
    
    subgraph UTIL["ðŸ› ï¸ Camada UtilitÃ¡ria"]
        RU[RouteUtils<br/>FormataÃ§Ã£o]
        TC[TileCache<br/>LRU Cache]
        TP[ThreadPool<br/>6 Threads]
    end
    
    subgraph EXTERNAL["ðŸŒ ServiÃ§os Externos"]
        OSM_API[OpenStreetMap]
        OSRM_API[OSRM API]
        NOM_API[Nominatim API]
    end
    
    MW --> MP
    MW --> CP
    MW --> PSL
    
    PSL --> MAIN
    MAIN --> CFG
    MAIN --> OSRM
    MAIN --> NOM
    
    MP --> TC
    MP --> TP
    
    OSRM --> HTTP
    NOM --> HTTP
    
    HTTP --> OSM_API
    HTTP --> OSRM_API
    HTTP --> NOM_API
    
    OSRM --> ROUTE
    OSRM --> TM
    NOM --> LOC
    
    ROUTE --> RU
    
    style PRESENTATION fill:#e1f5ff
    style CONTROLLER fill:#fff4e1
    style SERVICE fill:#f0f0f0
    style MODEL fill:#e8f5e9
    style UTIL fill:#fce4ec
    style EXTERNAL fill:#f3e5f5
    
    classDef pattern fill:#ffe0b2,stroke:#ff6f00,stroke-width:2px
    class CFG,PSL,HTTP,OSRM,NOM,TM pattern
```

**PadrÃµes de Projeto Aplicados:**

| PadrÃ£o | Componente | BenefÃ­cio |
|--------|-----------|-----------|
| **MVC** | SeparaÃ§Ã£o UI/Controller/Model | Manutenibilidade |
| **Singleton** | ConfigManager | InstÃ¢ncia Ãºnica de config |
| **Observer** | PointSelectionListener | Desacoplamento UI â†" Controller |
| **Adapter** | HttpClientService | AbstraÃ§Ã£o OkHttp |
| **Facade** | OSRMService, NominatimService | SimplificaÃ§Ã£o APIs |
| **Strategy** | TransportMode | Algoritmos intercambiÃ¡veis |

**PrincÃ­pios SOLID:**
- âœ… **SRP**: Cada classe tem uma responsabilidade
- âœ… **OCP**: ExtensÃ­vel via interfaces (TransportMode)
- âœ… **LSP**: Subtipos substituÃ­veis
- âœ… **ISP**: Interfaces especÃ­ficas (PointSelectionListener)
- âœ… **DIP**: DependÃªncias de abstraÃ§Ãµes (HttpClientService)

## âœ¨ Funcionalidades

### âœ… Implementadas

#### ðŸ—ºï¸ VisualizaÃ§Ã£o de Mapa
- Mapa interativo com tiles do OpenStreetMap
- Zoom (18 nÃ­veis) e pan com detecÃ§Ã£o inteligente drag vs click
- SeleÃ§Ã£o de pontos por clique
- Cache LRU de 100 tiles
- Thread pool de 6 threads para download concorrente

#### ðŸ›£ï¸ CÃ¡lculo de Rotas
- API OSRM para cÃ¡lculo otimizado
- Desenho visual de rotas no mapa
- Marcadores de origem (A - verde) e destino (B - vermelho)
- Cores diferentes por modo de transporte

#### ï¿½ InformaÃ§Ãµes de Rota
- DistÃ¢ncia total (km)
- Tempo estimado (minutos)
- InstruÃ§Ãµes de navegaÃ§Ã£o
- AtualizaÃ§Ã£o em tempo real

#### ðŸš— Modos de Transporte
- ðŸš— AutomÃ³vel (rotas otimizadas)
- ðŸš´ Bicicleta (ciclovias)
- ðŸš¶ A pÃ© (rotas pedonais)

#### ðŸ” Pesquisa de LocalizaÃ§Ã£o
- API Nominatim para geocodificaÃ§Ã£o
- CentralizaÃ§Ã£o automÃ¡tica do mapa

#### ðŸ”„ GestÃ£o de SessÃ£o
- Limpeza de pontos e rotas
- ReinÃ­cio sem restart da aplicaÃ§Ã£o

### â³ Planeadas (Roadmap)

| Funcionalidade | VersÃ£o | Trimestre |
|----------------|---------|-----------|
| ðŸ›ï¸ Pontos de Interesse (Overpass API) | 2.1.0 | Q1 2026 |
| ðŸ“ˆ EstatÃ­sticas AvanÃ§adas | 2.1.0 | Q1 2026 |
| ðŸŽ¯ MÃºltiplos Destinos | 2.2.0 | Q2 2026 |
| ðŸ’¾ ExportaÃ§Ã£o GPX/JSON | 2.2.0 | Q2 2026 |
| ðŸŒ API REST PÃºblica | 3.0.0 | Q3 2026 |
| ðŸ“± Modo Offline | 3.0.0 | Q3 2026 |

## ðŸ› ï¸ Tecnologias

| Categoria | Tecnologia | VersÃ£o | PropÃ³sito |
|-----------|------------|--------|-----------|
| **Linguagem** | Java | 17+ | Linguagem principal |
| **Build** | Maven | 3.9+ | GestÃ£o de dependÃªncias |
| **UI** | Swing + Graphics2D | Nativo | Interface e renderizaÃ§Ã£o |
| **HTTP Client** | OkHttp | 5.0.0 | RequisiÃ§Ãµes REST |
| **JSON** | Jackson | 2.18.2 | Parsing/SerializaÃ§Ã£o |
| **Logging** | Logback | 1.5.15 | Sistema de logs |
| **Testes** | JUnit 5 + Mockito | 5.11.3 / 5.14.2 | Framework de testes |
| **Container** | Docker + VNC | Latest | Deployment |

### APIs Externas

- **OpenStreetMap** - Tiles de mapas e dados cartogrÃ¡ficos
- **OSRM** - CÃ¡lculo e otimizaÃ§Ã£o de rotas
- **Nominatim** - GeocodificaÃ§Ã£o e pesquisa de locais
- **Overpass API** - Pontos de interesse (planeado)

## ðŸš€ UtilizaÃ§Ã£o

### NavegaÃ§Ã£o no Mapa

| AÃ§Ã£o | Como Fazer |
|------|------------|
| **Zoom In** | Roda do mouse para frente ou duplo clique |
| **Zoom Out** | Roda do mouse para trÃ¡s |
| **Pan (Arrastar)** | Clique + arraste (movimento > 5 pixels) |
| **Selecionar Ponto** | Clique simples (movimento < 5 pixels) |

> ðŸ’¡ **DetecÃ§Ã£o Inteligente**: O sistema diferencia automaticamente entre arrastar e clicar baseado no movimento do mouse.

### Fluxo de Trabalho

1. **Pesquisar LocalizaÃ§Ã£o** (opcional)
   - Digite endereÃ§o no campo de pesquisa (ex: "Lisboa, Portugal")
   - Pressione Enter ou clique em "Pesquisar"
   - O mapa serÃ¡ centralizado na localizaÃ§Ã£o

2. **Selecionar Pontos**
   - Clique no mapa para marcar origem (marcador verde - A)
   - Clique novamente para marcar destino (marcador vermelho - B)

3. **Calcular Rota**
   - Escolha o modo de transporte (ðŸš— ðŸš´ ðŸš¶)
   - Clique em "Calcular Rota"
   - Visualize a rota desenhada e informaÃ§Ãµes no painel lateral

4. **Limpar e RecomeÃ§ar**
   - Clique em "Limpar" para remover pontos e rotas
   - Repita o processo para nova rota

## ðŸ—ï¸ Arquitetura

### Estrutura de Camadas

```
ðŸ“¦ Map Route Explorer
â”‚
â”œâ”€â”€ ðŸŽ¨ UI Layer (Swing)
â”‚   â”œâ”€â”€ MainWindow - Janela principal
â”‚   â””â”€â”€ MapPanel - RenderizaÃ§Ã£o de mapas
â”‚
â”œâ”€â”€ ðŸŽ® Controller Layer
â”‚   â”œâ”€â”€ Main - Bootstrap da aplicaÃ§Ã£o
â”‚   â””â”€â”€ ConfigManager - ConfiguraÃ§Ãµes
â”‚
â”œâ”€â”€ ðŸ”§ Service Layer
â”‚   â”œâ”€â”€ OSRMService - CÃ¡lculo de rotas
â”‚   â”œâ”€â”€ NominatimService - GeocodificaÃ§Ã£o
â”‚   â””â”€â”€ HttpClientService - Cliente HTTP
â”‚
â”œâ”€â”€ ðŸ“Š Model Layer
â”‚   â”œâ”€â”€ Location - Coordenadas geogrÃ¡ficas
â”‚   â”œâ”€â”€ Route - Dados de rota
â”‚   â””â”€â”€ TransportMode - Modos de transporte
â”‚
â””â”€â”€ ðŸ› ï¸ Utility Layer
    â””â”€â”€ RouteUtils - FormataÃ§Ã£o e cÃ¡lculos
```

### PadrÃµes de Projeto

| PadrÃ£o | ImplementaÃ§Ã£o | PropÃ³sito |
|--------|---------------|-----------|
| **MVC** | MainWindow, MapPanel, Services | SeparaÃ§Ã£o de responsabilidades |
| **Singleton** | ConfigManager | InstÃ¢ncia Ãºnica de configuraÃ§Ã£o |
| **Observer** | PointSelectionListener | ComunicaÃ§Ã£o UI â†’ Controller |
| **Adapter** | HttpClientService â†’ OkHttp | AbstraÃ§Ã£o de biblioteca HTTP |
| **Facade** | OSRMService, NominatimService | SimplificaÃ§Ã£o de APIs externas |
| **Strategy** | TransportMode enum | Diferentes algoritmos de rota |

### MÃ©tricas

- **Linhas de CÃ³digo**: ~2,500
- **Classes**: 15
- **Testes**: 25+
- **Cobertura**: >80%
- **DependÃªncias**: 20+

## ðŸ”§ Desenvolvimento

### Setup RÃ¡pido

```bash
# 1. Clone e configure
git clone https://github.com/AlexandreMendesISCTE/Projeto-de-Arquitetura-e-Desenho-de-Software.git
cd Projeto-de-Arquitetura-e-Desenho-de-Software

# 2. Configure IDE (IntelliJ IDEA, Eclipse, VS Code)
# - Importe como projeto Maven
# - Configure Java 17+ como SDK

# 3. Execute testes
mvn test

# 4. Inicie aplicaÃ§Ã£o
./run-native.sh  # ou run-native.bat
```

### ConvenÃ§Ãµes

| Aspecto | ConvenÃ§Ã£o |
|---------|-----------|
| **Nomenclatura** | camelCase (mÃ©todos/variÃ¡veis), PascalCase (classes) |
| **DocumentaÃ§Ã£o** | Javadoc obrigatÃ³rio para classes/mÃ©todos pÃºblicos |
| **FormataÃ§Ã£o** | 4 espaÃ§os, mÃ¡ximo 120 caracteres por linha |
| **Testes** | Cobertura mÃ­nima 80% |
| **Commits** | Formato: `tipo(escopo): descriÃ§Ã£o` |

### Estrutura de Branches

- `main` â†’ ProduÃ§Ã£o
- `develop` â†’ Desenvolvimento
- `feature/*` â†’ Novas funcionalidades
- `fix/*` â†’ CorreÃ§Ãµes de bugs
- `docs/*` â†’ DocumentaÃ§Ã£o

### Testes

```bash
# Executar todos os testes
mvn test

# Testes especÃ­ficos
mvn test -Dtest=MapPanelTest
mvn test -Dtest=MapPanelIntegrationTest

# RelatÃ³rio de cobertura
mvn jacoco:report
# Ver em: target/site/jacoco/index.html
```

**Implementados:**
- âœ… MapPanelTest - Zoom, pan, seleÃ§Ã£o de pontos
- âœ… MapPanelIntegrationTest - NavegaÃ§Ã£o completa, carregamento de tiles

**planeados:**
- â³ OSRMServiceTest, NominatimServiceTest
- â³ LocationTest, RouteTest
- â³ MainWindowTest

### Como Contribuir

1. Fork â†’ Clone â†’ Branch (`feature/nova-funcionalidade`)
2. Implemente + Testes + DocumentaÃ§Ã£o
3. Commit (`feat: adicionar exportaÃ§Ã£o GPX`)
4. Push â†’ Pull Request


##  Documentação

###  Guias (docs/)

- **[INSTALACAO.md](docs/INSTALACAO.md)**  **[DESENVOLVIMENTO.md](docs/DESENVOLVIMENTO.md)**  **[CONTRIBUTOR.md](docs/CONTRIBUTOR.md)**  **[CHANGELOG.md](docs/CHANGELOG.md)**

###  Arquitetura (docs/)

- **[DIAGRAMA_ARQUITETURA_COMPLETA.md](docs/DIAGRAMA_ARQUITETURA_COMPLETA.md)** - Camadas e sequências
- **[DIAGRAMA_CLASSES.md](docs/DIAGRAMA_CLASSES.md)** - UML completo
- **[PADROES_E_BOAS_PRATICAS.md](docs/PADROES_E_BOAS_PRATICAS.md)** - Design patterns e SOLID
- **[REQUISITOS.md](docs/REQUISITOS.md)** - 10 RF + 10 RNF
- **[FLUXOS_DE_DADOS.md](docs/FLUXOS_DE_DADOS.md)** - Ciclo de vida e threads
- **[DOCKER_DEPLOYMENT.md](docs/DOCKER_DEPLOYMENT.md)** - Containerização e CI/CD

 **13 documentos**  **~4,200 linhas**  **20+ diagramas Mermaid**

##  Referências

- [OpenStreetMap](https://www.openstreetmap.org/)  [OSRM API](http://project-osrm.org/docs/v5.24.0/api/)  [Nominatim API](https://nominatim.org/release-docs/develop/api/Overview/)
- [Java 17 Docs](https://docs.oracle.com/en/java/javase/17/)  [Maven Guide](https://maven.apache.org/guides/)

##  Licença

Este projeto está licenciado sob a **Licença MIT** - veja o ficheiro [LICENSE](LICENSE) para detalhes.

---

<div align="center">

**Desenvolvido com  para a disciplina de Arquitetura e Desenho de Software**

**ISCTE-IUL  Engenharia Informática  2025**

[ Voltar ao topo](#-map-route-explorer)

</div>
