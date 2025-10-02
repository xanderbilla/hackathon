#!/bin/bash

# Spring Microservices Management Script for Sustain-a-thon
# Version: 3.0

set -e

# Configuration
PROJECT_NAME="sustain-a-thon"
SERVICES=("service-registry" "api-gateway" "auth" "users")
SERVICE_PORTS=(8761 8080 8082 8083)
MAVEN_PID_DIR="/tmp/sustain-a-thon-$$"
ENV_FILE=".env.hackathon"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}ℹ️ ${NC} $1"
}

log_success() {
    echo -e "${GREEN}✅${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}⚠️${NC} $1"
}

log_error() {
    echo -e "${RED}❌${NC} $1"
}

log_header() {
    echo ""
    echo -e "${GREEN}🚀 $1${NC}"
    echo ""
}

# Loading spinner function
show_spinner() {
    local pid=$1
    local delay=0.1
    local spinstr='|/-\\'
    while [ "$(ps a | awk '{print $1}' | grep $pid)" ]; do
        local temp=${spinstr#?}
        printf " [%c]  " "$spinstr"
        local spinstr=$temp${spinstr%"$temp"}
        sleep $delay
        printf "\b\b\b\b\b\b"
    done
    printf "    \b\b\b\b"
}

# Countdown function
countdown() {
    local seconds=$1
    local message="${2:-Waiting}"
    
    for ((i=seconds; i>0; i--)); do
        printf "\r${YELLOW}⏳${NC} $message... ${i}s remaining"
        sleep 1
    done
    printf "\r${GREEN}✅${NC} $message completed!            \n"
}

# Function to check if port is in use
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 0
    else
        return 1
    fi
}

# Function to kill process on port
kill_port() {
    local port=$1
    if check_port $port; then
        local pid=$(lsof -Pi :$port -sTCP:LISTEN -t)
        if [ ! -z "$pid" ]; then
            log_warning "Killing process $pid on port $port"
            kill -9 $pid 2>/dev/null || true
            sleep 2
        fi
    fi
}

# Function to load environment variables
load_env() {
    if [ -f "$ENV_FILE" ]; then
        log_info "Loading environment variables from $ENV_FILE"
        # Use set -a to automatically export variables, then source the file
        set -a
        source $ENV_FILE
        set +a
        log_success "Environment variables loaded"
    else
        log_warning "Environment file $ENV_FILE not found, using defaults"
    fi
}

# Function to check prerequisites
check_prerequisites() {
    log_header "Checking Prerequisites"
    
    # Check Java
    if command -v java &> /dev/null; then
        local java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
        log_success "Java found: $java_version"
    else
        log_error "Java not found. Please install Java 21 or higher."
        exit 1
    fi
    
    # Check Maven
    if command -v mvn &> /dev/null; then
        local maven_version=$(mvn -version | head -n 1)
        log_success "Maven found: $maven_version"
    else
        log_error "Maven not found. Please install Maven 3.6 or higher."
        exit 1
    fi
    
    # Check Docker
    if command -v docker &> /dev/null; then
        local docker_version=$(docker --version)
        log_success "Docker found: $docker_version"
    else
        log_warning "Docker not found. Docker commands will not be available."
    fi
    
    # All prerequisites checked - MySQL dependency removed
    log_info "All microservices configured to run without database dependencies"
}

# Function to build all services
build_services() {
    log_header "Building All Services"
    
    for service in "${SERVICES[@]}"; do
        if [ -d "$service" ]; then
            log_info "Building $service..."
            cd "$service"
            
            # Create temp file for error output
            local error_file="/tmp/build_error_${service}_$$.log"
            
            # Clean and compile with error capture
            if mvn clean compile -q > "$error_file" 2>&1; then
                log_success "$service built successfully"
                rm -f "$error_file"
            else
                log_error "Failed to build $service"
                echo -e "${RED}📋 Build Error Details for $service:${NC}"
                echo -e "${RED}═══════════════════════════════════════${NC}"
                cat "$error_file" | head -20
                echo -e "${RED}═══════════════════════════════════════${NC}"
                echo -e "${YELLOW}💡 Full error log saved to: $error_file${NC}"
                cd ..
                return 1
            fi
            cd ..
        else
            log_error "Service directory $service not found"
            return 1
        fi
    done
    
    log_success "All services built successfully"
}

