@echo off
:: Spring Microservices Management Script for Sustain-a-thon
:: Version: 3.0 (Windows Batch)

setlocal enabledelayedexpansion

:: Configuration
set PROJECT_NAME=sustain-a-thon
set SERVICES=service-registry api-gateway auth users
set SERVICE_PORTS=8761 8080 8082 8083

:: Default database credentials
set DB_USERNAME=xanderbilla
set DB_PASSWORD=0b1001001@
set DB_HOST=localhost
set DB_PORT=3306

:: Check if .env.hackathon exists and load it
if exist .env.hackathon (
    echo Loading environment variables from .env.hackathon
    for /f "usebackq tokens=1,2 delims==" %%a in (".env.hackathon") do (
        set %%a=%%b
    )
    echo Environment variables loaded
)

:: Function to display help
:show_help
echo.
echo Usage: %~nx0 [COMMAND] [SERVICE] [OPTIONS]
echo.
echo Commands:
echo   start [service]       Start all services or a specific service
echo   stop [service]        Stop all services or a specific service
echo   restart [service]     Restart all services or a specific service
echo   build [service]       Build all services or a specific service
echo   status                Show service status
echo   health                Comprehensive health check of all endpoints
echo   test                  Quick test - start services, test health, stop services
echo   check                 Check prerequisites
echo   help                  Show this help message
echo.
echo Available Services:
echo   service-registry api-gateway auth users
echo.
echo Examples:
echo   %~nx0 start              # Start all services
echo   %~nx0 start auth         # Start only auth service
echo   %~nx0 stop users         # Stop only users service
echo   %~nx0 restart api-gateway # Restart only api-gateway service
echo   %~nx0 build service-registry # Build only service-registry
echo.
goto :eof

:: Function to check prerequisites
:check_prerequisites
echo.
echo 🚀 Checking Prerequisites
echo.

:: Check Java
java -version >nul 2>&1
if !errorlevel! equ 0 (
    for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
        set JAVA_VERSION=%%g
        set JAVA_VERSION=!JAVA_VERSION:"=!
    )
    echo ✅ Java found: !JAVA_VERSION!
) else (
    echo ❌ Java not found. Please install Java 21 or higher.
    exit /b 1
)

:: Check Maven
mvn -version >nul 2>&1
if !errorlevel! equ 0 (
    for /f "tokens=*" %%g in ('mvn -version 2^>^&1 ^| findstr /r "Apache Maven"') do (
        set MAVEN_VERSION=%%g
    )
    echo ✅ Maven found: !MAVEN_VERSION!
) else (
    echo ❌ Maven not found. Please install Maven 3.6 or higher.
    exit /b 1
)

:: Check Docker
docker --version >nul 2>&1
if !errorlevel! equ 0 (
    for /f "tokens=*" %%g in ('docker --version 2^>^&1') do (
        set DOCKER_VERSION=%%g
    )
    echo ✅ Docker found: !DOCKER_VERSION!
) else (
    echo ⚠️ Docker not found. Docker commands will not be available.
)

:: Check MySQL and databases
call :check_mysql_and_databases
goto :eof

:: Function to check MySQL and databases
:check_mysql_and_databases
echo ℹ️ Checking MySQL connection...

:: Test MySQL connection
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USERNAME% -p%DB_PASSWORD% -e "SELECT 1;" >nul 2>&1
if !errorlevel! neq 0 (
    echo ❌ Cannot connect to MySQL database
    echo ❌ Please ensure MySQL is running and credentials are correct
    echo ℹ️ Database config: host=%DB_HOST%, port=%DB_PORT%, user=%DB_USERNAME%
    exit /b 1
)

echo ✅ MySQL connection successful

:: Check if databases exist
set MISSING_DBS=
for %%d in (aadhaar_db bank_db local_user_db) do (
    mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USERNAME% -p%DB_PASSWORD% -e "USE %%d;" >nul 2>&1
    if !errorlevel! neq 0 (
        set MISSING_DBS=!MISSING_DBS! %%d
    )
)

