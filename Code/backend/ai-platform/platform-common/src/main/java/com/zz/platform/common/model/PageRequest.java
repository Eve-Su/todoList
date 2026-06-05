package com.zz.platform.common.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PageRequest {

    @Min(value = 1, message = "pageNo必须大于等于1")
    private Integer pageNo = 1;

    @Min(value = 1, message = "pageSize必须大于等于1")
    @Max(value = 200, message = "pageSize不能超过200")
    private Integer pageSize = 20;
}
