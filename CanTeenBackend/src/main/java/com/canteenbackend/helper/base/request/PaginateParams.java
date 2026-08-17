package com.canteenbackend.helper.base.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

@Data
public class PaginateParams {

    @NotNull
    @Min(1)
    private Integer page = 1;

    @NotNull
    @Min(5)
    private Integer limit = 10;

    private String sortBy = "createdAt";
    private String sortOrder = "desc";
    private String search;
    private Boolean isPaginate = true;

    public Pageable toPageable() {
        int pageIndex = this.page > 0 ? this.page - 1 : 0;
        // Lấy tất cả
        if (Boolean.FALSE.equals(this.isPaginate)) {
            return Pageable.unpaged();
        }

        //Hướng sort
        Sort.Direction sortDirection = Sort.Direction.DESC;
        if(StringUtils.hasText(sortOrder) && "asc".equalsIgnoreCase(sortOrder.trim())) {
            sortDirection = Sort.Direction.ASC;
        }
        //Trường sort
        String field = StringUtils.hasText(sortBy) ? sortBy.trim() : "id";

        // Mặc định sắp xếp theo id giảm dần, có thể chế thêm tham số sort nếu muốn
        return PageRequest.of(pageIndex, this.limit, Sort.by(sortDirection, field));
    }
}
