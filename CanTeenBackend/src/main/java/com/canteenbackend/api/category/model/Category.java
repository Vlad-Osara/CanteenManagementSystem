package com.canteenbackend.api.category.model;

import com.canteenbackend.helper.base.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Table(name = "tbl_category")
@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Category extends BaseModel {
    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;
}
