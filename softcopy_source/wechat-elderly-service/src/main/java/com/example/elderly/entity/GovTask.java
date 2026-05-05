package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("government_tasks")
public class GovTask {
    @TableId(type = IdType.AUTO)
    private Long taskId;
    private Long userId;
    private String taskType;
    private String taskDesc;
    private String status;
    private LocalDateTime submittedTime;
    private LocalDateTime updatedTime;
}
