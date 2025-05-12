# Postman Collection cho Posting Service API

## Hướng dẫn sử dụng

### 1. Tải Postman Collection

Tạo một Postman Collection mới và import các request dưới đây:

### 2. Thiết lập Environment Variables

Tạo một Environment mới với các biến sau:
- `base_url`: http://localhost:8082/posting/api
- `token`: JWT token của bạn sau khi đăng nhập

### 3. Các Request mẫu

## Recruitment Post Requests

### 1. Create Recruitment Post
```
POST {{base_url}}/posts
Content-Type: application/json
Authorization: Bearer {{token}}

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

### 2. Update Recruitment Post
```
PUT {{base_url}}/posts/1
Content-Type: application/json
Authorization: Bearer {{token}}

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

### 3. Get Recruitment Post by ID
```
GET {{base_url}}/posts/1
```

### 4. Delete Recruitment Post
```
DELETE {{base_url}}/posts/1
Authorization: Bearer {{token}}
```

### 5. Search Posts
```
GET {{base_url}}/posts/search?title=trang điểm&makeupType=cô dâu&status=ACTIVE&page=0&size=10
```

### 6. Update Post Status
```
PATCH {{base_url}}/posts/1/status?status=EXPIRED
Authorization: Bearer {{token}}
```

## Application Request Requests

### 1. Create Application
```
POST {{base_url}}/applications
Content-Type: application/json
Authorization: Bearer {{token}}

{
  "recruitmentPostId": 1,
  "message": "Tôi có 3 năm kinh nghiệm làm trang điểm cô dâu và rất thích hợp với công việc này.",
  "portfolioImageUrls": [
    "https://example.com/portfolio1.jpg",
    "https://example.com/portfolio2.jpg"
  ]
}
```

### 2. Get Applications by Post ID
```
GET {{base_url}}/applications/post/1?page=0&size=10
Authorization: Bearer {{token}}
```

### 3. Get User Applications
```
GET {{base_url}}/applications/user
Authorization: Bearer {{token}}
```

### 4. Approve Application
```
PATCH {{base_url}}/applications/1/approve
Authorization: Bearer {{token}}
```

### 5. Reject Application
```
PATCH {{base_url}}/applications/1/reject
Authorization: Bearer {{token}}
```

## Booking Requests

### 1. Create Booking
```
POST {{base_url}}/bookings
Content-Type: application/json
Authorization: Bearer {{token}}

{
  "recruitmentPost": {
    "id": 1
  },
  "bookingTime": "2023-06-20T10:30:00",
  "location": "Cafe ABC, 123 Nguyễn Du, Quận 1, TP.HCM",
  "notes": "Vui lòng mang theo portfolio của bạn."
}
```

### 2. Get Bookings for User
```
GET {{base_url}}/bookings/user
Authorization: Bearer {{token}}
```

### 3. Confirm Booking
```
PATCH {{base_url}}/bookings/1/confirm
Authorization: Bearer {{token}}
```

### 4. Cancel Booking
```
PATCH {{base_url}}/bookings/1/cancel?reason=Bận lịch khác
Authorization: Bearer {{token}}
```

### 5. Complete Booking
```
PATCH {{base_url}}/bookings/1/complete
Authorization: Bearer {{token}}
```

## Thêm request vào collection

Để thêm các request trên vào collection:

1. Mở Postman
2. Tạo một Collection mới (Click vào "New" > "Collection")
3. Đặt tên là "Posting Service API"
4. Với mỗi request:
   - Click vào "Add request"
   - Đặt tên cho request
   - Copy URL, method và body từ các ví dụ trên
   - Lưu request

## Sử dụng Environment Variables

1. Tạo môi trường mới (Click vào "Environments" > "Create Environment")
2. Đặt tên là "Local Development"
3. Thêm các biến:
   - `base_url`: http://localhost:8082/posting/api
   - `token`: [JWT token của bạn sau khi đăng nhập]
4. Lưu môi trường
5. Chọn môi trường từ dropdown góc trên bên phải

## Sử dụng Scripts để quản lý token

Bạn có thể thêm một request đăng nhập và sử dụng Postman Scripts để tự động lưu token:

```
POST http://localhost:8082/auth/login
Content-Type: application/json

{
  "username": "your_username",
  "password": "your_password"
}
```

Thêm script sau vào tab "Tests":

```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("token", jsonData.token);
    console.log("Token saved to environment");
}
```

Sau khi gửi request đăng nhập, token sẽ tự động được lưu vào biến môi trường. 