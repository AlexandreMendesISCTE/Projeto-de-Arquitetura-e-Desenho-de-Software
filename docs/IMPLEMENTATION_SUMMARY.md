# Map Route Explorer - Resumo de Implementação

## ✅ Funcionalidades Implementadas

### 1. Chat Widget

- ✅ Ícone circular flutuante no canto inferior direito
- ✅ Interface de chat responsiva e intuitiva
- ✅ Integração com n8n workflow
- ✅ Suporte para definir origem, destino e paragens via chat
- ✅ Geocoding automático de localizações mencionadas

### 2. Integração n8n

- ✅ Serviço de comunicação com webhook n8n
- ✅ Envio de contexto da rota atual
- ✅ Processamento de respostas do bot
- ✅ Detecção automática de comandos de localização

### 3. Docker & Deploy

- ✅ Dockerfile multi-stage otimizado
- ✅ docker-compose.yml configurado
- ✅ Configuração nginx para proxy reverso
- ✅ Health checks implementados
- ✅ Variáveis de ambiente configuráveis

## 📁 Estrutura de Ficheiros Criados

```
Projeto-de-Arquitetura-e-Desenho-de-Software/
├── src/
│   ├── components/
│   │   └── ChatWidget.tsx          # Componente de chat
│   └── services/
│       └── api/
│           └── n8n.service.ts       # Serviço de integração n8n
├── Dockerfile                       # Build da aplicação
├── docker-compose.yml              # Orquestração Docker
├── nginx.conf                       # Configuração nginx interno
├── nginx.conf                       # Configuração nginx (proxy, rate limiting, security headers)
├── .dockerignore                    # Ficheiros ignorados no build
├── .env.example                     # Exemplo de variáveis de ambiente
├── DEPLOY.md                        # Guia de deploy
└── N8N_WORKFLOW_GUIDE.md           # Guia de configuração n8n
```

## 🚀 Como Fazer Deploy

### 1. Preparar Ambiente

```bash
cd Projeto-de-Arquitetura-e-Desenho-de-Software
cp .env.example .env
# Editar .env com as configurações corretas
```

### 2. Build e Deploy

```bash
docker-compose build
docker-compose up -d
```

### 3. Verificar

```bash
# Verificar status
docker-compose ps

# Ver logs
docker-compose logs -f

# Testar health check
curl http://192.168.100.178:81/nginx/proxy/health
```

## 🔗 URLs Importantes

- **Aplicação**: `http://192.168.100.178:81/nginx/proxy`
- **Health Check**: `http://192.168.100.178:81/nginx/proxy/health`
- **n8n Webhook**: `http://192.168.100.178:81/n8n/webhook/chat`

## 📝 Variáveis de Ambiente

Configurar no ficheiro `.env`:

```env
VITE_N8N_WEBHOOK_URL=http://192.168.100.178:81/n8n/webhook/chat
VITE_OSRM_BASE_URL=http://router.project-osrm.org/route/v1
VITE_NOMINATIM_BASE_URL=https://nominatim.openstreetmap.org
VITE_MAP_DEFAULT_CENTER_LAT=38.7223
VITE_MAP_DEFAULT_CENTER_LNG=-9.1393
VITE_MAP_DEFAULT_ZOOM=13
```

## 🎯 Funcionalidades do Chat

### Comandos Suportados

- **Definir Origem**: "Quero começar em Lisboa"
- **Definir Destino**: "O destino é Porto"
- **Adicionar Paragem**: "Quero parar em Coimbra"
- **Via Chat**: O bot pode solicitar que clique no mapa
- **Via Texto**: O bot pode fazer geocoding automático

### Fluxo de Interação

1. Utilizador envia mensagem no chat
2. Mensagem é enviada para n8n workflow
3. n8n processa e retorna resposta
4. Aplicação detecta comandos de localização
5. Localizações são definidas automaticamente ou solicitadas ao utilizador

## 🔧 Configuração n8n

Ver guia completo em `N8N_WORKFLOW_GUIDE.md`

### Payload Enviado

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

### Resposta Esperada

```json
{
  "message": "Perfeito! Vou ajudá-lo a definir a rota.",
  "action": null,
  "location": null
}
```

## 📊 Arquitetura

```
┌─────────────────┐
│   Frontend      │
│   (React/Vite)  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   Nginx Proxy   │
│   (Port 81)     │
└────────┬────────┘
         │
    ┌────┴────┐
    ▼         ▼
┌────────┐ ┌────────┐
│  App   │ │  n8n   │
│ Docker │ │Workflow│
└────────┘ └────────┘
```

## 🐛 Troubleshooting

### Chat não aparece

- Verificar se `ChatWidget` está importado em `App.tsx`
- Verificar se a imagem está em `public/`

### Chat não conecta ao n8n

- Verificar variável `VITE_N8N_WEBHOOK_URL`
- Verificar se n8n está acessível
- Verificar CORS no n8n

### Deploy falha

- Verificar logs: `docker-compose logs`
- Verificar espaço em disco
- Verificar portas disponíveis

## 📚 Documentação Adicional

- `DEPLOY.md` - Guia detalhado de deploy
- `N8N_WORKFLOW_GUIDE.md` - Configuração do workflow n8n
- `README.md` - Documentação geral do projeto

## ✨ Próximos Passos

1. Configurar workflow n8n seguindo `N8N_WORKFLOW_GUIDE.md`
2. Testar integração chat → n8n → aplicação
3. Ajustar variáveis de ambiente conforme necessário
4. Monitorizar logs e performance
