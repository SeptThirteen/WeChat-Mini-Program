package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bill_queries")
public class BillQuery {
    @TableId(type = IdType.AUTO)
    private Long queryId;
    private Long userId;
    private String queryType;
    private String queryParams;
    private String resultSnapshot;
    private LocalDateTime createdTime;
}
