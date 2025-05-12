#!/bin/bash

# Load environment variables from env.properties
export $(grep -v '^#' env.properties | xargs)

# Build the Docker image
docker build -t posting-service .

# Run the container with environment variables
docker run -p 8082:8082 \
  -e JWT_SECRET="$JWT_SECRET" \
  -e JWT_EXPIRATION="$JWT_EXPIRATION" \
  -e SERVER_PORT="$SERVER_PORT" \
  -e REDIS_HOST="$REDIS_HOST" \
  posting-service 