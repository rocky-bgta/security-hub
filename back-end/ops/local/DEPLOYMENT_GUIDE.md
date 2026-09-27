# Manual EKS Deployment Guide

This guide explains how to deploy services to Amazon EKS manually from your local machine.

## Prerequisites

Before running the deployment script, ensure you have the following tools installed:

- **Docker** - For building and pushing container images
- **AWS CLI** - For interacting with AWS services
- **kubectl** - For Kubernetes cluster management
- **jq** - For JSON processing
- **Gradle** - Already part of the project

### Install Missing Tools

#### macOS:
```bash
# Install Homebrew if not already installed
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install required tools
brew install docker awscli kubectl jq

# Install Docker Desktop for macOS
brew install --cask docker
```

#### Linux (Ubuntu/Debian):
```bash
# AWS CLI
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

# kubectl
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
chmod +x kubectl
sudo mv kubectl /usr/local/bin/

# jq
sudo apt-get update && sudo apt-get install -y jq

# Docker
sudo apt-get update
sudo apt-get install -y docker.io
sudo systemctl start docker
sudo usermod -aG docker $USER
```

## AWS Configuration

### 1. Configure AWS Credentials

```bash
aws configure
```

You'll need to enter:
- **AWS Access Key ID**: Your AWS access key
- **AWS Secret Access Key**: Your AWS secret key
- **Default region name**: `us-east-1`
- **Default output format**: `json`

### 2. Verify AWS Access

```bash
aws sts get-caller-identity
```

This should display your AWS account information.

### 3. Set EKS Cluster Name (Optional)

If your EKS cluster name is different from the default (`staging-asat_v2`), export it:

```bash
export EKS_CLUSTER_NAME="your-cluster-name"
```

## Deployment Process

### Basic Usage

Run the deployment script:

```bash
./deploy-to-eks.sh
```

The script will:

1. **Check Prerequisites** - Verify all required tools are installed
2. **Select Environment** - Choose dev, uat, or prod
3. **Select Services** - Choose which services to deploy
4. **Build Images** - Build Docker images for selected services
5. **Push to ECR** - Push images to AWS ECR
6. **Configure kubectl** - Connect to your EKS cluster
7. **Deploy Services** - Apply Kubernetes manifests and restart deployments
8. **Wait for Readiness** - Wait for all pods to be ready

### Example Session

```
ℹ Checking prerequisites...
✓ All prerequisites met!

ℹ Available environments:
  1) dev
  2) uat
  3) prod
Select environment [1-3]: 1
✓ Selected environment: dev

ℹ Available services in dev:
  1) auth
  2) cms
  3) gateway
  4) notification
  ...
  N) All services
Select services (comma-separated, e.g., 1,2,3 or all): 1,2,3

✓ Selected services: auth cms gateway

ℹ Deployment Configuration:
  Environment: dev
  Services: auth cms gateway
  Version: 1.0.20
  Region: us-east-1
  Cluster: staging-asat_v2

Continue with deployment? (y/n): y

...
```

## Deployment Details

### What the Script Does

1. **Builds Gradle Artifacts**
   - Runs `./gradlew :services:{service}:service:bootJar` for each service
   - Compiles the application with Java 17

2. **Builds Docker Images (with caching)**
   - Creates multi-stage Docker images
   - Includes RDS truststore for DocDB connections
   - Uses base image from ECR: `636499496141.dkr.ecr.us-east-1.amazonaws.com/aspire-java-base`
   - **Uses Docker BuildKit caching** to speed up subsequent builds
   - Reuses unchanged layers from previous builds

3. **Pushes to ECR**
   - Tags images with version (from `version.txt`) and `latest`
   - Image format: `{account-id}.dkr.ecr.us-east-1.amazonaws.com/aspire-{service}-asatv2:{version}`
   - Pushes both versioned and latest tags
   - Creates `cache` tag for faster rebuilds

