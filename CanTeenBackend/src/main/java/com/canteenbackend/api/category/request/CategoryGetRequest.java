package com.canteenbackend.api.category.request;

import com.canteenbackend.helper.base.request.PaginateParams;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CategoryGetRequest extends PaginateParams {
}
