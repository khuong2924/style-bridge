# Posting Service API Documentation

## Base URL
```
http://localhost:8082/posting/api
```

## Authentication
API sử dụng JWT token để xác thực. Token cần được đính kèm trong header của request:
```
Authorization: Bearer {jwt_token}
```

## Recruitment Posts API

### Create Recruitment Post
Tạo một bài đăng tuyển dụng mới.

- **URL**: `/posts`
- **Method**: `POST`
- **Auth required**: Yes

**Request Body**:
```json
{
  "title": "Cần tìm nghệ sĩ trang điểm cho đám cưới",
  "makeupType": "Trang điểm cô dâu",
  "startTime": "2023-07-15T09:00:00",
  "expectedDuration": "3 giờ",
  "address": "12 Nguyễn Huệ, Quận 1, TP.HCM",
  "hiringType": "Trọn gói",
  "compensation": "1,500,000 VND",
  "quantity": 1,
  "description": "Cần tìm nghệ sĩ trang điểm có kinh nghiệm làm việc với cô dâu, phong cách tự nhiên.",
  "deadline": "2023-07-01T23:59:59",
  "imageUrls": [
    "https://example.com/image1.jpg",
    "https://example.com/image2.jpg"
  ]
}
```

**Success Response**:
- **Code**: 201 CREATED
- **Content**:
```json
{
  "id": 1,
  "postedAt": "2023-06-15T10:30:00",
  "title": "Cần tìm nghệ sĩ trang điểm cho đám cưới",
  "makeupType": "Trang điểm cô dâu",
  "startTime": "2023-07-15T09:00:00",
  "expectedDuration": "3 giờ",
  "address": "12 Nguyễn Huệ, Quận 1, TP.HCM",
  "hiringType": "Trọn gói",
  "compensation": "1,500,000 VND",
  "quantity": 1,
  "description": "Cần tìm nghệ sĩ trang điểm có kinh nghiệm làm việc với cô dâu, phong cách tự nhiên.",
  "deadline": "2023-07-01T23:59:59",
  "status": "ACTIVE",
  "posterUserId": 123
}
```

### Update Recruitment Post
Cập nhật một bài đăng tuyển dụng.

- **URL**: `/posts/{postId}`
- **Method**: `PUT`
- **Auth required**: Yes

**URL Params**:
- `postId`: ID của bài đăng cần cập nhật

**Request Body**:
```json
{
  "title": "Cần gấp nghệ sĩ trang điểm cho đám cưới",
  "makeupType": "Trang điểm cô dâu",
  "startTime": "2023-07-15T10:00:00",
  "expectedDuration": "4 giờ",
  "address": "12 Nguyễn Huệ, Quận 1, TP.HCM",
  "hiringType": "Trọn gói",
  "compensation": "2,000,000 VND",
  "quantity": 1,
  "description": "Cần tìm nghệ sĩ trang điểm có kinh nghiệm làm việc với cô dâu, phong cách tự nhiên, hiện đại.",
  "deadline": "2023-07-01T23:59:59"
}
```

**Success Response**:
- **Code**: 200 OK
- **Content**: Bài đăng đã được cập nhật

### Get Recruitment Post by ID
Lấy thông tin chi tiết của một bài đăng tuyển dụng.

- **URL**: `/posts/{postId}`
- **Method**: `GET`
- **Auth required**: No

**URL Params**:
- `postId`: ID của bài đăng cần lấy thông tin

**Success Response**:
- **Code**: 200 OK
- **Content**: Thông tin chi tiết bài đăng

### Delete Recruitment Post
Xóa một bài đăng tuyển dụng.

- **URL**: `/posts/{postId}`
- **Method**: `DELETE`
- **Auth required**: Yes

**URL Params**:
- `postId`: ID của bài đăng cần xóa

**Success Response**:
- **Code**: 204 NO CONTENT

### Search Posts
Tìm kiếm bài đăng theo nhiều tiêu chí.

- **URL**: `/posts/search`
- **Method**: `GET`
- **Auth required**: No

**Query Params**:
- `title` (optional): Từ khóa tìm trong tiêu đề
- `makeupType` (optional): Loại trang điểm
- `status` (optional): Trạng thái bài đăng
- `startDate` (optional): Thời gian bắt đầu (ISO format)
- `endDate` (optional): Thời gian kết thúc (ISO format)
- `page` (default: 0): Trang
- `size` (default: 10): Số lượng kết quả mỗi trang

**Success Response**:
- **Code**: 200 OK
- **Content**: Danh sách các bài đăng phù hợp với tiêu chí

### Update Post Status
Cập nhật trạng thái của bài đăng.

- **URL**: `/posts/{postId}/status`
- **Method**: `PATCH`
- **Auth required**: Yes

**URL Params**:
- `postId`: ID của bài đăng cần cập nhật

**Query Params**:
- `status`: Trạng thái mới (ACTIVE, EXPIRED, CANCELLED)

**Success Response**:
- **Code**: 204 NO CONTENT

## Application Request API

### Create Application
Tạo một yêu cầu ứng tuyển mới.

- **URL**: `/applications`
- **Method**: `POST`
- **Auth required**: Yes

**Request Body**:
```json
{
  "recruitmentPostId": 1,
  "message": "Tôi có 3 năm kinh nghiệm làm trang điểm cô dâu và rất thích hợp với công việc này.",
  "portfolioImageUrls": [
    "https://example.com/portfolio1.jpg",
    "https://example.com/portfolio2.jpg"
  ]
}
```

