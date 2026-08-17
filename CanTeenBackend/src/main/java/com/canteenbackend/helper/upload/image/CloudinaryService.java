package com.canteenbackend.helper.upload.image;

import com.canteenbackend.exceptions.custom.BadRequestException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;
    public String uploadImage(MultipartFile file, String folderName) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Vui lòng chọn tệp hình ảnh hợp lệ!");
        }
        try {
            // Đẩy dữ liệu trực tiếp lên Cloudinary vào thư mục folderName
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folderName,
                            "resource_type", "auto"
                    )
            );
            // Trả về link ảnh HTTPS an toàn
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new BadRequestException("Tải ảnh lên Cloudinary thất bại: " + e.getMessage());
        }
    }

    //Hàm xóa ảnh trên Cloudinary bằng public_id
    public void deleteImageByPublicId(String publicId) {
        if (publicId == null || publicId.isBlank()) return;
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            System.err.println("Không thể xóa ảnh cũ trên Cloudinary: " + e.getMessage());
        }
    }

    // Trích xuất public_id từ URL Cloudinary và tiến hành xóa
    public void deleteImageByUrl(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) return;
        try {
            // Ví dụ URL: https://res.cloudinary.com/demo/image/upload/v1612345/canteen/dishes/dish123.jpg
            // public_id cần lấy là: "canteen/dishes/dish123"
            String[] parts = imageUrl.split("/upload/");
            if (parts.length > 1) {
                String pathAfterUpload = parts[1]; // "v1612345/canteen/dishes/dish123.jpg"
                String withoutVersion = pathAfterUpload.replaceFirst("^v\\d+/", ""); // "canteen/dishes/dish123.jpg"
                String publicId = withoutVersion.substring(0, withoutVersion.lastIndexOf('.')); // "canteen/dishes/dish123"

                deleteImageByPublicId(publicId);
            }
        } catch (Exception e) {
            System.err.println("Lỗi phân tích public_id để xóa ảnh: " + e.getMessage());
        }
    }
}
