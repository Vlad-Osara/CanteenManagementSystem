package com.canteenbackend.api.transaction.request;

import com.canteenbackend.helper.base.request.PaginateParams;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TransactionGetRequest extends PaginateParams {
}
