package com.canteenbackend.api.category.service;

import com.canteenbackend.api.category.dto.CategoryDTO;
import com.canteenbackend.api.category.mapper.CategoryMapper;
import com.canteenbackend.api.category.model.Category;
import com.canteenbackend.api.category.repository.CategoryJpaRepository;
import com.canteenbackend.api.category.repository.CategoryRepository;
import com.canteenbackend.api.category.request.CategoryGetRequest;
import com.canteenbackend.api.category.request.CategoryStoreRequest;
import com.canteenbackend.api.category.request.CategoryUpdateRequest;
import com.canteenbackend.api.dish.repository.DishRepository;
import com.canteenbackend.exceptions.custom.BadRequestException;
import com.canteenbackend.helper.base.construct.RestfullService;
import com.canteenbackend.helper.base.repository.BaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService extends RestfullService<CategoryDTO, CategoryGetRequest, CategoryStoreRequest, CategoryUpdateRequest> {
    private final BaseRepository<Category, UUID, CategoryJpaRepository> baseRepository;
    private final DishRepository dishRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;



    @Override
    public Page<CategoryDTO> getAll(CategoryGetRequest categoryGetRequest) {
        Pageable pageable = categoryGetRequest.toPageable();
        Page<Category> categories = baseRepository.getAll(pageable);
        return categories.map(categoryMapper::toCategoryDTO);
    }

    @Override
    public CategoryDTO get(UUID id) {
        return categoryMapper.toCategoryDTO(baseRepository.get(id));
    }

    @Override
    public CategoryDTO store(CategoryStoreRequest categoryStoreRequest) {
        Map<String, String> duplicateErrors = new HashMap<>();
        if (categoryRepository.existsByName(categoryStoreRequest.getName())) {
            duplicateErrors.put("Name", "Tên danh mục đã tồn tại");
            throw new BadRequestException("Thông tin đã tồn tại", duplicateErrors);
        }
        Category category = Category.builder()
                .name(categoryStoreRequest.getName())
                .description(categoryStoreRequest.getDescription())
                .build();
        return categoryMapper.toCategoryDTO(baseRepository.save(category));
    }

    @Override
    public CategoryDTO update(UUID id, CategoryUpdateRequest categoryUpdateRequest) {
        Map<String, String> duplicateErrors = new HashMap<>();
        if (categoryRepository.existsByName(categoryUpdateRequest.getName())) {
            duplicateErrors.put("Name", "Tên danh mục đã tồn tại");
            throw new BadRequestException("Thông tin đã tồn tại", duplicateErrors);
        }
        Category category = Category.builder()
                .name(categoryUpdateRequest.getName())
                .description(categoryUpdateRequest.getDescription())
                .build();
        return categoryMapper.toCategoryDTO(baseRepository.update(id, category));
    }

    @Override
    @Transactional
    public CategoryDTO destroy(UUID id) {
        dishRepository.deleteAllByCategoryId(id);
        return categoryMapper.toCategoryDTO(baseRepository.delete(id));
    }
}
