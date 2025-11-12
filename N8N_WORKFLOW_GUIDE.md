# Guia de Configuração n8n Workflow
# Integração com Map Route Explorer Chat

## 📋 Visão Geral

Este guia explica como configurar um workflow n8n para processar mensagens do chat da aplicação Map Route Explorer e ajudar os utilizadores a definir rotas.

## 🔗 Endpoint do Webhook

O workflow n8n deve estar configurado para receber POST requests em:
```
http://192.168.100.178:5678/webhook/chat
```

**Ou se usando Nginx Proxy Manager:**
```
http://192.168.100.178:81/n8n/webhook/chat
```

## 📥 Payload Recebido

O workflow receberá um JSON com a seguinte estrutura:

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

### Campos do Payload

- **message**: Mensagem do utilizador
- **currentRoute**: Estado atual da rota
  - **origin**: `{ name: string, lat: number, lng: number } | null`
  - **destination**: `{ name: string, lat: number, lng: number } | null`
  - **waypoints**: Array de `{ name: string, lat: number, lng: number }`
- **waitingForInput**: `'origin' | 'destination' | 'waypoint' | null`
- **timestamp**: ISO timestamp da mensagem

## 📤 Resposta Esperada

O workflow deve retornar um JSON com a seguinte estrutura:

```json
{
  "message": "Perfeito! Vou ajudá-lo a definir a rota de Lisboa ao Porto.",
  "action": null,
  "location": null,
  "suggestions": []
}
```

### Campos da Resposta

- **message**: Mensagem de resposta do bot (obrigatório)
- **action**: `'set_origin' | 'set_destination' | 'add_waypoint' | 'clear_route' | null` (opcional)
- **location**: `{ name: string, lat: number, lng: number } | null` (opcional)
- **suggestions**: Array de localizações sugeridas (opcional)

## 🔧 Configuração do Workflow n8n

### Passo 1: Criar Webhook Trigger

1. Adicionar nó **Webhook**
2. Configurar método: **POST**
3. Configurar path: `/webhook/chat`
4. Ativar **Respond When Last Node Finishes**

### Passo 2: Processar Mensagem

Adicionar nó **Code** ou **Function** para processar a mensagem:

```javascript
// Exemplo de processamento básico
const message = $input.item.json.message.toLowerCase();
const currentRoute = $input.item.json.currentRoute;
const waitingForInput = $input.item.json.waitingForInput;

let response = {
  message: "",
  action: null,
  location: null,
  suggestions: []
};

// Detectar intenção do utilizador
if (message.includes("origem") || message.includes("partida") || message.includes("começar")) {
  response.message = "Perfeito! Por favor, indique o ponto de partida. Pode clicar no mapa ou digitar o nome do local.";
  response.action = null;
} else if (message.includes("destino") || message.includes("chegada") || message.includes("terminar")) {
  response.message = "Ótimo! Agora preciso saber o destino. Pode clicar no mapa ou digitar o nome do local.";
  response.action = null;
} else if (message.includes("paragem") || message.includes("parada")) {
  response.message = "Vou adicionar uma paragem. Por favor, indique onde deseja parar.";
  response.action = null;
} else if (message.includes("lisboa") || message.includes("porto") || message.includes("coimbra")) {
  // Exemplo: extrair localização da mensagem
  if (message.includes("lisboa")) {
    response.message = "Origem definida: Lisboa. Agora preciso saber o destino.";
    response.location = {
      name: "Lisboa, Portugal",
      lat: 38.7223,
      lng: -9.1393
    };
    response.action = "set_origin";
  }
} else {
  response.message = "Como posso ajudá-lo? Posso ajudá-lo a definir origem, destino e paragens da sua rota.";
}

return response;
```

### Passo 3: Responder ao Webhook

1. Adicionar nó **Respond to Webhook**
2. Configurar para retornar o JSON processado
3. Status Code: **200**

## 💡 Exemplos de Respostas

### Solicitar Origem
```json
{
  "message": "Por favor, indique o ponto de partida. Pode clicar no mapa ou digitar o nome do local.",
  "action": null,
  "location": null
}
```

