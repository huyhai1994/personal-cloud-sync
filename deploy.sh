#!/usr/bin/env bash
set -euo pipefail

APP_NAME="personal-cloud-sync"
REGISTRY_HOST="localhost:5000"

PROFILE="${1:-staging}"
SERVER_PORT=20000
LOG_LEVEL="${2:-INFO}"
IMAGE_TAG="${3:-$(git rev-parse --short HEAD)}"

IMAGE="${REGISTRY_HOST}/${APP_NAME}:${IMAGE_TAG}"
HEALTH_URL="http://localhost:${SERVER_PORT}/actuator/health"

echo "=========================================="
echo "Deploying ${APP_NAME}"
echo "Image: ${IMAGE}"
echo "Profile: ${PROFILE}"
echo "Log level: ${LOG_LEVEL}"
echo "=========================================="

echo "1. Check local registry..."
curl -fsS "http://${REGISTRY_HOST}/v2/" > /dev/null
echo "Registry OK"

echo "2. Build jar..."
mvn clean package -DskipTests

echo "3. Build and push image..."
docker buildx build \
  -t "${IMAGE}" \
  --push .

echo "4. Pull and start container..."
export REGISTRY_HOST
export IMAGE_TAG
export SPRING_PROFILE_ACTIVE="${PROFILE}"
export LOG_LEVEL

docker compose pull
docker compose up -d

echo "5. Show status..."
docker compose ps

echo "6. Recent logs..."
docker compose logs --tail=50

echo "7. Health check..."
sleep 3

for i in {1..30}; do
  if curl -fsS "$HEALTH_URL" > /dev/null; then
    echo "Health check OK"
    echo "Deploy completed successfully"
    echo "Image: ${IMAGE}"
    exit 0
  fi

  echo "Waiting for app to be ready... ($i/30)"
  sleep 2
done

echo "Health check failed"
docker compose logs --tail=100
exit 1