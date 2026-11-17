package com.fitness.Exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Bắt các lỗi ResponseStatusException do lập trình viên chủ động ném ra (400, 404,...)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", ex.getStatusCode().value());
        body.put("message", ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    // 2. Bắt lỗi sai tham số hoặc vi phạm điều kiện nghiệp vụ
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 3. Bắt lỗi tài khoản bị khóa (status = LOCKED)
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Map<String, Object>> handleLockedAccount(LockedException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("message", "Tài khoản của bạn đã bị tạm khóa. Vui lòng liên hệ ban quản trị phòng gym!");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // 4. Bắt lỗi sai thông tin đăng nhập (email hoặc mật khẩu không đúng)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("message", "Email hoặc mật khẩu không chính xác!");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // 5. Bắt lỗi validate dữ liệu đầu vào (DTO vi phạm @NotBlank, @Email, @Size,...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());

        String defaultMsg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse("Dữ liệu đầu vào không hợp lệ");

        body.put("message", defaultMsg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 6. QUAN TRỌNG: Bắt lỗi phân quyền @PreAuthorize (trả về 403 Forbidden thay vì bị nuốt thành 500)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("message", "Bạn không có quyền truy cập vào chức năng này!");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // =========================================================================================
    // 7. FIX VULN-05: FALLBACK EXCEPTION HANDLER (CHẶN BẢO MẬT TUYỆT ĐỐI THEO CHUẨN OWASP)
    // =========================================================================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllUncaughtException(Exception ex) {
        // Ghi log chi tiết ra Console của Server để lập trình viên xem và debug
        log.error(">>> [SECURITY VULN-05 FIX] Đã chặn lỗi hệ thống chưa phân loại: ", ex);

        // Chỉ trả về thông điệp chung chung ra JSON cho người dùng bên ngoài, che giấu 100% chi tiết kỹ thuật
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("message", "Đã xảy ra sự cố trên hệ thống. Vui lòng thử lại sau!");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}