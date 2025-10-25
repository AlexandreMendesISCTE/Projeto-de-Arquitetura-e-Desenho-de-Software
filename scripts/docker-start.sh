#!/bin/bash
# Script para iniciar o projeto com Docker

echo "🐳 Iniciando Map Route Explorer com Docker..."

# Para containers existentes
echo "🛑 Parando containers anteriores..."
docker compose down

# Build da imagem
echo "🔨 Construindo imagem Docker..."
docker compose build

# Inicia containers
echo "🚀 Iniciando containers..."
docker compose up -d

# Mostra status
echo "📊 Status dos containers:"
docker compose ps

echo ""
echo "✅ Aplicação iniciada com sucesso!"
echo "🌐 Acesso via Browser: http://localhost:6080"
echo "🖥️  VNC Viewer: localhost:5901 (senha: maproute123)"
