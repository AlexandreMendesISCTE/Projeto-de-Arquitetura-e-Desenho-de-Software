# 📋 Modelo SCRUM para Trello - Map Route Explorer

## Configuração do Board Trello

### Listas (Colunas) Recomendadas
1. **📦 Product Backlog** - Todas as User Stories priorizadas
2. **📋 Sprint Backlog** - User Stories selecionadas para a Sprint atual
3. **🚧 In Progress** - Tarefas em desenvolvimento
4. **👀 Code Review** - Aguardando revisão de código
5. **🧪 Testing** - Em fase de testes
6. **✅ Done** - Concluídas e validadas

### Labels (Etiquetas)
- 🔴 **Alta Prioridade** (Must Have)
- 🟠 **Média Prioridade** (Should Have)
- 🟢 **Baixa Prioridade** (Could Have)
- 🔵 **Bug**
- 🟣 **Technical Debt**
- ⚪ **Spike/Research**

---

## 📦 PRODUCT BACKLOG COMPLETO

---

### ÉPICO 1: VISUALIZAÇÃO E NAVEGAÇÃO DE MAPAS

---

#### US-001: Visualização do Mapa Base
**Como** utilizador,  
**Quero** visualizar um mapa interativo,  
**Para** poder explorar geograficamente a área desejada.

**Critérios de Aceitação:**
- [ ] O mapa carrega automaticamente ao abrir a aplicação
- [ ] O mapa está centrado em Lisboa (38.7223, -9.1393) por defeito
- [ ] O utilizador pode fazer zoom in/out com scroll ou botões
- [ ] O utilizador pode arrastar o mapa para navegar
- [ ] Os tiles do OpenStreetMap carregam corretamente
- [ ] O mapa é responsivo em diferentes tamanhos de ecrã

**Story Points:** 5  
**Estimativa:** 8 horas  
**Prioridade:** 🔴 Alta (Must Have)  
**Sprint:** 1

**Tarefas Técnicas:**
- [ ] Configurar React Leaflet (2h)
- [ ] Implementar MapContainer component (2h)
- [ ] Configurar tiles OpenStreetMap (1h)
- [ ] Implementar controlos de zoom (1h)
- [ ] Testar responsividade (2h)

---

#### US-002: Geolocalização do Utilizador
**Como** utilizador,  
**Quero** que o sistema detete a minha localização atual,  
**Para** usar como ponto de partida sem ter de escrever o endereço.

**Critérios de Aceitação:**
- [ ] Existe um botão "Usar minha localização"
- [ ] O sistema pede permissão ao navegador para aceder à localização
- [ ] A localização é obtida com precisão GPS
- [ ] Um marcador indica a posição atual no mapa
- [ ] Se a permissão for negada, mostra mensagem de erro amigável
- [ ] Loading indicator enquanto obtém localização

**Story Points:** 3  
**Estimativa:** 5 horas  
**Prioridade:** 🔴 Alta (Must Have)  
**Sprint:** 1

**Tarefas Técnicas:**
- [ ] Implementar hook useGeolocation (2h)
- [ ] Criar botão de localização na UI (1h)
- [ ] Implementar tratamento de erros (1h)
- [ ] Testes unitários (1h)

---

#### US-003: Auto-fit do Mapa
**Como** utilizador,  
**Quero** que o mapa ajuste automaticamente o zoom para mostrar toda a rota,  
**Para** ver origem, destino e waypoints de uma só vez.

**Critérios de Aceitação:**
- [ ] Quando uma rota é calculada, o mapa ajusta o zoom automaticamente
- [ ] Todos os pontos (origem, waypoints, destino) ficam visíveis
- [ ] Existe margem adequada nas bordas
- [ ] O ajuste é animado suavemente

**Story Points:** 2  
**Estimativa:** 3 horas  
**Prioridade:** 🟠 Média (Should Have)  
**Sprint:** 2

