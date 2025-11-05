# 📝 Changelog - Map Route Explorer

Todas as alterações notáveis neste projeto serão documentadas neste ficheiro.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

## 👥 Autores

Este projeto foi desenvolvido por:

- **Alexandre Mendes** (111026)
- **Manuel Santos**
- **André Costa**
- **Ana Valente**

**Instituição**: Instituto Superior de Ciências do Trabalho e da Empresa (ISCTE-IUL)  
**Curso**: Engenharia Informática

---

## [2.0.1] - 2025-11-05

### 📚 Documentação Técnica Completa

#### Adicionado
- **Documentação Arquitetural Completa**:
  - `DIAGRAMA_ARQUITETURA_COMPLETA.md`: Visão arquitetural em camadas com diagramas Mermaid
  - `DIAGRAMA_CLASSES.md`: UML completo de todas as classes com atributos, métodos e relacionamentos
  - `REQUISITOS.md`: 10 RF + 10 RNF com mindmap, matriz de rastreabilidade e roadmap
  - `PADROES_E_BOAS_PRATICAS.md`: Todos os design patterns implementados (Singleton, Adapter, Facade, Observer, Strategy, MVC, Layered Architecture)
  - `FLUXOS_DE_DADOS.md`: Fluxos completos do ciclo de vida, cache LRU, thread management
  - `DOCKER_DEPLOYMENT.md`: Processo de containerização, multi-stage build, CI/CD

- **Diagramas Mermaid Interativos** (~20+ diagramas):
  - Arquitetura em camadas
  - Sequências de operação
  - Mindmaps de requisitos
  - Fluxos de dados completos
  - Ciclo de vida da aplicação

- **Métricas e Estatísticas**:
  - Performance metrics (tempos médios, utilização de recursos)
  - Cobertura de requisitos: 75% (15/20)
  - Métricas de código por classe
  - Estatísticas de containerização

#### Documentado
- **Requisitos Funcionais** (RF-01 a RF-10):
  - ✅ 6 implementados (Visualização Mapa, Seleção Rota, Informações Rota, Limpeza, Pesquisa, Modos Transporte)
  - ⏳ 4 planeados (POIs, Múltiplos Destinos, Exportação, Estatísticas)

- **Requisitos Não Funcionais** (RNF-01 a RNF-10):
  - ✅ 9 implementados (Performance, Escalabilidade, Manutenibilidade, Portabilidade, Usabilidade, Segurança, Confiabilidade, Disponibilidade, Documentação)
  - ⏳ 1 em progresso (Testabilidade)

- **Design Patterns Implementados**:
  - Creational: Singleton (ConfigManager)
  - Structural: Adapter (HttpClientService), Facade (OSRMService/NominatimService)
  - Behavioral: Observer (PointSelectionListener), Strategy (TransportMode)
  - Architectural: MVC, Layered Architecture, Service Layer

- **Princípios SOLID**:
  - SRP, OCP, LSP, ISP, DIP (todos documentados com exemplos de código)

#### Roadmap
- **v2.1.0 (Q1 2026)**: POIs, Estatísticas Avançadas, Cobertura 80%+
- **v2.2.0 (Q2 2026)**: Múltiplos Destinos, Exportação de Dados
- **v3.0.0 (Q3 2026)**: Refatoração, API REST, Modo Offline

---

## [2.0.0] - 2025-10-30

### 🗺️ Implementação Nativa de Renderização de Mapas

#### Adicionado
- **Renderização Nativa em Java Puro**:
  - Sistema de carregamento direto de tiles do OpenStreetMap
  - Renderização usando Swing e Graphics2D (sem dependências externas)
  - Cache LRU inteligente para 100 tiles
  - Thread pool de 6 threads para download concorrente

- **Interação Avançada com Mapa**:
  - Zoom com roda do mouse (mantém cursor como centro)
  - Zoom com duplo clique
  - Pan (arrastar) com detecção inteligente de drag vs click
  - Seleção de pontos diferenciada de arrastar
  - Sistema de priorização: tiles visíveis primeiro, depois buffer

- **Gestão de Requisições HTTP**:
  - Rate limiting amigável aos servidores OSM
  - Retry automático com fallback para servidores alternativos
  - Tratamento de erros HTTP (429, 503)
  - Placeholders durante carregamento

- **Desenho de Rotas no Mapa**:
  - Desenho de polylines com cores por modo de transporte
  - Marcadores visuais para origem (A - verde) e destino (B - vermelho)
  - Suavização de linhas de rota
  - Atualização dinâmica ao alterar pontos

- **Testes Implementados**:
  - ✅ Testes unitários para zoom, pan e seleção
  - ✅ Testes de integração para navegação completa
  - ✅ Validação de diferenciação drag vs click
  - ✅ Testes de carregamento de tiles

