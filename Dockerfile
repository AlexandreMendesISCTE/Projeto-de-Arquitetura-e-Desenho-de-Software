# Etapa 1: Build da aplicação
FROM maven:3.9-eclipse-temurin-25-alpine AS builder

# Metadados da etapa de build
LABEL maintainer="Alexandre Mendes <alexandre.mendes@iscte-iul.pt>"
LABEL description="Map Route Explorer - Build Stage"
LABEL repository="https://github.com/AlexandreMendesISCTE/Projeto-de-Arquitetura-e-Desenho-de-Software"

# Diretório de trabalho
WORKDIR /build

# Copiar apenas pom.xml primeiro (melhor cache de dependências)
COPY pom.xml .

# Baixar dependências (camada cacheada separadamente)
RUN mvn dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Build da aplicação
RUN mvn clean package -DskipTests -B

# ========================================
# Etapa 2: Imagem final com VNC
# ========================================
FROM eclipse-temurin:25-jdk

# Metadados da imagem final
LABEL maintainer="Alexandre Mendes <alexandre.mendes@iscte-iul.pt>"
LABEL version="2.0.0"
LABEL description="Map Route Explorer com VNC - Interface Gráfica via Browser"

# Evita interações durante instalação
ENV DEBIAN_FRONTEND=noninteractive

# Define variáveis de ambiente
ENV JAVA_OPTS="-Xmx1g -Xms512m" \
    APP_HOME=/app \
    DISPLAY=:1 \
    VNC_PORT=5901 \
    NOVNC_PORT=6080 \
    VNC_PASSWORD=maproute123

WORKDIR $APP_HOME

# Instala dependências do sistema
RUN apt-get update && apt-get install -y \
    wget \
    tar \
    x11vnc \
    xvfb \
    fluxbox \
    net-tools \
    supervisor \
    novnc \
    websockify \
    xfonts-base \
    xfonts-75dpi \
    xfonts-100dpi \
    fonts-dejavu \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Cria diretórios necessários
RUN mkdir -p /app/logs /root/.vnc

# Define senha VNC
RUN x11vnc -storepasswd ${VNC_PASSWORD} /root/.vnc/passwd

# Copia o JAR compilado
COPY --from=builder /build/target/map-route-explorer-*-jar-with-dependencies.jar $APP_HOME/app.jar

# Cria script de inicialização
RUN echo '#!/bin/bash\n\
# Inicia Xvfb (X virtual framebuffer)\n\
Xvfb :1 -screen 0 1280x720x24 &\n\
sleep 2\n\
\n\
# Inicia Fluxbox (window manager)\n\
DISPLAY=:1 fluxbox &\n\
sleep 1\n\
\n\
# Inicia x11vnc (VNC server)\n\
x11vnc -display :1 -rfbport 5901 -rfbauth /root/.vnc/passwd -forever -shared &\n\
sleep 2\n\
\n\
# Inicia noVNC (web interface)\n\
websockify --web=/usr/share/novnc/ 6080 localhost:5901 &\n\
sleep 2\n\
\n\
# Inicia a aplicação Java\n\
DISPLAY=:1 java $JAVA_OPTS -jar $APP_HOME/app.jar\n\
' > /app/start.sh && chmod +x /app/start.sh

# Expõe portas
EXPOSE 5901 6080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD pgrep -f "java.*app.jar" > /dev/null || exit 1

# Comando de inicialização
CMD ["/app/start.sh"]