#!/bin/bash

REPOSITORY=/home/ec2-user/app
CURRENT_PORT=$(cat /etc/nginx/conf.d/service-url.inc 2>/dev/null | grep -Po '[0-9]+' | tail -1)
TARGET_PORT=0

if [ "${CURRENT_PORT}" = "8081" ]; then
    TARGET_PORT=8082
elif [ "${CURRENT_PORT}" = "8082" ]; then
    TARGET_PORT=8081
else
    echo "> No WAS is connected to nginx. Falling back to target-port or default 8081."
    if [ -f "$REPOSITORY/target-port" ]; then
        TARGET_PORT=$(cat "$REPOSITORY/target-port")
    else
        TARGET_PORT=8081
    fi
fi

echo "> Start health check of WAS at 'http://127.0.0.1:${TARGET_PORT}' ..."

for RETRY_COUNT in {1..10}
do
    echo "> #${RETRY_COUNT} trying..."
    RESPONSE_CODE=$(curl -s -o /dev/null -w "%{http_code}"  http://127.0.0.1:${TARGET_PORT}/health-check)

    if [ ${RESPONSE_CODE} -eq 200 ]; then
        echo "> New WAS successfully running"
        exit 0
    elif [ ${RETRY_COUNT} -eq 10 ]; then
        echo "> Health check failed."
        exit 1
    fi
    sleep 10
done