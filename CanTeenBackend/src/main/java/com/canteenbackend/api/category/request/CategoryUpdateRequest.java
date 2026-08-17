package com.canteenbackend.api.category.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CategoryUpdateRequest {
    @Pattern(regexp = "^$|\\s*\\S.*", message = "Tên có thể để trống nhưng không được chỉ chứa khoảng trắng hoặc rỗng")
    private String name;

    private String description;
}
