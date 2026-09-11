package com.example.elderly.controller;

import com.example.elderly.common.ApiResponse;
import com.example.elderly.dto.AiTextQueryRequest;
import com.example.elderly.entity.AiQueryLog;
import com.example.elderly.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * AI 问答控制器
 *
 * <ul>
 * <li>POST /api/ai/text-query   — 文本问答</li>
 * <li>POST /api/ai/voice-query  — 语音问答（上传音频文件）</li>
 * <li>GET  /api/ai/faq-list     — 获取FAQ列表</li>
 * <li>GET  /api/ai/faq-audio/{id} — 获取FAQ预录音频（公开）</li>
 * <li>GET  /api/ai/history       — 查询历史记录</li>
 * </ul>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    /**
     * 文本问答
     */
    @PostMapping("/text-query")
    public ApiResponse<Map<String, Object>> textQuery(@RequestBody AiTextQueryRequest request) {
        if (request.getText() == null || request.getText().trim().isEmpty()) {
            return ApiResponse.error(400, "请输入问题内容");
        }
        Map<String, Object> result = aiService.queryByText(
                request.getUserId(),
                request.getProvider(),
                request.getIntent(),
                request.getText()
        );
        return ApiResponse.success(result);
    }

    /**
     * 语音问答：上传音频文件 → ASR转文字 → AI对话
     */
    @PostMapping("/voice-query")
    public ApiResponse<Map<String, Object>> voiceQuery(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam(value = "provider", required = false) String provider,
            @RequestParam(value = "intent", required = false, defaultValue = "free") String intent,
            @RequestParam("userId") Long userId) {
        if (audio == null || audio.isEmpty()) {
            return ApiResponse.error(400, "请上传音频文件");
        }
        try {
            byte[] audioData = audio.getBytes();
            log.info("收到语音问答请求 - user:{}, size:{}bytes, provider:{}, intent:{}",
                    userId, audioData.length, provider, intent);
            Map<String, Object> result = aiService.queryByVoice(userId, provider, intent, audioData);
            return ApiResponse.success(result);
        } catch (IOException e) {
            log.error("读取音频文件失败", e);
            return ApiResponse.error(500, "音频文件读取失败");
        }
    }

    /**
     * FAQ 列表
     */
    @GetMapping("/faq-list")
    public ApiResponse<List<Map<String, Object>>> faqList() {
        return ApiResponse.success(aiService.getFaqList());
    }

    /**
     * FAQ 预录音频下载（公开接口）
     * 文件存放于 resources/static/audio/faq-{id}.wav（Windows SAPI 中文TTS预生成，
     * 可用仓库根目录 generate_faq_audio.ps1 重新生成）
     */
    @GetMapping("/faq-audio/{id}")
    public ResponseEntity<Resource> faqAudio(@PathVariable String id) {
        String path = "static/audio/faq-" + id + ".wav";
        Resource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=faq-" + id + ".wav")
                .contentType(MediaType.parseMediaType("audio/wav"))
                .body(resource);
    }

    /**
     * 查询历史记录
     */
    @GetMapping("/history")
    public ApiResponse<List<AiQueryLog>> history(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        return ApiResponse.success(aiService.getQueryHistory(userId, limit));
    }
}
