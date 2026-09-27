#!/bin/bash

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get script directory and set project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/../../."  # Go to project root where version.txt is located

# Configuration
AWS_REGION="us-east-1"
EKS_CLUSTER_NAME="${EKS_CLUSTER_NAME:-staging-asat_v2}"

# Read version from version.txt (must be in project root)
if [ -f "version.txt" ]; then
    VERSION=$(cat version.txt | tr -d '[:space:]')
else
    echo -e "${RED}✗ version.txt not found in project root: $(pwd)${NC}"
    exit 1
fi

# Function to print colored messages
print_info() {
    echo -e "${BLUE}ℹ ${1}${NC}"
}

print_success() {
    echo -e "${GREEN}✓ ${1}${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ ${1}${NC}"
}

print_error() {
    echo -e "${RED}✗ ${1}${NC}"
}

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
check_prerequisites() {
    print_info "Checking prerequisites..."
    
    local missing=()
    
    ! command_exists docker && missing+=("docker")
    ! command_exists aws && missing+=("aws-cli")
    ! command_exists kubectl && missing+=("kubectl")
    ! command_exists jq && missing+=("jq")
    
    if [ ${#missing[@]} -ne 0 ]; then
        print_error "Missing prerequisites: ${missing[*]}"
        print_info "Please install the missing tools and try again."
        exit 1
    fi
    
    # Check and setup Docker buildx for better caching
    if docker buildx inspect multiarch 2>/dev/null > /dev/null; then
        print_info "Using existing buildx builder"
    else
        print_info "Creating buildx builder for better caching..."
        docker buildx create --name multiarch --use 2>/dev/null || true
    fi
    
    print_success "All prerequisites met!"
}

# Function to get available services for an environment
get_available_services() {
    local env=$1
    ls -1 ops/kubernetes/$env 2>/dev/null | grep -v '\.yml$' || echo ""
}

# Function to prompt for environment selection
select_environment() {
    local environments=("dev" "uat" "prod")
    
    echo ""
    print_info "Available environments:"
    for i in "${!environments[@]}"; do
        echo "  $((i+1))) ${environments[$i]}"
    done
    
    while true; do
        read -p "Select environment [1-${#environments[@]}]: " choice
        if [[ "$choice" =~ ^[1-${#environments[@]}]$ ]]; then
            SELECTED_ENV=${environments[$((choice-1))]}
            break
        else
            print_error "Invalid choice. Please enter a number between 1 and ${#environments[@]}"
        fi
    done
    
    print_success "Selected environment: $SELECTED_ENV"
}

# Function to prompt for services
select_services() {
    print_info "Available services in $SELECTED_ENV:"
    local services=($(get_available_services $SELECTED_ENV))
    
    if [ ${#services[@]} -eq 0 ]; then
        print_error "No services found for environment $SELECTED_ENV"
        exit 1
    fi
    
    for i in "${!services[@]}"; do
        echo "  $((i+1))) ${services[$i]}"
    done
    echo "  $(( ${#services[@]} + 1 ))) All services"
    
    while true; do
        read -p "Select services (comma-separated, e.g., 1,2,3 or all): " choice
        
        if [[ "$choice" == "all" ]] || [[ "$choice" == "$(( ${#services[@]} + 1 ))" ]]; then
            SELECTED_SERVICES=("${services[@]}")
            break
        else
            IFS=',' read -ra SELECTIONS <<< "$choice"
            SELECTED_SERVICES=()
            
            for selection in "${SELECTIONS[@]}"; do
                if [[ "$selection" =~ ^[0-9]+$ ]] && [ "$selection" -ge 1 ] && [ "$selection" -le "${#services[@]}" ]; then
                    SELECTED_SERVICES+=("${services[$((selection-1))]}")
                else
                    print_error "Invalid selection: $selection"
                    continue 2
                fi
            done
            
            if [ ${#SELECTED_SERVICES[@]} -gt 0 ]; then
                break
            fi
        fi
    done
    
    print_success "Selected services: ${SELECTED_SERVICES[*]}"
}

# Function to get ECR repository URL
get_ecr_repo_url() {
    aws sts get-caller-identity --region $AWS_REGION >/dev/null 2>&1 || {
        print_error "AWS credentials not configured. Please run 'aws configure'"
        exit 1
    }
    
    local account_id=$(aws sts get-caller-identity --query Account --output text --region $AWS_REGION)
    ECR_REGISTRY="${account_id}.dkr.ecr.${AWS_REGION}.amazonaws.com"
    
    print_success "ECR Registry: $ECR_REGISTRY"
}

# Function to login to ECR
login_to_ecr() {
    print_info "Logging in to ECR..."
    aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REGISTRY
    print_success "Logged in to ECR"
}

# Function to build Docker image for a service
build_and_push_image() {
    local service=$1
    
    print_info "Building image for service: $service"
    
    # Check if Dockerfile exists
    if [ ! -f "services/$service/service/Dockerfile" ]; then
        print_warning "Dockerfile not found for $service, skipping image build"
        return 0
    fi
    
    local image_name="$ECR_REGISTRY/aspire-${service}-asatv2"
    local image_tag="$image_name:$VERSION"
    local image_latest="$image_name:latest"
    local cache_tag="$image_name:cache"
    
    print_info "Building Docker image: $image_tag"
    
    # Enable BuildKit for better caching and parallel builds
    export DOCKER_BUILDKIT=1
    export BUILDKIT_PROGRESS=plain
    export COMPOSE_DOCKER_CLI_BUILD=1
    
    # Try to pull the latest image for cache (optimization for rebuilds)
    print_info "Pulling cache image for faster builds..."
    if docker pull "$cache_tag" 2>/dev/null; then
        print_success "Found cache image"
    else
        print_info "No cache image found, will build from scratch (first build takes longer)"
    fi
    
    # Build the image for linux/amd64 platform (EKS runs on AMD64) with caching
    print_info "Building with cache support (subsequent builds will be much faster)..."
    
    # Use buildx with cache if available, fallback to regular docker build
    if docker buildx version &>/dev/null; then
        # Try to use buildx with cache
        print_info "Using buildx with inline cache for optimal performance..."
        if docker buildx build \
            --platform linux/amd64 \
            --cache-from type=registry,ref=$cache_tag \
            --cache-to type=inline,mode=max \
            --tag "$image_tag" \
            --tag "$image_latest" \
            --file "services/$service/service/Dockerfile" \
            --load \
            .; then
            print_success "Buildx build completed successfully"
        else
            print_warning "buildx failed, falling back to standard docker build"
            docker build \
                --platform linux/amd64 \
                --cache-from "$cache_tag" \
                --tag "$image_tag" \
                --tag "$image_latest" \
                --file "services/$service/service/Dockerfile" \
                .
        fi
    else
        # Fallback to standard docker build with cache
        print_info "Using standard docker build with cache..."
        docker build \
            --platform linux/amd64 \
            --cache-from "$cache_tag" \
            --tag "$image_tag" \
            --tag "$image_latest" \
            --file "services/$service/service/Dockerfile" \
            .
    fi
    
    print_success "Image built successfully: $image_tag"
    
    # Push images
    print_info "Pushing image tags to ECR..."
    if docker push "$image_tag" && docker push "$image_latest"; then
        print_success "Images pushed successfully"
    else
        print_error "Failed to push images to ECR"
        exit 1
    fi
    
    # Push cache tag for next build
    print_info "Pushing cache tag for faster future builds..."
    if docker tag "$image_tag" "$cache_tag" 2>/dev/null && docker push "$cache_tag" 2>/dev/null; then
        print_success "Cache tag pushed successfully"
    else
        print_warning "Failed to push cache tag (build will work, but next build may be slower)"
    fi
    
    print_success "Image pushed successfully: $image_tag"
}

# Function to configure kubectl for EKS
configure_kubectl() {
    print_info "Configuring kubectl for EKS cluster: $EKS_CLUSTER_NAME"
    aws eks update-kubeconfig --region $AWS_REGION --name $EKS_CLUSTER_NAME
    print_success "Kubectl configured"
    
    # Verify connection
    print_info "Verifying cluster connection..."
    kubectl get nodes >/dev/null 2>&1 || {
        print_error "Failed to connect to EKS cluster"
        exit 1
    }
    print_success "Cluster connection verified"
}

# Function to create namespace
create_namespace() {
    print_info "Creating namespace: $SELECTED_ENV"
    kubectl create namespace "$SELECTED_ENV" --dry-run=client -o yaml | kubectl apply -f -
    print_success "Namespace ready: $SELECTED_ENV"
}

# Function to deploy services
deploy_services() {
    local image_url="$ECR_REGISTRY/aspire-{service}-asatv2:$VERSION"
    
    for service in "${SELECTED_SERVICES[@]}"; do
        print_info "Deploying service: $service"
        
        local manifest_dir="ops/kubernetes/$SELECTED_ENV/$service"
        local deployment_file="$manifest_dir/deployment.yaml"
        local service_file="$manifest_dir/service.yaml"
        
        # Deploy Ingress first (if exists)
        if [ "$service" == "${SELECTED_SERVICES[0]}" ]; then
            if [ -f "ops/kubernetes/$SELECTED_ENV/ingress.yml" ]; then
                print_info "Applying Ingress..."
                kubectl apply -f "ops/kubernetes/$SELECTED_ENV/ingress.yml" --namespace="$SELECTED_ENV"
            fi
        fi
        
        # Process deployment file
        if [ -f "$deployment_file" ]; then
            local full_image_url="${image_url//\{service\}/$service}"
            print_info "Using image: $full_image_url"
            
            # Create a temporary file with the image replacement
            local temp_deployment=$(mktemp)
            sed "s|image:.*|image: $full_image_url|" "$deployment_file" > "$temp_deployment"
            
            # Apply deployment
            kubectl apply -f "$temp_deployment" --namespace="$SELECTED_ENV"
            rm "$temp_deployment"
            
            print_success "Deployment applied for $service"
        else
            print_warning "Deployment file not found: $deployment_file"
        fi
        
        # Process service file
        if [ -f "$service_file" ]; then
            kubectl apply -f "$service_file" --namespace="$SELECTED_ENV"
            print_success "Service applied for $service"
        else
            print_warning "Service file not found: $service_file"
        fi
        
        # Restart deployment to use new image
        print_info "Restarting deployment: asat-$service"
        kubectl rollout restart deployment/asat-$service --namespace="$SELECTED_ENV"
        
        print_success "Deployment completed for $service"
        echo ""
    done
}

# Function to wait for deployments
wait_for_deployments() {
    print_info "Waiting for all deployments to be ready..."
    
    for service in "${SELECTED_SERVICES[@]}"; do
        if kubectl get deployment/asat-$service -n "$SELECTED_ENV" &>/dev/null; then
            print_info "Waiting for deployment: asat-$service"
            if kubectl rollout status deployment/asat-$service --namespace="$SELECTED_ENV" --timeout=300s; then
                print_success "Deployment ready: asat-$service"
            else
                print_warning "Deployment asat-$service not ready - check logs manually"
            fi
        fi
    done
}

# Main execution
main() {
    echo "========================================"
    echo "  EKS Manual Deployment Script"
    echo "========================================"
    echo ""
    
    echo -e "${BLUE}ℹ Working directory: $(pwd)${NC}"
    echo -e "${BLUE}ℹ Version: $VERSION${NC}"
    
    check_prerequisites
    select_environment
    select_services
    
    echo ""
    print_info "Deployment Configuration:"
    print_info "  Environment: $SELECTED_ENV"
    print_info "  Services: ${SELECTED_SERVICES[*]}"
    print_info "  Version: $VERSION"
    print_info "  Region: $AWS_REGION"
    print_info "  Cluster: $EKS_CLUSTER_NAME"
    echo ""
    
    read -p "Continue with deployment? (y/n): " -n 1 -r
    echo
    
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_info "Deployment cancelled"
        exit 0
    fi
    
    echo ""
    print_info "Starting deployment process..."
    
    get_ecr_repo_url
    login_to_ecr
    
    # Build and push images for all services
    for service in "${SELECTED_SERVICES[@]}"; do
        build_and_push_image "$service"
    done
    
    configure_kubectl
    create_namespace
    
    echo ""
    print_info "Deploying services to EKS..."
    deploy_services
    
    echo ""
    print_info "Waiting for deployments to be ready..."
    wait_for_deployments
    
    echo ""
    print_success "Deployment completed successfully!"
    echo ""
    print_info "To check pod status, run:"
    print_info "  kubectl get pods -n $SELECTED_ENV"
    print_info "  kubectl get services -n $SELECTED_ENV"
}

# Run main function
main

