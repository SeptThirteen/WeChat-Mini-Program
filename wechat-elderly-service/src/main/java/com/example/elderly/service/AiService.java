package com.example.elderly.service;

import com.example.elderly.entity.AiQueryLog;

import java.util.List;
import java.util.Map;

/**
 * AI 问答协调服务
 */
public interface AiService {

    /**
     * 文本问答
     */
    Map<String, Object> queryByText(Long userId, String provider, String intent, String text);

    /**
     * 语音问答（音频 → ASR → AI对话）
     */
    Map<String, Object> queryByVoice(Long userId, String provider, String intent, byte[] audioData);

    /**
     * 查询 FAQ 列表
     */
    List<Map<String, Object>> getFaqList();

    /**
     * 获取用户的历史查询记录
     */
    List<AiQueryLog> getQueryHistory(Long userId, int limit);
}
