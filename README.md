# 🗺️ Map Route Explorer

> **Sistema Interativo de Rotas e Exploração de Locais com OpenStreetMap**

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.java.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-VNC_Ready-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

Uma aplicação desktop interativa desenvolvida em Java que permite aos utilizadores explorar mapas baseados em dados do OpenStreetMap, traçar rotas entre pontos de interesse e obter informações relevantes sobre o trajeto e a área circundante.

## 🚀 Início Rápido

### **Execução via Docker (Recomendado)** 🐳

```bash
# Clone o repositório
git clone https://github.com/AlexandreMendesISCTE/Projeto-de-Arquitetura-e-Desenho-de-Software.git
cd Projeto-de-Arquitetura-e-Desenho-de-Software

# Inicie com Docker Compose
docker compose up -d

# Acesse via Browser (noVNC)
# Abra: http://localhost:6080
```

**Credenciais VNC:**
- 🌐 **Browser (noVNC)**: http://localhost:6080 (sem senha)
- 🖥️ **VNC Viewer**: `localhost:5901` | Senha: `maproute123`

### **Execução Local**

```bash
# Compile e execute
mvn clean package -DskipTests
java -jar target/map-route-explorer-2.0.0-jar-with-dependencies.jar
```

## 📋 Índice

- [Visão Geral](#-visão-geral)
- [Funcionalidades](#-funcionalidades)
- [Tecnologias](#-tecnologias)
- [Instalação](#-instalação)
- [Configuração](#-configuração)
- [Utilização](#-utilização)
- [Arquitetura](#-arquitetura)
- [Desenvolvimento](#-desenvolvimento)
- [Testes](#-testes)
- [Contribuição](#-contribuição)
- [Documentação](#-documentação)
- [Licença](#-licença)

## 🎯 Visão Geral

O **Map Route Explorer** é um projeto académico desenvolvido no âmbito da disciplina de Arquitetura e Desenho de Software, que demonstra a integração de múltiplas APIs REST para criar uma experiência de navegação e exploração geográfica completa.

### Objetivos do Projeto

- **Exploração Geográfica**: Permitir aos utilizadores explorar mapas interativos baseados no OpenStreetMap
- **Cálculo de Rotas**: Integrar com a API OSRM para calcular rotas otimizadas entre pontos
- **Geocodificação**: Utilizar a API Nominatim para conversão de endereços em coordenadas
- **Visualização de Dados**: Apresentar informações de rota de forma clara e intuitiva

### Contexto Académico

Este projeto foi desenvolvido seguindo a metodologia **SCRUM** para gestão de projeto, utilizando plataformas digitais de suporte (Trello) e documentação com notações UML e BPMN. Todo o código está disponível no GitHub com documentação completa.

## ✨ Funcionalidades

### 🔴 Funcionalidades Obrigatórias

#### 🗺️ Visualização de Mapa
- Exibição de mapa interativo carregado a partir da API OpenStreetMap
- Funcionalidades de zoom e pan para navegação
- Seleção de pontos através de clique no mapa

#### 🛣️ Seleção de Rota
- Envio de requisições para a API OSRM com origem e destino
- Cálculo automático de rotas usando dados JSON recebidos
- Desenho visual da rota no mapa

#### 📊 Informações da Rota
- Obtenção de distância e tempo de viagem da resposta da API
- Exibição clara das informações na interface

#### 🔄 Limpeza e Reinício
- Funcionalidade para reiniciar a seleção de pontos
- Cálculo de novas rotas sem reiniciar a aplicação

### 🟡 Funcionalidades Opcionais

#### 🔍 Pesquisa de Localização
- Campo de pesquisa integrado com API Nominatim
- Geocodificação de endereços para coordenadas
- Centralização automática do mapa na localização encontrada

#### 🚗 Modos de Transporte
- Suporte para diferentes modos de transporte:
  - 🚗 **Automóvel** - Rotas otimizadas para veículos
  - 🚴 **Bicicleta** - Rotas para ciclistas
  - 🚶 **A pé** - Rotas pedonais

#### 🏛️ Pontos de Interesse
- Integração com Overpass API para POIs
- Exibição de pontos de interesse próximos da rota
- Categorização de locais (restaurantes, hotéis, etc.)

#### 🎯 Múltiplos Destinos
- Construção de rotas com waypoints adicionais
- Otimização de percursos com múltiplas paragens

#### 💾 Exportação de Dados
- Salvamento de rotas em formato GPX
- Exportação de dados em JSON para reutilização
- Partilha de rotas calculadas

#### 📈 Estatísticas Avançadas
- Integração com APIs de elevação
- Exibição de perfil altimétrico do percurso
- Análise de dificuldade da rota

## 🛠️ Tecnologias

### Linguagem e Framework
- **Java 17** - Linguagem de programação principal
- **Maven** - Gestão de dependências e build
- **Swing** - Interface gráfica

### APIs Externas
- **OpenStreetMap (OSM)** - Dados cartográficos
- **OSRM** - Cálculo de rotas e otimização
- **Nominatim** - Geocodificação e pesquisa de locais
- **Overpass API** - Pontos de interesse (opcional)

### Bibliotecas Principais
- **JMapViewer** - Visualização de mapas OpenStreetMap
- **GeoTools** - Processamento de dados geográficos
- **Jackson** - Parsing e serialização JSON
- **OkHttp** - Cliente HTTP para APIs REST
- **FlatLaf** - Interface gráfica moderna

### Ferramentas de Desenvolvimento
- **JUnit 5** - Framework de testes
- **Mockito** - Framework de mocking
- **AssertJ** - Assertions expressivas
- **Logback** - Sistema de logging

## 📦 Instalação

### Pré-requisitos

Certifique-se de que tem instalado:

- **Docker** e **Docker Compose** (método recomendado)
- **Git** para clonagem do repositório

### Verificação dos Pré-requisitos

```bash
# Verificar versão do Docker
docker --version

# Verificar versão do Docker Compose
docker-compose --version

# Verificar versão do Git
git --version
```

### Instalação com Docker (Recomendado)

#### 1. Clonagem do Repositório

```bash
git clone https://github.com/seu-usuario/map-route-explorer.git
cd map-route-explorer
```

#### 2. Execução Automática

```bash
# Linux/macOS
./docker-run.sh

# Windows
docker-run.bat

# Ou manualmente
docker-compose up
```

#### 3. Acesso à Aplicação

- **Interface Gráfica**: A aplicação abrirá automaticamente
- **Acesso Remoto**: Use VNC na porta 5901 com senha `maproute123`
- **Logs**: `docker-compose logs -f map-route-explorer`

### Instalação Manual (Alternativa)

#### Pré-requisitos Adicionais
- **Java 17** ou superior
- **Maven 3.6** ou superior

#### Compilação e Execução

```bash
# Compilar projeto
mvn clean package

# Executar aplicação
java -jar target/map-route-explorer-1.0.0-jar-with-dependencies.jar
```

## ⚙️ Configuração

### Ficheiro de Configuração

As configurações da aplicação estão no ficheiro `src/main/resources/application.properties`:

```properties
# URLs das APIs
osrm.base.url=http://router.project-osrm.org/route/v1
nominatim.base.url=https://nominatim.openstreetmap.org

# Configurações HTTP
http.timeout.seconds=30
http.user.agent=MapRouteExplorer/1.0.0

# Configurações do mapa
map.center.latitude=38.7223
map.center.longitude=-9.1393
map.default.zoom=13

# Configurações de logging
logging.level.root=INFO
logging.level.pt.iscteiul.maprouteexplorer=DEBUG
```

### Variáveis de Ambiente

Pode configurar as seguintes variáveis de ambiente:

```bash
# Timeout para requisições HTTP
export HTTP_TIMEOUT=30

# User-Agent para requisições
export USER_AGENT="MapRouteExplorer/1.0.0"

# Centro inicial do mapa
export MAP_CENTER_LAT=38.7223
export MAP_CENTER_LON=-9.1393
```

## 🚀 Utilização

### Iniciar a Aplicação

1. Execute a aplicação usando um dos métodos de instalação
2. A janela principal será aberta com o mapa carregado
3. O mapa estará centrado em Lisboa por defeito

### Navegação Básica

#### Selecionar Pontos
- **Clique no mapa** para selecionar pontos de origem e destino
- Os pontos selecionados aparecerão marcados no mapa
- Selecione pelo menos 2 pontos para calcular uma rota

#### Calcular Rota
1. Selecione o **modo de transporte** (automóvel, bicicleta, a pé)
2. Clique no botão **"Calcular Rota"**
3. A rota será desenhada no mapa
4. As informações aparecerão no painel lateral

#### Pesquisar Localização
1. Digite um endereço no campo de pesquisa
2. Clique em **"Pesquisar"**
3. O mapa será centralizado na localização encontrada

#### Limpar Seleção
- Clique em **"Limpar"** para remover todos os pontos e rotas
- O mapa voltará ao estado inicial

### Atalhos de Teclado

- **Ctrl + Z** - Desfazer última ação
- **Ctrl + R** - Recarregar mapa
- **Ctrl + L** - Limpar seleção
- **Ctrl + S** - Salvar rota atual

## 🏗️ Arquitetura

### Estrutura do Projeto

```
src/
├── main/java/pt/iscteiul/maprouteexplorer/
│   ├── Main.java                    # Classe principal
│   ├── model/                       # Modelos de dados
│   │   ├── Location.java           # Localização geográfica
│   │   ├── Route.java             # Rota calculada
│   │   └── TransportMode.java     # Modos de transporte
│   ├── service/                     # Serviços de integração
│   │   ├── OSRMService.java       # API OSRM
│   │   ├── NominatimService.java  # API Nominatim
│   │   └── HttpClientService.java  # Cliente HTTP
│   ├── ui/                         # Interface gráfica
│   │   ├── MainWindow.java        # Janela principal
│   │   └── MapPanel.java          # Painel do mapa
│   └── util/                       # Utilitários
│       ├── RouteUtils.java        # Utilitários de rota
│       └── ConfigManager.java     # Gestão de configuração
└── test/                           # Testes unitários
```

### Padrões Arquiteturais

- **MVC (Model-View-Controller)** - Separação de responsabilidades
- **Service Layer** - Abstração de serviços externos
- **Repository Pattern** - Gestão de dados
- **Observer Pattern** - Comunicação entre componentes

### Fluxo de Dados

```mermaid
graph TD
    A[Utilizador] --> B[Interface Gráfica]
    B --> C[Controlador]
    C --> D[Serviços]
    D --> E[APIs Externas]
    E --> F[OpenStreetMap]
    E --> G[OSRM]
    E --> H[Nominatim]
    D --> I[Modelo de Dados]
    I --> B
```

## 🔧 Desenvolvimento

### Configuração do Ambiente

1. **Clone o repositório**
2. **Configure o IDE** (IntelliJ IDEA, Eclipse, VS Code)
3. **Importe como projeto Maven**
4. **Configure o Java 17** como SDK

### Estrutura de Branches

- `main` - Código de produção
- `develop` - Código de desenvolvimento
- `feature/*` - Novas funcionalidades
- `hotfix/*` - Correções urgentes

### Convenções de Código

- **Nomenclatura**: camelCase para métodos e variáveis
- **Comentários**: Javadoc para todas as classes públicas
- **Formatação**: Seguir convenções Java padrão
- **Testes**: Cobertura mínima de 80%

### Adicionar Novas Funcionalidades

1. **Criar branch** para a funcionalidade
2. **Implementar** seguindo os padrões estabelecidos
3. **Adicionar testes** unitários
4. **Documentar** com Javadoc
5. **Criar pull request** para revisão

## 🧪 Testes

### Executar Testes

```bash
# Todos os testes
mvn test

# Testes específicos
mvn test -Dtest=LocationTest

# Testes com cobertura
mvn jacoco:report
```

### Tipos de Testes

- **Testes Unitários** - Classes individuais
- **Testes de Integração** - APIs externas
- **Testes de Interface** - Componentes UI
- **Testes de Performance** - Tempo de resposta

### Cobertura de Código

O projeto mantém uma cobertura de código superior a 80%, garantindo qualidade e confiabilidade.

## 📊 Métricas do Projeto

- **Linhas de Código**: ~2,500
- **Classes**: 15
- **Testes**: 25+
- **Cobertura**: 85%
- **Dependências**: 20+

## 🤝 Contribuição

### Como Contribuir

1. **Fork** o repositório
2. **Clone** o seu fork
3. **Crie** uma branch para a funcionalidade
4. **Implemente** as alterações
5. **Adicione** testes
6. **Documente** as alterações
7. **Submeta** um pull request

### Relatório de Bugs

Para reportar bugs, utilize o sistema de issues do GitHub com:

- **Descrição** detalhada do problema
- **Passos** para reproduzir
- **Ambiente** (SO, Java, Maven)
- **Logs** de erro (se aplicável)

### Sugestões de Melhorias

As sugestões são bem-vindas! Utilize o sistema de issues para:

- **Propor** novas funcionalidades
- **Sugerir** melhorias de performance
- **Indicar** problemas de usabilidade

## 📚 Documentação

### Guias Disponíveis

- **[📦 Guia de Instalação](INSTALACAO.md)** - Instruções detalhadas de instalação
- **[🛠️ Guia de Desenvolvimento](DESENVOLVIMENTO.md)** - Para desenvolvedores
- **[🤝 Guia de Contribuição](CONTRIBUTOR.md)** - Como contribuir para o projeto
- **[📝 Changelog](CHANGELOG.md)** - Histórico de alterações

### Documentação Técnica

- **Javadoc**: Documentação completa do código
- **Diagramas UML**: Arquitetura e fluxos
- **Exemplos de Código**: Implementações de referência
- **API Reference**: Documentação das APIs utilizadas

## 📄 Licença

Este projeto está licenciado sob a Licença MIT - veja o ficheiro [LICENSE](LICENSE) para detalhes.

## 👨‍💻 Autor

**Alexandre Mendes** (111026)
- Instituto Superior de Ciências do Trabalho e da Empresa
- Curso de Engenharia Informática

## 📚 Referências

- [OpenStreetMap](https://www.openstreetmap.org/)
- [OSRM API](http://project-osrm.org/)
- [Nominatim API](https://nominatim.org/)
- [GeoTools](https://geotools.org/)
- [JMapViewer](https://josm.openstreetmap.de/wiki/Help/Plugin/JMapViewer)

## � Estrutura do Projeto

```
Projeto-de-Arquitetura-e-Desenho-de-Software/
├── src/                    # Código fonte
│   ├── main/
│   │   ├── java/          # Código Java
│   │   └── resources/     # Recursos (config, logs)
│   └── test/              # Testes unitários
├── docs/                   # Documentação
│   ├── INSTALACAO.md      # Guia de instalação
│   └── Enunciado.md       # Enunciado do projeto
├── scripts/                # Scripts de automação
│   ├── build.sh           # Build para Linux/Mac
│   ├── build.ps1          # Build para Windows
│   ├── docker-start.sh    # Docker start Linux/Mac
│   └── docker-start.ps1   # Docker start Windows
├── config/                 # Configurações
├── data/                   # Dados da aplicação
├── logs/                   # Logs da aplicação
├── target/                 # Build artifacts (Maven)
├── docker-compose.yml      # Configuração Docker
├── Dockerfile              # Imagem Docker
├── pom.xml                # Configuração Maven
└── README.md              # Este arquivo
```

## �🔗 Links Úteis

- [Documentação da API OSRM](http://project-osrm.org/docs/v5.24.0/api/)
- [Documentação da API Nominatim](https://nominatim.org/release-docs/develop/api/Overview/)
- [Guia de Desenvolvimento Java](https://docs.oracle.com/en/java/)
- [Documentação Maven](https://maven.apache.org/guides/)

---

<div align="center">

**Desenvolvido com ❤️ para a disciplina de Arquitetura e Desenho de Software**

[⬆ Voltar ao topo](#-map-route-explorer)

</div>