#### Técnico
- Eliminada dependência de JMapViewer
- Implementação 100% nativa em Java 17+
- Compatível com Swing headless para Docker
- Performance otimizada com cache e threads
- Suporte a 18 níveis de zoom (0-18)

---

## [2.0.0] - 2025-10-25

### 🚀 Atualização Completa para Versões Mais Recentes

#### Adicionado
- **Multi-stage Docker Build**: Otimização da imagem usando build em duas etapas
- **Alpine Linux**: Migração para Eclipse Temurin 23-jdk-alpine (redução de ~50% no tamanho)
- **Health Check Docker**: Monitoramento automático do estado da aplicação
- **Compatibilidade Java 17-23**: Código compatível com Java 17+ rodando em Java 23 no Docker
- **Release Plugin**: Configuração Maven para versionamento automático

#### Atualizado - Docker & Infraestrutura
- **Docker Base Image**: OpenJDK 17-jdk-slim → **Eclipse Temurin 23-jdk-alpine** ✅
- **Build Stage**: Maven 3.8 → **Maven 3.9-eclipse-temurin-23-alpine** ✅
- **Imagem Docker**: ~500MB → ~250MB (redução de 50%)
- **Java Runtime**: Compatível com Java 17-23 (testado em ambos)

#### Atualizado - Dependências Maven (Todas para Versões Mais Recentes)
- Jackson: 2.17.x → **2.18.2** ✅
- OkHttp: 4.x → **5.0.0-alpha.14** ✅
- JUnit Jupiter: 5.10.x → **5.11.3** ✅
- Mockito: 5.11.x → **5.14.2** ✅
- SLF4J: 2.0.13 → **2.0.16** ✅
- Logback: 1.5.6 → **1.5.15** ✅
- GeoTools: 31.x → **32.1** ✅
- JTS: 1.19.x → **1.20.0** ✅
- FlatLaf: 3.4.x → **3.5.2** ✅
- Apache Commons (Lang3 3.14.0, IO 2.15.1, Validator 1.8.0, Math3 3.6.1) ✅
- JGoodies (Forms 1.9.0, Common 1.8.1) ✅
- JUnit Pioneer: **2.2.0** ✅
- AssertJ: **3.25.3** ✅

#### Atualizado - Plugins Maven (Todos para Versões Mais Recentes)
- Maven Compiler: 3.11.0 → **3.13.0** ✅
- Maven Surefire: 3.2.5 → **3.5.2** ✅
- Maven Failsafe: 3.2.5 → **3.5.2** ✅
- Maven Javadoc: 3.6.3 → **3.11.2** ✅
- Maven Source: 3.3.0 → **3.3.1** ✅
- Maven Assembly: 3.7.0 → **3.7.1** ✅

#### Melhorado
- **Build Performance**: Cache de dependências Maven otimizado em multi-stage
- **Security**: Execução com utilizador não-root no Docker (appuser)
- **Compatibilidade**: Funciona com Java 17 local e Java 23 no Docker
- **Documentação**: Todos os ficheiros .md atualizados e consistentes

#### Técnico
- Tamanho da imagem: Reduzido 50% (500MB → 250MB) com Alpine
- Build otimizado: Multi-stage com cache de dependências
- Java compliance: release=17 (compatível 17-23)
- Docker testado: Build e execução bem-sucedidos
- Compilação local: Testada e funcional com Java 17

## [Não Lançado]

### planeado
- Funcionalidade de exportação GPX
- Suporte a múltiplos destinos
- Integração com Overpass API para POIs
- Perfil altimétrico das rotas
- Testes unitários e de integração
- Configuração CI/CD

## [1.0.0] - 2025-10-24

### Adicionado
- 🎉 **Lançamento inicial** do Map Route Explorer
- 🗺️ **Visualização de mapa** interativo com OpenStreetMap
- 🛣️ **Cálculo de rotas** integrado com API OSRM
- 🔍 **Geocodificação** com API Nominatim
- 🚗 **Modos de transporte** (automóvel, bicicleta, a pé)
- 📊 **Informações de rota** (distância, tempo, instruções)
- 🎨 **Interface gráfica** moderna com Swing e FlatLaf
- 🐳 **Containerização Docker** para execução simplificada
- 🧪 **Testes unitários** com JUnit 5 e Mockito
- 📚 **Documentação completa** com Javadoc
- ⚙️ **Sistema de configuração** flexível
- 📝 **Logging** estruturado com Logback
- 🚀 **Scripts de execução** para Linux, macOS e Windows

