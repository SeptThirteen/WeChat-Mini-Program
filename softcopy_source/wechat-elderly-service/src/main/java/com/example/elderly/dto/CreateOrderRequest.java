package com.example.elderly.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "服务ID不能为空")
    private Long serviceId;

    private String scheduledDate;
    private String scheduledSlot;
    private String address;
    private String remark;

    /** 订单类型: SINGLE(默认) / RECURRING */
    private String orderType;
    /** 长期订单开始日期 */
    private String dateStart;
    /** 长期订单结束日期 */
    private String dateEnd;
    /** 重复规则: 每天/每周/每周一三五 等 */
    private String recurrenceRule;
}
