# Hướng dẫn triển khai môi trường Production

Tài liệu này cung cấp hướng dẫn để triển khai ứng dụng trong môi trường production sử dụng Docker Compose.

## Chuẩn bị

### 1. Tạo file .env.prod

Tạo file `.env.prod` với nội dung sau và điều chỉnh các giá trị cho phù hợp:

```
# Database Configuration
POSTGRES_USER=postgres
POSTGRES_PASSWORD=strong_production_password

# RabbitMQ Configuration
RABBITMQ_USERNAME=rabbitmq
RABBITMQ_PASSWORD=strong_rabbitmq_password

# JWT Configuration
JWT_SECRET=your_very_strong_production_jwt_secret_key
JWT_EXPIRATION=86400000

# Docker Registry Configuration
DOCKER_REGISTRY=your-registry.example.com
IMAGE_TAG=1.0.0

# Domain Configuration
DOMAIN_NAME=your-domain.com

# Other Production Settings
SPRING_PROFILES_ACTIVE=prod
```

### 2. Chuẩn bị thư mục Traefik

```bash
mkdir -p traefik
touch traefik/acme.json
chmod 600 traefik/acme.json
```

### 3. Cập nhật cấu hình

Chỉnh sửa các thông tin sau trong các file cấu hình:

- Trong `traefik/traefik.toml`: Thay `your-email@domain.com` bằng email của bạn
- Trong `docker-compose.prod.yml`: Thay `your-email@domain.com` bằng email của bạn

## Build và đẩy Docker Images

```bash
# Build images
docker-compose -f docker-compose.yml build

# Tag images
docker tag auth-service:latest ${DOCKER_REGISTRY}/auth-service:${IMAGE_TAG}
docker tag posting-service:latest ${DOCKER_REGISTRY}/posting-service:${IMAGE_TAG}
docker tag style-bridge-fe:latest ${DOCKER_REGISTRY}/style-bridge-fe:${IMAGE_TAG}

# Push images
docker push ${DOCKER_REGISTRY}/auth-service:${IMAGE_TAG}
docker push ${DOCKER_REGISTRY}/posting-service:${IMAGE_TAG}
docker push ${DOCKER_REGISTRY}/style-bridge-fe:${IMAGE_TAG}
```

## Triển khai

### 1. Khởi động dịch vụ

```bash
# Sử dụng file .env.prod
docker-compose --env-file .env.prod -f docker-compose.prod.yml up -d
```

### 2. Kiểm tra trạng thái

```bash
docker-compose -f docker-compose.prod.yml ps
```

### 3. Xem logs

```bash
docker-compose -f docker-compose.prod.yml logs -f
```

## Những cải tiến trong môi trường Production

1. **Bảo mật**:
   - Sử dụng HTTPS với Let's Encrypt
   - Chuyển hướng HTTP sang HTTPS
   - Tắt chế độ debug và API dashboard của Traefik
   - Giới hạn các endpoints được expose

2. **Hiệu suất**:
   - Cấu hình giới hạn tài nguyên cho các container
   - Tối ưu cấu hình PostgreSQL và Redis
   - Tăng connection pool cho các dịch vụ

3. **Độ tin cậy**:
   - Thiết lập `restart: always` cho tất cả các dịch vụ
   - Tăng thời gian timeout và giảm tần suất kiểm tra health

4. **CI/CD**:
   - Sử dụng Docker Registry để lưu trữ và triển khai images
   - Sử dụng biến IMAGE_TAG để quản lý phiên bản

## Khôi phục và Sao lưu

### Sao lưu dữ liệu

```bash
# Sao lưu PostgreSQL
docker exec db1 pg_dump -U ${POSTGRES_USER} auth_db > auth_db_backup.sql
docker exec db2 pg_dump -U ${POSTGRES_USER} posting_db > posting_db_backup.sql

# Sao lưu volumes
docker run --rm -v pgdata1:/source -v $(pwd)/backups:/backup alpine tar -czf /backup/pgdata1.tar.gz /source
docker run --rm -v pgdata2:/source -v $(pwd)/backups:/backup alpine tar -czf /backup/pgdata2.tar.gz /source
docker run --rm -v rabbitmqdata:/source -v $(pwd)/backups:/backup alpine tar -czf /backup/rabbitmqdata.tar.gz /source
docker run --rm -v redisdata:/source -v $(pwd)/backups:/backup alpine tar -czf /backup/redisdata.tar.gz /source
```

### Khôi phục dữ liệu

```bash
# Khôi phục PostgreSQL
cat auth_db_backup.sql | docker exec -i db1 psql -U ${POSTGRES_USER} auth_db
cat posting_db_backup.sql | docker exec -i db2 psql -U ${POSTGRES_USER} posting_db

# Khôi phục volumes (thực hiện khi các container đã dừng)
docker run --rm -v pgdata1:/target -v $(pwd)/backups:/backup alpine sh -c "rm -rf /target/* && tar -xzf /backup/pgdata1.tar.gz -C /target --strip-components=1"
docker run --rm -v pgdata2:/target -v $(pwd)/backups:/backup alpine sh -c "rm -rf /target/* && tar -xzf /backup/pgdata2.tar.gz -C /target --strip-components=1"
docker run --rm -v rabbitmqdata:/target -v $(pwd)/backups:/backup alpine sh -c "rm -rf /target/* && tar -xzf /backup/rabbitmqdata.tar.gz -C /target --strip-components=1"
docker run --rm -v redisdata:/target -v $(pwd)/backups:/backup alpine sh -c "rm -rf /target/* && tar -xzf /backup/redisdata.tar.gz -C /target --strip-components=1"
``` 