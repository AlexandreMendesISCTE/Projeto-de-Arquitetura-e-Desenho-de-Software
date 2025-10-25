# Script para iniciar o projeto com Docker (PowerShell)

Write-Host "🐳 Iniciando Map Route Explorer com Docker..." -ForegroundColor Cyan

# Para containers existentes
Write-Host "🛑 Parando containers anteriores..." -ForegroundColor Yellow
docker compose down

# Build da imagem
Write-Host "🔨 Construindo imagem Docker..." -ForegroundColor Yellow
docker compose build

# Inicia containers
Write-Host "🚀 Iniciando containers..." -ForegroundColor Yellow
docker compose up -d

# Mostra status
Write-Host "📊 Status dos containers:" -ForegroundColor Cyan
docker compose ps

Write-Host ""
Write-Host "✅ Aplicação iniciada com sucesso!" -ForegroundColor Green
Write-Host "🌐 Acesso via Browser: http://localhost:6080" -ForegroundColor Cyan
Write-Host "🖥️  VNC Viewer: localhost:5901 (senha: maproute123)" -ForegroundColor Cyan
