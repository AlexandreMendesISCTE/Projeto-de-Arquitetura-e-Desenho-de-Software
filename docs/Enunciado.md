# Enunciado

Em qualquer dos projetos, os grupos de trabalho deverão adotar a framework **SCRUM** para a gestão do projeto, bem como alguma plataforma digital de suporte à gestão do projeto (por exemplo, <https://trello.com>). Deverão também disponibilizar na plataforma **GitHub** todos os artefactos produzidos no contexto do projeto e **documentar o projeto** usando notações habitualmente usadas na área da engenharia de software e dos processos (por exemplo, **UML**, **BPMN**).

---

# Projeto 2 — Integração de Sistemas API  
## Opção 2) Sistema Interativo de Rotas e Exploração de Locais com OpenStreetMap

**Objetivo:** Desenvolver uma aplicação interativa que permita ao utilizador explorar um mapa baseado nos dados do **OpenStreetMap (OSM)**, traçar rotas entre pontos de interesse e obter informações relevantes sobre o trajeto e a área circundante.

**APIs a utilizar:**
- **OSRM** para cálculo de rotas, distância e tempo.  
- **Nominatim** para geocodificação e pesquisa de locais.  
- *(Opcional)* **Overpass API** para obter pontos de interesse (POIs) ao longo do trajeto.

O projeto trabalha conceitos de interface gráfica e mapas, **integração com APIs REST**, *parsing* de **JSON** e **visualização de dados dinâmicos**.

---

## Requisitos Funcionais (Obrigatórios)

### a) Visualização de mapa
- Exibir um **mapa interativo** carregado a partir de uma API (OSM).
- Permitir **zoom**, **pan** e **seleção de pontos** através de clique.

### b) Seleção de rota
- Enviar **requisições** para a API **OSRM** com **origem** e **destino**.
- Calcular a **rota** usando os dados recebidos (**JSON**).
- **Desenhar a rota** no mapa.

### c) Informações da rota
- Obter **distância** e **tempo de viagem** da resposta da API.
- Exibir essas informações de forma **clara** na interface.

### d) Limpeza e reinício
- Permitir **reiniciar** a seleção de pontos para calcular **nova rota**.

---

## Requisitos Funcionais (Opcionais)

- **a) Pesquisa de Localização (Nominatim):** Campo de pesquisa que chama a API de geocodificação para encontrar locais e **centralizar o mapa**.  
- **b) Modos de Transporte:** Enviar parâmetro diferente para a API OSRM (**carro**, **bicicleta**, **a pé**).  
- **c) Pontos de Interesse (Overpass API):** Fazer chamadas a essa API para buscar **POIs** próximos da rota.  
- **d) Múltiplos Destinos:** Construir a **URL da API OSRM** com **waypoints** adicionais.  
- **e) Exportação de Dados:** **Salvar a rota** em **GPX** ou **JSON** para reutilização.  
- **f) Estatísticas Avançadas:** Chamar APIs de **elevação** e exibir **perfil altimétrico** do percurso.
