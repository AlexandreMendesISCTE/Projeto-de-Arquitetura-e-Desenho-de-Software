# Deploy via Portainer - Map Route Explorer

## 📋 Pré-requisitos

- Portainer instalado e acessível
- Acesso ao servidor Ubuntu (192.168.100.178)
- n8n workflow configurado (ver `N8N_WORKFLOW_GUIDE.md`)

## 🚀 Passos para Deploy via Portainer

### 1. Preparar o Repositório

```bash
# No seu computador local
cd Projeto-de-Arquitetura-e-Desenho-de-Software

# Verificar que todas as alterações estão commitadas
git status

# Push para o repositório
git push
```

### 2. Clonar no Servidor Ubuntu

```bash
# SSH para o servidor
ssh yocoms@192.168.100.178

# Criar diretório para o projeto
mkdir -p ~/projects/map-route-explorer
cd ~/projects/map-route-explorer

# Clonar o repositório
git clone <seu-repositorio-url> .

# Ou se já tiver o código, copiar para o servidor
```

### 3. Criar Ficheiro .env

```bash
# Criar ficheiro .env
nano .env
```

Conteúdo do `.env`:

```env
# n8n Webhook URL
# Opção 1: Acesso direto (recomendado)
VITE_N8N_WEBHOOK_URL=http://192.168.100.178:5678/webhook/chat

# Opção 2: Via Nginx Proxy Manager (se configurado)
# VITE_N8N_WEBHOOK_URL=http://192.168.100.178:81/n8n/webhook/chat

# APIs
VITE_OSRM_BASE_URL=http://router.project-osrm.org/route/v1
VITE_NOMINATIM_BASE_URL=https://nominatim.openstreetmap.org

# Mapa (Lisboa, Portugal)
VITE_MAP_DEFAULT_CENTER_LAT=38.7223
VITE_MAP_DEFAULT_CENTER_LNG=-9.1393
VITE_MAP_DEFAULT_ZOOM=13

# Google Maps API Key (OBRIGATÓRIO)
VITE_GOOGLE_MAPS_API_KEY=your_google_maps_api_key_here
```

### 4. Deploy via Portainer

#### Opção A: Usando Stack (Recomendado)

1. **Abrir Portainer**
   - Acesse: `http://192.168.100.178:9000` (ou porta configurada)
   - Faça login

2. **Criar Nova Stack**
   - Vá em **Stacks** → **Add Stack**
   - Nome: `map-route-explorer`
   - Método: **Repository**

3. **Configurar Repository**
   - **Repository URL**: URL do seu repositório Git
   - **Repository Reference**: `main` ou `master`
   - **Compose Path**: `docker-compose.yml`
   - **Auto-update**: Ativar se quiser atualizações automáticas

4. **Configurar Variáveis de Ambiente**
   - Clique em **Environment variables**
   - Adicione as variáveis do `.env`:
     ```
     VITE_N8N_WEBHOOK_URL=http://192.168.100.178:5678/webhook/chat
     VITE_OSRM_BASE_URL=http://router.project-osrm.org/route/v1
     VITE_NOMINATIM_BASE_URL=https://nominatim.openstreetmap.org
     VITE_MAP_DEFAULT_CENTER_LAT=38.7223
     VITE_MAP_DEFAULT_CENTER_LNG=-9.1393
     VITE_MAP_DEFAULT_ZOOM=13
     VITE_GOOGLE_MAPS_API_KEY=your_key_here
     ```

5. **Deploy**
   - Clique em **Deploy the stack**
   - Aguarde o build e deploy

#### Opção B: Upload do docker-compose.yml

1. **Abrir Portainer**
   - Vá em **Stacks** → **Add Stack**
   - Nome: `map-route-explorer`
   - Método: **Web editor**

2. **Colar docker-compose.yml**
   - Copie o conteúdo de `docker-compose.yml`
   - Cole no editor

3. **Adicionar Variáveis de Ambiente**
   - Clique em **Environment variables**
   - Adicione as variáveis (mesmas do Opção A)

4. **Deploy**
   - Clique em **Deploy the stack**

### 5. Verificar Deploy

```bash
# Verificar containers
docker ps | grep map-route-explorer

# Verificar logs
docker logs map-route-explorer-app
docker logs map-route-explorer-nginx

# Testar aplicação
curl http://192.168.100.178:8082/nginx/proxy/health
```

### 6. Acessar Aplicação

- **URL Direta**: `http://192.168.100.178:8082/nginx/proxy`
- **Via Nginx Proxy Manager**: Configure um proxy apontando para `http://192.168.100.178:8082/nginx/proxy`

## 🔧 Atualizações

### Via Portainer

1. **Se usando Repository method:**
   - Portainer detecta mudanças automaticamente (se auto-update ativado)
   - Ou clique em **Editor** → **Pull and redeploy**

2. **Se usando Web editor:**
   - Atualize o `docker-compose.yml` no editor
   - Clique em **Update the stack**

### Via SSH

```bash
# SSH para o servidor
ssh yocoms@192.168.100.178

# Ir para o diretório do projeto
cd ~/projects/map-route-explorer

# Pull das alterações
git pull

# Rebuild e restart via Portainer ou:
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

## 🐛 Troubleshooting

### Container não inicia

1. Verificar logs no Portainer:
   - Vá em **Containers** → Selecione o container → **Logs**

2. Verificar variáveis de ambiente:
   - Vá em **Stacks** → **map-route-explorer** → **Editor** → Verifique variáveis

### Porta 8082 já em uso

1. Editar `docker-compose.yml`:
   - Mude `"8082:80"` para outra porta (ex: `"8083:80"`)

2. Atualizar stack no Portainer

### Chat não funciona

1. Verificar n8n webhook:
   ```bash
   curl -X POST http://192.168.100.178:5678/webhook/chat \
     -H "Content-Type: application/json" \
     -d '{"message":"test","currentRoute":{"origin":null,"destination":null,"waypoints":[]},"waitingForInput":null,"timestamp":"2025-01-15T10:30:00.000Z"}'
   ```

2. Verificar variável `VITE_N8N_WEBHOOK_URL` no build
3. Verificar se workflow n8n está ativo

## 📝 Notas

- As variáveis de ambiente são injetadas no **build time**, não em runtime
- Para alterar variáveis, é necessário fazer **rebuild** da imagem
- O build pode demorar alguns minutos na primeira vez
- Certifique-se de que o Google Maps API Key está configurado corretamente

## 🔗 Próximos Passos

1. Configurar n8n workflow (ver `N8N_WORKFLOW_GUIDE.md`)
2. Configurar Nginx Proxy Manager (opcional)
3. Configurar SSL/HTTPS (opcional, recomendado para produção)

