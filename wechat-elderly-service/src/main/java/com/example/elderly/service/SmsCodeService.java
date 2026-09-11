package com.example.elderly.service;

/**
 * 短信验证码服务（内存版）。
 * 演示项目未接真实短信网关，验证码打印到后端日志；
 * 生产环境需替换为真实短信服务商实现。
 */
public interface SmsCodeService {

    /**
     * 生成并发送验证码（60秒重发冷却，验证码5分钟有效）。
     *
     * @return 本次生成的验证码（供开发模式回显，生产环境忽略返回值）
     */
    String send(String phone);

    /** 校验验证码，失败抛 BusinessException；校验通过后立即作废（一次性） */
    void verify(String phone, String code);
}
