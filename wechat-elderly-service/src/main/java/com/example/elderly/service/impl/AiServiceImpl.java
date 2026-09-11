package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.elderly.common.BusinessException;
import com.example.elderly.config.AiProperties;
import com.example.elderly.entity.AiQueryLog;
import com.example.elderly.entity.ServiceItem;
import com.example.elderly.mapper.AiQueryLogMapper;
import com.example.elderly.mapper.ServiceItemMapper;
import com.example.elderly.service.AiService;
import com.example.elderly.service.IAiProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final ServiceItemMapper serviceItemMapper;
    private static final ObjectMapper JSON = new ObjectMapper();
    /** timeSlot 白名单，与前端 create.vue 的 slotConfigs 对应 */
    private static final List<String> VALID_SLOTS = Arrays.asList("morning", "noon", "afternoon");

    /**
     * Spring 自动注入所有 IAiProvider 实现
     */
    public AiServiceImpl(List<IAiProvider> providers,
                         AiQueryLogMapper aiQueryLogMapper,
                         AiProperties aiProperties,
                         ServiceItemMapper serviceItemMapper) {
        for (IAiProvider p : providers) {
            providerMap.put(p.getName(), p);
        }
        this.aiQueryLogMapper = aiQueryLogMapper;
        this.aiProperties = aiProperties;
        this.serviceItemMapper = serviceItemMapper;
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
    public Map<String, Object> parseOrderIntent(Long userId, String providerName, String text, byte[] audioData) {
        IAiProvider provider = getProvider(providerName);

        // Step 1: 获取文本 —— 优先直接文本（调试/降级），否则 ASR
        String recognizedText = (text != null && !text.trim().isEmpty()) ? text.trim() : null;
        if (recognizedText == null && audioData != null && audioData.length > 0) {
            log.info("语音下单 - user:{}, provider:{}, audioSize:{}bytes", userId, provider.getName(), audioData.length);
            recognizedText = provider.speechToText(audioData);
        }
        if (recognizedText == null || recognizedText.trim().isEmpty() || recognizedText.startsWith("[")) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("provider", provider.getName());
            result.put("queryText", recognizedText != null ? recognizedText : "");
            result.put("error", "没听清您说的话，请靠近麦克风再试一次");
            return result;
        }

        // Step 2: LLM 抽取下单意图 JSON
        String response;
        try {
            response = provider.chat(buildOrderIntentPrompt(), recognizedText);
        } catch (Exception e) {
            log.error("下单意图LLM调用失败", e);
            return failResult(provider.getName(), recognizedText, "AI服务暂时不可用，请稍后再试或手动选择服务下单");
        }
        if (isProviderDegraded(response)) {
            return failResult(provider.getName(), recognizedText, "AI服务暂时不可用，请检查AI密钥配置或稍后再试");
        }

        // Step 3: 解析 + 规范化
        Map<String, Object> raw = extractJson(response);
        if (raw == null) {
            log.warn("下单意图JSON解析失败 - response: {}", response);
            return failResult(provider.getName(), recognizedText, "没听懂您的需求，请试着说“明天上午帮我修水龙头”");
        }

        String serviceKey = strOrNull(raw.get("serviceKey"));
        Map<String, Object> matched = matchService(serviceKey);
        if (matched == null) {
            return failResult(provider.getName(), recognizedText, "没认出您要的服务，可以说：跑腿帮买、上门维修、暖心陪伴、贴心出行、洁净到家、就医陪护、健康小站");
        }

        Map<String, Object> intent = new LinkedHashMap<>();
        intent.put("serviceId", matched.get("serviceId"));
        intent.put("serviceName", matched.get("displayName"));
        intent.put("servicePrice", matched.get("price"));
        intent.put("date", normalizeDate(strOrNull(raw.get("date"))));
        intent.put("timeSlot", normalizeSlot(strOrNull(raw.get("timeSlot"))));
        intent.put("address", strOrNull(raw.get("address")));
        intent.put("remark", strOrNull(raw.get("remark")));

        saveLog(userId, provider.getName(), "order", recognizedText, response);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("provider", provider.getName());
        result.put("queryText", recognizedText);
        result.put("intent", intent);
        return result;
    }

    private Map<String, Object> failResult(String provider, String queryText, String error) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("provider", provider);
        result.put("queryText", queryText);
        result.put("error", error);
        return result;
    }

    /**
     * provider 实现对异常采取“返回友好文案”的降级策略而非抛异常，
     * 这里识别典型降级文案，避免把引擎故障误报为“没听懂”。
     */
    private boolean isProviderDegraded(String response) {
        if (response == null) return false;
        String r = response.trim();
        return r.contains("AI服务暂时不可用")
                || r.contains("密钥未配置")
                || r.contains("请联系管理员")
                || (r.startsWith("抱歉，") && r.length() < 60);
    }

    /**
     * 下单意图抽取 System Prompt：限定 JSON Schema，注入服务目录与当前日期
     */
    private String buildOrderIntentPrompt() {
        LocalDate today = LocalDate.now();
        String[] weekdays = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
        String weekday = weekdays[today.getDayOfWeek().getValue() - 1];

        StringBuilder catalog = new StringBuilder();
        for (ServiceItem item : serviceItemMapper.selectList(null)) {
            catalog.append("- ").append(item.getCategory());
            if (item.getDisplayName() != null && !item.getDisplayName().equals(item.getCategory())) {
                catalog.append("（也叫").append(item.getDisplayName()).append("）");
            }
            catalog.append("\n");
        }

        return "你是社区助老服务的下单意图解析器。老人会说一句口语化的下单需求，请抽取成JSON。" +
                "\n今天是" + today.format(DateTimeFormatter.ISO_LOCAL_DATE) + "（" + weekday + "）。" +
                "\n可选服务（serviceKey 必须严格使用其中的类别名）：" + catalog +
                "抽取规则：\n" +
                "1. serviceKey：根据口语匹配上面某个类别（如“修水龙头”→维修，“帮我买点菜”→代购，“陪我去医院”→外出陪同），不确定时填 null。\n" +
                "2. date：把“今天/明天/后天/大后天/星期X”换算成 YYYY-MM-DD；没提到日期填 null；换算出的日期早于今天也填 null。\n" +
                "3. timeSlot：只能是 \"morning\"（上午）、\"noon\"（中午/下午）、\"afternoon\"（傍晚/晚上）之一，没提到填 null。\n" +
                "4. address：服务地址，没提到填 null。\n" +
                "5. remark：补充要求（如“修水龙头”），没提到填 null。\n" +
                "只输出一个JSON对象，不要输出任何其他文字或markdown，格式：\n" +
                "{\"serviceKey\":\"...\",\"date\":\"YYYY-MM-DD或null\",\"timeSlot\":\"...\",\"address\":\"...\",\"remark\":\"...\"}";
    }

    /** 从 LLM 回复中提取第一个 JSON 对象，失败返回 null */
    private Map<String, Object> extractJson(String response) {
        if (response == null) return null;
        String cleaned = response.replace("```json", "").replace("```", "").trim();
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start < 0 || end <= start) return null;
        try {
            return JSON.readValue(cleaned.substring(start, end + 1), Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    /** 服务匹配：类别/友好名 精确优先，其次双向包含 */
    private Map<String, Object> matchService(String serviceKey) {
        if (serviceKey == null || serviceKey.trim().isEmpty()) return null;
        String key = serviceKey.trim();
        List<ServiceItem> items = serviceItemMapper.selectList(null);
        Map<String, Object> hit = null;
        for (ServiceItem item : items) {
            if (key.equals(item.getCategory()) || key.equals(item.getDisplayName())) {
                return toServiceMap(item);
            }
            if (hit == null && (item.getCategory().contains(key) || key.contains(item.getCategory())
                    || (item.getDisplayName() != null
                        && (item.getDisplayName().contains(key) || key.contains(item.getDisplayName()))))) {
                hit = toServiceMap(item);
            }
        }
        return hit;
    }

    private Map<String, Object> toServiceMap(ServiceItem item) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("serviceId", item.getServiceId());
        m.put("displayName", item.getDisplayName() != null ? item.getDisplayName() : item.getCategory());
        m.put("price", item.getPrice());
        return m;
    }

    /** 日期规范化：仅接受今天及以后的 YYYY-MM-DD */
    private String normalizeDate(String date) {
        if (date == null || !date.matches("\\d{4}-\\d{2}-\\d{2}")) return null;
        try {
            LocalDate d = LocalDate.parse(date);
            return d.isBefore(LocalDate.now()) ? null : date;
        } catch (Exception e) {
            return null;
        }
    }

    /** 时段规范化：LLM 可能输出中文口语，统一映射到前端枚举 */
    private String normalizeSlot(String slot) {
        if (slot == null) return null;
        String s = slot.trim().toLowerCase();
        if (VALID_SLOTS.contains(s)) return s;
        if (s.contains("上午")) return "morning";
        if (s.contains("中午") || s.contains("下午")) return "noon";
        if (s.contains("傍晚") || s.contains("晚上")) return "afternoon";
        return null;
    }

    private String strOrNull(Object v) {
        if (v == null) return null;
        String s = String.valueOf(v).trim();
        return (s.isEmpty() || "null".equalsIgnoreCase(s) || "无".equals(s)) ? null : s;
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
