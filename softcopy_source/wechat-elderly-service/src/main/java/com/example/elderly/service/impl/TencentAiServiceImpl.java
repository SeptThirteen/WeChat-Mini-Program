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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 腾讯混元大模型实现
 *
 * API 文档：https://cloud.tencent.com/document/product/1729/97732
 * 使用 TC3-HMAC-SHA256 签名认证
 * ASR 复用百度服务（腾讯 ASR 需单独接入），此处标记为 fallback
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TencentAiServiceImpl implements IAiProvider {

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private OkHttpClient httpClient;

    private static final String HUNYUAN_HOST = "hunyuan.tencentcloudapi.com";
    private static final String HUNYUAN_URL = "https://hunyuan.tencentcloudapi.com";
    private static final String SERVICE = "hunyuan";
    private static final String REGION = "ap-beijing"; // 官方文档推荐区域
    private static final String ALGORITHM = "TC3-HMAC-SHA256";

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
        return "TENCENT";
    }

    @Override
    public String speechToText(byte[] audioData) {
        // 腾讯 ASR（一句话识别）简化实现
        // 实际生产可替换为腾讯云 ASR API；这里先做一个兼容的HTTP POST方式
        try {
            String secretId = aiProperties.getTencent().getSecretId();
            String secretKey = aiProperties.getTencent().getSecretKey();
            if (secretId == null || secretId.startsWith("YOUR_")) {
                return "[腾讯语音识别未配置API密钥]";
            }

            long timestamp = System.currentTimeMillis() / 1000;
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(timestamp * 1000));

            // 腾讯云一句话识别 API
            String asrHost = "asr.tencentcloudapi.com";
            String asrService = "asr";
            String action = "SentenceRecognition";

            Map<String, Object> params = new LinkedHashMap<>();
            params.put("ProjectId", 0);
            params.put("SubServiceType", 2);
            params.put("EngSerViceType", "16k_zh");
            params.put("SourceType", 1);
            params.put("VoiceFormat", "wav");
            params.put("Data", Base64.getEncoder().encodeToString(audioData));
            params.put("DataLen", audioData.length);
            String payload = objectMapper.writeValueAsString(params);

            String authorization = buildTc3Authorization(
                    secretId, secretKey, asrService, asrHost,
                    action, "2019-06-14", timestamp, date, payload);

            Request request = new Request.Builder()
                    .url("https://" + asrHost)
                    .post(RequestBody.create(payload, MediaType.parse("application/json; charset=utf-8")))
                    .addHeader("Authorization", authorization)
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .addHeader("Host", asrHost)
                    .addHeader("X-TC-Action", action)
                    .addHeader("X-TC-Version", "2019-06-14")
                    .addHeader("X-TC-Timestamp", String.valueOf(timestamp))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body().string();
                JsonNode node = objectMapper.readTree(body);
                JsonNode resp = node.path("Response");
                if (resp.has("Result")) {
                    return resp.get("Result").asText();
                }
                log.warn("腾讯 ASR 识别失败: {}", body);
                return "[语音识别失败，请重试]";
            }
        } catch (Exception e) {
            log.error("腾讯 ASR 异常", e);
            return "[语音识别服务异常]";
        }
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        try {
            String secretId = aiProperties.getTencent().getSecretId();
            String secretKey = aiProperties.getTencent().getSecretKey();
            if (secretId == null || secretId.startsWith("YOUR_")) {
                return "腾讯混元API密钥未配置，请联系管理员。";
            }

            long timestamp = System.currentTimeMillis() / 1000;
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(timestamp * 1000));

            String action = "ChatCompletions";
            String version = "2023-09-01";

            // 构建请求体
            List<Map<String, String>> messages = new ArrayList<>();
            if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
                Map<String, String> sysMsg = new LinkedHashMap<>();
                sysMsg.put("Role", "system");
                sysMsg.put("Content", systemPrompt);
                messages.add(sysMsg);
            }
            Map<String, String> userMsg = new LinkedHashMap<>();
            userMsg.put("Role", "user");
            userMsg.put("Content", userMessage);
            messages.add(userMsg);

            Map<String, Object> bodyMap = new LinkedHashMap<>();
            bodyMap.put("Model", "hunyuan-lite");
            bodyMap.put("Messages", messages);
            bodyMap.put("Stream", false);
            String payload = objectMapper.writeValueAsString(bodyMap);

                String authorization = buildTc3Authorization(
                    secretId, secretKey, SERVICE, HUNYUAN_HOST,
                    action, version, timestamp, date, payload);

            Request request = new Request.Builder()
                    .url(HUNYUAN_URL)
                    .post(RequestBody.create(payload, MediaType.parse("application/json; charset=utf-8")))
                    .addHeader("Authorization", authorization)
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .addHeader("Host", HUNYUAN_HOST)
                    .addHeader("X-TC-Action", action)
                    .addHeader("X-TC-Version", version)
                    .addHeader("X-TC-Timestamp", String.valueOf(timestamp))
                    .addHeader("X-TC-Region", REGION)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body().string();
                JsonNode node = objectMapper.readTree(body);
                JsonNode resp = node.path("Response");
                if (resp.has("Error")) {
                    JsonNode err = resp.get("Error");
                    String msg = err.path("Message").asText();
                    String code = err.path("Code").asText();
                    log.warn("腾讯混元返回错误 code={} msg={}", code, msg);
                    return "AI服务暂时不可用：" + msg;
                }
                if (resp.has("Choices") && resp.get("Choices").isArray() && resp.get("Choices").size() > 0) {
                    return resp.get("Choices").get(0).path("Message").path("Content").asText();
                }
                log.warn("腾讯混元对话失败: {}", body);
                return "抱歉，AI暂时无法回答，请稍后再试。";
            }
        } catch (Exception e) {
            log.error("腾讯混元对话异常", e);
            return "抱歉，AI服务暂时不可用，请稍后再试。";
        }
    }

    // ============ TC3-HMAC-SHA256 签名辅助方法 ============

    private String buildTc3Authorization(String secretId, String secretKey,
                                          String service, String host,
                                          String action, String version,
                                          long timestamp, String date,
                                          String payload) throws Exception {
        // Step 1: CanonicalRequest
        String httpRequestMethod = "POST";
        String canonicalUri = "/";
        String canonicalQueryString = "";
        String canonicalHeaders = "content-type:application/json; charset=utf-8\nhost:" + host + "\n"
                + "x-tc-action:" + action.toLowerCase() + "\n";
        String signedHeaders = "content-type;host;x-tc-action";
        String hashedPayload = sha256Hex(payload);
        String canonicalRequest = httpRequestMethod + "\n" + canonicalUri + "\n" + canonicalQueryString + "\n"
                + canonicalHeaders + "\n" + signedHeaders + "\n" + hashedPayload;

        // Step 2: StringToSign
        String credentialScope = date + "/" + service + "/tc3_request";
        String stringToSign = ALGORITHM + "\n" + timestamp + "\n" + credentialScope + "\n" + sha256Hex(canonicalRequest);

        // Step 3: Signature
        byte[] secretDate = hmac256(("TC3" + secretKey).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = hmac256(secretDate, service);
        byte[] secretSigning = hmac256(secretService, "tc3_request");
        String signature = bytesToHex(hmac256(secretSigning, stringToSign));

        // Step 4: Authorization
        return ALGORITHM + " Credential=" + secretId + "/" + credentialScope
                + ", SignedHeaders=" + signedHeaders + ", Signature=" + signature;
    }

    private static byte[] hmac256(byte[] key, String msg) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(msg.getBytes(StandardCharsets.UTF_8));
    }

    private static String sha256Hex(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(d);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
