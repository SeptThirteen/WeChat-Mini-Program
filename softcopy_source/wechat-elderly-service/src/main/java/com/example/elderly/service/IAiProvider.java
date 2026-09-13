package com.example.elderly.service;

/**
 * AI 服务商统一接口（策略模式）
 */
public interface IAiProvider {

    /**
     * 返回此 provider 的标识，如 "BAIDU" / "TENCENT"
     */
    String getName();

    /**
     * 语音转文字（ASR）
     * @param audioData WAV 格式音频字节流
     * @return 识别后的文字
     */
    String speechToText(byte[] audioData);

    /**
     * 文本对话（Chat）
     * @param systemPrompt 系统提示词（可控制领域）
     * @param userMessage 用户输入文本
     * @return AI 回复文本
     */
    String chat(String systemPrompt, String userMessage);

    /**
     * 语音合成（TTS,文字转语音）
     * @param text 待合成文本
     * @return 音频字节流(mp3)
     * @throws UnsupportedOperationException 当前引擎不支持时抛出,由上层协调器降级到其他引擎
     */
    default byte[] synthesize(String text) {
        throw new UnsupportedOperationException("该引擎不支持语音合成");
    }
}
