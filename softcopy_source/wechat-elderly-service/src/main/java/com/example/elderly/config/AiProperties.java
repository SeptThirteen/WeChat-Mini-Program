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
        private String apiKey;
        private String secretKey;
    }

    @Data
    public static class Tencent {
        private String secretId;
        private String secretKey;
    }
}
