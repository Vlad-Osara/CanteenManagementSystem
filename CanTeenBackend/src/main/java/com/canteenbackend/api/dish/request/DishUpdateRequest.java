package com.canteenbackend.api.dish.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class DishUpdateRequest {
    @Pattern(regexp = "^$|\\s*\\S.*", message = "Có thể để trống nhưng không được chỉ chứa khoảng trắng hoặc rỗng")
    private String name;

    private String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "Giá món ăn không được nhỏ hơn 0đ")
    @Digits(integer = 9, fraction = 2, message = "Giá món ăn không hợp lệ (tối đa 9 chữ số phần nguyên và 2 chữ số thập phân)")
    private BigDecimal price;

    @NotNull
    private UUID categoryId;

    @Pattern(regexp = "^$|\\s*\\S.*", message = "Đường dẫn ảnh có thể bỏ trống nhưng không được chỉ chứa khoảng trắng hoặc rỗng")
    @URL(message = "Đường dẫn ảnh minh họa không đúng định dạng URL (vd: https://...)")
    private String imageUrl;

    private Boolean isAvailable;
}
