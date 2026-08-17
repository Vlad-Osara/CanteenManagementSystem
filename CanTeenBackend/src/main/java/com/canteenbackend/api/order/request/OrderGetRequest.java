package com.canteenbackend.api.order.request;

import com.canteenbackend.helper.base.request.PaginateParams;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderGetRequest extends PaginateParams {
}
