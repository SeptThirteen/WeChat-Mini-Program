package com.example.elderly.service.impl;

import com.example.elderly.config.AiProperties;
import com.example.elderly.entity.ServiceItem;
import com.example.elderly.mapper.AiQueryLogMapper;
import com.example.elderly.mapper.ServiceItemMapper;
import com.example.elderly.service.IAiProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 语音下单意图解析单元测试（Mock LLM，不依赖真实 API Key）
 * 覆盖 ROADMAP 需求3 测试项：JSON 输出格式稳定性 + 兜底逻辑覆盖率
 */
class AiServiceImplTest {

    private IAiProvider mockProvider;
    private AiServiceImpl aiService;
    private static final String TOMORROW = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
    private static final String YESTERDAY = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);

    @BeforeEach
    void setUp() {
        mockProvider = mock(IAiProvider.class);
        when(mockProvider.getName()).thenReturn("MOCK");

        ServiceItemMapper serviceItemMapper = mock(ServiceItemMapper.class);
        when(serviceItemMapper.selectList(Mockito.any())).thenReturn(seedServices());

        AiQueryLogMapper aiQueryLogMapper = mock(AiQueryLogMapper.class);

        aiService = new AiServiceImpl(
                List.of(mockProvider),
                aiQueryLogMapper,
                new AiProperties(),
                serviceItemMapper);
    }

    private List<ServiceItem> seedServices() {
        ServiceItem s1 = new ServiceItem();
        s1.setServiceId(1L);
        s1.setCategory("家务帮助");
        s1.setDisplayName("洁净到家");
        s1.setPrice(new BigDecimal("50.00"));
        ServiceItem s2 = new ServiceItem();
        s2.setServiceId(7L);
        s2.setCategory("维修");
        s2.setDisplayName("上门维修");
        s2.setPrice(new BigDecimal("80.00"));
        return Arrays.asList(s1, s2);
    }

    @Test
    @DisplayName("纯JSON回复 → 正常解析出服务/日期/时段")
    void parsePlainJson() {
        when(mockProvider.chat(anyString(), anyString())).thenReturn(
                "{\"serviceKey\":\"维修\",\"date\":\"" + TOMORROW + "\",\"timeSlot\":\"morning\","
                        + "\"address\":\"幸福小区3号楼\",\"remark\":\"修水龙头\"}");

        Map<String, Object> result = aiService.parseOrderIntent(1L, "MOCK", "明天上午帮我修水龙头", null);

        Map<String, Object> intent = assertIntentOk(result);
        assertEquals(7L, ((Number) intent.get("serviceId")).longValue());
        assertEquals("上门维修", intent.get("serviceName"));
        assertEquals(TOMORROW, intent.get("date"));
        assertEquals("morning", intent.get("timeSlot"));
        assertEquals("幸福小区3号楼", intent.get("address"));
        assertEquals("修水龙头", intent.get("remark"));
    }

    @Test
    @DisplayName("markdown围栏+前后废话包裹的JSON → 仍能提取")
    void parseFencedJsonWithNoise() {
        when(mockProvider.chat(anyString(), anyString())).thenReturn(
                "好的，解析结果如下：\n```json\n{\"serviceKey\":\"洁净到家\",\"date\":null,"
                        + "\"timeSlot\":\"afternoon\",\"address\":null,\"remark\":\"打扫厨房\"}\n```\n以上。");

        Map<String, Object> result = aiService.parseOrderIntent(1L, "MOCK", "明天下午打扫厨房", null);

        Map<String, Object> intent = assertIntentOk(result);
        assertEquals("洁净到家", intent.get("serviceName"));
        assertNull(intent.get("date"));
        assertEquals("afternoon", intent.get("timeSlot"));
        assertEquals("打扫厨房", intent.get("remark"));
    }

    @Test
    @DisplayName("口语类别（跑腿/修理）→ 双向包含匹配到正确服务")
    void parseFuzzyServiceKey() {
        when(mockProvider.chat(anyString(), anyString())).thenReturn(
                "{\"serviceKey\":\"上门\",\"date\":null,\"timeSlot\":null,\"address\":null,\"remark\":null}");

        Map<String, Object> result = aiService.parseOrderIntent(1L, "MOCK", "找人上门看看", null);

        Map<String, Object> intent = assertIntentOk(result);
        assertEquals(7L, ((Number) intent.get("serviceId")).longValue());
    }

    @Test
    @DisplayName("中文时段 → 映射为前端枚举")
    void normalizeChineseSlot() {
        when(mockProvider.chat(anyString(), anyString())).thenReturn(
                "{\"serviceKey\":\"维修\",\"date\":\"" + TOMORROW + "\",\"timeSlot\":\"上午\",\"address\":null,\"remark\":null}");

        Map<String, Object> intent = assertIntentOk(
                aiService.parseOrderIntent(1L, "MOCK", "明天上午修东西", null));
        assertEquals("morning", intent.get("timeSlot"));
    }

    @Test
    @DisplayName("过去日期 → 置null兜底")
    void pastDateBecomesNull() {
        when(mockProvider.chat(anyString(), anyString())).thenReturn(
                "{\"serviceKey\":\"维修\",\"date\":\"" + YESTERDAY + "\",\"timeSlot\":null,\"address\":null,\"remark\":null}");

        Map<String, Object> intent = assertIntentOk(
                aiService.parseOrderIntent(1L, "MOCK", "昨天修的东西", null));
        assertNull(intent.get("date"));
    }

    @Test
    @DisplayName("LLM输出非法JSON → 返回无法理解错误")
    void invalidJsonReturnsError() {
        when(mockProvider.chat(anyString(), anyString())).thenReturn("我觉得老人想下单，但我不确定。");

        Map<String, Object> result = aiService.parseOrderIntent(1L, "MOCK", "随便说点啥", null);
        assertNotNull(result.get("error"));
        assertEquals("随便说点啥", result.get("queryText"));
        assertNull(result.get("intent"));
    }

    @Test
    @DisplayName("serviceKey为null → 返回未识别服务错误")
    void nullServiceKeyReturnsError() {
        when(mockProvider.chat(anyString(), anyString())).thenReturn(
                "{\"serviceKey\":null,\"date\":null,\"timeSlot\":null,\"address\":null,\"remark\":null}");

        Map<String, Object> result = aiService.parseOrderIntent(1L, "MOCK", "今天天气怎么样", null);
        assertNotNull(result.get("error"));
    }

    @Test
    @DisplayName("provider降级文案 → 返回AI不可用错误而非没听懂")
    void providerDegradedText() {
        when(mockProvider.chat(anyString(), anyString())).thenReturn("抱歉，AI服务暂时不可用，请稍后再试。");

        Map<String, Object> result = aiService.parseOrderIntent(1L, "MOCK", "帮我修水龙头", null);
        String error = String.valueOf(result.get("error"));
        assertTrue(error.contains("AI服务暂时不可用"));
    }

    @Test
    @DisplayName("ASR失败文本（[开头）→ 返回没听清错误")
    void asrFailureHandled() {
        when(mockProvider.speechToText(any())).thenReturn("[录音时间太短]");

        Map<String, Object> result = aiService.parseOrderIntent(1L, "MOCK", null, new byte[]{1, 2, 3});
        String error = String.valueOf(result.get("error"));
        assertTrue(error.contains("没听清"));
    }

    @Test
    @DisplayName("空文本且无音频 → 400语义错误")
    void emptyInputReturnsError() {
        Map<String, Object> result = aiService.parseOrderIntent(1L, "MOCK", "  ", null);
        assertNotNull(result.get("error"));
    }

    private Map<String, Object> assertIntentOk(Map<String, Object> result) {
        assertNull(result.get("error"), "不应返回错误: " + result.get("error"));
        Object intentObj = result.get("intent");
        assertNotNull(intentObj);
        @SuppressWarnings("unchecked")
        Map<String, Object> intent = (Map<String, Object>) intentObj;
        return intent;
    }
}
