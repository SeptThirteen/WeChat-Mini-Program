package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("emergency_contacts")
public class EmergencyContact {
    @TableId(type = IdType.AUTO)
    private Long contactId;
    private Long userId;
    private String name;
    private String phone;
    private String relation;
    private LocalDateTime createdTime;
}