if not "!MISSING_DBS!"=="" (
    echo ⚠️ Missing databases:!MISSING_DBS!
    echo ℹ️ Initializing databases...
    call :initialize_databases
) else (
    echo ✅ All required databases are available: aadhaar_db bank_db local_user_db
    
    :: Verify data exists
    for /f %%a in ('mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USERNAME% -p%DB_PASSWORD% -e "USE aadhaar_db; SELECT COUNT(*) FROM aadhaar_users;" -N 2^>nul') do set AADHAAR_COUNT=%%a
    for /f %%b in ('mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USERNAME% -p%DB_PASSWORD% -e "USE bank_db; SELECT COUNT(*) FROM bank_accounts;" -N 2^>nul') do set BANK_COUNT=%%b
    
    if !AADHAAR_COUNT! gtr 0 if !BANK_COUNT! gtr 0 (
        echo ℹ️ ✓ Databases contain data: !AADHAAR_COUNT! Aadhaar users and !BANK_COUNT! bank accounts
        echo ℹ️ ✓ Local user database ready for authentication service
    ) else (
        echo ⚠️ Databases exist but appear to be empty. Reinitializing...
        call :initialize_databases
    )
)
goto :eof

:: Function to initialize databases
:initialize_databases
echo.
echo 🚀 Initializing Databases
echo.

echo ℹ️ Creating databases and tables with dummy data...

if exist init-databases.sql (
    mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USERNAME% -p%DB_PASSWORD% < init-databases.sql >nul 2>&1
    if !errorlevel! equ 0 (
        echo ✅ Databases initialized successfully
        echo ℹ️ ✓ Created databases: aadhaar_db, bank_db, local_user_db
        
        :: Verify the data was inserted
        for /f %%a in ('mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USERNAME% -p%DB_PASSWORD% -e "USE aadhaar_db; SELECT COUNT(*) FROM aadhaar_users;" -N 2^>nul') do set AADHAAR_COUNT=%%a
        for /f %%b in ('mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USERNAME% -p%DB_PASSWORD% -e "USE bank_db; SELECT COUNT(*) FROM bank_accounts;" -N 2^>nul') do set BANK_COUNT=%%b
        
        echo ℹ️ ✓ Added dummy data: !AADHAAR_COUNT! Aadhaar users and !BANK_COUNT! bank accounts
        echo ℹ️ ✓ Local user database ready for authentication service
    ) else (
        echo ❌ Failed to initialize databases
        exit /b 1
    )
) else (
    echo ❌ init-databases.sql file not found
    exit /b 1
)
goto :eof

:: Function to build all services
:build_services
echo.
echo 🚀 Building All Services
echo.

for %%s in (%SERVICES%) do (
    if exist %%s (
        echo ℹ️ Building %%s...
        cd %%s
        call mvn clean compile -q >nul 2>&1
        if !errorlevel! equ 0 (
            echo ✅ %%s built successfully
        ) else (
            echo ❌ Failed to build %%s
            cd ..
            exit /b 1
        )
        cd ..
    ) else (
        echo ⚠️ Service directory %%s not found
    )
)
echo ✅ All services built successfully
goto :eof

:: Function to build single service
:build_single_service
set SERVICE_NAME=%1
if exist %SERVICE_NAME% (
    echo ℹ️ Building %SERVICE_NAME%...
    cd %SERVICE_NAME%
    call mvn clean compile -q >nul 2>&1
    if !errorlevel! equ 0 (
        echo ✅ %SERVICE_NAME% built successfully
    ) else (
        echo ❌ Failed to build %SERVICE_NAME%
        cd ..
        exit /b 1
    )
    cd ..
) else (
    echo ❌ Service %SERVICE_NAME% not found
    exit /b 1
)
goto :eof

:: Function to start all services
:start_services
call :check_prerequisites
if !errorlevel! neq 0 exit /b 1

call :build_services
if !errorlevel! neq 0 exit /b 1

