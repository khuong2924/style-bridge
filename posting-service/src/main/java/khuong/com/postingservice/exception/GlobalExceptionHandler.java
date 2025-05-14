package khuong.com.postingservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.LazyInitializationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        log.error("Unsupported media type: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                "Unsupported Media Type",
                "Please ensure you're using 'multipart/form-data' for file uploads. " + ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Access denied",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid argument",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        log.error("Invalid state: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Invalid state",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<Object> handleDateTimeParseException(DateTimeParseException ex) {
        log.error("Date parse error: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Invalid Date Format");
        body.put("message", "The provided date could not be parsed. Use ISO format (yyyy-MM-ddTHH:mm:ss)");
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.error("File size exceeded: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("error", "File Size Exceeded");
        body.put("message", "The uploaded file exceeds the maximum allowed size.");
        return new ResponseEntity<>(body, HttpStatus.PAYLOAD_TOO_LARGE);
    }
    
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Object> handleMultipartException(MultipartException ex) {
        log.error("Multipart error: {}", ex.getMessage(), ex);
        Map<String, Object> body = new HashMap<>();
        body.put("error", "File Upload Error");
        body.put("message", "There was an error processing the uploaded file(s).");
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(IOException.class)
    public ResponseEntity<Object> handleIOException(IOException ex) {
        log.error("IO error: {}", ex.getMessage(), ex);
        Map<String, Object> body = new HashMap<>();
        body.put("error", "File Processing Error");
        body.put("message", "There was an error processing the uploaded file(s).");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage(), ex);
        
        String errorMessage = ex.getMessage();
        String userFriendlyMessage = "Data constraint violation occurred";
        
        // Extract more specific message for common constraints
        if (errorMessage.contains("ngay_gio_hen")) {
            userFriendlyMessage = "Booking date (ngay_gio_hen) cannot be null";
        } else if (errorMessage.contains("ma_bai_dang_tuyen")) {
            userFriendlyMessage = "Recruitment post ID (ma_bai_dang_tuyen) cannot be null";
        } else if (errorMessage.contains("ma_nguoi_dung_khach_hang")) {
            userFriendlyMessage = "Client user ID (ma_nguoi_dung_khach_hang) cannot be null";
        }
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Data validation error");
        body.put("message", userFriendlyMessage);
        body.put("timestamp", LocalDateTime.now());
        
        if (log.isDebugEnabled()) {
            body.put("exception", ex.getClass().getName());
            body.put("detail", errorMessage);
        }
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        // Log the full stack trace for debugging
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Server error");
        body.put("message", "An unexpected error occurred");
        body.put("timestamp", LocalDateTime.now());
        
        // Add more details in development environment
        if (log.isDebugEnabled()) {
            body.put("exception", ex.getClass().getName());
            body.put("detail", ex.getMessage());
        }
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(LazyInitializationException.class)
    public ResponseEntity<Map<String, Object>> handleLazyInitializationException(LazyInitializationException ex) {
        log.error("Lazy initialization exception", ex);
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Data access error");
        body.put("message", "Could not initialize lazy data");
        body.put("timestamp", LocalDateTime.now());
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotWritableException(HttpMessageNotWritableException ex) {
        log.error("HTTP message not writable exception", ex);
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Data serialization error");
        body.put("message", "Không thể chuyển đổi dữ liệu thành JSON");
        body.put("timestamp", LocalDateTime.now());
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Error response class
    public static class ErrorResponse {
        private int status;
        private String error;
        private String message;

        public ErrorResponse(int status, String error, String message) {
            this.status = status;
            this.error = error;
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public String getError() {
            return error;
        }

        public String getMessage() {
            return message;
        }
    }
}