4. **Deploys to EKS**
   - Connects to EKS cluster using AWS CLI
   - Creates namespace if it doesn't exist
   - Applies Ingress configuration
   - Deploys each service with:
     - Deployment manifest (from `ops/kubernetes/{env}/{service}/deployment.yaml`)
     - Service manifest (from `ops/kubernetes/{env}/{service}/service.yaml`)
   - Restarts deployments to pick up new images

### Build Performance Optimization

The deployment script uses Docker BuildKit caching to dramatically reduce build times:

| Build Scenario | First Build | Subsequent Builds |
|----------------|-------------|-------------------|
| No code changes | ~10-15 min | ~1-2 min (85-90% faster) |
| Small code changes | ~10-15 min | ~3-5 min (70-80% faster) |
| Big code changes | ~10-15 min | ~5-7 min (50-60% faster) |

**How it works:**
- Cache is pulled from ECR before building
- Only changed layers are rebuilt
- Cache is pushed back to ECR after build
- Gradle distribution is cached (saves 10+ minutes)
- Gradle dependencies and base layers are cached

**Gradle Download Optimization:**
- First build: Downloads Gradle distribution (cached in Docker layer)
- Subsequent builds: Uses cached Gradle (saves 10+ minutes per build)
- Gradle download only happens when gradle wrapper files change

### Dockerfile Optimization Pattern

To avoid downloading Gradle on every build (saves 1000+ seconds), use this pattern in service Dockerfiles:

**Before (downloads Gradle every time):**
```dockerfile
FROM gradle:8.8-jdk17-alpine AS build
WORKDIR /build
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .
COPY common ./common
COPY services ./services
RUN chmod +x ./gradlew
RUN ./gradlew :services:auth:service:bootJar --no-daemon
```

**After (caches Gradle download):**
```dockerfile
FROM gradle:8.8-jdk17-alpine AS build
WORKDIR /build

# Copy Gradle wrapper first to cache the Gradle download
COPY gradlew .
COPY gradle ./gradle
RUN chmod +x ./gradlew

# Pre-download Gradle (cached layer - only re-downloads if gradle wrapper changes)
RUN ./gradlew --version || true

# Copy build configuration files
COPY build.gradle .
COPY settings.gradle .

# Copy only source code dependencies
COPY common ./common

# Copy and build the specific service
COPY services ./services
RUN ./gradlew :services:auth:service:bootJar --no-daemon
```

**Optimized Services:**
- ✅ `services/auth/service/Dockerfile`
- ✅ `services/notification/service/Dockerfile`

**Services that need optimization:**
- `services/billing/service/Dockerfile`
- `services/cms/service/Dockerfile`
- `services/gateway/service/Dockerfile`
- `services/registration/service/Dockerfile`
- `services/sesame/service/Dockerfile`
- `services/universal/service/Dockerfile`
- `services/vps/service/Dockerfile`

### Image Naming Convention

Images are named using the following pattern:
```
{account-id}.dkr.ecr.us-east-1.amazonaws.com/aspire-{service}-asatv2:{version}
```

Example:
```
123456789012.dkr.ecr.us-east-1.amazonaws.com/aspire-auth-asatv2:1.0.20
```

### Cache Tags

For each service, the script creates 3 tags:
- `aspire-{service}-asatv2:{version}` - Versioned image (used in deployments)
- `aspire-{service}-asatv2:latest` - Latest image
- `aspire-{service}-asatv2:cache` - Cache tag (for faster rebuilds)

## Troubleshooting

### 1. AWS Credentials Not Configured

**Error**: `AWS credentials not configured`

**Solution**:
```bash
aws configure
```

### 2. Cannot Connect to EKS Cluster

**Error**: `Failed to connect to EKS cluster`

**Solutions**:
- Check that you have the correct AWS permissions
- Verify the cluster name: `echo $EKS_CLUSTER_NAME`
- Update kubeconfig: `aws eks update-kubeconfig --region us-east-1 --name staging-asat_v2`

### 3. Docker Build Fails

**Error**: Docker build errors

**Solutions**:
- Check if Gradle build completes successfully: `./gradlew :services:{service}:service:bootJar`
- Verify Docker is running: `docker ps`
- Check disk space: `df -h`

### 4. Pods Not Starting

