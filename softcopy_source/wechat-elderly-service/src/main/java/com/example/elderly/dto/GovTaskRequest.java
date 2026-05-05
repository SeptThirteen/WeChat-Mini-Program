package com.example.elderly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GovTaskRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "申办类型不能为空")
    private String taskType;

    private String taskDesc;
}
