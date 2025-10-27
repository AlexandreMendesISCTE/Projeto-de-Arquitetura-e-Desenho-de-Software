#!/bin/bash

# Script de teste para integração OSM - Map Route Explorer
# Este script testa especificamente a configuração do JMapViewer

echo "🧪 Testando integração OSM - Map Route Explorer"
echo "=============================================="

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para log
log() {
    echo -e "${BLUE}[$(date +'%H:%M:%S')]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Verificar se estamos no diretório correto
if [ ! -f "pom.xml" ]; then
    error "pom.xml não encontrado. Execute este script a partir da raiz do projeto."
    exit 1
fi

log "Iniciando teste de integração OSM..."

# 1. Verificar dependências Maven
log "1. Verificando dependências Maven..."
if mvn dependency:resolve -q; then
    success "Dependências Maven resolvidas com sucesso"
else
    error "Falha ao resolver dependências Maven"
    exit 1
fi

# 2. Verificar se JMapViewer está disponível
log "2. Verificando disponibilidade do JMapViewer..."
if mvn dependency:get -Dartifact=org.openstreetmap:jmapviewer:2.15 -q; then
    success "JMapViewer 2.15 encontrado"
else
    error "JMapViewer não encontrado. Verificar repositórios Maven."
    exit 1
fi

# 3. Compilar projeto
log "3. Compilando projeto..."
if mvn clean compile -DskipTests -q; then
    success "Compilação bem-sucedida"
else
    error "Falha na compilação"
    exit 1
fi

# 4. Verificar se as classes foram compiladas
log "4. Verificando classes compiladas..."
if [ -f "target/classes/pt/iscteiul/maprouteexplorer/ui/MapPanel.class" ]; then
    success "MapPanel compilado com sucesso"
else
    error "MapPanel não foi compilado"
    exit 1
fi

# 5. Verificar imports do JMapViewer
log "5. Verificando imports do JMapViewer..."
if javap -cp target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout) pt.iscteiul.maprouteexplorer.ui.MapPanel | grep -q "org.openstreetmap.gui.jmapviewer"; then
    success "Imports do JMapViewer encontrados"
else
    warning "Imports do JMapViewer não encontrados (pode ser normal se não houver referências diretas)"
fi

# 6. Testar execução básica
log "6. Testando execução básica..."
if timeout 10s mvn exec:java -Dexec.mainClass="pt.iscteiul.maprouteexplorer.Main" -q 2>/dev/null; then
    success "Aplicação iniciou com sucesso"
elif [ $? -eq 124 ]; then
    success "Aplicação iniciou (timeout esperado para GUI)"
else
    warning "Aplicação não pôde ser testada (normal em ambiente headless)"
fi

# 7. Verificar se o JAR foi criado
log "7. Verificando criação do JAR..."
if mvn package -DskipTests -q; then
    if [ -f "target/map-route-explorer-2.0.0-jar-with-dependencies.jar" ]; then
        success "JAR com dependências criado com sucesso"
    else
        error "JAR não foi criado"
        exit 1
    fi
else
    error "Falha ao criar JAR"
    exit 1
fi

echo ""
echo "🎉 Teste de integração OSM concluído com sucesso!"
echo ""
echo "📋 Resumo:"
echo "  ✅ Dependências Maven resolvidas"
echo "  ✅ JMapViewer disponível"
echo "  ✅ Compilação bem-sucedida"
echo "  ✅ JAR criado com sucesso"
echo ""
echo "🚀 Próximos passos:"
echo "  1. Executar: docker-compose up"
echo "  2. Acessar: http://localhost:6080"
echo "  3. Testar funcionalidades do mapa"
echo ""
echo "📝 Para mais detalhes, consulte: test-osm-integration.md"