### Funcionalidades Principais
- **Mapa Interativo**: Navegação com zoom e pan
- **Seleção de Pontos**: Clique no mapa para selecionar origem e destino
- **Cálculo de Rotas**: Integração com API OSRM para rotas otimizadas
- **Pesquisa de Locais**: Campo de pesquisa com geocodificação
- **Informações Detalhadas**: Distância, tempo e instruções de navegação
- **Limpeza e Reinício**: Funcionalidade para limpar seleções

### APIs Integradas
- **OpenStreetMap**: Dados cartográficos
- **OSRM**: Cálculo de rotas e otimização
- **Nominatim**: Geocodificação e pesquisa de locais

### Tecnologias Utilizadas
- **Java 17**: Linguagem de programação
- **Maven**: Gestão de dependências
- **Swing**: Interface gráfica
- **GeoTools**: Processamento geográfico (32.1)
- **JTS**: Geometria espacial (1.20.0)
- **Jackson**: Parsing JSON (2.18.2)
- **OkHttp**: Cliente HTTP (5.0.0-alpha.14)
- **FlatLaf**: Interface moderna (3.5.2)
- **Logback**: Sistema de logging (1.5.15)

### Estrutura do Projeto
```
src/main/java/pt/iscteiul/maprouteexplorer/
├── Main.java                          # Classe principal
├── model/                              # Modelos de dados
│   ├── Location.java                  # Localização geográfica
│   ├── Route.java                     # Rota calculada
│   └── TransportMode.java             # Modos de transporte
├── service/                            # Serviços de integração
│   ├── OSRMService.java               # API OSRM
│   ├── NominatimService.java          # API Nominatim
│   └── HttpClientService.java         # Cliente HTTP
├── ui/                                 # Interface gráfica
│   ├── MainWindow.java                # Janela principal
│   └── MapPanel.java                  # Painel do mapa
└── util/                               # Utilitários
    ├── RouteUtils.java                # Utilitários de rota
    └── ConfigManager.java             # Gestão de configuração
```

### Documentação
- **README.md**: Visão geral e instruções principais
- **INSTALACAO.md**: Guia detalhado de instalação
- **DESENVOLVIMENTO.md**: Guia para desenvolvedores
- **CONTRIBUTOR.md**: Guia de contribuição
- **Javadoc**: Documentação completa do código

### Configuração
- **application.properties**: Configurações da aplicação
- **logback.xml**: Configuração de logging
- **pom.xml**: Dependências Maven
- **Dockerfile**: Containerização da aplicação com health check
- **docker-compose.yml**: Orquestração de containers (versão 3.9)
- **docker-run.sh**: Script de execução para Linux/macOS
- **docker-run.bat**: Script de execução para Windows

### Containerização Docker
- **Docker**: Versão 28.5.1 testada e funcional
- **Docker Compose**: v2.40.2 (sem atributo `version` obsoleto)
- **Base Image**: OpenJDK 17-jdk-slim
- **Redis**: 7.4.1-alpine para cache
- **Nginx**: Alpine (versão mais recente - atualizada há 2 semanas)
- **VNC Server**: Ubuntu XFCE para acesso remoto
- **Health Check**: Monitoramento automático do container (intervalo 30s)
- **Multi-volume**: Logs, dados e configurações persistentes
- **Build Time**: ~51 segundos
- **Container Status**: Healthy e executando

## [0.9.0] - 2025-10-20

### Adicionado
- Estrutura inicial do projeto
- Configuração Maven
- Classes base do modelo
- Interface gráfica básica

### Alterado
- Nenhuma alteração significativa

### Corrigido
- Nenhuma correção significativa

## [0.8.0] - 2025-10-15

### Adicionado
- Configuração inicial do repositório
- Estrutura de diretórios
- Ficheiros de configuração base

### Alterado
- Nenhuma alteração significativa

### Corrigido
- Nenhuma correção significativa

---

## Legenda

- **Adicionado** para novas funcionalidades
- **Alterado** para alterações em funcionalidades existentes
- **Depreciado** para funcionalidades que serão removidas
- **Removido** para funcionalidades removidas
- **Corrigido** para correções de bugs
- **Segurança** para correções de vulnerabilidades

## Formato de Versão

Este projeto utiliza [Semantic Versioning](https://semver.org/lang/pt-BR/):

- **MAJOR** (1.0.0): Alterações incompatíveis com versões anteriores
- **MINOR** (0.1.0): Funcionalidades compatíveis com versões anteriores
- **PATCH** (0.0.1): Correções compatíveis com versões anteriores

## Links Úteis

- [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/)
- [Semantic Versioning](https://semver.org/lang/pt-BR/)
- [Conventional Commits](https://www.conventionalcommits.org/pt-br/v1.0.0/)

---

<div align="center">

**Histórico completo de alterações 📚**

[⬆ Voltar ao topo](#-changelog---map-route-explorer)

</div>
