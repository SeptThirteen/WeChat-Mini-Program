# v1.3 测试报告 — 语音下单(2026-09-12)

> 对应提交:v1.3 语音下单(后端 parse-order-intent + 前端 voice-create.vue)
> 测试方式:Mock LLM 单元测试 + 本地服务接口实测(TTS 合成语音 / 文本双路)
> 结论:**单元测试 10/10 通过,接口实测通过;LLM/ASR 真实调用受限于当前 AI Key 失效,待配置有效 Key 后回归**

---

## 1. 单元测试(Mock LLM,`AiServiceImplTest`,不依赖真实 Key)

| # | 用例 | 结果 |
|---|------|------|
| 1 | 纯 JSON 回复 → 解析出服务/日期/时段/地址/备注 | ✅ |
| 2 | markdown 围栏 + 前后废话包裹的 JSON → 仍能提取 | ✅ |
| 3 | 口语类别(如"上门")→ 双向包含模糊匹配到正确服务 | ✅ |
| 4 | 中文时段("上午")→ 映射为前端枚举 morning | ✅ |
| 5 | 过去日期 → 置 null 兜底 | ✅ |
| 6 | LLM 输出非法 JSON → 返回"没听懂"错误 + queryText | ✅ |
| 7 | serviceKey 为 null(非下单语句)→ 返回未识别服务错误 | ✅ |
| 8 | provider 降级文案 → 识别为"AI服务暂时不可用"而非"没听懂" | ✅ |
| 9 | ASR 失败文本([开头)→ 返回"没听清"错误 | ✅ |
| 10 | 空文本且无音频 → 错误兜底 | ✅ |

`mvn test -Dtest=AiServiceImplTest` → Tests run: 10, Failures: 0, Errors: 0

## 2. 接口实测(本地服务,真实 HTTP)

| # | 用例 | 结果 |
|---|------|------|
| 11 | 文本模式 + 有效 provider 参数 → 走到 LLM 步骤 | ✅ |
| 12 | TTS 合成语音(16kHz WAV,205KB)上传 → ASR 失败时优雅降级("没听清您说的话…") | ✅ |
| 13 | 无 audio 无 text → 400 请上传音频或输入文字 | ✅ |
| 14 | 无 Token → 401(接口在鉴权白名单外,符合预期) | ✅ |
| 15 | provider 不可用(当前 invalid_client Key)→ 准确报"AI服务暂时不可用,请检查AI密钥配置" | ✅ |

## 3. 待真实 Key / 真机回归

- [ ] 在 `application-dev.yml` 配置有效百度或腾讯 Key(当前百度 Key 报 `invalid_client`,腾讯未配置)
- [ ] 10 条口语用例真实回归(修水龙头/买菜/陪护就医/测血压/打扫/非下单闲聊等)
- [ ] 微信小程序真机:didi.vue → 语音下单 → 录音(60s上限) → 确认卡 → create.vue 预填核对
- [ ] 真机录音权限(wx.record 语音授权)与上传格式兼容性

## 4. 实现说明

- 后端复用 `IAiProvider.speechToText + chat` 抽象,LLM 输出 JSON 经正则提取 + Jackson 解析 + 服务目录匹配(精确→双向包含)+ 日期/时段规范化
- System Prompt 动态注入:当前日期(含星期,供"明天/周X"换算)+ services 表实时目录(category 及 displayName 别名)
- 前端 `voice-create.vue` 走"录音→确认→预填下单"流程,识别缺项显示"到下一步再选/填",失败可重试或转手动选服务