# Function to build single service
build_single_service() {
    local service=$1
    
    if [ -z "$service" ]; then
        log_error "Service name is required"
        log_info "Available services: ${SERVICES[*]}"
        return 1
    fi
    
    # Check if service exists in array
    local service_exists=false
    for s in "${SERVICES[@]}"; do
        if [ "$s" = "$service" ]; then
            service_exists=true
            break
        fi
    done
    
    if [ "$service_exists" = false ]; then
        log_error "Service '$service' not found"
        log_info "Available services: ${SERVICES[*]}"
        return 1
    fi
    
    if [ -d "$service" ]; then
        log_info "Building $service..."
        cd "$service"
        
        # Create temp file for error output
        local error_file="/tmp/build_error_${service}_$$.log"
        
        # Clean and compile with error capture
        if mvn clean compile > "$error_file" 2>&1; then
            log_success "$service built successfully"
            rm -f "$error_file"
            cd ..
            return 0
        else
            log_error "Failed to build $service"
            echo -e "${RED}📋 Build Error Details for $service:${NC}"
            echo -e "${RED}═══════════════════════════════════════${NC}"
            cat "$error_file"
            echo -e "${RED}═══════════════════════════════════════${NC}"
            cd ..
            return 1
        fi
    else
        log_error "Service directory $service not found"
        return 1
    fi
}

# Function to get service port
get_service_port() {
    local service=$1
    
    for i in "${!SERVICES[@]}"; do
        if [ "${SERVICES[$i]}" = "$service" ]; then
            echo "${SERVICE_PORTS[$i]}"
            return 0
        fi
    done
    
    return 1
}

# Function to validate service name
validate_service() {
    local service=$1
    
    if [ -z "$service" ]; then
        log_error "Service name is required"
        log_info "Available services: ${SERVICES[*]}"
        return 1
    fi
    
    # Check if service exists in array
    for s in "${SERVICES[@]}"; do
        if [ "$s" = "$service" ]; then
            return 0
        fi
    done
    
    log_error "Service '$service' not found"
    log_info "Available services: ${SERVICES[*]}"
    return 1
}

# Function to start individual service
start_service() {
    local service=$1
    local port=$2
    
    if [ ! -d "$service" ]; then
        log_error "Service directory $service not found"
        return 1
    fi
    
    # Kill any existing process on the port
    kill_port $port
    
    cd "$service"
    
    # Create PID directory if it doesn't exist
    mkdir -p "$MAVEN_PID_DIR"
    
    # Start the service in background
    nohup mvn spring-boot:run \
        -Dspring-boot.run.jvmArguments="-Xmx512m -Xms256m" \
        > "$MAVEN_PID_DIR/${service}.log" 2>&1 &
    
    local pid=$!
    echo $pid > "$MAVEN_PID_DIR/${service}.pid"
    
    cd ..
    
    # Wait with spinner and check if service started
    printf "${YELLOW}⏳${NC} Starting $service"
    for i in {1..20}; do
        if check_port $port; then
            printf "\n"
            log_success "$service started successfully (PID: $pid, Port: $port)"
            return 0
        fi
        printf "."
        sleep 0.5
    done
    printf "\n"
    
    log_error "$service failed to start on port $port"
    echo -e "${RED}📋 Startup Error Details for $service:${NC}"
    echo -e "${RED}═══════════════════════════════════════${NC}"
    if [ -f "$MAVEN_PID_DIR/${service}.log" ]; then
        tail -20 "$MAVEN_PID_DIR/${service}.log" | grep -E "ERROR|Exception|Failed|error"
    else
        echo "No log file found at $MAVEN_PID_DIR/${service}.log"
    fi
    echo -e "${RED}═══════════════════════════════════════${NC}"
    echo -e "${YELLOW}💡 Full startup log: $MAVEN_PID_DIR/${service}.log${NC}"
    return 1
}