### Solicitar Destino
```json
{
  "message": "Agora preciso saber o destino. Pode clicar no mapa ou digitar o nome do local.",
  "action": null,
  "location": null
}
```

### Solicitar Paragem
```json
{
  "message": "Vou adicionar uma paragem. Por favor, indique onde deseja parar.",
  "action": null,
  "location": null
}
```

### Definir Localização Automaticamente
```json
{
  "message": "Origem definida: Lisboa, Portugal",
  "action": "set_origin",
  "location": {
    "name": "Lisboa, Portugal",
    "lat": 38.7223,
    "lng": -9.1393
  }
}
```

### Sugerir Localizações
```json
{
  "message": "Encontrei várias opções. Qual delas pretende?",
  "action": null,
  "location": null,
  "suggestions": [
    {
      "name": "Lisboa, Portugal",
      "lat": 38.7223,
      "lng": -9.1393
    },
    {
      "name": "Lisboa Airport",
      "lat": 38.7742,
      "lng": -9.1342
    }
  ]
}
```

## 🎯 Fluxo Recomendado

1. **Receber Mensagem** → Webhook Trigger
2. **Processar Intenção** → Code/Function Node
3. **Validar Contexto** → Verificar currentRoute e waitingForInput
4. **Gerar Resposta** → Baseado no contexto e intenção
5. **Responder** → Respond to Webhook

## 🔍 Detecção de Intenções

### Padrões de Mensagem Comuns

- **Definir Origem**: "origem", "partida", "começar em", "início"
- **Definir Destino**: "destino", "chegada", "terminar em", "fim"
- **Adicionar Paragem**: "paragem", "parada", "parar em"
- **Limpar Rota**: "limpar", "reset", "começar de novo"
- **Perguntar Rota**: "como", "onde", "quando"

## 🛠️ Integração com APIs Externas

### Geocoding (Nominatim)

Se quiser fazer geocoding no n8n:

```javascript
// Exemplo usando HTTP Request node
const locationName = "Lisboa";
const geocodingUrl = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(locationName)}&format=json&limit=1`;

// Fazer requisição HTTP
const response = await $http.get(geocodingUrl);
const result = response.data[0];

return {
  name: result.display_name,
  lat: parseFloat(result.lat),
  lng: parseFloat(result.lon)
};
```

## 📝 Notas Importantes

1. **CORS**: Certifique-se de que o n8n permite requisições da origem da aplicação
2. **Timeout**: O workflow deve responder em menos de 30 segundos
3. **Formato**: Sempre retornar JSON válido
4. **Mensagens**: Use português para melhor experiência do utilizador
5. **Contexto**: Utilize `currentRoute` e `waitingForInput` para contexto

## 🐛 Troubleshooting

### Problema: Webhook não recebe requisições

- Verificar se o n8n está acessível em `http://192.168.100.178:5678`
- Verificar configuração do webhook (método POST, path correto)
- Verificar logs do n8n: `docker logs n8n`
- Testar webhook diretamente:
  ```bash
  curl -X POST http://192.168.100.178:5678/webhook/chat \
    -H "Content-Type: application/json" \
    -d '{"message":"test","currentRoute":{"origin":null,"destination":null,"waypoints":[]},"waitingForInput":null,"timestamp":"2025-01-15T10:30:00.000Z"}'
  ```

### Problema: Resposta não é processada

- Verificar formato JSON da resposta
- Verificar se o campo `message` está presente
- Verificar status code (deve ser 200)

### Problema: Localizações não são definidas

- Verificar se o campo `action` está correto
- Verificar formato do campo `location`
- Verificar se coordenadas são válidas (lat: -90 a 90, lng: -180 a 180)

## 📚 Recursos Adicionais

- [Documentação n8n](https://docs.n8n.io/)
- [Nominatim API](https://nominatim.org/release-docs/develop/api/Overview/)
- [OpenStreetMap](https://www.openstreetmap.org/)

