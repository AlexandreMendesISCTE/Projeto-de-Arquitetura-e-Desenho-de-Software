# 📦 Guia de Instalação - Map Route Explorer

Este guia fornece instruções detalhadas para instalar e configurar o Map Route Explorer usando Docker, o método recomendado para execução da aplicação.

## � Autores

Este projeto foi desenvolvido por:

- **Alexandre Mendes** (111026)
- **Manuel Santos**
- **André Costa**
- **Ana Valente**

**Instituição**: Instituto Superior de Ciências do Trabalho e da Empresa (ISCTE-IUL)  
**Curso**: Engenharia Informática

## �📋 Índice

- [Pré-requisitos](#-pré-requisitos)
- [Instalação com Docker (Recomendado)](#-instalação-com-docker-recomendado)
- [Instalação Manual (Alternativa)](#-instalação-manual-alternativa)
- [Verificação da Instalação](#-verificação-da-instalação)
- [Resolução de Problemas](#-resolução-de-problemas)
- [Configuração Avançada](#-configuração-avançada)

## 🔧 Pré-requisitos

### Requisitos Mínimos do Sistema

| Componente | Versão Mínima | Versão Recomendada |
|------------|---------------|-------------------|
| **Docker** | 20.10 | 28.5 ou superior |
| **Docker Compose** | 2.0 | 2.40 ou superior |
| **Git** | 2.0 | 2.43 ou superior |
| **RAM** | 4 GB | 8 GB ou superior |
| **Espaço em Disco** | 2 GB | 5 GB ou superior |

### Verificação dos Pré-requisitos

Execute os seguintes comandos para verificar se tem os pré-requisitos instalados:

```bash
# Verificar Docker
docker --version

# Verificar Docker Compose
docker-compose --version

# Verificar Git
git --version
```

**Saída esperada:**
```
Docker version 28.5.x (ou superior)
Docker Compose version v2.40.x (ou superior)
git version 2.43.x (ou superior)
```

## 🐳 Instalação com Docker (Recomendado)

> **✨ Novidade**: A aplicação agora inclui **VNC Server e noVNC** para acesso à interface gráfica via browser ou VNC Viewer!

### Vantagens do Docker
- ✅ Instalação em 1 comando
- ✅ Não requer instalação de Java localmente
- ✅ Acesso via browser (sem instalar VNC Viewer)
- ✅ Isolamento completo do sistema
- ✅ Funciona em Windows, Linux e macOS

### Instalação do Docker

#### Windows

1. **Docker Desktop para Windows**
   - Descarregue de [Docker Desktop](https://www.docker.com/products/docker-desktop/)
   - Execute o instalador
   - Reinicie o computador
   - Inicie o Docker Desktop

2. **Verificar Instalação**
   ```cmd
   docker --version
   docker-compose --version
   ```

#### macOS

1. **Docker Desktop para Mac**
   - Descarregue de [Docker Desktop](https://www.docker.com/products/docker-desktop/)
   - Execute o instalador DMG
   - Arraste para a pasta Applications
   - Inicie o Docker Desktop

2. **Verificar Instalação**
   ```bash
   docker --version
   docker-compose --version
   ```

#### Linux

1. **Ubuntu/Debian**
   ```bash
   # Atualizar repositórios
   sudo apt update
   
   # Instalar Docker
   sudo apt install docker.io docker-compose
   
   # Adicionar utilizador ao grupo docker
   sudo usermod -aG docker $USER
   
   # Reiniciar sessão
   logout
   ```

2. **CentOS/RHEL/Fedora**
   ```bash
   # Instalar Docker
   sudo yum install docker docker-compose
   
   # Iniciar Docker
   sudo systemctl start docker
   sudo systemctl enable docker
   
   # Adicionar utilizador ao grupo docker
   sudo usermod -aG docker $USER
   ```

### Instalação do Map Route Explorer

#### 1. Clonagem do Repositório

```bash
git clone https://github.com/AlexandreMendesISCTE/Projeto-de-Arquitetura-e-Desenho-de-Software.git
cd Projeto-de-Arquitetura-e-Desenho-de-Software
```

#### 2. Execução Automática

**Linux/macOS:**
```bash
# Tornar script executável
chmod +x docker-run.sh

# Executar aplicação
./docker-run.sh
```

**Windows:**
```cmd
# Executar aplicação
docker-run.bat
```

#### 3. Execução Manual

```bash
# Construir e executar
docker-compose up --build

# Executar em background
docker-compose up -d

# Parar aplicação
docker-compose down
```

#### 4. Acesso à Aplicação

**A aplicação oferece 2 formas de acesso:**

##### **Opção 1: Via Browser (noVNC) - Mais Fácil** 🌐

1. Abra o seu browser
2. Acesse: **http://localhost:6080**
3. Clique em **"Connect"**
4. A interface gráfica aparecerá no browser! ✨

**Vantagens:**
- ✅ Sem instalação adicional
- ✅ Funciona em qualquer browser moderno
- ✅ Sem necessidade de senha
- ✅ Acesso de qualquer lugar da rede local

##### **Opção 2: Via VNC Viewer - Melhor Desempenho** 🖥️

1. **Instale um VNC Viewer:**
   - **TigerVNC** (Recomendado): https://github.com/TigerVNC/tigervnc/releases
   - **RealVNC Viewer**: https://www.realvnc.com/en/connect/download/viewer/
   - **UltraVNC** (Windows): https://uvnc.com/downloads/ultravnc.html

2. **Conecte ao servidor:**
   - Servidor: `localhost:5901`
   - Senha: `maproute123`

3. **Desfrute da melhor qualidade visual!** 🎨

**Vantagens:**
- ✅ Melhor desempenho gráfico
- ✅ Menor latência
- ✅ Suporte a ecrã completo
- ✅ Maior resolução

##### **Portas Expostas**

| Serviço | Porta | Descrição |
|---------|-------|-----------|
| **noVNC (Browser)** | 6080 | Acesso via http://localhost:6080 |
| **VNC Server** | 5901 | Conexão VNC Viewer (senha: maproute123) |
| **HTTP API** | 8080 | APIs futuras (reservado) |

##### **Credenciais**

```
🌐 Browser (noVNC):
   URL: http://localhost:6080
   Senha: Não necessária

🖥️ VNC Viewer:
   Servidor: localhost:5901
   Senha: maproute123
```

- **Interface Gráfica**: A aplicação abrirá automaticamente
- **Acesso Remoto VNC**: 
  - Porta: 5901
  - Senha: `maproute123`
  - Cliente VNC: RealVNC, TightVNC, etc.
- **Logs**: `docker-compose logs -f map-route-explorer`

## 🔧 Instalação Manual (Alternativa)

### Pré-requisitos Adicionais

| Componente | Versão Mínima | Versão Recomendada |
|------------|---------------|-------------------|
| **Java** | 17 | 17 ou superior |
| **Maven** | 3.6 | 3.8 ou superior |
| **Git** | 2.0 | 2.30 ou superior |

### Instalação do Java

#### Windows
```cmd
# Usando Chocolatey
choco install openjdk17

# Ou descarregar de https://adoptium.net/
```

#### macOS
```bash
# Usando Homebrew
brew install openjdk@17

# Configurar Java
sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
```

#### Linux
```bash
# Ubuntu/Debian
sudo apt install openjdk-17-jdk

# CentOS/RHEL/Fedora
sudo yum install java-17-openjdk-devel
```

### Instalação do Maven

#### Windows
```cmd
# Usando Chocolatey
choco install maven

# Ou descarregar de https://maven.apache.org/download.cgi
```

#### macOS
```bash
# Usando Homebrew
brew install maven
```

#### Linux
```bash
# Ubuntu/Debian
sudo apt install maven

# CentOS/RHEL/Fedora
sudo yum install maven
```

### Compilação e Execução

> **✨ Implementação Nativa**: O projeto utiliza uma **implementação nativa em Java puro** para renderização de mapas, sem dependências externas como JMapViewer ou JavaFX. Os tiles do OpenStreetMap são carregados diretamente via HTTP e renderizados usando Swing e Graphics2D, proporcionando uma solução leve e portável.

```bash
# Clonar repositório
git clone https://github.com/AlexandreMendesISCTE/Projeto-de-Arquitetura-e-Desenho-de-Software.git
cd Projeto-de-Arquitetura-e-Desenho-de-Software

# Compilar projeto
mvn clean package

# Executar aplicação
java -jar target/map-route-explorer-2.0.0-jar-with-dependencies.jar

# Ou usar scripts de execução
./run-native.sh    # Linux/Mac
run-native.bat     # Windows
```

## ✅ Verificação da Instalação

### 1. Verificar Docker

```bash
# Verificar se Docker está a funcionar
docker --version
docker-compose --version

# Verificar se Docker está a correr
docker ps
```

### 2. Clonar e Executar

```bash
# Clonar repositório
git clone https://github.com/AlexandreMendesISCTE/Projeto-de-Arquitetura-e-Desenho-de-Software.git
cd Projeto-de-Arquitetura-e-Desenho-de-Software

# Executar com Docker
./docker-run.sh
# ou
docker-run.bat
# ou
docker-compose up
```

### 3. Verificar Funcionalidades

1. **Container**: Deve estar a correr sem erros
2. **Interface Gráfica**: A aplicação deve abrir automaticamente
3. **VNC**: Deve conseguir aceder via VNC na porta 5901
4. **Logs**: Deve mostrar logs sem erros críticos
5. **Mapa**: Deve carregar o mapa de Lisboa
6. **APIs**: Deve conseguir fazer requisições HTTP

### 4. Testes de Conectividade

```bash
# Verificar se o container está a correr
docker-compose ps

# Verificar logs
docker-compose logs map-route-explorer

# Verificar conectividade VNC
telnet localhost 5901
```

## 🔧 Resolução de Problemas

### Problema: Docker não encontrado

**Erro:**
```
'docker' não é reconhecido como um comando interno ou externo
```

**Solução:**
1. Instalar Docker Desktop
2. Reiniciar o computador
3. Verificar se Docker está a correr: `docker ps`

### Problema: Docker Compose não encontrado

**Erro:**
```
'docker-compose' não é reconhecido como um comando interno ou externo
```

**Solução:**
1. Instalar Docker Desktop (inclui Docker Compose)
2. Verificar instalação: `docker-compose --version`
3. Reiniciar terminal

### Problema: Erro de build da imagem

**Erro:**
```
[ERROR] Failed to build Docker image
```

**Solução:**
1. Verificar se o JAR existe: `ls target/*.jar`
2. Compilar projeto primeiro: `mvn clean package`
3. Limpar cache Docker: `docker system prune -f`

### Problema: Container não inicia

**Erro:**
```
Container exited with code 1
```

**Solução:**
1. Verificar logs: `docker-compose logs map-route-explorer`
2. Verificar permissões: `chmod +x docker-run.sh`
3. Verificar recursos do sistema (RAM, CPU)

### Problema: Interface gráfica não aparece

**Erro:**
```
Cannot connect to display
```

**Solução:**
1. **Linux**: Configurar X11 forwarding
2. **Windows**: Usar VNC na porta 5901
3. **macOS**: Verificar permissões de acessibilidade

### Problema: VNC não funciona

**Erro:**
```
Connection refused on port 5901
```

**Solução:**
1. Verificar se o container está a correr: `docker-compose ps`
2. Verificar portas: `docker-compose port map-route-explorer 5900`
3. Usar cliente VNC: RealVNC, TightVNC, etc.

### Problema: Erro de permissões Docker

**Erro:**
```
Permission denied while trying to connect to Docker daemon
```

**Solução:**
1. **Linux**: Adicionar utilizador ao grupo docker: `sudo usermod -aG docker $USER`
2. **Windows/macOS**: Reiniciar Docker Desktop
3. Reiniciar sessão do utilizador

## ⚙️ Configuração Avançada

### Configuração de Recursos Docker

```yaml
# docker-compose.override.yml
version: '3.8'
services:
  map-route-explorer:
    deploy:
      resources:
        limits:
          memory: 2G
          cpus: '1.0'
        reservations:
          memory: 1G
          cpus: '0.5'
```

### Configuração de Variáveis de Ambiente

```bash
# Criar ficheiro .env
cat > .env << EOF
JAVA_OPTS=-Xmx2g -Xms1g -Djava.awt.headless=false
DISPLAY=:0
APP_HOME=/app
LOGS_DIR=/app/logs
EOF
```

### Configuração de Volumes Persistentes

```yaml
# docker-compose.yml
services:
  map-route-explorer:
    volumes:
      - ./logs:/app/logs
      - ./data:/app/data
      - ./config:/app/config
      - cache-data:/app/cache
```

### Configuração de Rede

```yaml
# docker-compose.yml
networks:
  map-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.20.0.0/16
```

### Configuração de Logging

```yaml
# docker-compose.yml
services:
  map-route-explorer:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

### Configuração de APIs

```properties
# config/application.properties
osrm.base.url=http://router.project-osrm.org/route/v1
nominatim.base.url=https://nominatim.openstreetmap.org
http.timeout.seconds=30
```

## 📊 Verificação Final

Execute este script para verificar se tudo está funcionando:

```bash
#!/bin/bash
echo "🔍 Verificando instalação do Map Route Explorer..."

# Verificar Docker
if docker --version > /dev/null 2>&1; then
    echo "✅ Docker instalado"
else
    echo "❌ Docker não encontrado"
    exit 1
fi

# Verificar Docker Compose
if docker-compose --version > /dev/null 2>&1; then
    echo "✅ Docker Compose instalado"
else
    echo "❌ Docker Compose não encontrado"
    exit 1
fi

# Verificar Git
if git --version > /dev/null 2>&1; then
    echo "✅ Git instalado"
else
    echo "❌ Git não encontrado"
    exit 1
fi

# Compilar projeto
echo "🔨 Compilando projeto..."
if mvn clean package -DskipTests -q; then
    echo "✅ Projeto compilado com sucesso"
else
    echo "❌ Erro na compilação"
    exit 1
fi

# Construir imagem Docker
echo "🐳 Construindo imagem Docker..."
if docker-compose build -q; then
    echo "✅ Imagem Docker construída com sucesso"
else
    echo "❌ Erro na construção da imagem"
    exit 1
fi

echo "🎉 Instalação verificada com sucesso!"
echo "🚀 Pode executar: ./docker-run.sh"
```

---

<div align="center">

**Instalação concluída com sucesso! 🎉**

[⬆ Voltar ao topo](#-guia-de-instalação---map-route-explorer)

</div>
