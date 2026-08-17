package com.canteenbackend.api.admin;

import com.canteenbackend.api.user.dto.UserDTO;
import com.canteenbackend.api.user.request.UserGetRequest;
import com.canteenbackend.api.user.request.UserStoreRequest;
import com.canteenbackend.api.user.request.UserUpdateRequest;
import com.canteenbackend.api.user.service.UserService;
import com.canteenbackend.helper.base.construct.RestfullController;
import com.canteenbackend.helper.base.response.ResponseObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/account")
public class AccountController extends RestfullController<UserGetRequest, UserStoreRequest, UserUpdateRequest> {
    private final UserService userService;
    @Override
    public ResponseEntity<?> getPaginate(UserGetRequest userGetRequest) {
        return ResponseEntity.ok(ResponseObject.success("Lấy danh sách người dùng hệ thống thành công", userService.getAll(userGetRequest)));
    }

    @Override
    public ResponseEntity<?> get(UUID id) {
        return ResponseEntity.ok(ResponseObject.success("Lấy người dùng thành công", userService.get(id)));
    }

    @Override
    public ResponseEntity<?> store(UserStoreRequest userStoreRequest) {
        UserDTO userDTO = userService.store(userStoreRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(userDTO.getId())
                .toUri();
        return ResponseEntity
                .created(location)
                .body(ResponseObject.success("Thêm người dùng thành công", userDTO));
    }

    @Override
    public ResponseEntity<?> update(UUID id, UserUpdateRequest userUpdateRequest) {
        return ResponseEntity.ok(ResponseObject.success("Cập nhật thông tin người dùng thành công", userService.update(id, userUpdateRequest)));
    }

    @Override
    public ResponseEntity<?> destroy(UUID id) {
        return ResponseEntity.ok(ResponseObject.success("xóa người dùng thành công", userService.destroy(id)));
    }

}
