#!/bin/bash
set -euo pipefail

REPOSITORY=/home/ec2-user/app
NGINX_INC=/etc/nginx/conf.d/service-url.inc

# 현재 Nginx가 바라보는 포트 읽기
CURRENT_PORT=$(grep -Po '[0-9]+' "$NGINX_INC" | tail -1 || echo 8081)
TARGET_PORT=8082
if [ "$CURRENT_PORT" -eq 8082 ]; then TARGET_PORT=8081; fi

echo "> CURRENT_PORT=$CURRENT_PORT, TARGET_PORT=$TARGET_PORT"

# 최신 JAR 선택 후 고정 경로로 심볼릭 링크
LATEST_JAR=$(ls -t "$REPOSITORY"/*.jar | head -1)
mkdir -p "$REPOSITORY/current"
ln -sf "$LATEST_JAR" "$REPOSITORY/current/app.jar"
chmod +x "$REPOSITORY/current/app.jar"
echo "> Linked $(basename "$LATEST_JAR") -> current/app.jar"

# 템플릿 유닛 설치 여부 확인 (없으면 오류)
if [ ! -f /etc/systemd/system/donggurami@.service ]; then
  echo "ERROR: /etc/systemd/system/donggurami@.service not found."; exit 1
fi

# 데몬 리로드
sudo systemctl daemon-reload

# 대상 포트 인스턴스 시작(있으면 재시작)
if systemctl is-active --quiet "donggurami@${TARGET_PORT}"; then
  echo "> Restart donggurami@${TARGET_PORT}"
  sudo systemctl restart "donggurami@${TARGET_PORT}"
else
  echo "> Start donggurami@${TARGET_PORT}"
  sudo systemctl start "donggurami@${TARGET_PORT}"
fi

# 부팅 자동시작: 새 포트 enable, 이전 포트 disable
sudo systemctl enable "donggurami@${TARGET_PORT}"
sudo systemctl disable "donggurami@${CURRENT_PORT}" || true

echo "> Started donggurami@${TARGET_PORT} (jar: $(basename "$LATEST_JAR"))"
