FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
RUN apt-get update && apt-get install -y curl
COPY --from=build /app/target/posting-service-0.0.1-SNAPSHOT.jar app.jar

# Set default values for environment variables
ENV JWT_SECRET=${JWT_SECRET:-default_secret_key_do_not_use_in_production}
ENV JWT_EXPIRATION=${JWT_EXPIRATION:-86400000}
ENV SERVER_PORT=${SERVER_PORT:-8082}
ENV TZ=Asia/Ho_Chi_Minh
ENV REDIS_HOST=${REDIS_HOST:-redis}

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8082/posting/actuator/health || exit 1

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]

