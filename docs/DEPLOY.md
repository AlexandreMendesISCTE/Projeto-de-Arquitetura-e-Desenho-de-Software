# Guia de Deploy - Map Route Explorer

# Deploy em Docker com nginx no servidor Ubuntu

## 📋 Pré-requisitos

- Docker e Docker Compose instalados no servidor Ubuntu
- Acesso SSH ao servidor (192.168.100.178)
- n8n workflow configurado e acessível em `http://192.168.100.178:5678/webhook/chat`
- Porta 8082 disponível no servidor

## 🚀 Passos para Deploy

### 1. Preparar o Ambiente

```bash
# Clonar ou copiar o projeto para o servidor
cd /caminho/para/projeto

# Criar ficheiro .env a partir do exemplo
cp .env.example .env

# Editar .env com as configurações corretas
nano .env
```

### 2. Configurar Variáveis de Ambiente

Crie um ficheiro `.env` na raiz do projeto (ou configure via Portainer):

```env
# n8n Webhook URL (ajuste conforme sua configuração)
# Opção 1: Acesso direto ao n8n
VITE_N8N_WEBHOOK_URL=http://192.168.100.178:5678/webhook/chat

# Opção 2: Se usando Nginx Proxy Manager
# VITE_N8N_WEBHOOK_URL=http://192.168.100.178:81/n8n/webhook/chat

# APIs de Roteamento e Geocodificação
VITE_OSRM_BASE_URL=http://router.project-osrm.org/route/v1
VITE_NOMINATIM_BASE_URL=https://nominatim.openstreetmap.org

# Configurações do Mapa (Lisboa, Portugal)
VITE_MAP_DEFAULT_CENTER_LAT=38.7223
VITE_MAP_DEFAULT_CENTER_LNG=-9.1393
VITE_MAP_DEFAULT_ZOOM=13

# Google Maps API Key (OBRIGATÓRIO para cálculo de rotas)
VITE_GOOGLE_MAPS_API_KEY=your_google_maps_api_key_here
```

**Nota:** As variáveis de ambiente são injetadas no build. Se alterar após o build, será necessário fazer rebuild.

### 3. Build e Deploy

```bash
# Build da imagem Docker
docker-compose build

# Iniciar os containers
docker-compose up -d

# Verificar logs
docker-compose logs -f

# Verificar status
docker-compose ps
```

### 4. Verificar Deploy

A aplicação estará disponível em:

- **URL Principal**: `http://192.168.100.178:8082/nginx/proxy`
- **Health Check**: `http://192.168.100.178:8082/nginx/proxy/health`

**Nota:** Se estiver usando Nginx Proxy Manager, pode configurar um proxy reverso apontando para `http://192.168.100.178:8082/nginx/proxy`

### 5. Configurar n8n Workflow

O workflow n8n deve estar configurado para receber POST requests em:

```
http://192.168.100.178:5678/webhook/chat
```

**Ou se usando Nginx Proxy Manager:**

```
http://192.168.100.178:81/n8n/webhook/chat
```

**Payload esperado:**

```json
{
  "message": "Quero ir de Lisboa ao Porto",
  "currentRoute": {
    "origin": null,
    "destination": null,
    "waypoints": []
  },
  "waitingForInput": null,
  "timestamp": "2025-01-15T10:30:00.000Z"
}
```

**Resposta esperada:**

```json
{
  "message": "Perfeito! Vou ajudá-lo a definir a rota de Lisboa ao Porto.",
  "action": null,
  "location": null,
  "suggestions": []
}
```

## 🔧 Comandos Úteis

```bash
# Parar containers
docker-compose down

# Rebuild sem cache
docker-compose build --no-cache

# Ver logs em tempo real
docker-compose logs -f map-route-explorer

# Entrar no container
docker exec -it map-route-explorer-app sh

# Reiniciar apenas a aplicação
docker-compose restart map-route-explorer
```

## 📊 Monitorização

### Health Checks

```bash
# Verificar health da aplicação
curl http://192.168.100.178:8082/nginx/proxy/health

# Verificar health do container
docker inspect --format='{{.State.Health.Status}}' map-route-explorer-app
```

### Logs

```bash
# Logs da aplicação
docker-compose logs map-route-explorer

# Logs do nginx proxy
docker-compose logs nginx-proxy

# Logs do nginx dentro do container da app
docker exec map-route-explorer-app cat /var/log/nginx/error.log
```

## 🔄 Atualizações

Para atualizar a aplicação:

```bash
# 1. Fazer pull das alterações
git pull

# 2. Rebuild da imagem
docker-compose build

# 3. Reiniciar com nova imagem
docker-compose up -d

# 4. Verificar se está funcionando
curl http://192.168.100.178:8082/nginx/proxy/health
```

## 🐛 Troubleshooting

### Problema: Aplicação não carrega

1. Verificar se os containers estão a correr:

   ```bash
   docker-compose ps
   ```

2. Verificar logs de erro:

   ```bash
   docker-compose logs map-route-explorer
   ```

3. Verificar conectividade com n8n:

   ```bash
   # Testar webhook n8n diretamente
   curl -X POST http://192.168.100.178:5678/webhook/chat \
     -H "Content-Type: application/json" \
     -d '{"message":"test","currentRoute":{"origin":null,"destination":null,"waypoints":[]},"waitingForInput":null,"timestamp":"2025-01-15T10:30:00.000Z"}'

   # Ou se usando Nginx Proxy Manager
   curl -X POST http://192.168.100.178:81/n8n/webhook/chat \
     -H "Content-Type: application/json" \
     -d '{"message":"test","currentRoute":{"origin":null,"destination":null,"waypoints":[]},"waitingForInput":null,"timestamp":"2025-01-15T10:30:00.000Z"}'
   ```

### Problema: Chat não funciona

1. Verificar variável de ambiente `VITE_N8N_WEBHOOK_URL`
2. Verificar se o workflow n8n está ativo
3. Verificar CORS no n8n (deve permitir origem da aplicação)

### Problema: Erro 502 Bad Gateway

1. Verificar se o container da aplicação está a correr
2. Verificar configuração do nginx proxy
3. Verificar conectividade entre containers na mesma network

## 📝 Notas Importantes

- A aplicação é uma SPA (Single Page Application), por isso o nginx está configurado para servir `index.html` para todas as rotas
- As variáveis de ambiente são injetadas no build, não em runtime
- Para alterar variáveis de ambiente, é necessário fazer rebuild da imagem
- O chat integra com n8n através de webhook HTTP POST
- O n8n deve processar mensagens e retornar respostas em formato JSON

## 🔐 Segurança

- Certifique-se de que o nginx proxy está configurado corretamente
- Use HTTPS em produção (configurar certificados SSL)
- Configure firewall para permitir apenas portas necessárias
- Mantenha imagens Docker atualizadas
