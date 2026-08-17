package com.canteenbackend.api.admin;

import com.canteenbackend.api.dish.request.DishGetRequest;
import com.canteenbackend.api.dish.request.DishStoreRequest;
import com.canteenbackend.api.dish.request.DishUpdateRequest;
import com.canteenbackend.api.dish.service.DishService;
import com.canteenbackend.helper.base.construct.RestfullController;
import com.canteenbackend.helper.base.response.ResponseObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/dish")
@RequiredArgsConstructor
public class DishController extends RestfullController<DishGetRequest, DishStoreRequest, DishUpdateRequest> {
    private final DishService dishService;

    @Override
    public ResponseEntity<?> getPaginate(DishGetRequest dishGetRequest) {
        return ResponseEntity.ok(ResponseObject.success("Lấy danh sách món ăn thành công", dishService.getAll(dishGetRequest)));
    }

    @Override
    public ResponseEntity<?> get(UUID id) {
        return ResponseEntity.ok(ResponseObject.success("Lấy món ăn thành công", dishService.get(id)));
    }

    @Override
    public ResponseEntity<?> store(DishStoreRequest dishStoreRequest) {
        return ResponseEntity.ok(ResponseObject.success("Lưu món ăn thành công", dishService.store(dishStoreRequest)));
    }

    @Override
    public ResponseEntity<?> update(UUID id, DishUpdateRequest dishUpdateRequest) {
        return ResponseEntity.ok(ResponseObject.success("Cập nhật món ăn thành công", dishService.update(id, dishUpdateRequest)));
    }

    @Override
    public ResponseEntity<?> destroy(UUID id) {
        return ResponseEntity.ok(ResponseObject.success("Xóa món ăn thành công", dishService.destroy(id)));
    }
}
