# 📋 Lista de Tarefas SCRUM - Map Route Explorer

## ÉPICO 1: VISUALIZAÇÃO E NAVEGAÇÃO DE MAPAS

### US-001: Visualização do Mapa Base

- Configurar React Leaflet (2h)
- Implementar MapContainer component (2h)
- Configurar tiles OpenStreetMap (1h)
- Implementar controlos de zoom (1h)
- Testar responsividade (2h)

### US-002: Geolocalização do Utilizador

- Implementar hook useGeolocation (2h)
- Criar botão de localização na UI (1h)
- Implementar tratamento de erros (1h)
- Testes unitários (1h)

### US-003: Auto-fit do Mapa

- Implementar AutoFitBounds component (2h)
- Calcular bounds dinâmicos (1h)

## ÉPICO 2: PESQUISA E GEOCODIFICAÇÃO

### US-004: Pesquisa de Localização por Texto

- Implementar serviço Nominatim (2h)
- Criar componente LocationSearch (3h)
- Implementar debouncing (1h)
- Implementar autocomplete dropdown (2h)

### US-005: Marcadores no Mapa

- Implementar MarkerLayer component (2h)
- Criar ícones personalizados (1h)
- Implementar drag & drop (2h)

## ÉPICO 3: CÁLCULO E GESTÃO DE ROTAS

### US-006: Cálculo de Rota Básico

- Implementar serviço OSRM (3h)
- Criar RouteLayer component (2h)
- Implementar hook useRoute (2h)
- Tratamento de erros (1h)

### US-007: Informações da Rota

- Criar RouteInfo component (2h)
- Formatar distância e tempo (1h)

### US-008: Múltiplos Modos de Transporte

- Criar TransportModeSelector component (2h)
- Adaptar serviço OSRM para diferentes perfis (2h)
- Atualizar cálculos de tempo (1h)

### US-009: Waypoints (Paragens Intermédias)

- Implementar gestão de waypoints no store (2h)
- Criar UI para adicionar/remover waypoints (3h)
- Implementar drag & drop para reordenar (2h)
- Atualizar serviço de routing (1h)

### US-010: Transporte Público (Transit)

- Integrar Google Routes API (4h)
- Criar tipos TypeScript para Transit (2h)
- Implementar transit.service.ts (4h)
- Criar componentes UI Transit (6h)
- Implementar visualização no mapa (4h)

### US-011: Comparação de Tempos entre Modos

- Calcular rotas para todos os modos (2h)
- Criar painel de comparação UI (2h)
- Implementar cache para evitar requisições repetidas (1h)

## ÉPICO 4: PONTOS DE INTERESSE (POIs)

### US-012: Exibição de POIs ao Longo da Rota

- Implementar poi.service.ts com Overpass API (3h)
- Criar POILayer component (2h)
- Implementar filtros de categoria (2h)
- Criar popups de detalhes (1h)

## ÉPICO 5: INTEGRAÇÕES EXTERNAS

### US-013: Integração com Google Maps App

- Implementar geração de URL Google Maps (1h)
- Criar botão na UI (1h)
- Testar em diferentes dispositivos (1h)

### US-014: Chatbot Assistente (n8n)

- Implementar n8n.service.ts (2h)
- Criar ChatWidget component (4h)
- Implementar UI de mensagens (2h)

## ÉPICO 6: UI/UX E RESPONSIVIDADE

### US-015: Design Responsivo Mobile

- Configurar Tailwind breakpoints (1h)
- Criar layouts responsivos (4h)
- Implementar gestos touch (2h)
- Testar em dispositivos reais (1h)

### US-016: Tema Visual e Acessibilidade

- Definir design system (1h)
- Implementar tema Tailwind (2h)
- Auditar acessibilidade (2h)

## ÉPICO 7: INFRAESTRUTURA E DEVOPS

### US-017: Deploy com Docker

- Criar Dockerfile multi-stage (2h)
- Configurar docker-compose.yml (2h)
- Configurar Nginx (2h)
- Documentar processo de deploy (2h)

### US-018: CI/CD Pipeline

- Criar workflow GitHub Actions (2h)
- Configurar jobs de lint e build (2h)
- Documentar processo (1h)

## ÉPICO 8: FUNCIONALIDADES FUTURAS

### US-019: Histórico de Rotas

- Implementar persistência localStorage (2h)
- Criar UI de histórico (3h)
- Implementar restauro de rotas (2h)
- Gestão de limite de rotas (1h)

### US-020: Exportação de Rotas (GPX/KML)

- Implementar gerador GPX (2h)
- Implementar gerador KML (2h)
- Criar UI de exportação (1h)

### US-021: Perfil Altimétrico

- Integrar API de elevação (4h)
- Implementar componente de gráfico (5h)
- Sincronizar com mapa (4h)
