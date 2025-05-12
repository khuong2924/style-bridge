#!/bin/bash
set -e

echo "Environment Configuration:"
echo "POSTGRES_HOST: ${POSTGRES_HOST}"
echo "POSTGRES_PORT: ${POSTGRES_PORT}"
echo "POSTGRES_DB: ${POSTGRES_DB}"
echo "POSTGRES_USER: ${POSTGRES_USER}"

echo "Waiting for PostgreSQL to start on ${POSTGRES_HOST}:${POSTGRES_PORT}..."
# Wait for PostgreSQL to be available
until PGPASSWORD=$POSTGRES_PASSWORD psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -c '\q' >/dev/null 2>&1; do
  echo "PostgreSQL is unavailable - sleeping"
  sleep 2
done

echo "PostgreSQL is up - checking if database exists"

# Check if database exists
PGPASSWORD=$POSTGRES_PASSWORD psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -lqt | cut -d \| -f 1 | grep -qw $POSTGRES_DB
if [ $? -ne 0 ]; then
  echo "Database $POSTGRES_DB does not exist. Creating..."
  PGPASSWORD=$POSTGRES_PASSWORD psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -c "CREATE DATABASE $POSTGRES_DB;"
  echo "Database created successfully"
else
  echo "Database $POSTGRES_DB already exists"
fi

echo "JWT_SECRET is set: ${JWT_SECRET:0:10}..."

echo "Starting Java application..."
exec java -jar app.jar 