# Function to start single service by name
start_single_service() {
    local service=$1
    
    if ! validate_service "$service"; then
        return 1
    fi
    
    local port=$(get_service_port "$service")
    if [ $? -ne 0 ]; then
        log_error "Could not find port for service $service"
        return 1
    fi
    
    log_header "Starting Single Service: $service"
    load_env
    
    # Build the service first
    build_single_service "$service"
    if [ $? -ne 0 ]; then
        log_error "Failed to build $service"
        return 1
    fi
    
    # Check if service-registry is running (required for other services)
    if [ "$service" != "service-registry" ]; then
        if ! check_port 8761; then
            log_warning "service-registry is not running. Starting it first..."
            start_single_service "service-registry"
            if [ $? -ne 0 ]; then
                log_error "Failed to start service-registry"
                return 1
            fi
            sleep 5
        fi
    fi
    
    start_service "$service" "$port"
    return $?
}

# Function to start all services
start_all() {
    log_header "Starting All Services"
    load_env
    
    # Start services in order (service-registry first)
    for i in "${!SERVICES[@]}"; do
        local service="${SERVICES[$i]}"
        local port="${SERVICE_PORTS[$i]}"
        
        start_service "$service" "$port"
        
        if [ $? -ne 0 ]; then
            log_error "Failed to start $service. Stopping startup process."
            return 1
        fi
        
        # Extra wait for service-registry to be fully up
        if [ "$service" = "service-registry" ]; then
            printf "${YELLOW}⏳${NC} Waiting for service-registry to be fully ready"
            for i in {1..15}; do
                printf "."
                sleep 1
            done
            printf "\n"
        fi
    done
    
    log_success "All services started successfully!"
    show_status
}

# Function to stop individual service
stop_service() {
    local service=$1
    local port=$2
    
    log_info "Stopping $service..."
    
    # Check PID file
    if [ -f "$MAVEN_PID_DIR/${service}.pid" ]; then
        local pid=$(cat "$MAVEN_PID_DIR/${service}.pid")
        if kill -0 $pid 2>/dev/null; then
            kill $pid
            sleep 5
            # Force kill if still running
            if kill -0 $pid 2>/dev/null; then
                kill -9 $pid
            fi
            log_success "$service stopped (PID: $pid)"
        fi
        rm -f "$MAVEN_PID_DIR/${service}.pid"
    fi
    
    # Also kill any process on the port
    kill_port $port
}

# Function to stop single service by name
stop_single_service() {
    local service=$1
    
    if ! validate_service "$service"; then
        return 1
    fi
    
    local port=$(get_service_port "$service")
    if [ $? -ne 0 ]; then
        log_error "Could not find port for service $service"
        return 1
    fi
    
    log_header "Stopping Single Service: $service"
    
    stop_service "$service" "$port"
    log_success "$service stopped successfully"
}

# Function to restart single service by name
restart_single_service() {
    local service=$1
    
    if ! validate_service "$service"; then
        return 1
    fi
    
    log_header "Restarting Single Service: $service"
    
    # Stop the service first
    stop_single_service "$service"
    
    # Wait a moment
    sleep 3
    
    # Start the service
    start_single_service "$service"
}

# Function to stop all services
stop_all() {
    log_header "Stopping All Services"
    
    # Stop services in reverse order
    for ((i=${#SERVICES[@]}-1; i>=0; i--)); do
        local service="${SERVICES[$i]}"
        local port="${SERVICE_PORTS[$i]}"
        stop_service "$service" "$port"
    done
    
    # Clean up PID directory
    rm -rf "$MAVEN_PID_DIR"
    
    log_success "All services stopped"
}

# Function to restart all services
restart_all() {
    log_header "Restarting All Services"
    stop_all
    sleep 3
    start_all
}

# Function to show service status
show_status() {
    log_header "Service Status"
    
    printf "%-20s %-10s %-15s %-10s\n" "SERVICE" "PORT" "STATUS" "PID"
    printf "%-20s %-10s %-15s %-10s\n" "-------" "----" "------" "---"
    
    for i in "${!SERVICES[@]}"; do
        local service="${SERVICES[$i]}"
        local port="${SERVICE_PORTS[$i]}"
        local status="STOPPED"
        local pid="N/A"
        
        if check_port $port; then
            status="RUNNING"
            if [ -f "$MAVEN_PID_DIR/${service}.pid" ]; then
                pid=$(cat "$MAVEN_PID_DIR/${service}.pid")
            else
                pid=$(lsof -Pi :$port -sTCP:LISTEN -t)
            fi
            printf "%-20s %-10s ${GREEN}%-15s${NC} %-10s\n" "$service" "$port" "$status" "$pid"
        else
            status="STOPPED"
            printf "%-20s %-10s ${RED}%-15s${NC} %-10s\n" "$service" "$port" "$status" "$pid"
        fi
    done
    
    echo ""
    log_info "Health check endpoints:"
    for i in "${!SERVICES[@]}"; do
        local service="${SERVICES[$i]}"
        local port="${SERVICE_PORTS[$i]}"
        if check_port $port; then
            case $service in
                "service-registry")
                    echo "  $service: http://localhost:$port/api/v1/service/health"
                    ;;
                "api-gateway")
                    echo "  $service: http://localhost:$port/health"
                    ;;
                "auth")
                    echo "  $service: http://localhost:$port/api/v1/auth/health"
                    ;;
                "users")
                    echo "  $service: http://localhost:$port/api/v1/users/health"
                    ;;
            esac
        fi
    done
}



