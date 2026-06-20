#!/usr/bin/env bash
set -euo pipefail

APP_NAME="${APP_NAME:-liontech-resorts}"
WAR_FILE="${WAR_FILE:-target/${APP_NAME}.war}"
TOMCAT_SERVICE="${TOMCAT_SERVICE:-tomcat10}"
CATALINA_HOME="${CATALINA_HOME:-}"

if [[ ! -f "$WAR_FILE" ]]; then
  echo "WAR file not found: $WAR_FILE"
  echo "Build it first with: mvn clean package"
  exit 1
fi

if [[ -n "$CATALINA_HOME" && -d "$CATALINA_HOME/webapps" ]]; then
  WEBAPPS_DIR="$CATALINA_HOME/webapps"
elif [[ -d /var/lib/tomcat10/webapps ]]; then
  WEBAPPS_DIR="/var/lib/tomcat10/webapps"
elif [[ -d /var/lib/tomcat9/webapps ]]; then
  WEBAPPS_DIR="/var/lib/tomcat9/webapps"
else
  echo "Tomcat webapps directory not found."
  echo "Set CATALINA_HOME, for example: export CATALINA_HOME=/opt/tomcat"
  exit 1
fi

if command -v systemctl >/dev/null 2>&1 && systemctl list-unit-files "$TOMCAT_SERVICE.service" >/dev/null 2>&1; then
  sudo systemctl stop "$TOMCAT_SERVICE" || true
fi

sudo rm -rf "$WEBAPPS_DIR/$APP_NAME" "$WEBAPPS_DIR/$APP_NAME.war"
sudo cp "$WAR_FILE" "$WEBAPPS_DIR/$APP_NAME.war"

if id tomcat >/dev/null 2>&1; then
  sudo chown tomcat:tomcat "$WEBAPPS_DIR/$APP_NAME.war"
elif id tomcat10 >/dev/null 2>&1; then
  sudo chown tomcat10:tomcat10 "$WEBAPPS_DIR/$APP_NAME.war"
fi

if command -v systemctl >/dev/null 2>&1 && systemctl list-unit-files "$TOMCAT_SERVICE.service" >/dev/null 2>&1; then
  sudo systemctl start "$TOMCAT_SERVICE"
fi

APP_URL="${APP_URL:-http://localhost:8080/$APP_NAME/}"
echo "Waiting for $APP_URL"

for attempt in {1..30}; do
  status="$(curl -k -s -o /dev/null -w '%{http_code}' "$APP_URL" || true)"
  if [[ "$status" == "200" || "$status" == "302" ]]; then
    echo "Application is available at $APP_URL"
    exit 0
  fi
  sleep 5
done

echo "Application did not become available at $APP_URL"
echo "Check Tomcat logs:"
echo "  sudo journalctl -u $TOMCAT_SERVICE -n 120 --no-pager"
echo "  sudo tail -n 120 /var/log/tomcat10/catalina.out"
exit 1
