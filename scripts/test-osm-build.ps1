# Script de teste para integração OSM - Map Route Explorer (PowerShell)
# Este script testa especificamente a configuração do JMapViewer

Write-Host "🧪 Testando integração OSM - Map Route Explorer" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan

# Verificar se estamos no diretório correto
if (-not (Test-Path "pom.xml")) {
    Write-Host "[ERROR] pom.xml não encontrado. Execute este script a partir da raiz do projeto." -ForegroundColor Red
    exit 1
}

function Log-Info {
    param($Message)
    $timestamp = Get-Date -Format "HH:mm:ss"
    Write-Host "[$timestamp] $Message" -ForegroundColor Blue
}

function Log-Success {
    param($Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green
}

function Log-Error {
    param($Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

function Log-Warning {
    param($Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

Log-Info "Iniciando teste de integração OSM..."

# 1. Verificar dependências Maven
Log-Info "1. Verificando dependências Maven..."
try {
    mvn dependency:resolve -q
    Log-Success "Dependências Maven resolvidas com sucesso"
} catch {
    Log-Error "Falha ao resolver dependências Maven"
    exit 1
}

# 2. Verificar se JMapViewer está disponível
Log-Info "2. Verificando disponibilidade do JMapViewer..."
try {
    mvn dependency:get -Dartifact=org.openstreetmap:jmapviewer:2.15 -q
    Log-Success "JMapViewer 2.15 encontrado"
} catch {
    Log-Error "JMapViewer não encontrado. Verificar repositórios Maven."
    exit 1
}

# 3. Compilar projeto
Log-Info "3. Compilando projeto..."
try {
    mvn clean compile -DskipTests -q
    Log-Success "Compilação bem-sucedida"
} catch {
    Log-Error "Falha na compilação"
    exit 1
}

# 4. Verificar se as classes foram compiladas
Log-Info "4. Verificando classes compiladas..."
if (Test-Path "target/classes/pt/iscteiul/maprouteexplorer/ui/MapPanel.class") {
    Log-Success "MapPanel compilado com sucesso"
} else {
    Log-Error "MapPanel não foi compilado"
    exit 1
}

# 5. Verificar se o JAR foi criado
Log-Info "5. Verificando criação do JAR..."
try {
    mvn package -DskipTests -q
    if (Test-Path "target/map-route-explorer-2.0.0-jar-with-dependencies.jar") {
        Log-Success "JAR com dependências criado com sucesso"
    } else {
        Log-Error "JAR não foi criado"
        exit 1
    }
} catch {
    Log-Error "Falha ao criar JAR"
    exit 1
}

Write-Host ""
Write-Host "🎉 Teste de integração OSM concluído com sucesso!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Resumo:" -ForegroundColor Cyan
Write-Host "  ✅ Dependências Maven resolvidas" -ForegroundColor Green
Write-Host "  ✅ JMapViewer disponível" -ForegroundColor Green
Write-Host "  ✅ Compilação bem-sucedida" -ForegroundColor Green
Write-Host "  ✅ JAR criado com sucesso" -ForegroundColor Green
Write-Host ""
Write-Host "🚀 Próximos passos:" -ForegroundColor Cyan
Write-Host "  1. Executar: docker-compose up" -ForegroundColor White
Write-Host "  2. Acessar: http://localhost:6080" -ForegroundColor White
Write-Host "  3. Testar funcionalidades do mapa" -ForegroundColor White
Write-Host ""
Write-Host "📝 Para mais detalhes, consulte: test-osm-integration.md" -ForegroundColor Cyan
