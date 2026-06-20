#!/usr/bin/env bash
set -euo pipefail

APP_NAME="${APP_NAME:-liontech-resorts}"
WAR_FILE="${WAR_FILE:-target/${APP_NAME}.war}"
TOMCAT_SERVICE="${TOMCAT_SERVICE:-tomcat10}"
CATALINA_HOME="${CATALINA_HOME:-}"
DATA_DIR="${LIONTECH_DATA_DIR:-/var/lib/liontech-resorts/data}"
DB_URL="${LIONTECH_DB_URL:-jdbc:h2:file:${DATA_DIR}/liontech-resorts;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH}"
DB_USERNAME="${LIONTECH_DB_USERNAME:-sa}"
DB_PASSWORD="${LIONTECH_DB_PASSWORD:-}"
JAVA_HOME_OVERRIDE="${JAVA_HOME_OVERRIDE:-}"

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

if id tomcat >/dev/null 2>&1; then
  TOMCAT_USER="${TOMCAT_USER:-tomcat}"
elif id tomcat10 >/dev/null 2>&1; then
  TOMCAT_USER="${TOMCAT_USER:-tomcat10}"
else
  TOMCAT_USER="${TOMCAT_USER:-}"
fi

if [[ -n "$TOMCAT_USER" ]]; then
  TOMCAT_GROUP="${TOMCAT_GROUP:-$(id -gn "$TOMCAT_USER")}"
  sudo install -d -o "$TOMCAT_USER" -g "$TOMCAT_GROUP" -m 0750 "$DATA_DIR"
else
  sudo install -d -m 0750 "$DATA_DIR"
fi

if [[ -z "$JAVA_HOME_OVERRIDE" ]]; then
  for candidate in /usr/lib/jvm/java-21-openjdk-amd64 /usr/lib/jvm/java-17-openjdk-amd64; do
    if [[ -x "$candidate/bin/java" ]]; then
      JAVA_HOME_OVERRIDE="$candidate"
      break
    fi
  done
fi

service_exists=false
if command -v systemctl >/dev/null 2>&1 && systemctl list-unit-files "$TOMCAT_SERVICE.service" >/dev/null 2>&1; then
  service_exists=true
fi

if [[ "$service_exists" == "true" ]]; then
  sudo systemctl stop "$TOMCAT_SERVICE" || true
  sudo install -d -m 0755 "/etc/systemd/system/$TOMCAT_SERVICE.service.d"
  {
    echo "[Service]"
    echo "Environment=\"LIONTECH_DB_URL=$DB_URL\""
    echo "Environment=\"LIONTECH_DB_USERNAME=$DB_USERNAME\""
    echo "Environment=\"LIONTECH_DB_PASSWORD=$DB_PASSWORD\""
    if [[ -n "$JAVA_HOME_OVERRIDE" ]]; then
      echo "Environment=\"JAVA_HOME=$JAVA_HOME_OVERRIDE\""
    fi
  } | sudo tee "/etc/systemd/system/$TOMCAT_SERVICE.service.d/liontech-resorts.conf" >/dev/null
  sudo systemctl daemon-reload
fi

sudo rm -rf "$WEBAPPS_DIR/$APP_NAME" "$WEBAPPS_DIR/$APP_NAME.war"
sudo cp "$WAR_FILE" "$WEBAPPS_DIR/$APP_NAME.war"

if [[ -n "${TOMCAT_USER:-}" ]]; then
  sudo chown "$TOMCAT_USER:$TOMCAT_GROUP" "$WEBAPPS_DIR/$APP_NAME.war"
fi

if [[ "$service_exists" == "true" ]]; then
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