echo.
echo 🚀 Starting All Services
echo.

:: Start service-registry first
echo ⏳ Starting service-registry...
cd service-registry
start /b mvn spring-boot:run -Dspring-boot.run.profiles=default >nul 2>&1
cd ..
timeout /t 15 /nobreak >nul
echo ✅ service-registry started successfully (Port: 8761)

:: Wait for service-registry to be ready
echo ⏳ Waiting for service-registry to be fully ready...
:wait_registry
timeout /t 2 /nobreak >nul
curl -s http://localhost:8761/health >nul 2>&1
if !errorlevel! neq 0 goto wait_registry

:: Start other services
for %%s in (api-gateway auth users) do (
    echo ⏳ Starting %%s...
    cd %%s
    start /b mvn spring-boot:run -Dspring-boot.run.profiles=default >nul 2>&1
    cd ..
    timeout /t 10 /nobreak >nul
    if %%s==api-gateway echo ✅ %%s started successfully (Port: 8080)
    if %%s==auth echo ✅ %%s started successfully (Port: 8082)
    if %%s==users echo ✅ %%s started successfully (Port: 8083)
)

echo ✅ All services started successfully!
call :show_status
goto :eof

:: Function to stop all services
:stop_services
echo.
echo 🚀 Stopping All Services
echo.

for %%p in (8083 8082 8080 8761) do (
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr :%%p ^| findstr LISTENING') do (
        if not "%%a"=="" (
            echo ℹ️ Stopping service on port %%p (PID: %%a)
            taskkill /f /pid %%a >nul 2>&1
        )
    )
)
echo ✅ All services stopped
goto :eof

:: Function to show service status
:show_status
echo.
echo 🚀 Service Status
echo.
echo SERVICE              PORT       STATUS          
echo -------              ----       ------          

for %%s in (service-registry api-gateway auth users) do (
    if %%s==service-registry set PORT=8761
    if %%s==api-gateway set PORT=8080
    if %%s==auth set PORT=8082
    if %%s==users set PORT=8083
    
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr :!PORT! ^| findstr LISTENING 2^>nul') do (
        set PID=%%a
        set STATUS=RUNNING
        goto found_%%s
    )
    set STATUS=STOPPED
    set PID=N/A
    :found_%%s
    echo %%s                   !PORT!       !STATUS!        !PID!
)

echo.
echo ℹ️ Health check endpoints (via API Gateway - Port 8080):
echo   service-registry: http://localhost:8080/api/v1/service/health
echo   api-gateway: http://localhost:8080/health
echo   auth: http://localhost:8080/api/v1/auth/health  
echo   users: http://localhost:8080/api/v1/users/health
echo.
echo ℹ️ Direct service endpoints (for debugging):
echo   service-registry: http://localhost:8761/health
echo   api-gateway: http://localhost:8080/health
echo   auth: http://localhost:8082/health
echo   users: http://localhost:8083/users/health
goto :eof

:: Main script logic
if "%1"=="" goto show_help
if "%1"=="help" goto show_help
if "%1"=="check" (
    call :check_prerequisites
    goto :eof
)
if "%1"=="start" (
    if "%2"=="" (
        call :start_services
    ) else (
        echo Starting single service: %2
        call :build_single_service %2
        if !errorlevel! equ 0 call :start_single_service %2
    )
    goto :eof
)
if "%1"=="stop" (
    if "%2"=="" (
        call :stop_services
    ) else (
        call :stop_single_service %2
    )
    goto :eof
)
if "%1"=="restart" (
    if "%2"=="" (
        call :stop_services
        call :start_services
    ) else (
        call :stop_single_service %2
        call :build_single_service %2
        if !errorlevel! equ 0 call :start_single_service %2
    )
    goto :eof
)
if "%1"=="build" (
    if "%2"=="" (
        call :build_services
    ) else (
        call :build_single_service %2
    )
    goto :eof
)
if "%1"=="status" (
    call :show_status
    goto :eof
)

echo Invalid command: %1
call :show_help