package com.example.elderly.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private String name;
    private Integer age;
    private String address;
    private String community;
}
