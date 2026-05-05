package com.example.elderly.service.impl;

import com.example.elderly.config.AiProperties;
import com.example.elderly.service.IAiProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 百度文心一言（ERNIE-Bot-turbo）实现
 *
 * API 文档：https://cloud.baidu.com/doc/WENXINWORKSHOP/s/Nlks5zkzu
 * ASR 文档：https://ai.baidu.com/ai-doc/SPEECH/rlbxklrhx
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BaiduAiServiceImpl implements IAiProvider {

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private OkHttpClient httpClient;
    private volatile String accessToken;
    private volatile long tokenExpireAt = 0;

    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String CHAT_URL = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-speed-128k";
    private static final String ASR_URL = "https://vop.baidu.com/server_api";

    @PostConstruct
    public void init() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public String getName() {
        return "BAIDU";
    }

    /**
     * 获取百度 access_token（缓存30天，自动刷新）
     */
    private synchronized String getAccessToken() {
        if (accessToken != null && System.currentTimeMillis() < tokenExpireAt) {
            return accessToken;
        }
        try {
            String url = TOKEN_URL
                    + "?grant_type=client_credentials"
                    + "&client_id=" + aiProperties.getBaidu().getApiKey()
                    + "&client_secret=" + aiProperties.getBaidu().getSecretKey();

            Request request = new Request.Builder().url(url)
                    .post(RequestBody.create("", MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body().string();
                JsonNode node = objectMapper.readTree(body);
                if (node.has("access_token")) {
                    accessToken = node.get("access_token").asText();
                    long expiresIn = node.get("expires_in").asLong();
                    tokenExpireAt = System.currentTimeMillis() + (expiresIn - 600) * 1000;
                    log.info("百度 access_token 获取成功，有效期 {}s", expiresIn);
                    return accessToken;
                } else {
                    log.error("百度 access_token 获取失败: {}", body);
                    throw new RuntimeException("百度鉴权失败: " + body);
                }
            }
        } catch (IOException e) {
            log.error("百度 access_token 请求异常", e);
            throw new RuntimeException("百度鉴权请求异常", e);
        }
    }

    @Override
    public String speechToText(byte[] audioData) {
        try {
            String token = getAccessToken();

            // 百度短语音识别 REST API
            java.util.Map<String, Object> asrBody = new java.util.LinkedHashMap<>();
            asrBody.put("format", "wav");
            asrBody.put("rate", 16000);
            asrBody.put("channel", 1);
            asrBody.put("cuid", "wechat-elderly-service");
            asrBody.put("token", token);
            asrBody.put("speech", java.util.Base64.getEncoder().encodeToString(audioData));
            asrBody.put("len", audioData.length);
            String jsonBody = objectMapper.writeValueAsString(asrBody);

            Request request = new Request.Builder()
                    .url(ASR_URL)
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body().string();
                JsonNode node = objectMapper.readTree(body);
                if (node.has("err_no") && node.get("err_no").asInt() == 0) {
                    JsonNode results = node.get("result");
                    return results != null && results.isArray() && results.size() > 0
                            ? results.get(0).asText()
                            : "";
                } else {
                    log.warn("百度 ASR 识别失败: {}", body);
                    return "[语音识别失败，请重试]";
                }
            }
        } catch (Exception e) {
            log.error("百度 ASR 异常", e);
            return "[语音识别服务异常]";
        }
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        try {
            String token = getAccessToken();
            String url = CHAT_URL + "?access_token=" + token;

            java.util.Map<String, Object> msgMap = new java.util.LinkedHashMap<>();
            msgMap.put("role", "user");
            msgMap.put("content", userMessage);

            java.util.Map<String, Object> chatBody = new java.util.LinkedHashMap<>();
            if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
                chatBody.put("system", systemPrompt);
            }
            chatBody.put("messages", new Object[]{ msgMap });
            String jsonBody = objectMapper.writeValueAsString(chatBody);

            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body().string();
                JsonNode node = objectMapper.readTree(body);
                if (node.has("result")) {
                    return node.get("result").asText();
                } else {
                    log.warn("百度 ERNIE 对话失败: {}", body);
                    return "抱歉，AI暂时无法回答，请稍后再试。";
                }
            }
        } catch (Exception e) {
            log.error("百度 ERNIE 对话异常", e);
            return "抱歉，AI服务暂时不可用，请稍后再试。";
        }
    }
}
