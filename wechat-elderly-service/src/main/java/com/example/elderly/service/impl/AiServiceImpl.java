package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.elderly.common.BusinessException;
import com.example.elderly.config.AiProperties;
import com.example.elderly.entity.AiQueryLog;
import com.example.elderly.mapper.AiQueryLogMapper;
import com.example.elderly.service.AiService;
import com.example.elderly.service.IAiProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * AI 问答协调服务实现
 * <p>
 * 根据 provider 参数选择百度文心 / 腾讯混元，
 * 负责 ASR 转文字 → 领域提示词注入 → AI 对话 → 保存日志.
 */
@Slf4j
@Service
public class AiServiceImpl implements AiService {

    private final Map<String, IAiProvider> providerMap = new HashMap<>();
    private final AiQueryLogMapper aiQueryLogMapper;
    private final AiProperties aiProperties;

    /**
     * Spring 自动注入所有 IAiProvider 实现
     */
    public AiServiceImpl(List<IAiProvider> providers,
                         AiQueryLogMapper aiQueryLogMapper,
                         AiProperties aiProperties) {
        for (IAiProvider p : providers) {
            providerMap.put(p.getName(), p);
        }
        this.aiQueryLogMapper = aiQueryLogMapper;
        this.aiProperties = aiProperties;
        log.info("已注册 AI 供应商: {}", providerMap.keySet());
    }

    /**
     * 按 intent 返回对应领域系统提示词
     */
    private String buildSystemPrompt(String intent) {
        if (intent == null) intent = "free";
        switch (intent) {
            case "shengbao":
                return "你是社区适老化服务小程序的AI助手，专门回答社保、医保、养老金、政务办事相关问题。" +
                       "请用简洁易懂的中文回答，适合老年人阅读。如果不确定，请建议用户联系当地社区服务中心。";
            case "health":
                return "你是社区适老化服务小程序的AI健康助手，帮助老年用户解答常见健康问题、慢病管理、用药注意事项。" +
                       "请用通俗易懂的中文回答。重要提醒：你不能替代医生诊断，请建议用户如有严重症状立即就医。";
            case "bangfu":
                return "你是社区适老化服务小程序的AI助手，帮助老年用户了解帮扶服务项目，包括上门家务、陪护就医、代购、维修等。" +
                       "请介绍服务内容和预约流程，用简洁亲切的语言。";
            default:
                return "你是社区适老化服务小程序的AI助手，面向老年用户提供友好的问答服务。" +
                       "请用简洁、通俗、温暖的中文回答。";
        }
    }

    private IAiProvider getProvider(String providerName) {
        if (providerName == null || providerName.trim().isEmpty()) {
            providerName = aiProperties.getDefaultProvider();
        }
        IAiProvider provider = providerMap.get(providerName.toUpperCase());
        if (provider == null) {
            throw new BusinessException(400, "不支持的AI服务商: " + providerName);
        }
        return provider;
    }

    @Override
    public Map<String, Object> queryByText(Long userId, String providerName, String intent, String text) {
        IAiProvider provider = getProvider(providerName);
        String systemPrompt = buildSystemPrompt(intent);

        log.info("AI文本问答 - user:{}, provider:{}, intent:{}, text:{}", userId, provider.getName(), intent, text);
        String response = provider.chat(systemPrompt, text);

        // 保存日志
        saveLog(userId, provider.getName(), intent, text, response);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("provider", provider.getName());
        result.put("queryText", text);
        result.put("responseText", response);
        return result;
    }

    @Override
    public Map<String, Object> queryByVoice(Long userId, String providerName, String intent, byte[] audioData) {
        IAiProvider provider = getProvider(providerName);

        // Step 1: ASR 语音转文字
        log.info("AI语音问答 - user:{}, provider:{}, intent:{}, audioSize:{}bytes", userId, provider.getName(), intent, audioData.length);
        String recognizedText = provider.speechToText(audioData);

        if (recognizedText == null || recognizedText.trim().isEmpty() || recognizedText.startsWith("[")) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("provider", provider.getName());
            result.put("queryText", recognizedText != null ? recognizedText : "");
            result.put("responseText", "语音识别未成功，请靠近麦克风重新说一遍。");
            return result;
        }

        // Step 2: AI 对话
        String systemPrompt = buildSystemPrompt(intent);
        String response = provider.chat(systemPrompt, recognizedText);

        // 保存日志
        saveLog(userId, provider.getName(), intent, recognizedText, response);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("provider", provider.getName());
        result.put("queryText", recognizedText);
        result.put("responseText", response);
        return result;
    }

    @Override
    public List<Map<String, Object>> getFaqList() {
        // 预定义 FAQ 列表
        List<Map<String, Object>> faqs = new ArrayList<>();
        faqs.add(makeFaq("001", "如何查询社保？", "介绍社保查询的方式和步骤"));
        faqs.add(makeFaq("002", "如何预约帮扶服务？", "说明预约上门服务的流程"));
        faqs.add(makeFaq("003", "水电费怎么查询？", "水电费缴费查询方法"));
        faqs.add(makeFaq("004", "紧急联系人怎么设置？", "设置紧急联系人的操作指南"));
        faqs.add(makeFaq("005", "订单取消/评价怎么操作？", "订单取消和评价操作说明"));
        faqs.add(makeFaq("006", "政务代办如何申请？", "政务代办的申办流程"));
        return faqs;
    }

    private Map<String, Object> makeFaq(String id, String title, String desc) {
        Map<String, Object> faq = new LinkedHashMap<>();
        faq.put("id", id);
        faq.put("title", title);
        faq.put("desc", desc);
        return faq;
    }

    @Override
    public List<AiQueryLog> getQueryHistory(Long userId, int limit) {
        QueryWrapper<AiQueryLog> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("created_time");
        wrapper.last("LIMIT " + Math.min(limit, 50));
        return aiQueryLogMapper.selectList(wrapper);
    }

    private void saveLog(Long userId, String provider, String intent, String queryText, String responseText) {
        try {
            AiQueryLog logEntry = new AiQueryLog();
            logEntry.setUserId(userId);
            logEntry.setProvider(provider);
            logEntry.setIntent(intent != null ? intent : "free");
            logEntry.setQueryText(queryText);
            logEntry.setResponseText(responseText);
            aiQueryLogMapper.insert(logEntry);
        } catch (Exception e) {
            log.warn("保存AI查询日志失败", e);
        }
    }
}
