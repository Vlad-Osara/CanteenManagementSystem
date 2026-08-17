package com.canteenbackend.exceptions;

import com.canteenbackend.exceptions.custom.ResourceNotFoundException;
import com.canteenbackend.helper.base.response.ResponseObject;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Xử lý lỗi validate @Valid trong Request Body / ModelAttribute
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseObject<Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex) {
        // Gom danh sách các trường bị lỗi vào một Map cho frontend dễ check
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Không hợp lệ",
                        (existing, replacement) -> existing // Tránh trùng lặp key
                ));

        ResponseObject<Map<String, String>> responseBody = ResponseObject.<Map<String, String>>builder()
                .message("Dữ liệu gửi lên không hợp lệ!")
                .data(errors) // Danh sách lỗi nằm gọn trong data
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }

    // 2. Xử lý lỗi validate @RequestParam, @PathVariable
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseObject<String>> handleConstraintViolationException(ConstraintViolationException ex) {
        ResponseObject<String> responseBody = ResponseObject.<String>builder()
                .message("Vi phạm ràng buộc dữ liệu!")
                .data(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }

    // 3. Xử lý lỗi không tìm thấy tài nguyên (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseObject<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ResponseObject<Void> responseBody = ResponseObject.<Void>builder()
                .message(ex.getMessage()) // "Không tìm thấy danh mục này!"
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
    }

    // 4. Xử lý tất cả các lỗi hệ thống còn lại (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseObject<Void>> handleAllException(Exception ex) {
        //log lỗi thực tế bổ sung sau

        ResponseObject<Void> responseBody = ResponseObject.<Void>builder()
                .message("Đã có lỗi xảy ra từ phía hệ thống, vui lòng thử lại sau!")
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
    }

    // 5. Xử lý lỗi dữ liệu không hợp lệ do logic nghiệp vụ (400)
    @ExceptionHandler(com.canteenbackend.exceptions.custom.BadRequestException.class)
    public ResponseEntity<ResponseObject<Object>> handleBadRequestException(com.canteenbackend.exceptions.custom.BadRequestException ex) {
        ResponseObject<Object> responseBody = ResponseObject.<Object>builder()
                .message(ex.getMessage()) // Trả về tin nhắn lỗi như: "Mật khẩu cũ không chính xác"
                .data(ex.getData())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }

    // 6. Xử lý lỗi từ chối truy cập do sai phân quyền hoặc lỗi IDOR (403)
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ResponseObject<Void>> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex) {
        ResponseObject<Void> responseBody = ResponseObject.<Void>builder()
                .message(ex.getMessage() != null ? ex.getMessage() : "Bạn không có quyền truy cập vào tài nguyên này!")
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseBody);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseObject<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String message = "Dữ liệu gửi lên không đúng định dạng!";

        // Kiểm tra xem lỗi có phải do parse sai Enum hay không
        if (ex.getCause() instanceof InvalidFormatException ife) {
            if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {
                // Lấy ra danh sách các giá trị Enum hợp lệ để gợi ý cho Frontend luôn
                String validValues = Arrays.toString(ife.getTargetType().getEnumConstants());
                message = "Trạng thái không hợp lệ! Chỉ chấp nhận các giá trị: " + validValues;
            }
        }

        ResponseObject<Void> responseBody = ResponseObject.<Void>builder()
                .message(message)
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseObject<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        // Trích xuất nguyên nhân từ DB để phân tích xem bị trùng trường nào
        String customMessage = "Dữ liệu đã tồn tại trên hệ thống!";

        ResponseObject<Void> responseBody = ResponseObject.<Void>builder()
                .message(customMessage)
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }
}