# Function to run with Docker Compose
docker_up() {
    log_header "Starting Services with Docker Compose"
    
    if ! command -v docker-compose &> /dev/null && ! command -v docker &> /dev/null; then
        log_error "Docker or docker-compose not found"
        return 1
    fi
    
    load_env
    
    # Use docker compose (newer) or docker-compose (older)
    if command -v docker &> /dev/null && docker compose version &> /dev/null; then
        docker compose --env-file $ENV_FILE up -d
    elif command -v docker-compose &> /dev/null; then
        docker-compose --env-file $ENV_FILE up -d
    else
        log_error "Neither 'docker compose' nor 'docker-compose' found"
        return 1
    fi
    
    log_success "Services started with Docker Compose"
}

# Function to stop Docker Compose
docker_down() {
    log_header "Stopping Docker Compose Services"
    
    if command -v docker &> /dev/null && docker compose version &> /dev/null; then
        docker compose down
    elif command -v docker-compose &> /dev/null; then
        docker-compose down
    else
        log_error "Neither 'docker compose' nor 'docker-compose' found"
        return 1
    fi
    
    log_success "Docker Compose services stopped"
}

# Function to test health endpoint
test_health_endpoint() {
    local service=$1
    local port=$2
    local url=""
    
    # Set correct health endpoint for each service
    case $service in
        "service-registry")
            url="http://localhost:$port/api/v1/service/health"
            ;;
        "api-gateway")
            url="http://localhost:$port/health"
            ;;
        "auth")
            url="http://localhost:$port/api/v1/auth/health"
            ;;
        "users")
            url="http://localhost:$port/api/v1/users/health"
            ;;
        *)
            log_error "Unknown service: $service"
            return 1
            ;;
    esac
    
    if command -v curl &> /dev/null; then
        local response=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null)
        if [ "$response" = "200" ]; then
            log_success "$service health check passed (HTTP $response)"
            return 0
        else
            log_error "$service health check failed (HTTP $response)"
            return 1
        fi
    else
        log_warning "curl not found, skipping health check for $service"
        return 0
    fi
}

