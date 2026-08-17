package com.canteenbackend.api.dish.service;

import com.canteenbackend.api.category.model.Category;
import com.canteenbackend.api.category.repository.CategoryJpaRepository;
import com.canteenbackend.api.dish.dto.DishDTO;
import com.canteenbackend.api.dish.mapper.DishMapper;
import com.canteenbackend.api.dish.model.Dish;
import com.canteenbackend.api.dish.repository.DishJpaRepository;
import com.canteenbackend.api.dish.repository.DishRepository;
import com.canteenbackend.api.dish.request.DishGetRequest;
import com.canteenbackend.api.dish.request.DishStoreRequest;
import com.canteenbackend.api.dish.request.DishUpdateRequest;
import com.canteenbackend.api.order.service.OrderNotificationService;
import com.canteenbackend.exceptions.custom.BadRequestException;
import com.canteenbackend.helper.base.construct.RestfullService;
import com.canteenbackend.helper.base.repository.BaseRepository;
import com.canteenbackend.helper.upload.image.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DishService extends RestfullService<DishDTO, DishGetRequest, DishStoreRequest, DishUpdateRequest> {
    private final BaseRepository<Category, UUID, CategoryJpaRepository> baseCategoryRepository;
    private final BaseRepository<Dish, UUID, DishJpaRepository> baseRepository;
    private final DishRepository dishRepository;
    private final DishMapper dishMapper;
    private final OrderNotificationService orderNotificationService;
    private final CloudinaryService cloudinaryService;

    @Override
    public Page<DishDTO> getAll(DishGetRequest dishGetRequest) {
        Pageable pageable = dishGetRequest.toPageable();
        Page<Dish> dishes = dishRepository.searchDishes(dishGetRequest.getCategoryId(), dishGetRequest.getSearch(), pageable);
        return dishes.map(dishMapper::toDishDTO);
    }

    @Override
    public DishDTO get(UUID id) {
        return dishMapper.toDishDTO(baseRepository.get(id));
    }

    @Override
    public DishDTO store(DishStoreRequest dishStoreRequest) {
        Map<String, String> duplicateErrors = new HashMap<>();
        if (dishRepository.existsByName(dishStoreRequest.getName())) {
            duplicateErrors.put("Name", "Tên món ăn đã tồn tại");
            throw new BadRequestException("Thông tin đã tồn tại", duplicateErrors);
        }
        Category category = baseCategoryRepository.get(dishStoreRequest.getCategoryId());

        Dish dish = Dish.builder()
                .name(dishStoreRequest.getName())
                .description(dishStoreRequest.getDescription())
                .price(dishStoreRequest.getPrice())
                .isAvailable(dishStoreRequest.getIsAvailable())
                .imageUrl(dishStoreRequest.getImageUrl())
                .category(category)
                .build();
        return dishMapper.toDishDTO(baseRepository.save(dish));
    }

    @Override
    @Transactional
    public DishDTO update(UUID id, DishUpdateRequest dishUpdateRequest) {
        //Lấy dữ liệu cũ trước khi cập nhật cho phần xóa ảnh trên cloudinary
        Dish currentDish = baseRepository.get(id);
        String oldImageUrl = currentDish.getImageUrl();
        String newImageUrl = dishUpdateRequest.getImageUrl();

        Map<String, String> duplicateErrors = new HashMap<>();
        if (!currentDish.getName().equalsIgnoreCase(dishUpdateRequest.getName())
                && dishRepository.existsByName(dishUpdateRequest.getName())) {
            duplicateErrors.put("Name", "Tên món ăn đã tồn tại");
            throw new BadRequestException("Thông tin đã tồn tại", duplicateErrors);
        }


        Dish dish = Dish.builder()
                .name(dishUpdateRequest.getName())
                .description(dishUpdateRequest.getDescription())
                .price(dishUpdateRequest.getPrice())
                .isAvailable(dishUpdateRequest.getIsAvailable())
                .imageUrl(dishUpdateRequest.getImageUrl())
                .build();
        if (dishUpdateRequest.getCategoryId() != null) {
            Category category = baseCategoryRepository.get(dishUpdateRequest.getCategoryId());
            dish.setCategory(category);
        }
        DishDTO updatedDish = dishMapper.toDishDTO(baseRepository.update(id, dish));

        //Xóa ảnh trên Cloudinary sau khi cập nhật trên database
        // Nếu quản trị viên tải lên ảnh mới khác với ảnh cũ -> Xóa ảnh cũ trên Cloudinary
        if (newImageUrl != null && !newImageUrl.equals(oldImageUrl)) {
            cloudinaryService.deleteImageByUrl(oldImageUrl);
        }

        return updatedDish;
    }

    @Override
    public DishDTO destroy(UUID id) {
        Dish dish = baseRepository.get(id);
        DishDTO deletedDish = dishMapper.toDishDTO(baseRepository.delete(id));
        if (dish.getImageUrl() != null) {
            // Xóa luôn ảnh tương ứng trên Cloudinary sau khi món ăn bị xóa
            cloudinaryService.deleteImageByUrl(dish.getImageUrl());
        }
        return deletedDish;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public DishDTO toggleAvailability(UUID id) {
        Dish dish = baseRepository.get(id);
        dish.setIsAvailable(!dish.getIsAvailable());

        DishDTO updatedDishDTO = dishMapper.toDishDTO(baseRepository.update(id, dish));
        orderNotificationService.notifyDishAvailability(updatedDishDTO);

        return updatedDishDTO;
    }
}
