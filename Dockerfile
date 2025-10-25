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
ENV JAVA_OPTS="-Xmx1g -Xms512m" \
    APP_HOME=/app \
    LOGS_DIR=/app/logs \
    LANG=pt_PT.UTF-8 \
    TZ=Europe/Lisbon

# Cria diretório da aplicação
WORKDIR $APP_HOME

# Instala dependências básicas do sistema (Alpine Linux)
RUN apk add --no-cache \
    bash \
    curl \
    wget \
    ttf-dejavu \
    fontconfig \
    tzdata && \
    # Configura timezone
    cp /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# Cria diretórios necessários
RUN mkdir -p $LOGS_DIR

# Copia o JAR compilado da etapa de build
COPY --from=builder /build/target/map-route-explorer-*-jar-with-dependencies.jar $APP_HOME/app.jar

# Define o utilizador não-root para segurança e ajusta permissões
RUN addgroup -S appuser && adduser -S appuser -G appuser && \
    chown -R appuser:appuser $APP_HOME
USER appuser

# Expõe portas (para APIs futuras)
EXPOSE 8080

# Health check para monitorar estado da aplicação
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD pgrep -f "java.*app.jar" > /dev/null || exit 1

# Comando de inicialização - Executa Java diretamente
CMD ["java", "-jar", "/app/app.jar"]