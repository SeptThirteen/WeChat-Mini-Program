package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("workers")
public class Worker {
    @TableId(type = IdType.AUTO)
    private Long workerId;
    private String phone;
    private String name;
    private String password;
    private String category;
    private LocalDateTime createdTime;
}