# Function for quick test
quick_test() {
    log_header "Quick Test - Full Service Validation"
    
    # Check prerequisites first
    check_prerequisites
    if [ $? -ne 0 ]; then
        log_error "Prerequisites check failed. Aborting quick test."
        return 1
    fi
    
    load_env
    
    # Check which services are running and start missing ones
    log_info "Checking service status..."
    local services_to_start=()
    
    for i in "${!SERVICES[@]}"; do
        local service="${SERVICES[$i]}"
        local port="${SERVICE_PORTS[$i]}"
        
        if check_port $port; then
            log_success "$service is already running on port $port"
        else
            log_info "$service is not running, will start it"
            services_to_start+=("$service")
        fi
    done
    
    # Start missing services in correct order
    if [ ${#services_to_start[@]} -gt 0 ]; then
        log_header "Starting Missing Services"
        build_services
        if [ $? -ne 0 ]; then
            log_error "Build failed. Aborting quick test."
            return 1
        fi
        
        # Start services in correct order: service-registry -> api-gateway -> auth -> users
        local start_order=("service-registry" "api-gateway" "auth" "users")
        
        for service in "${start_order[@]}"; do
            # Check if this service needs to be started
            for missing_service in "${services_to_start[@]}"; do
                if [ "$service" = "$missing_service" ]; then
                    local port=$(get_service_port "$service")
                    log_info "Starting $service..."
                    
                    start_service "$service" "$port"
                    if [ $? -ne 0 ]; then
                        log_error "Failed to start $service. Aborting quick test."
                        return 1
                    fi
                    
                    # Extra wait for service-registry
                    if [ "$service" = "service-registry" ]; then
                        log_info "Waiting for service-registry to be fully ready..."
                        sleep 15
                    fi
                    break
                fi
            done
        done
    else
        log_success "All services are already running"
    fi
    
    # Wait 30 seconds with countdown
    log_header "Waiting for Services to Stabilize"
    countdown 30 "Services stabilizing"
    
    # Test health endpoints
    log_header "Testing Health Endpoints"
    local all_tests_passed=true
    
    for i in "${!SERVICES[@]}"; do
        local service="${SERVICES[$i]}"
        local port="${SERVICE_PORTS[$i]}"
        
        log_info "Testing $service health endpoint..."
        test_health_endpoint "$service" "$port"
        if [ $? -ne 0 ]; then
            all_tests_passed=false
        fi
    done
    
    # Show final results
    log_header "Quick Test Results"
    if [ "$all_tests_passed" = true ]; then
        log_success "All health checks passed! ✨"
        log_info "Services are ready for development"
    else
        log_error "Some health checks failed! ❌"
        log_info "Service failed to start - check if port is already in use"
    fi
    
    # Stop all services
    log_header "Cleaning Up - Stopping All Services"
    stop_all
    
    if [ "$all_tests_passed" = true ]; then
        log_success "Quick test completed successfully! 🎉"
        return 0
    else
        log_error "Quick test completed with failures! 💥"
        return 1
    fi
}

# Function to perform comprehensive health check
health_check() {
    log_header "Health Check - Testing All Endpoints"
    
    local failed_services=()
    local passed_count=0
    local total_count=0
    
    echo -e "${BLUE}Testing all service health endpoints...${NC}"
    echo ""
    
    for i in "${!SERVICES[@]}"; do
        local service="${SERVICES[$i]}"
        local port="${SERVICE_PORTS[$i]}"
        local url=""
        total_count=$((total_count + 1))
        
        # Set correct health endpoint for each service
        case $service in
            "service-registry")
                url="http://localhost:$port/api/v1/service/health"
                ;;
            "api-gateway")
                url="http://localhost:$port/health"
                ;;
            "auth")
                url="http://localhost:$port/api/v1/auth/health"
                ;;
            "users")
                url="http://localhost:$port/api/v1/users/health"
                ;;
        esac
        
        printf "%-20s %-10s " "$service" "$port"
        
        if ! check_port $port; then
            echo -e "${RED}❌ FAILED${NC} - Service not running"
            failed_services+=("$service: Service not running on port $port")
            continue
        fi
        
        if command -v curl &> /dev/null; then
            local temp_file="/tmp/health_check_${service}_$$.json"
            local http_code=$(curl -s -o "$temp_file" -w "%{http_code}" "$url" 2>/dev/null)
            
            if [ "$http_code" = "200" ]; then
                # Check if response is valid JSON and has status field
                if jq -e '.status' "$temp_file" > /dev/null 2>&1; then
                    local status=$(jq -r '.status' "$temp_file" 2>/dev/null)
                    if [ "$status" = "true" ]; then
                        echo -e "${GREEN}✅ PASSED${NC} - HTTP $http_code"
                        passed_count=$((passed_count + 1))
                    else
                        echo -e "${RED}❌ FAILED${NC} - Status: false"
                        local message=$(jq -r '.message // "Unknown error"' "$temp_file" 2>/dev/null)
                        failed_services+=("$service: Health check returned status=false - $message")
                    fi
                else
                    echo -e "${YELLOW}⚠️  WARNING${NC} - Invalid JSON response"
                    failed_services+=("$service: Invalid JSON response from health endpoint")
                fi
            else
                echo -e "${RED}❌ FAILED${NC} - HTTP $http_code"
                local error_msg="HTTP $http_code"
                if [ -f "$temp_file" ] && [ -s "$temp_file" ]; then
                    local response_msg=$(jq -r '.message // .error // "No error message"' "$temp_file" 2>/dev/null || cat "$temp_file" | head -1)
                    error_msg="$error_msg - $response_msg"
                fi
                failed_services+=("$service: $error_msg")
            fi
            
            rm -f "$temp_file"
        else
            echo -e "${YELLOW}⚠️  SKIPPED${NC} - curl not available"
        fi
    done
    
    echo ""
    echo -e "${BLUE}Health Check Summary:${NC}"
    echo -e "${GREEN}✅ Passed: $passed_count/$total_count services${NC}"
    
    if [ ${#failed_services[@]} -eq 0 ]; then
        echo -e "${GREEN}🎉 All services are healthy!${NC}"
        return 0
    else
        echo -e "${RED}❌ Failed: ${#failed_services[@]}/$total_count services${NC}"
        echo ""
        echo -e "${RED}Failed Services Details:${NC}"
        for failure in "${failed_services[@]}"; do
            echo -e "${RED}  • $failure${NC}"
        done
        echo ""
        echo -e "${YELLOW}💡 Troubleshooting Tips:${NC}"
        echo -e "${YELLOW}  • Check if services are running: ./local-run.sh status${NC}"

        echo -e "${YELLOW}  • Restart failed services: ./local-run.sh restart <service-name>${NC}"
        return 1
    fi
}

# Function to show help
show_help() {
    echo "Usage: $0 [COMMAND] [SERVICE] [OPTIONS]"
    echo ""
    echo "Commands:"
    echo "  start [service]       Start all services or a specific service"
    echo "  stop [service]        Stop all services or a specific service"
    echo "  restart [service]     Restart all services or a specific service"
    echo "  build [service]       Build all services or a specific service"
    echo "  status                Show service status"

    echo "  health                Comprehensive health check of all endpoints"
    echo "  test                  Quick test - start services, test health, stop services"
    echo "  docker-up             Start services with Docker Compose"
    echo "  docker-down           Stop Docker Compose services"
    echo "  check                 Check prerequisites"
    echo "  help                  Show this help message"
    echo ""
    echo "Available Services:"
    echo "  ${SERVICES[*]}"
    echo ""
    echo "Examples:"
    echo "  $0 start              # Start all services"
    echo "  $0 start auth         # Start only auth service"
    echo "  $0 stop users         # Stop only users service"
    echo "  $0 restart api-gateway # Restart only api-gateway service"
    echo "  $0 build service-registry # Build only service-registry"

    echo "  $0 docker-up          # Start with Docker"
    echo ""
}

# Main script logic
main() {
    case "${1:-help}" in
        "start")
            if [ -n "$2" ]; then
                # Start single service
                start_single_service "$2"
            else
                # Start all services
                start_all
            fi
            ;;
        "stop")
            if [ -n "$2" ]; then
                # Stop single service
                stop_single_service "$2"
            else
                # Stop all services
                stop_all
            fi
            ;;
        "restart")
            if [ -n "$2" ]; then
                # Restart single service
                check_prerequisites
                restart_single_service "$2"
            else
                # Restart all services
                check_prerequisites
                build_services
                restart_all
            fi
            ;;
        "build")
            if [ -n "$2" ]; then
                # Build single service
                check_prerequisites
                build_single_service "$2"
            else
                # Build all services
                check_prerequisites
                build_services
            fi
            ;;
        "status")
            show_status
            ;;

        "health")
            health_check
            ;;
        "test")
            quick_test
            ;;
        "docker-up")
            docker_up
            ;;
        "docker-down")
            docker_down
            ;;
        "check")
            check_prerequisites
            ;;
        "help"|"--help"|"-h"|"")
            show_help
            ;;
        *)
            log_error "Unknown command: $1"
            show_help
            exit 1
            ;;
    esac
}

# Run main function with all arguments
main "$@"