**Tarefas Técnicas:**
- [ ] Implementar AutoFitBounds component (2h)
- [ ] Calcular bounds dinâmicos (1h)

---

### ÉPICO 2: PESQUISA E GEOCODIFICAÇÃO

---

#### US-004: Pesquisa de Localização por Texto
**Como** utilizador,  
**Quero** pesquisar localizações escrevendo o endereço,  
**Para** encontrar facilmente o local pretendido.

**Critérios de Aceitação:**
- [ ] Existe campo de pesquisa para origem e destino
- [ ] A pesquisa mostra sugestões em tempo real (autocomplete)
- [ ] Mínimo de 3 caracteres para iniciar pesquisa
- [ ] Debouncing de 300ms para evitar requisições excessivas
- [ ] Resultados mostram nome e endereço completo
- [ ] Ao selecionar, o mapa centra na localização

**Story Points:** 5  
**Estimativa:** 8 horas  
**Prioridade:** 🔴 Alta (Must Have)  
**Sprint:** 1

**Tarefas Técnicas:**
- [ ] Implementar serviço Nominatim (2h)
- [ ] Criar componente LocationSearch (3h)
- [ ] Implementar debouncing (1h)
- [ ] Implementar autocomplete dropdown (2h)

---

#### US-005: Marcadores no Mapa
**Como** utilizador,  
**Quero** ver marcadores visuais para origem, destino e waypoints,  
**Para** identificar claramente os pontos da rota.

**Critérios de Aceitação:**
- [ ] Marcador verde para origem (A)
- [ ] Marcador vermelho para destino (B)
- [ ] Marcadores numerados para waypoints (1, 2, 3...)
- [ ] Marcadores são clicáveis e mostram tooltip com nome
- [ ] Marcadores podem ser arrastados para reposicionar

**Story Points:** 3  
**Estimativa:** 5 horas  
**Prioridade:** 🔴 Alta (Must Have)  
**Sprint:** 1

**Tarefas Técnicas:**
- [ ] Implementar MarkerLayer component (2h)
- [ ] Criar ícones personalizados (1h)
- [ ] Implementar drag & drop (2h)

---

### ÉPICO 3: CÁLCULO E GESTÃO DE ROTAS

---

#### US-006: Cálculo de Rota Básico
**Como** utilizador,  
**Quero** calcular uma rota entre origem e destino,  
**Para** saber o caminho a percorrer.

**Critérios de Aceitação:**
- [ ] Botão "Calcular Rota" ativo quando origem e destino definidos
- [ ] A rota é desenhada no mapa como polyline
- [ ] Mostra indicador de loading durante o cálculo
- [ ] Trata erros de rota não encontrada
- [ ] A rota segue estradas reais

**Story Points:** 5  
**Estimativa:** 8 horas  
**Prioridade:** 🔴 Alta (Must Have)  
**Sprint:** 1

**Tarefas Técnicas:**
- [ ] Implementar serviço OSRM (3h)
- [ ] Criar RouteLayer component (2h)
- [ ] Implementar hook useRoute (2h)
- [ ] Tratamento de erros (1h)

---

#### US-007: Informações da Rota
**Como** utilizador,  
**Quero** ver a distância e tempo estimado da rota,  
**Para** planear a minha viagem.

**Critérios de Aceitação:**
- [ ] Mostra distância total em km
- [ ] Mostra tempo estimado em formato legível (1h 30min)
- [ ] Informações atualizadas quando a rota muda
- [ ] Painel de informações bem visível

**Story Points:** 2  
**Estimativa:** 3 horas  
**Prioridade:** 🔴 Alta (Must Have)  
**Sprint:** 1

**Tarefas Técnicas:**
- [ ] Criar RouteInfo component (2h)
- [ ] Formatar distância e tempo (1h)

---

#### US-008: Múltiplos Modos de Transporte
**Como** utilizador,  
**Quero** escolher o modo de transporte (carro, bicicleta, a pé),  
**Para** obter rotas adequadas ao meu meio de deslocação.

