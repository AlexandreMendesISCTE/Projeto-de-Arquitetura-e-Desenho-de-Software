#!/bin/bash
# Script de build do projeto Map Route Explorer

echo "🔨 Iniciando build do Map Route Explorer..."

# Limpa build anterior
echo "🧹 Limpando build anterior..."
mvn clean

# Compila o projeto
echo "📦 Compilando projeto..."
mvn package -DskipTests

echo "✅ Build concluído com sucesso!"