**Check pod status**:
```bash
kubectl get pods -n {environment}
kubectl describe pod {pod-name} -n {environment}
kubectl logs {pod-name} -n {environment}
```

### 5. Image Pull Errors

**Error**: `ImagePullBackOff`

**Solutions**:
- Verify image exists in ECR: `aws ecr describe-images --repository-name aspire-{service}-asatv2`
- Check IAM permissions for EKS nodes to pull from ECR
- Verify the image tag in deployment.yaml

### 6. Build Cache Not Working

**Issue**: Builds still take 10+ minutes

**Solutions**:
- First build always takes full time (this is normal)
- Second build should be faster with cache
- Clear and rebuild: `aws ecr batch-delete-image --repository-name aspire-{service}-asatv2 --image-ids imageTag=cache`
- Check cache exists: `aws ecr list-images --repository-name aspire-{service}-asatv2 --region us-east-1`

### 7. Gradle Download on Every Build

**Issue**: Gradle distribution downloads on every build (1000+ seconds)

**Solutions**:
- Ensure Dockerfile uses the optimization pattern (see "Dockerfile Optimization Pattern" above)
- Check if `RUN ./gradlew --version` appears before source code COPY
- First build will download Gradle (expected)
- Subsequent builds should use cached Gradle layer
- If still downloading, check if gradle wrapper files changed

## Useful Commands

### Check Deployment Status
```bash
# All deployments in an environment
kubectl get deployments -n {environment}

# Specific service
kubectl get deployment asat-auth -n dev

# Pods for a service
kubectl get pods -n {environment} -l app=asat-auth

# Service endpoints
kubectl get services -n {environment}
```

### View Logs
```bash
# All pods of a service
kubectl logs -l app=asat-auth -n {environment} --tail=100

# Specific pod
kubectl logs {pod-name} -n {environment}
```

### Restart a Service
```bash
kubectl rollout restart deployment/asat-{service} -n {environment}
```

### Delete a Service
```bash
kubectl delete deployment asat-{service} -n {environment}
kubectl delete service asat-{service} -n {environment}
```

### Manage Cache

```bash
# List all cache tags
aws ecr list-images --repository-name aspire-auth-asatv2 --region us-east-1 | grep cache

# Clear cache for a service (forces full rebuild next time)
aws ecr batch-delete-image \
  --repository-name aspire-auth-asatv2 \
  --image-ids imageTag=cache \
  --region us-east-1

# Check cache size
aws ecr describe-images \
  --repository-name aspire-auth-asatv2 \
  --region us-east-1 \
  --query 'imageDetails[?contains(imageTags, `cache`)].imageSizeInBytes' \
  --output json
```

## Advanced Usage

### Deploy Single Service

```bash
./deploy-to-eks.sh
# Select environment: dev
# Select services: auth
```

### Deploy All Services

```bash
./deploy-to-eks.sh
# Select environment: dev
# Select services: all
```

### Change Version

Edit `version.txt` to set a new version before deploying:

```bash
echo "1.0.21" > version.txt
```

### Skip Build (Use Existing Images)

If you only want to redeploy without rebuilding images, you can modify the script to skip the build step.

### Optimize Build Performance

The deployment script automatically uses caching to speed up rebuilds:

1. **First deployment**: Takes full time (~10-15 min) - builds everything
2. **Subsequent deployments**: Much faster (~1-5 min) - only rebuilds changed layers
3. **No code changes**: Fastest (~1-2 min) - uses cached layers

**Tips:**
- Deploy frequently to maintain cache
- Don't clear cache tags unless experiencing issues
- Cache is stored in ECR, no local disk needed

## Security Notes

- **Never commit AWS credentials** to the repository
- **Use IAM roles** for EKS nodes instead of embedding credentials
- **Rotate access keys** regularly
- **Use least privilege** IAM policies
- **Review ECR repository policies** to ensure proper access control

## Support

For issues or questions:
- Check the troubleshooting section above
- Review AWS CloudWatch logs for EKS clusters
- Consult the AWS EKS documentation
- Contact your team's DevOps lead

