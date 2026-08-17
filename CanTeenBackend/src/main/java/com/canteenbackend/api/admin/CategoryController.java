package com.canteenbackend.api.admin;

import com.canteenbackend.api.category.dto.CategoryDTO;
import com.canteenbackend.api.category.request.CategoryGetRequest;
import com.canteenbackend.api.category.request.CategoryStoreRequest;
import com.canteenbackend.api.category.request.CategoryUpdateRequest;
import com.canteenbackend.api.category.service.CategoryService;
import com.canteenbackend.helper.base.construct.RestfullController;
import com.canteenbackend.helper.base.response.ResponseObject;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/category")
public class CategoryController extends RestfullController<CategoryGetRequest, CategoryStoreRequest, CategoryUpdateRequest> {

    @Autowired
    private CategoryService categoryService;

    @Override
    public ResponseEntity<?> getPaginate(@Valid @ModelAttribute CategoryGetRequest categoryGetRequest) {
        Page<CategoryDTO> categoryPage = categoryService.getAll(categoryGetRequest);
        return ResponseEntity.ok(ResponseObject.success("Lấy danh sách danh mục thành công", categoryPage));
    }

    @Override
    public ResponseEntity<?> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ResponseObject.success("Lấy danh mục thành công", categoryService.get(id)));
    }

    @Override
    public ResponseEntity<?> store(@RequestBody @Valid CategoryStoreRequest categoryStoreRequest) {

        return ResponseEntity.ok(ResponseObject.success("Thêm mới danh mục thành công", categoryService.store(categoryStoreRequest)));
    }

    @Override
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody @Valid CategoryUpdateRequest categoryUpdateRequest) {
        return ResponseEntity.ok(ResponseObject.success("Cập nhật danh mục thành công", categoryService.update(id, categoryUpdateRequest)));
    }

    @Override
    public ResponseEntity<?> destroy(@PathVariable UUID id) {
        return ResponseEntity.ok(ResponseObject.success("Xóa danh mục thành công", categoryService.destroy(id)));
    }
}
