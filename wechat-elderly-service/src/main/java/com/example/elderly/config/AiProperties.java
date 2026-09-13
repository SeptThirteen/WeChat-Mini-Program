package com.example.elderly.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 服务商配置属性（百度文心 + 腾讯混元）
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    private String defaultProvider = "BAIDU";
    private Baidu baidu = new Baidu();
    private Tencent tencent = new Tencent();

    @Data
    public static class Baidu {
        /** 传统应用的 API Key(配合 secretKey 走 OAuth,用于 ASR 与旧版对话接口) */
        private String apiKey;
        private String secretKey;
        /** 千帆 V2 API 密钥(bce-v3/ 开头,新账号对话接口必须);为空时对话走旧版 OAuth 接口 */
        private String chatApiKey;
    }

    @Data
    public static class Tencent {
        private String secretId;
        private String secretKey;
    }
}
