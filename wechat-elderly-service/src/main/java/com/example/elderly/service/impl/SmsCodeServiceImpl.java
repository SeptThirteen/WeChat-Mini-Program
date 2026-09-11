package com.example.elderly.service.impl;

import com.example.elderly.common.BusinessException;
import com.example.elderly.service.SmsCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存版短信验证码实现：验证码存储于本地 Map，重启即失效。
 * 防护措施：60秒重发冷却、5分钟有效期、连续输错5次作废。
 */
@Slf4j
@Service
public class SmsCodeServiceImpl implements SmsCodeService {

    private static final int MAX_ATTEMPTS = 5;

    private final ConcurrentHashMap<String, CodeEntry> store = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    @Value("${sms.code.expire-seconds:300}")
    private long expireSeconds;

    @Value("${sms.code.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    @Override
    public String send(String phone) {
        CodeEntry existing = store.get(phone);
        if (existing != null
                && System.currentTimeMillis() - existing.sendAt < resendCooldownSeconds * 1000) {
            throw new BusinessException(429, "验证码发送太频繁，请稍后再试");
        }
        String code = String.format("%06d", random.nextInt(1_000_000));
        store.put(phone, new CodeEntry(code, System.currentTimeMillis()));
        // 演示项目：未接短信网关，验证码输出到日志（手机号打码）
        log.info("【模拟短信】向 {}{}{} 发送验证码: {}",
                phone.substring(0, 3), "****", phone.substring(7), code);
        return code;
    }

    @Override
    public void verify(String phone, String code) {
        CodeEntry entry = store.get(phone);
        if (entry == null || System.currentTimeMillis() - entry.sendAt > expireSeconds * 1000) {
            store.remove(phone);
            throw new BusinessException(401, "验证码已失效，请重新获取");
        }
        if (entry.attempts >= MAX_ATTEMPTS) {
            store.remove(phone);
            throw new BusinessException(401, "错误次数过多，请重新获取验证码");
        }
        if (!entry.code.equals(code)) {
            entry.attempts++;
            throw new BusinessException(401, "验证码错误");
        }
        // 一次性使用，校验通过即作废
        store.remove(phone);
    }

    private static class CodeEntry {
        final String code;
        final long sendAt;
        int attempts;

        CodeEntry(String code, long sendAt) {
            this.code = code;
            this.sendAt = sendAt;
        }
    }
}
