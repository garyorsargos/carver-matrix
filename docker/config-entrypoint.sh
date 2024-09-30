#!/bin/bash
set -e

source /app/.env

if [ ! -z "$PROFILE" ]; then
  cp /backend/src/resources/application.yml /backend/src/resources/application-${PROFILE}.yml
fi

exec "$@"