**Success Response**:
- **Code**: 201 CREATED
- **Content**: Thông tin ứng tuyển đã tạo

### Get Applications by Post ID
Lấy danh sách các ứng tuyển cho một bài đăng.

- **URL**: `/applications/post/{postId}`
- **Method**: `GET`
- **Auth required**: Yes

**URL Params**:
- `postId`: ID của bài đăng

**Query Params**:
- `page` (default: 0): Trang
- `size` (default: 10): Số lượng kết quả mỗi trang

**Success Response**:
- **Code**: 200 OK
- **Content**: Danh sách các ứng tuyển

### Get User Applications
Lấy danh sách các ứng tuyển của người dùng hiện tại.

- **URL**: `/applications/user`
- **Method**: `GET`
- **Auth required**: Yes

**Success Response**:
- **Code**: 200 OK
- **Content**: Danh sách các ứng tuyển của người dùng

### Approve Application
Chấp nhận một đơn ứng tuyển.

- **URL**: `/applications/{applicationId}/approve`
- **Method**: `PATCH`
- **Auth required**: Yes

**URL Params**:
- `applicationId`: ID của đơn ứng tuyển

**Success Response**:
- **Code**: 204 NO CONTENT

### Reject Application
Từ chối một đơn ứng tuyển.

- **URL**: `/applications/{applicationId}/reject`
- **Method**: `PATCH`
- **Auth required**: Yes

**URL Params**:
- `applicationId`: ID của đơn ứng tuyển

**Success Response**:
- **Code**: 204 NO CONTENT

## Booking API

### Create Booking
Tạo một lịch hẹn phỏng vấn mới.

- **URL**: `/bookings`
- **Method**: `POST`
- **Auth required**: Yes

**Request Body**:
```json
{
  "recruitmentPost": {
    "id": 1
  },
  "bookingTime": "2023-06-20T10:30:00",
  "location": "Cafe ABC, 123 Nguyễn Du, Quận 1, TP.HCM",
  "notes": "Vui lòng mang theo portfolio của bạn."
}
```

**Success Response**:
- **Code**: 201 CREATED
- **Content**: Thông tin lịch hẹn đã tạo

### Get Bookings for User
Lấy danh sách lịch hẹn của người dùng hiện tại.

- **URL**: `/bookings/user`
- **Method**: `GET`
- **Auth required**: Yes

**Success Response**:
- **Code**: 200 OK
- **Content**: Danh sách lịch hẹn

### Confirm Booking
Xác nhận một lịch hẹn.

- **URL**: `/bookings/{bookingId}/confirm`
- **Method**: `PATCH`
- **Auth required**: Yes

**URL Params**:
- `bookingId`: ID của lịch hẹn

**Success Response**:
- **Code**: 204 NO CONTENT

### Cancel Booking
Hủy một lịch hẹn.

- **URL**: `/bookings/{bookingId}/cancel`
- **Method**: `PATCH`
- **Auth required**: Yes

**URL Params**:
- `bookingId`: ID của lịch hẹn

**Query Params**:
- `reason`: Lý do hủy lịch hẹn

**Success Response**:
- **Code**: 204 NO CONTENT

### Complete Booking
Đánh dấu một lịch hẹn đã hoàn thành.

- **URL**: `/bookings/{bookingId}/complete`
- **Method**: `PATCH`
- **Auth required**: Yes

**URL Params**:
- `bookingId`: ID của lịch hẹn

**Success Response**:
- **Code**: 204 NO CONTENT

## Mã lỗi

- **400 Bad Request**: Request không hợp lệ
- **401 Unauthorized**: Chưa xác thực
- **403 Forbidden**: Không có quyền thực hiện hành động
- **404 Not Found**: Không tìm thấy tài nguyên
- **500 Internal Server Error**: Lỗi server

## Ví dụ sử dụng với cURL

### Tạo bài đăng tuyển dụng
```bash
curl -X POST http://localhost:8082/posting/api/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {jwt_token}" \
  -d '{
    "title": "Cần tìm nghệ sĩ trang điểm cho đám cưới",
    "makeupType": "Trang điểm cô dâu",
    "startTime": "2023-07-15T09:00:00",
    "expectedDuration": "3 giờ",
    "address": "12 Nguyễn Huệ, Quận 1, TP.HCM",
    "hiringType": "Trọn gói",
    "compensation": "1,500,000 VND",
    "quantity": 1,
    "description": "Cần tìm nghệ sĩ trang điểm có kinh nghiệm làm việc với cô dâu, phong cách tự nhiên.",
    "deadline": "2023-07-01T23:59:59"
  }
```

### Tìm kiếm bài đăng
```bash
curl -X GET "http://localhost:8082/posting/api/posts/search?makeupType=Trang%20điểm%20cô%20dâu&status=ACTIVE&page=0&size=10" \
  -H "Content-Type: application/json"
```

### Gửi đơn ứng tuyển
```bash
curl -X POST http://localhost:8082/posting/api/applications \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {jwt_token}" \
  -d '{
    "recruitmentPostId": 1,
    "message": "Tôi có 3 năm kinh nghiệm làm trang điểm cô dâu và rất thích hợp với công việc này."
  }'
```

### Tạo lịch hẹn
```bash
curl -X POST http://localhost:8082/posting/api/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {jwt_token}" \
  -d '{
    "recruitmentPost": {
      "id": 1
    },
    "bookingTime": "2023-06-20T10:30:00",
    "location": "Cafe ABC, 123 Nguyễn Du, Quận 1, TP.HCM",
    "notes": "Vui lòng mang theo portfolio của bạn."
  }'
``` 