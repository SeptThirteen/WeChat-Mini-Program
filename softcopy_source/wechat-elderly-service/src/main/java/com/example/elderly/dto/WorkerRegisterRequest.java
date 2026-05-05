package com.example.elderly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkerRegisterRequest {
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度过长")
    private String name;

    @NotBlank(message = "手机号不能为空")
    @Size(max = 20, message = "手机号长度过长")
    private String phone;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度需在6-100之间")
    private String password;

    @Size(max = 20, message = "类别名称过长")
    private String category;
}
