package com.example.elderly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmergencyContactRequest {
    private Long contactId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "电话不能为空")
    private String phone;

    private String relation;
}
