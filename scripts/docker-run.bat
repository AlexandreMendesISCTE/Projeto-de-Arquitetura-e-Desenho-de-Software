@echo off
REM Script para executar Map Route Explorer com Docker no Windows
REM Autor: Alexandre Mendes
REM Versão: 1.0.0

setlocal enabledelayedexpansion

REM Cores (limitadas no Windows)
set "INFO=[INFO]"
set "WARNING=[WARNING]"
set "ERROR=[ERROR]"

REM Função para imprimir mensagens
:print_message
echo %INFO% %~1
goto :eof

:print_warning
echo %WARNING% %~1
goto :eof

:print_error
echo %ERROR% %~1
goto :eof

:print_header
echo ================================
echo   Map Route Explorer - Docker
echo ================================
goto :eof

REM Verificar se Docker está instalado
:check_docker
docker --version >nul 2>&1
if errorlevel 1 (
    call :print_error "Docker não está instalado. Por favor, instale o Docker Desktop primeiro."
    exit /b 1
)

docker-compose --version >nul 2>&1
if errorlevel 1 (
    call :print_error "Docker Compose não está instalado. Por favor, instale o Docker Desktop primeiro."
    exit /b 1
)

call :print_message "Docker e Docker Compose encontrados."
goto :eof

REM Verificar se o projeto foi compilado
:check_build
if not exist "target\map-route-explorer-1.0.0-jar-with-dependencies.jar" (
    call :print_warning "JAR não encontrado. Compilando projeto..."
    mvn clean package -DskipTests
    if errorlevel 1 (
        call :print_error "Falha na compilação do projeto."
        exit /b 1
    )
)

if not exist "target\map-route-explorer-1.0.0-jar-with-dependencies.jar" (
    call :print_error "JAR não encontrado após compilação."
    exit /b 1
)

call :print_message "JAR encontrado e pronto para uso."
goto :eof

REM Criar diretórios necessários
:create_directories
call :print_message "Criando diretórios necessários..."
if not exist "logs" mkdir logs
if not exist "data" mkdir data
if not exist "config" mkdir config
call :print_message "Diretórios criados com sucesso."
goto :eof

REM Construir imagem Docker
:build_image
call :print_message "Construindo imagem Docker..."
docker-compose build --no-cache
if errorlevel 1 (
    call :print_error "Falha na construção da imagem Docker."
    exit /b 1
)
call :print_message "Imagem construída com sucesso."
goto :eof

REM Executar aplicação
:run_application
call :print_message "Iniciando Map Route Explorer..."
call :print_message "A aplicação será executada em modo gráfico."
call :print_message "Para acessar remotamente, use VNC na porta 5901 com senha 'maproute123'"
docker-compose up map-route-explorer
goto :eof

REM Parar aplicação
:stop_application
call :print_message "Parando Map Route Explorer..."
docker-compose down
call :print_message "Aplicação parada com sucesso."
goto :eof

REM Limpar recursos Docker
:clean_docker
call :print_message "Limpando recursos Docker..."
docker-compose down -v
docker system prune -f
call :print_message "Recursos limpos com sucesso."
goto :eof

REM Mostrar logs
:show_logs
call :print_message "Mostrando logs da aplicação..."
docker-compose logs -f map-route-explorer
goto :eof

REM Mostrar ajuda
:show_help
echo Uso: %0 [OPÇÃO]
echo.
echo Opções:
echo   run        Executar a aplicação (padrão)
echo   build      Apenas construir a imagem
echo   stop       Parar a aplicação
echo   restart    Reiniciar a aplicação
echo   logs       Mostrar logs
echo   clean      Limpar recursos Docker
echo   help       Mostrar esta ajuda
echo.
echo Exemplos:
echo   %0 run     # Executar aplicação
echo   %0 build   # Apenas construir
echo   %0 stop    # Parar aplicação
goto :eof

REM Função principal
:main
call :print_header

if "%1"=="" set "1=run"

if "%1"=="run" (
    call :check_docker
    if errorlevel 1 exit /b 1
    call :check_build
    if errorlevel 1 exit /b 1
    call :create_directories
    call :build_image
    if errorlevel 1 exit /b 1
    call :run_application
) else if "%1"=="build" (
    call :check_docker
    if errorlevel 1 exit /b 1
    call :check_build
    if errorlevel 1 exit /b 1
    call :create_directories
    call :build_image
    if errorlevel 1 exit /b 1
    call :print_message "Imagem construída com sucesso."
) else if "%1"=="stop" (
    call :stop_application
) else if "%1"=="restart" (
    call :stop_application
    call :check_docker
    if errorlevel 1 exit /b 1
    call :check_build
    if errorlevel 1 exit /b 1
    call :create_directories
    call :build_image
    if errorlevel 1 exit /b 1
    call :run_application
) else if "%1"=="logs" (
    call :show_logs
) else if "%1"=="clean" (
    call :clean_docker
) else if "%1"=="help" (
    call :show_help
) else if "%1"=="-h" (
    call :show_help
) else if "%1"=="--help" (
    call :show_help
) else (
    call :print_error "Opção inválida: %1"
    call :show_help
    exit /b 1
)

goto :eof

REM Executar função principal
call :main %*
