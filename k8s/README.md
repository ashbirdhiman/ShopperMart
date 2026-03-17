# ShopperMart Kubernetes Deployment

This directory contains all Kubernetes manifests for deploying ShopperMart microservices to a Kubernetes cluster.

## Directory Structure

```
k8s/
├── namespace.yml              # Create shoppermart namespace
├── secrets.yml                # Database credentials and app secrets
├── configmaps.yml             # Application configuration
├── ingress.yml                # External access routing
├── network-policy.yml         # Network security policies
├── databases/
│   ├── mysql.yml              # MySQL database for user/order/inventory services
│   ├── mongodb.yml            # MongoDB for product service
│   └── keycloak.yml           # Keycloak authentication service
├── microservices/
│   ├── config-server.yml      # Spring Cloud Config Server
│   ├── eureka-server.yml      # Service registry and discovery
│   ├── api-gateway.yml        # API Gateway (entry point)
│   ├── user-service.yml       # User service
│   ├── product-service.yml    # Product service
│   ├── order-service.yml      # Order service
│   ├── inventory-service.yml  # Inventory service
│   └── notification-service.yml # Notification service
└── monitoring/
    ├── prometheus.yml         # Metrics collection
    ├── grafana.yml            # Metrics visualization
    ├── loki.yml               # Log aggregation
    └── tempo.yml              # Distributed tracing
```

## Prerequisites

- Kubernetes cluster (v1.24+)
- kubectl CLI installed
- Container images pushed to a registry
- 4GB+ RAM and 2+ CPUs available

## Quick Start

### 1. Build Docker Images

```bash
# Build all images
mvn clean package jib:build

# Or use Docker buildx for multi-platform
docker buildx build --platform linux/amd64,linux/arm64 \
  -t your-registry/shoppermart-microservice:latest .
```

### 2. Create Namespace and Secrets

```bash
# Create namespace and secrets
kubectl apply -f k8s/namespace.yml
kubectl apply -f k8s/secrets.yml

# Verify
kubectl get ns
kubectl get secrets -n shoppermart
```

### 3. Update Configuration

Edit `k8s/secrets.yml` and `k8s/configmaps.yml` with your environment-specific values:

- Database passwords
- API keys and JWT secrets
- Service URLs
- Config server Git repository

### 4. Deploy Infrastructure

```bash
# Deploy configuration and secrets
kubectl apply -f k8s/configmaps.yml

# Deploy databases and keycloak
kubectl apply -f k8s/databases/

# Wait for databases to be ready
kubectl wait --for=condition=ready pod -l app=mysql -n shoppermart --timeout=300s
kubectl wait --for=condition=ready pod -l app=mongodb -n shoppermart --timeout=300s
```

### 5. Deploy Microservices

```bash
# Deploy core services
kubectl apply -f k8s/microservices/config-server.yml
kubectl apply -f k8s/microservices/eureka-server.yml

# Wait for config server to be ready
kubectl wait --for=condition=ready pod -l app=config-server -n shoppermart --timeout=300s

# Deploy remaining services
kubectl apply -f k8s/microservices/

# Verify all pods are running
kubectl get pods -n shoppermart
```

### 6. Deploy Monitoring

```bash
# Deploy monitoring stack
kubectl apply -f k8s/monitoring/

# Verify monitoring pods
kubectl get pods -n shoppermart -l app in (prometheus,grafana,loki,tempo)
```

### 7. Configure Ingress and Network Policies

```bash
# Update hosts in ingress.yml
kubectl apply -f k8s/ingress.yml
kubectl apply -f k8s/network-policy.yml

# Get ingress details
kubectl get ingress -n shoppermart
```

## Verification

### Check Pod Status

```bash
# All pods
kubectl get pods -n shoppermart

# Specific service
kubectl get pods -n shoppermart -l app=api-gateway

# Pod details
kubectl describe pod <pod-name> -n shoppermart

# Pod logs
kubectl logs -f deployment/api-gateway -n shoppermart
```

### Access Services

