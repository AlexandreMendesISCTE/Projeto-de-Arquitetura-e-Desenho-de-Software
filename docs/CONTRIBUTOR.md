# 🤝 Guia de Contribuição - Map Route Explorer

Obrigado pelo seu interesse em contribuir para o Map Route Explorer! Este guia fornece todas as informações necessárias para contribuir de forma eficaz para o projeto.

## 📋 Índice

- [Como Contribuir](#-como-contribuir)
- [Código de Conduta](#-código-de-conduta)
- [Configuração do Ambiente](#-configuração-do-ambiente)
- [Processo de Contribuição](#-processo-de-contribuição)
- [Tipos de Contribuição](#-tipos-de-contribuição)
- [Padrões de Código](#-padrões-de-código)
- [Testes](#-testes)
- [Documentação](#-documentação)
- [Resolução de Conflitos](#-resolução-de-conflitos)

## 🚀 Como Contribuir

### Primeiros Passos

1. **Fork do Repositório**
   - Clique no botão "Fork" no canto superior direito
   - Clone o seu fork localmente
   ```bash
   git clone https://github.com/seu-usuario/Projeto-de-Arquitetura-e-Desenho-de-Software.git
   cd Projeto-de-Arquitetura-e-Desenho-de-Software
   ```

2. **Configurar Remote**
   ```bash
   git remote add upstream https://github.com/AlexandreMendesISCTE/Projeto-de-Arquitetura-e-Desenho-de-Software.git
   git remote -v
   ```

3. **Manter Fork Atualizado**
   ```bash
   git fetch upstream
   git checkout main
   git merge upstream/main
   ```

### Configuração do Ambiente

#### Pré-requisitos
- Docker 24.0+
- Docker Compose 2.23+
- Git 2.43+
- IDE (IntelliJ IDEA, Eclipse, VS Code)

#### Configuração Inicial
```bash
# Verificar instalações
docker --version
docker-compose --version
git --version

# Configurar Git
git config --global user.name "Seu Nome"
git config --global user.email "seu.email@exemplo.com"

# Clonar e configurar projeto
git clone https://github.com/seu-usuario/Projeto-de-Arquitetura-e-Desenho-de-Software.git
cd Projeto-de-Arquitetura-e-Desenho-de-Software

# Executar com Docker
./docker-run.sh
```

## 📝 Código de Conduta

### Nossos Compromissos

- **Respeito**: Tratamos todos com respeito e dignidade
- **Inclusão**: Bem-vindos contribuidores de todas as origens
- **Colaboração**: Trabalhamos juntos para o sucesso do projeto
- **Transparência**: Comunicação clara e honesta

### Comportamento Esperado

- ✅ Usar linguagem acolhedora e inclusiva
- ✅ Respeitar diferentes pontos de vista
- ✅ Aceitar críticas construtivas
- ✅ Focar no que é melhor para a comunidade
- ✅ Mostrar empatia com outros membros

### Comportamento Inaceitável

- ❌ Linguagem ou imagens sexualizadas
- ❌ Trolling, comentários insultuosos ou ataques pessoais
- ❌ Assédio público ou privado
- ❌ Publicar informações privadas sem permissão
- ❌ Outras condutas inadequadas em ambiente profissional

## 🔄 Processo de Contribuição

### 1. Escolher uma Tarefa

#### Issues Disponíveis
- 🔴 **Bug**: Correção de problemas existentes
- 🟡 **Enhancement**: Melhorias de funcionalidades
- 🟢 **Feature**: Novas funcionalidades
- 📚 **Documentation**: Melhorias na documentação
- 🧪 **Test**: Adição de testes

#### Etiquetas de Dificuldade
- `good first issue` - Ideal para iniciantes
- `help wanted` - Precisa de ajuda
- `priority: high` - Alta prioridade
- `priority: medium` - Prioridade média
- `priority: low` - Baixa prioridade

### 2. Criar Branch

```bash
# Atualizar main
git checkout main
git pull upstream main

# Criar branch para a funcionalidade
git checkout -b feature/nome-da-funcionalidade
# ou
git checkout -b fix/correcao-do-bug
# ou
git checkout -b docs/melhoria-documentacao
```

### 3. Desenvolver

#### Estrutura de Commit
```bash
# Formato: tipo(escopo): descrição

# Exemplos
git commit -m "feat(route): adicionar suporte a múltiplos destinos"
git commit -m "fix(ui): corrigir erro de renderização do mapa"
git commit -m "docs(readme): atualizar instruções de instalação"
git commit -m "test(service): adicionar testes para OSRMService"
```

#### Tipos de Commit
- `feat`: Nova funcionalidade
- `fix`: Correção de bug
- `docs`: Documentação
- `style`: Formatação
- `refactor`: Refatoração
- `test`: Testes
- `chore`: Tarefas de manutenção

### 4. Testar

```bash
# Executar todos os testes
mvn test

# Executar testes específicos
mvn test -Dtest=LocationTest

# Verificar cobertura
mvn jacoco:report

# Verificar qualidade do código
mvn checkstyle:check
mvn spotbugs:check
```

### 5. Submeter Pull Request

#### Checklist Antes do PR
- [ ] Código compila sem erros
- [ ] Todos os testes passam
- [ ] Cobertura de testes adequada (>80%)
- [ ] Documentação atualizada
- [ ] Código segue padrões estabelecidos
- [ ] Commits com mensagens descritivas
- [ ] Branch atualizada com main

#### Template de Pull Request
```markdown
## Descrição
Breve descrição das alterações realizadas.

## Tipo de Alteração
- [ ] Bug fix
- [ ] Nova funcionalidade
- [ ] Breaking change
- [ ] Documentação

## Como Testar
1. Passo 1
2. Passo 2
3. Passo 3

## Screenshots (se aplicável)
Adicionar screenshots das alterações visuais.

## Checklist
- [ ] Código compila
- [ ] Testes passam
- [ ] Documentação atualizada
- [ ] Código revisado
```

## 🎯 Tipos de Contribuição

### 🐛 Correção de Bugs

#### Identificar Bug
1. Verificar se já existe issue
2. Criar issue se não existir
3. Descrever problema detalhadamente
4. Incluir passos para reproduzir

#### Corrigir Bug
```bash
# Criar branch
git checkout -b fix/descricao-do-bug

# Implementar correção
# ... código ...

# Adicionar teste para o bug
# ... teste ...

# Commit
git commit -m "fix(componente): corrigir descrição do bug"
```

### ✨ Novas Funcionalidades

#### Propor Funcionalidade
1. Criar issue com label `enhancement`
2. Descrever funcionalidade detalhadamente
3. Discutir com mantenedores
4. Aguardar aprovação

#### Implementar Funcionalidade
```bash
# Criar branch
git checkout -b feature/nome-da-funcionalidade

# Implementar funcionalidade
# ... código ...

# Adicionar testes
# ... testes ...

# Atualizar documentação
# ... docs ...

# Commits
git commit -m "feat(componente): adicionar nova funcionalidade"
git commit -m "test(componente): adicionar testes para nova funcionalidade"
git commit -m "docs(componente): documentar nova funcionalidade"
```

### 📚 Melhorias na Documentação

#### Tipos de Documentação
- **README**: Instruções principais
- **Javadoc**: Documentação de código
- **Guias**: Instruções detalhadas
- **Exemplos**: Código de exemplo
- **Tutoriais**: Passo a passo

#### Processo
```bash
# Criar branch
git checkout -b docs/melhoria-documentacao

# Editar documentação
# ... edições ...

# Commit
git commit -m "docs(secao): melhorar descrição da funcionalidade"
```

### 🧪 Testes

#### Adicionar Testes
```java
@Test
void testNovaFuncionalidade() {
    // Given
    Location origin = new Location(38.7223, -9.1393);
    Location destination = new Location(40.4168, -3.7038);
    
    // When
    Route route = service.calculateRoute(origin, destination, TransportMode.DRIVING);
    
    // Then
    assertThat(route).isNotNull();
    assertThat(route.getTotalDistance()).isGreaterThan(0);
}
```

#### Melhorar Cobertura
```bash
# Verificar cobertura atual
mvn jacoco:report

# Identificar áreas com baixa cobertura
# Adicionar testes para essas áreas
```

## 📏 Padrões de Código

### Convenções Java

#### Nomenclatura
```java
// Classes: PascalCase
public class LocationService { }

// Métodos e variáveis: camelCase
public void calculateRoute() { }
private String userName = "admin";

// Constantes: UPPER_SNAKE_CASE
public static final String API_BASE_URL = "https://api.example.com";

// Pacotes: lowercase
package pt.iscteiul.maprouteexplorer.service;
```

#### Formatação
```java
// Indentação: 4 espaços
public class Example {
    private String field;
    
    public void method() {
        if (condition) {
            doSomething();
        }
    }
}

// Quebras de linha: máximo 120 caracteres
public Route calculateRoute(Location origin, Location destination, 
                          TransportMode transportMode) {
    // implementação
}
```

### Documentação

#### Javadoc Obrigatório
```java
/**
 * Calcula uma rota entre dois pontos.
 * 
 * @param origin ponto de origem
 * @param destination ponto de destino
 * @param transportMode modo de transporte
 * @return rota calculada
 * @throws IOException se ocorrer erro na comunicação com a API
 * @throws OSRMException se a API retornar erro
 */
public Route calculateRoute(Location origin, Location destination, 
                          TransportMode transportMode) 
        throws IOException, OSRMException {
    // implementação
}
```

### Tratamento de Exceções

```java
// ✅ Correto: exceções específicas
try {
    String response = httpClient.get(url);
    return parseResponse(response);
} catch (IOException e) {
    logger.error("Erro de comunicação com a API", e);
    throw new OSRMException("Falha na comunicação", e);
}

// ❌ Incorreto: captura genérica
try {
    String response = httpClient.get(url);
    return parseResponse(response);
} catch (Exception e) {
    // muito genérico
}
```

## 🧪 Testes

### Estratégia de Testes

#### Testes Unitários (70%)
```java
@Test
void testCalculateDistance() {
    // Given
    Location lisboa = new Location(38.7223, -9.1393);
    Location porto = new Location(41.1579, -8.6291);
    
    // When
    double distance = lisboa.distanceTo(porto);
    
    // Then
    assertThat(distance).isCloseTo(274.0, within(10.0));
}
```

#### Testes de Integração (20%)
```java
@Test
void testOSRMServiceIntegration() {
    // Given
    OSRMService service = new OSRMService(httpClient);
    Location origin = new Location(38.7223, -9.1393);
    Location destination = new Location(40.4168, -3.7038);
    
    // When
    Route route = service.calculateRoute(origin, destination, TransportMode.DRIVING);
    
    // Then
    assertThat(route).isNotNull();
    assertThat(route.getWaypoints()).hasSize(2);
}
```

#### Testes de Interface (10%)
```java
@Test
void testMapPanelClick() {
    // Given
    MapPanel panel = new MapPanel();
    PointSelectionListener listener = mock(PointSelectionListener.class);
    panel.setPointSelectionListener(listener);
    
    // When
    panel.handleMapClick(100, 200);
    
    // Then
    verify(listener).onPointSelected(any(Location.class));
}
```

### Executar Testes

```bash
# Todos os testes
mvn test

# Testes específicos
mvn test -Dtest=LocationTest

# Testes com cobertura
mvn jacoco:report

# Verificar qualidade
mvn checkstyle:check
mvn spotbugs:check
```

## 📚 Documentação

### Tipos de Documentação

#### README.md
- Visão geral do projeto
- Instruções de instalação
- Exemplos de utilização

#### Javadoc
- Documentação de classes
- Documentação de métodos
- Exemplos de código

#### Guias Específicos
- Guia de instalação
- Guia de desenvolvimento
- Guia de contribuição

### Atualizar Documentação

```bash
# Criar branch
git checkout -b docs/atualizacao-documentacao

# Editar ficheiros
# ... edições ...

# Commit
git commit -m "docs(secao): atualizar instruções de instalação"
```

## 🔧 Resolução de Conflitos

### Conflitos de Merge

#### Identificar Conflitos
```bash
git merge upstream/main
# Auto-merging src/main/java/.../Main.java
# CONFLICT (content): Merge conflict in src/main/java/.../Main.java
```

#### Resolver Conflitos
```bash
# Abrir ficheiro com conflitos
# Editar para resolver conflitos
# Remover marcadores de conflito

# Adicionar ficheiro resolvido
git add src/main/java/.../Main.java

# Completar merge
git commit -m "resolve: resolver conflitos de merge"
```

#### Prevenir Conflitos
```bash
# Manter branch atualizada
git fetch upstream
git merge upstream/main

# Rebase antes de submeter PR
git rebase upstream/main
```

### Conflitos de Código

#### Estratégias de Resolução
1. **Comunicação**: Discutir com outros contribuidores
2. **Compromisso**: Encontrar solução que satisfaça todos
3. **Documentação**: Documentar decisões tomadas
4. **Testes**: Garantir que soluções funcionam

## 🎉 Reconhecimento

### Contribuidores

Todos os contribuidores são reconhecidos na página de [Contributors do GitHub](https://github.com/AlexandreMendesISCTE/Projeto-de-Arquitetura-e-Desenho-de-Software/graphs/contributors) e na página de releases.

### Tipos de Reconhecimento

- **Contribuidor**: Qualquer contribuição aceite
- **Mantenedor**: Contribuidores ativos e confiáveis
- **Mentor**: Ajudam outros contribuidores
- **Revisor**: Revisam código e documentação

### Como Ser Reconhecido

1. **Contribuir Regularmente**: Submeter PRs de qualidade
2. **Ajudar Outros**: Responder a issues e PRs
3. **Melhorar Projeto**: Sugerir melhorias
4. **Comunidade**: Participar em discussões

## 📞 Suporte

### Canais de Comunicação

- **GitHub Issues**: Para bugs e funcionalidades - [Issues](https://github.com/AlexandreMendesISCTE/Projeto-de-Arquitetura-e-Desenho-de-Software/issues)
- **GitHub Discussions**: Para perguntas e discussões - [Discussions](https://github.com/AlexandreMendesISCTE/Projeto-de-Arquitetura-e-Desenho-de-Software/discussions)
- **Pull Requests**: Para contribuições de código - [Pull Requests](https://github.com/AlexandreMendesISCTE/Projeto-de-Arquitetura-e-Desenho-de-Software/pulls)

### Obter Ajuda

1. **Verificar Documentação**: Ler guias disponíveis
2. **Procurar Issues**: Verificar se problema já foi reportado
3. **Criar Issue**: Descrever problema detalhadamente
4. **Participar em Discussões**: Fazer perguntas na comunidade

---

<div align="center">

**Obrigado por contribuir para o Map Route Explorer! 🙏**

[⬆ Voltar ao topo](#-guia-de-contribuição---map-route-explorer)

</div>