**Critérios de Aceitação:**
- [ ] Selector com opções: Carro, Bicicleta, A pé
- [ ] Ícones visuais para cada modo
- [ ] Rota recalculada automaticamente ao mudar modo
- [ ] Tempo estimado ajustado à velocidade do modo
- [ ] Modo selecionado destacado visualmente

**Story Points:** 3  
**Estimativa:** 5 horas  
**Prioridade:** 🔴 Alta (Must Have)  
**Sprint:** 2

**Tarefas Técnicas:**
- [ ] Criar TransportModeSelector component (2h)
- [ ] Adaptar serviço OSRM para diferentes perfis (2h)
- [ ] Atualizar cálculos de tempo (1h)

---

#### US-009: Waypoints (Paragens Intermédias)
**Como** utilizador,  
**Quero** adicionar paragens intermédias na rota,  
**Para** planear viagens com múltiplas paragens.

**Critérios de Aceitação:**
- [ ] Botão "Adicionar paragem" disponível
- [ ] Máximo de 5 paragens intermédias
- [ ] Waypoints podem ser reordenados por drag & drop
- [ ] Waypoints podem ser removidos individualmente
- [ ] Rota recalculada ao adicionar/remover waypoint

**Story Points:** 5  
**Estimativa:** 8 horas  
**Prioridade:** 🟠 Média (Should Have)  
**Sprint:** 2

**Tarefas Técnicas:**
- [ ] Implementar gestão de waypoints no store (2h)
- [ ] Criar UI para adicionar/remover waypoints (3h)
- [ ] Implementar drag & drop para reordenar (2h)
- [ ] Atualizar serviço de routing (1h)

---

#### US-010: Transporte Público (Transit)
**Como** utilizador,  
**Quero** ver rotas de transporte público,  
**Para** planear viagens usando metro, autocarro e comboio.

**Critérios de Aceitação:**
- [ ] Opção "Transporte Público" no selector de modos
- [ ] Mostra linhas de transporte com cores reais
- [ ] Indica horários de partida e chegada
- [ ] Mostra transbordos necessários
- [ ] Indica tempo de caminhada até/das paragens
- [ ] Mostra alternativas de rotas

**Story Points:** 13  
**Estimativa:** 20 horas  
**Prioridade:** 🔴 Alta (Must Have)  
**Sprint:** 3

**Tarefas Técnicas:**
- [ ] Integrar Google Routes API (4h)
- [ ] Criar tipos TypeScript para Transit (2h)
- [ ] Implementar transit.service.ts (4h)
- [ ] Criar componentes UI Transit (6h)
- [ ] Implementar visualização no mapa (4h)

---

#### US-011: Comparação de Tempos entre Modos
**Como** utilizador,  
**Quero** comparar tempos de viagem entre diferentes modos,  
**Para** escolher a opção mais rápida ou conveniente.

**Critérios de Aceitação:**
- [ ] Painel mostra tempo para cada modo disponível
- [ ] Destaque visual para o modo mais rápido
- [ ] Comparação atualizada automaticamente
- [ ] Ícones identificam cada modo

**Story Points:** 3  
**Estimativa:** 5 horas  
**Prioridade:** 🟠 Média (Should Have)  
**Sprint:** 3

**Tarefas Técnicas:**
- [ ] Calcular rotas para todos os modos (2h)
- [ ] Criar painel de comparação UI (2h)
- [ ] Implementar cache para evitar requisições repetidas (1h)

---

### ÉPICO 4: PONTOS DE INTERESSE (POIs)

---

#### US-012: Exibição de POIs ao Longo da Rota
**Como** utilizador,  
**Quero** ver pontos de interesse próximos à minha rota,  
**Para** descobrir restaurantes, postos de combustível, etc.