```bash
# Port-forward to services
kubectl port-forward svc/api-gateway 8080:8080 -n shoppermart
kubectl port-forward svc/grafana 3000:3000 -n shoppermart
kubectl port-forward svc/eureka-server 8761:8761 -n shoppermart

# API Gateway: http://localhost:8080
# Grafana: http://localhost:3000 (admin/admin)
# Eureka: http://localhost:8761
```

### View Logs

```bash
# Recent logs
kubectl logs deployment/order-service -n shoppermart --tail=50

# Continuous logs
kubectl logs -f deployment/order-service -n shoppermart

# Multiple containers
kubectl logs deployment/api-gateway -n shoppermart --all-containers

# Previous pod logs (if crashed)
kubectl logs pod/<pod-name> -n shoppermart --previous
```

## Scaling

```bash
# Scale a deployment
kubectl scale deployment api-gateway --replicas=3 -n shoppermart

# Auto-scaling
kubectl autoscale deployment api-gateway \
  --min=2 --max=5 --cpu-percent=70 \
  -n shoppermart
```

## Updates and Rollouts

```bash
# Update image
kubectl set image deployment/api-gateway \
  api-gateway=your-registry/shoppermart-api-gateway:v1.1.0 \
  -n shoppermart

# Check rollout status
kubectl rollout status deployment/api-gateway -n shoppermart

# Rollback to previous version
kubectl rollout undo deployment/api-gateway -n shoppermart
```

## Environment-Specific Deployments

### Development Environment

```bash
# Create dev overlay
cp -r k8s k8s-dev

# Edit k8s-dev files with dev settings
# kubectl apply -f k8s-dev/
```

### Production Deployment

1. Update secrets with production credentials
2. Increase replica counts for HA
3. Configure resource limits appropriately
4. Enable TLS/SSL in ingress
5. Set up backup strategies for databases
6. Configure persistent volume provisioners

```bash
# Production deployment
kubectl apply -f k8s/ -n shoppermart-prod
```

## Cleanup

```bash
# Delete all resources in namespace
kubectl delete namespace shoppermart

# Or delete specific resources
kubectl delete -f k8s/ -n shoppermart
```

## Troubleshooting

### Pods not starting

```bash
kubectl describe pod <pod-name> -n shoppermart
kubectl logs <pod-name> -n shoppermart
```

### Database connection issues

```bash
# Verify database service
kubectl get svc mysql -n shoppermart

# Test connectivity
kubectl run -it --rm debug --image=mysql:8.3.0 --restart=Never -- \
  mysql -hmysql -uroot -p<password>
```

### Image pull errors

```bash
# Verify image availability
kubectl get nodes -o wide

# Create image pull secret if using private registry
kubectl create secret docker-registry regcred \
  --docker-server=your-registry \
  --docker-username=<username> \
  --docker-password=<password> \
  -n shoppermart
```

### Service discovery issues

```bash
# Check DNS resolution
kubectl run -it --rm debug --image=nicolaka/netshoot --restart=Never -- \
  nslookup api-gateway.shoppermart.svc.cluster.local
```

## Monitoring and Observability

### Prometheus Metrics

Access Prometheus at: `http://prometheus:9090`

### Grafana Dashboards

Access Grafana at: `http://grafana:3000` (admin/admin)

### Loki Logs

Query logs through Grafana's Loki data source

### Tempo Traces

View distributed traces through Grafana's Tempo integration

## Advanced Configuration

### Helm Charts

Convert to Helm for better template management:

```bash
helm create shoppermart
# Move manifests to chart templates/
helm install shoppermart ./shoppermart -n shoppermart
```

### Service Mesh (Optional)

For advanced features like canary deployments and traffic management:

```bash
# Install Istio
istioctl install --set profile=demo -y

# Enable sidecar injection
kubectl label namespace shoppermart istio-injection=enabled
```

### Horizontal Pod Autoscaler

```bash
kubectl apply -f - <<EOF
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: api-gateway-hpa
  namespace: shoppermart
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: api-gateway
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
EOF
```

## Support and Documentation

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Spring Cloud on Kubernetes](https://spring.io/projects/spring-cloud-kubernetes)
- [Docker Documentation](https://docs.docker.com/)
