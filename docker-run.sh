#!/bin/bash

# Script para executar Map Route Explorer com Docker
# Autor: Alexandre Mendes
# Versão: 1.0.0

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para imprimir mensagens coloridas
print_message() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}  Map Route Explorer - Docker${NC}"
    echo -e "${BLUE}================================${NC}"
}

# Verificar se Docker está instalado
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker não está instalado. Por favor, instale o Docker primeiro."
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose não está instalado. Por favor, instale o Docker Compose primeiro."
        exit 1
    fi
    
    print_message "Docker e Docker Compose encontrados."
}

# Verificar se o projeto foi compilado
check_build() {
    if [ ! -f "target/map-route-explorer-1.0.0-jar-with-dependencies.jar" ]; then
        print_warning "JAR não encontrado. Compilando projeto..."
        mvn clean package -DskipTests
    fi
    
    if [ ! -f "target/map-route-explorer-1.0.0-jar-with-dependencies.jar" ]; then
        print_error "Falha na compilação do projeto."
        exit 1
    fi
    
    print_message "JAR encontrado e pronto para uso."
}

# Criar diretórios necessários
create_directories() {
    print_message "Criando diretórios necessários..."
    mkdir -p logs
    mkdir -p data
    mkdir -p config
    print_message "Diretórios criados com sucesso."
}

# Construir imagem Docker
build_image() {
    print_message "Construindo imagem Docker..."
    docker-compose build --no-cache
    print_message "Imagem construída com sucesso."
}

# Executar aplicação
run_application() {
    print_message "Iniciando Map Route Explorer..."
    print_message "A aplicação será executada em modo gráfico."
    print_message "Para acessar remotamente, use VNC na porta 5901 com senha 'maproute123'"
    
    docker-compose up map-route-explorer
}

# Parar aplicação
stop_application() {
    print_message "Parando Map Route Explorer..."
    docker-compose down
    print_message "Aplicação parada com sucesso."
}

# Limpar recursos Docker
clean_docker() {
    print_message "Limpando recursos Docker..."
    docker-compose down -v
    docker system prune -f
    print_message "Recursos limpos com sucesso."
}

# Mostrar logs
show_logs() {
    print_message "Mostrando logs da aplicação..."
    docker-compose logs -f map-route-explorer
}

# Mostrar ajuda
show_help() {
    echo "Uso: $0 [OPÇÃO]"
    echo ""
    echo "Opções:"
    echo "  run        Executar a aplicação (padrão)"
    echo "  build      Apenas construir a imagem"
    echo "  stop       Parar a aplicação"
    echo "  restart    Reiniciar a aplicação"
    echo "  logs       Mostrar logs"
    echo "  clean      Limpar recursos Docker"
    echo "  help       Mostrar esta ajuda"
    echo ""
    echo "Exemplos:"
    echo "  $0 run     # Executar aplicação"
    echo "  $0 build   # Apenas construir"
    echo "  $0 stop    # Parar aplicação"
}

# Função principal
main() {
    print_header
    
    case "${1:-run}" in
        "run")
            check_docker
            check_build
            create_directories
            build_image
            run_application
            ;;
        "build")
            check_docker
            check_build
            create_directories
            build_image
            print_message "Imagem construída com sucesso."
            ;;
        "stop")
            stop_application
            ;;
        "restart")
            stop_application
            check_docker
            check_build
            create_directories
            build_image
            run_application
            ;;
        "logs")
            show_logs
            ;;
        "clean")
            clean_docker
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        *)
            print_error "Opção inválida: $1"
            show_help
            exit 1
            ;;
    esac
}

# Executar função principal
main "$@"