**Critérios de Aceitação:**
- [ ] POIs aparecem automaticamente quando rota é calculada
- [ ] Categorias: restaurantes, cafés, postos, estacionamentos, atrações
- [ ] Ícones diferentes para cada categoria
- [ ] Máximo de 20 POIs visíveis por categoria
- [ ] POIs podem ser filtrados por categoria
- [ ] Clicar num POI mostra detalhes

**Story Points:** 5  
**Estimativa:** 8 horas  
**Prioridade:** 🟠 Média (Should Have)  
**Sprint:** 2

**Tarefas Técnicas:**
- [ ] Implementar poi.service.ts com Overpass API (3h)
- [ ] Criar POILayer component (2h)
- [ ] Implementar filtros de categoria (2h)
- [ ] Criar popups de detalhes (1h)

---

### ÉPICO 5: INTEGRAÇÕES EXTERNAS

---

#### US-013: Integração com Google Maps App
**Como** utilizador,  
**Quero** abrir a rota diretamente no Google Maps,  
**Para** usar a navegação GPS durante a viagem.

**Critérios de Aceitação:**
- [ ] Botão "Abrir no Google Maps" disponível
- [ ] Link abre o Google Maps com a rota completa
- [ ] Inclui origem, waypoints e destino
- [ ] Funciona em desktop (web) e mobile (app)

**Story Points:** 2  
**Estimativa:** 3 horas  
**Prioridade:** 🟢 Baixa (Could Have)  
**Sprint:** 3

**Tarefas Técnicas:**
- [ ] Implementar geração de URL Google Maps (1h)
- [ ] Criar botão na UI (1h)
- [ ] Testar em diferentes dispositivos (1h)

---

#### US-014: Chatbot Assistente (n8n)
**Como** utilizador,  
**Quero** interagir com um chatbot para ajuda,  
**Para** obter assistência na criação de rotas.

**Critérios de Aceitação:**
- [ ] Widget de chat acessível no canto inferior
- [ ] Pode ser minimizado/expandido
- [ ] Chatbot responde a perguntas sobre rotas
- [ ] Histórico de mensagens mantido na sessão
- [ ] Indicador de "a escrever..." enquanto processa

**Story Points:** 5  
**Estimativa:** 8 horas  
**Prioridade:** 🟢 Baixa (Could Have)  
**Sprint:** 4

**Tarefas Técnicas:**
- [ ] Implementar n8n.service.ts (2h)
- [ ] Criar ChatWidget component (4h)
- [ ] Implementar UI de mensagens (2h)

---

### ÉPICO 6: UI/UX E RESPONSIVIDADE

---

#### US-015: Design Responsivo Mobile
**Como** utilizador mobile,  
**Quero** usar a aplicação no telemóvel,  
**Para** planear rotas em qualquer lugar.

**Critérios de Aceitação:**
- [ ] Layout adapta-se a ecrãs pequenos (<768px)
- [ ] Controlos touch-friendly (mínimo 44px)
- [ ] Menu colapsável em mobile
- [ ] Mapa ocupa ecrã inteiro com overlay de controlos
- [ ] Performance adequada em dispositivos móveis

**Story Points:** 5  
**Estimativa:** 8 horas  
**Prioridade:** 🔴 Alta (Must Have)  
**Sprint:** 2

**Tarefas Técnicas:**
- [ ] Configurar Tailwind breakpoints (1h)
- [ ] Criar layouts responsivos (4h)
- [ ] Implementar gestos touch (2h)
- [ ] Testar em dispositivos reais (1h)

---

#### US-016: Tema Visual e Acessibilidade
**Como** utilizador,  
**Quero** uma interface visualmente apelativa e acessível,  
**Para** ter uma boa experiência de utilização.

**Critérios de Aceitação:**
- [ ] Paleta de cores consistente
- [ ] Contraste adequado (WCAG AA)
- [ ] Fontes legíveis em todos os tamanhos
- [ ] Ícones intuitivos com labels
- [ ] Focus states visíveis para navegação por teclado

