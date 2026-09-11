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
     * 语音/文本下单意图解析（ASR → LLM抽取JSON → 规范化）
     *
     * @param text      直接传入文本（调试/降级用），可为 null
     * @param audioData WAV 音频，text 为空时必传
     * @return {provider, queryText, intent:{serviceId,serviceName,servicePrice,date,timeSlot,address,remark}}
     *         解析失败时返回 {provider, queryText, error}
     */
    Map<String, Object> parseOrderIntent(Long userId, String provider, String text, byte[] audioData);

    /**
     * 查询 FAQ 列表
     */
    List<Map<String, Object>> getFaqList();

    /**
     * 获取用户的历史查询记录
     */
    List<AiQueryLog> getQueryHistory(Long userId, int limit);
}
