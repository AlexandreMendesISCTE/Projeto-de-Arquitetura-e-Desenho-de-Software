# Dockerfile para aplicação React/Vite
# Multi-stage build para otimizar tamanho da imagem

# Stage 1: Build da aplicação
FROM node:20-alpine AS builder

# Definir diretório de trabalho
WORKDIR /app

# Definir ARGs primeiro (antes de COPY) para que possam ser usados durante o build
ARG VITE_N8N_WEBHOOK_URL=http://192.168.100.178:5678/webhook/chat
ARG VITE_OSRM_BASE_URL=http://router.project-osrm.org/route/v1
ARG VITE_NOMINATIM_BASE_URL=https://nominatim.openstreetmap.org
ARG VITE_MAP_DEFAULT_CENTER_LAT=38.7223
ARG VITE_MAP_DEFAULT_CENTER_LNG=-9.1393
ARG VITE_MAP_DEFAULT_ZOOM=13
ARG VITE_GOOGLE_MAPS_API_KEY

# Definir ENVs para que o Vite possa acessá-los durante o build
ENV VITE_N8N_WEBHOOK_URL=$VITE_N8N_WEBHOOK_URL
ENV VITE_OSRM_BASE_URL=$VITE_OSRM_BASE_URL
ENV VITE_NOMINATIM_BASE_URL=$VITE_NOMINATIM_BASE_URL
ENV VITE_MAP_DEFAULT_CENTER_LAT=$VITE_MAP_DEFAULT_CENTER_LAT
ENV VITE_MAP_DEFAULT_CENTER_LNG=$VITE_MAP_DEFAULT_CENTER_LNG
ENV VITE_MAP_DEFAULT_ZOOM=$VITE_MAP_DEFAULT_ZOOM
ENV VITE_GOOGLE_MAPS_API_KEY=$VITE_GOOGLE_MAPS_API_KEY

# Copiar ficheiros de dependências
COPY package*.json ./

# Instalar dependências
RUN npm ci --only=production=false

# Copiar código fonte
COPY . .

# Build da aplicação
RUN npm run build

# Stage 2: Servidor Nginx para servir a aplicação
FROM nginx:alpine

# Copiar ficheiros buildados do stage anterior
COPY --from=builder /app/dist /usr/share/nginx/html

# Copiar configuração customizada do nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expor porta 80
EXPOSE 80


RUN apk add --no-cache wget

# Health check para Docker
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost/ || exit 1

# Comando para iniciar nginx
CMD ["nginx", "-g", "daemon off;"]

