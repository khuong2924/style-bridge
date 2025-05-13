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

## User Registration with Avatar Upload

The auth-service now supports user avatar upload during registration. The avatar is uploaded to Cloudinary and the URL is stored in the user's profile.

### Registration Endpoint

```
POST /auth/signup
Content-Type: multipart/form-data
```

### Request Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| username | String | Yes | Unique username (3-20 characters) |
| email | String | Yes | Valid email address (max 50 characters) |
| password | String | Yes | Password (6-40 characters) |
| fullName | String | No | User's full name |
| phone | String | No | User's phone number |
| address | String | No | User's address |
| gender | String | No | User's gender |
| roles | Set<String> | No | User roles (defaults to ROLE_CUSTOMER) |
| avatar | File | No | User's profile image |

### Response

```json
{
  "message": "User registered successfully!"
}
```

Note: The avatar will be uploaded to Cloudinary and the URL will be stored in the user's profile. 

## Avatar Update

Users can update their profile avatar after registration using a dedicated endpoint.

### Avatar Update Endpoint

```
POST /users/avatar
Content-Type: multipart/form-data
Authorization: Bearer {jwt_token}
```

### Request Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| avatar | File | Yes | User's new profile image |

### Response

```json
{
  "id": 1,
  "username": "username",
  "email": "user@example.com",
  "fullName": "User Name",
  "phone": "1234567890",
  "address": "User Address",
  "gender": "Male",
  "avatarUrl": "https://res.cloudinary.com/decz34g1a/image/upload/v1234567890/abcdef.jpg",
  "roles": ["ROLE_CUSTOMER"]
}
```

Note: The avatar will be uploaded to Cloudinary and the URL will be stored in the user's profile. 