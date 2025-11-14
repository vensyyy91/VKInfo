#!/bin/bash
set -e

POSTGRES_PASSWORD=${POSTGRES_PASSWORD:-qwerty}
JWT_SECRET=${JWT_SECRET:-VG75QcmfGUWwtCDhk7SByBfTeQEdRSk9GHpF6gfyHShepd8t6VvX4HrhTmNn7pbG}
ADMIN_PASSWORD=${ADMIN_PASSWORD:-qwerty123}

minikube image build -t vk-info:latest .

kubectl apply -f k8s/namespace.yml

echo "Creating secrets"
kubectl create secret generic vk-info-secrets \
  --from-literal=POSTGRES_PASSWORD="$POSTGRES_PASSWORD" \
  --from-literal=JWT_SECRET="$JWT_SECRET" \
  --from-literal=ADMIN_PASSWORD="$ADMIN_PASSWORD" \
  -n vk-info --dry-run=client -o yaml | kubectl apply -f -

echo "Configmap creating"
kubectl apply -f k8s/configmap.yml

echo "Deploying PostgreSQL"
kubectl apply -f k8s/postgres-deployment.yml
kubectl apply -f k8s/postgres-service.yml

kubectl wait --for=condition=ready pod -l app=postgres -n vk-info --timeout=60s
echo "PostgreSQL deployed"

echo "Deploying VK-Info application"
kubectl apply -f k8s/app-deployment.yml
kubectl apply -f k8s/app-service.yml

echo "Application starting..."
kubectl wait --for=condition=available deployment/vk-info-app -n vk-info --timeout=120s
echo "VK-Info application deployed"

echo "Current pods status:"
kubectl get pods -n vk-info