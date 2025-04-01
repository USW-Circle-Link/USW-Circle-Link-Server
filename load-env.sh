#!/usr/bin/env bash

ENV_FILE=".env"

if [ ! -f "$ENV_FILE" ]; then
  echo "❌ $ENV_FILE 이 없습니다."
  exit 1
fi

echo "✅ $ENV_FILE 에서 환경 변수 로딩중 ..."
set -a
source "$ENV_FILE"
set +a