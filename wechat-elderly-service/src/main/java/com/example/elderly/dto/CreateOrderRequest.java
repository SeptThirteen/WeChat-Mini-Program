package com.example.elderly.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "服务ID不能为空")
    private Long serviceId;
}
