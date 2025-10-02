#!/bin/bash

# Enhanced Multi-platform Build and ECR Push Script for Sustain-a-thon
set -e

# Configuration
ECR_REGISTRY="public.ecr.aws/k2g9v6r8"
AWS_REGION="us-east-1"
PLATFORMS="linux/amd64,linux/arm64"
PROJECT_NAME="sustain-a-thon"

# Services and their exact ECR repository names (updated for sustain-a-thon)
SERVICES="service-registry:spring-microservice/shop-service-registry api-gateway:spring-microservice/shop-api-gateway auth:spring-microservice/shop-auth users:spring-microservice/shop-users"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_header() {
    echo -e "${PURPLE}========================================${NC}"
    echo -e "${PURPLE}$1${NC}"
    echo -e "${PURPLE}========================================${NC}"
}

# Function to check prerequisites
check_prerequisites() {
    log_header "Checking Prerequisites"
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker not found. Please install Docker."
        exit 1
    fi
    log_success "Docker found: $(docker --version)"
    
    # Check AWS CLI
    if ! command -v aws &> /dev/null; then
        log_error "AWS CLI not found. Please install AWS CLI."
        exit 1
    fi
    log_success "AWS CLI found: $(aws --version 2>&1 | head -n1)"
    
    # Check Docker Buildx
    if ! docker buildx version &> /dev/null; then
        log_error "Docker Buildx not found. Please enable buildx."
        exit 1
    fi
    log_success "Docker Buildx found: $(docker buildx version)"
    
    # Check if services exist
    for service_pair in $SERVICES; do
        service_dir=$(echo "$service_pair" | cut -d':' -f1)
        if [ ! -d "$service_dir" ]; then
            log_error "Service directory '$service_dir' not found"
            exit 1
        fi
        if [ ! -f "$service_dir/Dockerfile" ]; then
            log_warning "Dockerfile not found in '$service_dir', will try to create one"
        fi
    done
    log_success "All service directories found"
}

# Function to create Dockerfile if it doesn't exist
create_dockerfile() {
    local service_dir=$1
    local dockerfile_path="$service_dir/Dockerfile"
    
    if [ ! -f "$dockerfile_path" ]; then
        log_info "Creating Dockerfile for $service_dir"
        cat > "$dockerfile_path" << EOF
# Multi-stage build for $service_dir
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml first to leverage Docker cache
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Production stage
FROM eclipse-temurin:21-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Create app user
RUN addgroup -g 1001 -S app && \\
    adduser -S app -u 1001 -G app

# Set working directory
WORKDIR /app

# Copy the jar file from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to app user
RUN chown -R app:app /app

# Switch to app user
USER app

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \\
    CMD curl -f http://localhost:8080/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
EOF
        log_success "Dockerfile created for $service_dir"
    fi
}

docker context use default

# Main execution
main() {
    log_header "Starting ECR Build and Push Process"
    
    check_prerequisites
    
    log_header "Authenticating with ECR"
    log_info "Authenticating with ECR public registry..."
    if aws ecr-public get-login-password --region ${AWS_REGION} | \
        docker login --username AWS --password-stdin public.ecr.aws; then
        log_success "Successfully authenticated with ECR"
    else
        log_error "Failed to authenticate with ECR"
        exit 1
    fi

    log_header "Setting up Docker buildx"
    log_info "Setting up Docker buildx for multi-platform builds..."
    # Use the default builder which already supports multi-platform
    docker buildx use default
    log_success "Using default builder with multi-platform support"

    log_header "Building and Pushing Services"
    local success_count=0
    local total_services=$(echo $SERVICES | wc -w)
    
    for service_pair in $SERVICES; do
        service_dir=$(echo "$service_pair" | cut -d':' -f1)
        ecr_repo=$(echo "$service_pair" | cut -d':' -f2)
        image_name="${ECR_REGISTRY}/${ecr_repo}:latest"
        
        log_info "Processing service: $service_dir"
        log_info "Target image: $image_name"
        log_info "Platforms: $PLATFORMS"
        
        # Create Dockerfile if it doesn't exist
        create_dockerfile "$service_dir"
        
        cd "${service_dir}"
        
        # Build and push for multiple platforms
        log_info "Building and pushing $service_dir..."
        if docker buildx build \
            --platform "${PLATFORMS}" \
            --tag "${image_name}" \
            --push \
            --progress=plain \
            .; then
            log_success "Successfully built and pushed $service_dir"
            ((success_count++))
        else
            log_error "Failed to build and push $service_dir"
            cd ..
            continue
        fi
        
        cd ..
        echo ""
    done

    log_header "Build Summary"
    log_info "Total services: $total_services"
    log_info "Successfully built: $success_count"
    log_info "Failed: $((total_services - success_count))"
    
    if [ $success_count -eq $total_services ]; then
        log_success "All services built and pushed successfully!"
        
        log_info "Available images:"
        for service_pair in $SERVICES; do
            ecr_repo=$(echo "$service_pair" | cut -d':' -f2)
            image_name="${ECR_REGISTRY}/${ecr_repo}:latest"
            echo "  - $image_name"
        done
        
        log_info "You can now use these images in your docker-compose.yml or Kubernetes deployments"
    else
        log_error "Some services failed to build. Please check the logs above."
        exit 1
    fi
}

# Function to show help
show_help() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Enhanced Multi-platform Build and ECR Push Script for $PROJECT_NAME"
    echo ""
    echo "Options:"
    echo "  --help, -h     Show this help message"
    echo "  --dry-run      Show what would be built without actually building"
    echo ""
    echo "Environment Variables:"
    echo "  ECR_REGISTRY   ECR registry URL (default: $ECR_REGISTRY)"
    echo "  AWS_REGION     AWS region (default: $AWS_REGION)"
    echo "  PLATFORMS      Target platforms (default: $PLATFORMS)"
    echo ""
    echo "Prerequisites:"
    echo "  - Docker with buildx support"
    echo "  - AWS CLI configured with appropriate permissions"
    echo "  - Dockerfile in each service directory (will be created if missing)"
    echo ""
}

# Function for dry run
dry_run() {
    log_header "Dry Run - Services to be built"
    
    for service_pair in $SERVICES; do
        service_dir=$(echo "$service_pair" | cut -d':' -f1)
        ecr_repo=$(echo "$service_pair" | cut -d':' -f2)
        image_name="${ECR_REGISTRY}/${ecr_repo}:latest"
        
        echo "Service: $service_dir"
        echo "  → Image: $image_name"
        echo "  → Platforms: $PLATFORMS"
        echo "  → Directory exists: $([ -d "$service_dir" ] && echo "Yes" || echo "No")"
        echo "  → Dockerfile exists: $([ -f "$service_dir/Dockerfile" ] && echo "Yes" || echo "No (will be created)")"
        echo ""
    done
    
    log_info "This is a dry run. No actual building will be performed."
}

# Parse command line arguments
case "${1:-}" in
    "--help"|"-h")
        show_help
        exit 0
        ;;
    "--dry-run")
        check_prerequisites
        dry_run
        exit 0
        ;;
    "")
        main
        ;;
    *)
        log_error "Unknown option: $1"
        show_help
        exit 1
        ;;
esac
echo ""
echo "📋 Built Images:"
for service_pair in $SERVICES; do
    service_dir=$(echo "$service_pair" | cut -d':' -f1)
    ecr_repo=$(echo "$service_pair" | cut -d':' -f2)
    echo "   ${ECR_REGISTRY}/${ecr_repo}:latest"
done

echo ""
echo "🧹 Cleaning up build cache..."
docker buildx prune -f