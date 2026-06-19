#!/usr/bin/env bash
set -euo pipefail

APP_NAME="personal-cloud-sync"
DEPLOY_DIR="/opt/${APP_NAME}"
PROFILE="${1:-dev}"
LOG_LEVEL="${2:-INFO}"
HEALTH_URL="http://localhost:20000/actuator/health"

echo "1. Build jar..."
mvn clean package -DskipTests

echo "2. Check build jar..."
ls -lh target/*.jar

echo "3. Prepare deploy dir..."
sudo mkdir -p "${DEPLOY_DIR}"

echo "4. Copy jar and env file..."
sudo cp target/*.jar "${DEPLOY_DIR}/${APP_NAME}.jar"
sudo cp .env "${DEPLOY_DIR}/.env"

echo "5. Restart service..."
sudo systemctl daemon-reload
sudo systemctl enable "${APP_NAME}"
sudo systemctl restart "${APP_NAME}"

echo "6. Show service status..."
sudo systemctl status "${APP_NAME}" --no-pager

echo "7. Health check..."
sleep 3

for i in {1..30}; do
  if curl -fsS "${HEALTH_URL}" > /dev/null; then
    echo "Health check OK"
    echo "Deploy completed successfully"
    exit 0
  fi

  echo "Waiting for app... ($i/30)"
  sleep 2
done

echo "Health check failed. Recent logs:"
journalctl -u "${APP_NAME}" -n 100 --no-pager
exit 1