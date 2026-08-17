package com.canteenbackend.api.user.request;

import com.canteenbackend.helper.base.model.Role;
import com.canteenbackend.helper.base.request.PaginateParams;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserGetRequest extends PaginateParams {
    private Role role;
}
