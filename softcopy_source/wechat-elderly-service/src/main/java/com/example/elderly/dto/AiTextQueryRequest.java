package com.example.elderly.dto;

import lombok.Data;

/**
 * AI 文本问答请求
 */
@Data
public class AiTextQueryRequest {
    private Long userId;
    private String provider;  // BAIDU / TENCENT
    private String intent;    // shengbao / health / bangfu / faq / free
    private String text;
}
