# Dockerfile para Map Route Explorer
# Utiliza OpenJDK 23 como base (versão LTS mais recente estável)
FROM openjdk:23-jdk-slim

# Metadados da imagem
LABEL maintainer="Equipa de Desenvolvimento"
LABEL version="1.0.0"
LABEL description="Sistema Interativo de Rotas e Exploração de Locais com OpenStreetMap"

# Define variáveis de ambiente
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV APP_HOME=/app
ENV LOGS_DIR=/app/logs

# Cria diretório da aplicação
WORKDIR $APP_HOME

# Instala dependências do sistema
RUN apt-get update && \
    apt-get install -y curl wget && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Cria diretório para logs
RUN mkdir -p $LOGS_DIR

# Copia o JAR da aplicação
COPY target/map-route-explorer-*-jar-with-dependencies.jar app.jar

# Expõe a porta da aplicação (se necessário para interface web futura)
EXPOSE 8080

# Define o utilizador não-root para segurança
RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN chown -R appuser:appuser $APP_HOME
USER appuser

# Comando de inicialização
ENTRYPOINT ["java", "-jar", "app.jar"]