**Story Points:** 3  
**Estimativa:** 5 horas  
**Prioridade:** 🟠 Média (Should Have)  
**Sprint:** 2

**Tarefas Técnicas:**
- [ ] Definir design system (1h)
- [ ] Implementar tema Tailwind (2h)
- [ ] Auditar acessibilidade (2h)

---

### ÉPICO 7: INFRAESTRUTURA E DEVOPS

---

#### US-017: Deploy com Docker
**Como** DevOps,  
**Quero** fazer deploy da aplicação com Docker,  
**Para** garantir consistência entre ambientes.

**Critérios de Aceitação:**
- [ ] Dockerfile funcional para build de produção
- [ ] docker-compose.yml configurado
- [ ] Nginx como servidor web
- [ ] Variáveis de ambiente configuráveis
- [ ] Health check implementado
- [ ] Deploy em menos de 5 minutos

**Story Points:** 5  
**Estimativa:** 8 horas  
**Prioridade:** 🔴 Alta (Must Have)  
**Sprint:** 1

**Tarefas Técnicas:**
- [ ] Criar Dockerfile multi-stage (2h)
- [ ] Configurar docker-compose.yml (2h)
- [ ] Configurar Nginx (2h)
- [ ] Documentar processo de deploy (2h)

---

#### US-018: CI/CD Pipeline
**Como** DevOps,  
**Quero** um pipeline de CI/CD automatizado,  
**Para** garantir qualidade em cada commit.

**Critérios de Aceitação:**
- [ ] GitHub Actions configurado
- [ ] Lint executado em cada PR
- [ ] Build verificado em cada PR
- [ ] Type checking executado
- [ ] Deploy automático para staging em merge

**Story Points:** 3  
**Estimativa:** 5 horas  
**Prioridade:** 🟠 Média (Should Have)  
**Sprint:** 1

**Tarefas Técnicas:**
- [ ] Criar workflow GitHub Actions (2h)
- [ ] Configurar jobs de lint e build (2h)
- [ ] Documentar processo (1h)

---

### ÉPICO 8: FUNCIONALIDADES FUTURAS

---

#### US-019: Histórico de Rotas
**Como** utilizador,  
**Quero** ver o histórico das minhas rotas pesquisadas,  
**Para** reutilizar rotas frequentes.

**Critérios de Aceitação:**
- [ ] Rotas guardadas em localStorage
- [ ] Lista de rotas recentes (últimas 10)
- [ ] Clicar numa rota passada restaura-a
- [ ] Opção de limpar histórico
- [ ] Mostra data e destino de cada rota

**Story Points:** 5  
**Estimativa:** 8 horas  
**Prioridade:** 🟢 Baixa (Could Have)  
**Sprint:** 4

**Tarefas Técnicas:**
- [ ] Implementar persistência localStorage (2h)
- [ ] Criar UI de histórico (3h)
- [ ] Implementar restauro de rotas (2h)
- [ ] Gestão de limite de rotas (1h)

---

#### US-020: Exportação de Rotas (GPX/KML)
**Como** utilizador,  
**Quero** exportar as minhas rotas em GPX ou KML,  
**Para** usar em outros dispositivos GPS.

**Critérios de Aceitação:**
- [ ] Botão "Exportar" disponível
- [ ] Opções: GPX, KML, JSON
- [ ] Ficheiro descarregado automaticamente
- [ ] Inclui todos os waypoints
- [ ] Metadados (nome, data) incluídos

**Story Points:** 3  
**Estimativa:** 5 horas  
**Prioridade:** 🟢 Baixa (Could Have)  
**Sprint:** 5

**Tarefas Técnicas:**
- [ ] Implementar gerador GPX (2h)
- [ ] Implementar gerador KML (2h)
- [ ] Criar UI de exportação (1h)

---

#### US-021: Perfil Altimétrico
**Como** ciclista/caminhante,  
**Quero** ver o perfil de elevação da rota,  
**Para** avaliar a dificuldade do percurso.

