package com.example.elderly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BillQueryRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "查询类型不能为空")
    private String queryType;

    private String queryParams;
}
