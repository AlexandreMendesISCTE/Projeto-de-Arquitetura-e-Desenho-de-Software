# Etapa 1: Build da aplicação
FROM maven:3.9-eclipse-temurin-23-alpine AS builder

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
# Etapa 2: Imagem final otimizada
# ========================================
FROM eclipse-temurin:23-jdk-alpine

# Metadados da imagem final
LABEL maintainer="Alexandre Mendes <alexandre.mendes@iscte-iul.pt>"
LABEL version="2.0.0"
LABEL description="Sistema Interativo de Rotas e Exploração de Locais com OpenStreetMap - Java 23"
LABEL org.opencontainers.image.source="https://github.com/AlexandreMendesISCTE/Projeto-de-Arquitetura-e-Desenho-de-Software"

# Define variáveis de ambiente
ENV JAVA_OPTS="-Xmx1g -Xms512m -Djava.awt.headless=false" \
    APP_HOME=/app \
    LOGS_DIR=/app/logs \
    DISPLAY=:0 \
    LANG=pt_PT.UTF-8 \
    TZ=Europe/Lisbon

# Cria diretório da aplicação
WORKDIR $APP_HOME

# Instala dependências do sistema (Alpine Linux usa apk)
RUN apk add --no-cache \
    bash \
    curl \
    wget \
    xvfb \
    x11vnc \
    fluxbox \
    ttf-dejavu \
    fontconfig \
    libxrender \
    libxtst \
    libxi \
    libxrandr \
    mesa-gl \
    # Bibliotecas adicionais para Java AWT/Swing
    libx11 \
    libxext \
    libxt \
    libxcursor \
    libxmu \
    libxi6-compat \
    freetype \
    tzdata && \
    # Configura timezone
    cp /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone && \
    # Fix permissões do X11
    chmod 1777 /tmp/.X11-unix || true

# Cria diretórios necessários
RUN mkdir -p $LOGS_DIR /tmp/.X11-unix

# Copia o JAR compilado da etapa de build
COPY --from=builder /build/target/map-route-explorer-*-jar-with-dependencies.jar $APP_HOME/app.jar

# Cria script de inicialização (usando múltiplos RUN para garantir funcionamento)
RUN echo '#!/bin/bash' > $APP_HOME/start.sh && \
    echo '# Inicia servidor X virtual' >> $APP_HOME/start.sh && \
    echo 'Xvfb :0 -screen 0 1024x768x24 -ac +extension GLX +render -noreset &' >> $APP_HOME/start.sh && \
    echo 'export DISPLAY=:0' >> $APP_HOME/start.sh && \
    echo '' >> $APP_HOME/start.sh && \
    echo '# Inicia window manager' >> $APP_HOME/start.sh && \
    echo 'fluxbox &' >> $APP_HOME/start.sh && \
    echo '' >> $APP_HOME/start.sh && \
    echo '# Aguarda o X server estar pronto' >> $APP_HOME/start.sh && \
    echo 'sleep 2' >> $APP_HOME/start.sh && \
    echo '' >> $APP_HOME/start.sh && \
    echo '# Inicia a aplicação Java' >> $APP_HOME/start.sh && \
    echo 'java $JAVA_OPTS -jar $APP_HOME/app.jar' >> $APP_HOME/start.sh && \
    chmod +x $APP_HOME/start.sh

# Define o utilizador não-root para segurança e ajusta permissões
RUN addgroup -S appuser && adduser -S appuser -G appuser && \
    chown -R appuser:appuser $APP_HOME
USER appuser

# Expõe portas (para VNC se necessário)
EXPOSE 5900

# Health check para monitorar estado da aplicação
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD pgrep -f "java.*app.jar" > /dev/null || exit 1

# Comando de inicialização
ENTRYPOINT ["/app/start.sh"]