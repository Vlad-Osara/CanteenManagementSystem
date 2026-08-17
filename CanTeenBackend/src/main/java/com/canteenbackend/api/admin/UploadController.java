package com.canteenbackend.api.admin;

import com.canteenbackend.helper.upload.image.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/upload/img")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadDishImage(@RequestParam("file") MultipartFile file) {
        // Lưu ảnh vào thư mục 'canteen/dishes' trên Cloudinary
        String imageUrl = cloudinaryService.uploadImage(file, "canteen/dishes");

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Tải ảnh lên thành công!",
                "url", imageUrl
        ));
    }
}