**Critérios de Aceitação:**
- [ ] Gráfico de elevação ao longo da rota
- [ ] Mostra altitude mínima/máxima
- [ ] Indica subidas e descidas totais
- [ ] Interativo: hover mostra ponto no mapa

**Story Points:** 8  
**Estimativa:** 13 horas  
**Prioridade:** 🟢 Baixa (Could Have)  
**Sprint:** 5

**Tarefas Técnicas:**
- [ ] Integrar API de elevação (4h)
- [ ] Implementar componente de gráfico (5h)
- [ ] Sincronizar com mapa (4h)

---

## 📅 PLANEAMENTO DE SPRINTS

### Sprint 1 - Fundação (2 semanas)
**Objetivo:** MVP funcional com mapa e routing básico

| User Story | Story Points | Horas |
|------------|--------------|-------|
| US-001: Visualização do Mapa | 5 | 8h |
| US-002: Geolocalização | 3 | 5h |
| US-004: Pesquisa de Localização | 5 | 8h |
| US-005: Marcadores no Mapa | 3 | 5h |
| US-006: Cálculo de Rota Básico | 5 | 8h |
| US-007: Informações da Rota | 2 | 3h |
| US-017: Deploy com Docker | 5 | 8h |
| US-018: CI/CD Pipeline | 3 | 5h |
| **TOTAL** | **31** | **50h** |

**Velocity Esperada:** 30-35 SP

---

### Sprint 2 - Modos de Transporte e UX (2 semanas)
**Objetivo:** Múltiplos modos de transporte e design responsivo

| User Story | Story Points | Horas |
|------------|--------------|-------|
| US-003: Auto-fit do Mapa | 2 | 3h |
| US-008: Múltiplos Modos de Transporte | 3 | 5h |
| US-009: Waypoints | 5 | 8h |
| US-012: POIs ao Longo da Rota | 5 | 8h |
| US-015: Design Responsivo | 5 | 8h |
| US-016: Tema e Acessibilidade | 3 | 5h |
| **TOTAL** | **23** | **37h** |

**Velocity Esperada:** 25-30 SP

---

### Sprint 3 - Transit e Integrações (2 semanas)
**Objetivo:** Transporte público e comparação de modos

| User Story | Story Points | Horas |
|------------|--------------|-------|
| US-010: Transporte Público | 13 | 20h |
| US-011: Comparação de Tempos | 3 | 5h |
| US-013: Integração Google Maps | 2 | 3h |
| **TOTAL** | **18** | **28h** |

**Velocity Esperada:** 20-25 SP

---

### Sprint 4 - Extras e Chatbot (2 semanas)
**Objetivo:** Chatbot e histórico de rotas

| User Story | Story Points | Horas |
|------------|--------------|-------|
| US-014: Chatbot n8n | 5 | 8h |
| US-019: Histórico de Rotas | 5 | 8h |
| Refinamento e Bug Fixes | 5 | 8h |
| **TOTAL** | **15** | **24h** |

**Velocity Esperada:** 15-20 SP

---

### Sprint 5 - Funcionalidades Avançadas (2 semanas)
**Objetivo:** Exportação e perfil altimétrico

| User Story | Story Points | Horas |
|------------|--------------|-------|
| US-020: Exportação GPX/KML | 3 | 5h |
| US-021: Perfil Altimétrico | 8 | 13h |
| Documentação Final | 3 | 5h |
| **TOTAL** | **14** | **23h** |

**Velocity Esperada:** 15-20 SP

---

## 📊 MÉTRICAS DO PROJETO

### Resumo Total

| Métrica | Valor |
|---------|-------|
| **Total User Stories** | 21 |
| **Total Story Points** | 101 |
| **Total Horas Estimadas** | 162h |
| **Número de Sprints** | 5 |
| **Duração Total** | 10 semanas |

### Distribuição por Épico

