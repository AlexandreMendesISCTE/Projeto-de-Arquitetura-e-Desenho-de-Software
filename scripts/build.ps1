# Script de build do projeto Map Route Explorer (PowerShell)

Write-Host "🔨 Iniciando build do Map Route Explorer..." -ForegroundColor Cyan

# Limpa build anterior
Write-Host "🧹 Limpando build anterior..." -ForegroundColor Yellow
mvn clean

# Compila o projeto
Write-Host "📦 Compilando projeto..." -ForegroundColor Yellow
mvn package -DskipTests

Write-Host "✅ Build concluído com sucesso!" -ForegroundColor Green
