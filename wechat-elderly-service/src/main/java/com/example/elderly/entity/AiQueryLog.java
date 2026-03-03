package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_query_logs")
public class AiQueryLog {
    @TableId(type = IdType.AUTO)
    private Long logId;
    private Long userId;
    private String provider;
    private String intent;
    private String queryText;
    private String responseText;
    private LocalDateTime createdTime;
}