| Épico | Story Points | % |
|-------|--------------|---|
| Visualização e Navegação | 10 | 10% |
| Pesquisa e Geocodificação | 8 | 8% |
| Cálculo de Rotas | 31 | 31% |
| POIs | 5 | 5% |
| Integrações | 7 | 7% |
| UI/UX | 8 | 8% |
| DevOps | 8 | 8% |
| Funcionalidades Futuras | 16 | 16% |
| Buffer/Bugs | 8 | 8% |

### Distribuição por Prioridade

| Prioridade | Story Points | % |
|------------|--------------|---|
| 🔴 Must Have | 56 | 55% |
| 🟠 Should Have | 29 | 29% |
| 🟢 Could Have | 16 | 16% |

---

## 🏃 CERIMÓNIAS SCRUM

### Daily Standup (15 min)
- **Quando:** Todos os dias, 09:30
- **Formato:** 
  - O que fiz ontem?
  - O que vou fazer hoje?
  - Tenho algum blocker?

### Sprint Planning (2h)
- **Quando:** Primeiro dia da Sprint
- **Output:** Sprint Backlog definido

### Sprint Review (1h)
- **Quando:** Último dia da Sprint
- **Output:** Demo das funcionalidades implementadas

### Sprint Retrospective (1h)
- **Quando:** Após Sprint Review
- **Output:** Melhorias para próxima Sprint

### Backlog Refinement (1h)
- **Quando:** Meio da Sprint
- **Output:** User Stories refinadas para próximas Sprints

---

## 📝 DEFINITION OF READY (DoR)

Uma User Story está "Ready" quando:
- [ ] Tem descrição clara (Como/Quero/Para)
- [ ] Critérios de aceitação definidos
- [ ] Story Points estimados pela equipa
- [ ] Dependências identificadas
- [ ] Designs/mockups disponíveis (se aplicável)
- [ ] Não tem blockers

---

## ✅ DEFINITION OF DONE (DoD)

Uma User Story está "Done" quando:
- [ ] Código implementado e funcional
- [ ] Code review aprovado
- [ ] Testes passam (unit + integration)
- [ ] Sem erros de lint/TypeScript
- [ ] Documentação atualizada (se necessário)
- [ ] Deployado em staging
- [ ] Aceite pelo Product Owner
- [ ] Merge na branch main

---

## 🎯 ROLES DA EQUIPA

### Product Owner
- Define prioridades do Product Backlog
- Aceita ou rejeita User Stories
- Disponível para clarificar requisitos

### Scrum Master
- Facilita cerimónias Scrum
- Remove impedimentos
- Protege a equipa de interrupções

### Development Team
- Implementa User Stories
- Estima Story Points
- Compromete-se com Sprint Backlog
- Auto-organiza o trabalho

---

## 🔧 CONFIGURAÇÃO DO TRELLO

### Power-Ups Recomendados
1. **Scrum for Trello** - Story Points e Burndown
2. **Card Aging** - Identificar cards parados
3. **Calendar** - Visualizar deadlines
4. **GitHub** - Integração com commits

### Automações (Butler)
1. Mover card para "In Progress" quando membro atribuído
2. Adicionar label "Blocked" quando checkbox "Tem blockers" marcada
3. Mover para "Done" quando todas checklists completas
4. Notificar equipa quando card em "Code Review" há mais de 2 dias

### Templates de Card

**Template User Story:**
```
## Descrição
Como [tipo de utilizador],
Quero [funcionalidade],
Para [benefício].

## Critérios de Aceitação
- [ ] Critério 1
- [ ] Critério 2
- [ ] Critério 3

## Tarefas Técnicas
- [ ] Tarefa 1 (Xh)
- [ ] Tarefa 2 (Xh)

## Notas
- Dependências: 
- Links úteis:

---
📊 Story Points: X
⏱️ Estimativa: Xh
🏷️ Sprint: X
```

---

*Documento gerado em 10/12/2025 para o projeto Map Route Explorer v3.0*
