# Auth Service

This is a Spring Boot authentication service that uses PostgreSQL for data storage and JWT for authentication.

## Prerequisites

- Docker and Docker Compose
- Java 17 (for local development)
- Maven (for local development)

## Configuration

The following environment variables can be configured:

| Variable | Default | Description |
|----------|---------|-------------|
| POSTGRES_HOST | localhost | PostgreSQL host |
| POSTGRES_PORT | 5434 | PostgreSQL port |
| POSTGRES_USER | postgres | PostgreSQL username |
| POSTGRES_PASSWORD | 123456 | PostgreSQL password |
| POSTGRES_DB | auth_db | PostgreSQL database name |
| JWT_SECRET | (preset value) | Secret key for JWT token generation |
| JWT_EXPIRATION | 86400000 | JWT token expiration time in milliseconds (default: 24 hours) |
| SERVER_PORT | 8081 | Application server port |

## Running with Docker

1. Make sure Docker and Docker Compose are installed
2. Clone this repository
3. Navigate to the project directory
4. Run the application with Docker Compose:

```bash
docker-compose up -d
```

This will:
- Start a PostgreSQL database on port 5434
- Build and start the Auth Service
- Create the database if it doesn't exist
- Initialize the application

## Accessing the Application

The application will be available at:
```
http://localhost:8081/auth
```

Health check endpoint:
```
http://localhost:8081/auth/actuator/health
```

## Local Development

1. Make sure PostgreSQL is running and the database `auth_db` exists on port 5434
2. Configure the application.properties file if needed
3. Run the application:

```bash
mvn spring-boot:run
```

## Troubleshooting

### Database Connection Issues

If you encounter database connection issues, ensure that:
- PostgreSQL is running on port 5434
- The database `auth_db` exists
- The connection details are correct

You can manually create the database:

```bash
psql -U postgres -p 5434
CREATE DATABASE auth_db;
```

### JWT Configuration Issues

If you encounter JWT configuration issues, ensure that:
- The `app.jwt.secret` property is correctly set in application.properties
- The JWT_SECRET environment variable is set when running in Docker 