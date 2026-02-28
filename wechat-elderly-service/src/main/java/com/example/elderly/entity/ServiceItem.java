package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("services")
public class ServiceItem {
    @TableId(type = IdType.AUTO)
    private Long serviceId;
    private String category;
    private String description;
    private BigDecimal price;
    private LocalDateTime createdTime;
}
