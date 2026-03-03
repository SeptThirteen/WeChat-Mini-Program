package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long orderId;
    private Long userId;
    private Long serviceId;
    private String serviceName;
    private String scheduledDate;
    private String scheduledSlot;
    private String address;
    private String remark;
    private String contactPhone;
    private Integer rating;
    private Long workerId;
    private LocalDateTime assignedTime;
    private String status;
    private LocalDateTime createdTime;

    /** 非持久化字段：接单服务人员信息（关联查询填充） */
    @TableField(exist = false)
    private String workerName;

    @TableField(exist = false)
    private String workerPhone;
}
