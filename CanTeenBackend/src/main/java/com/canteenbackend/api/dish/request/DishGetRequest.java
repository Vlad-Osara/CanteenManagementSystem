package com.canteenbackend.api.dish.request;

import com.canteenbackend.helper.base.request.PaginateParams;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class DishGetRequest extends PaginateParams {
    private UUID categoryId;
}
