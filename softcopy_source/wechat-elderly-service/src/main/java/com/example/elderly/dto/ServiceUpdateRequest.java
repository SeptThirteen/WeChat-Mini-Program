package com.example.elderly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServiceUpdateRequest {
    @NotBlank(message = "服务分类不能为空")
    private String category;

    @NotBlank(message = "服务描述不能为空")
    private String description;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;
}
