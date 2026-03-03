# AI API 配置教程

本项目集成了两种大语言模型（LLM）引擎：**百度文心一言（ERNIE）** 和 **腾讯混元（Hunyuan）**，用于 AI 问答功能。用户可以在前端自由切换引擎。

本文档指导你如何获取 API 密钥并配置到项目中。

---

## 目录

1. [整体架构](#整体架构)
2. [百度文心一言 API 配置](#百度文心一言-api-配置)
3. [腾讯混元 API 配置](#腾讯混元-api-配置)
4. [项目配置文件修改](#项目配置文件修改)
5. [切换默认引擎](#切换默认引擎)
6. [验证配置](#验证配置)
7. [常见问题](#常见问题)

---

## 整体架构

```
前端 (ai-chat.vue)
  ↓ 选择引擎 BAIDU / TENCENT
  ↓ POST /api/ai/text-query 或 /api/ai/voice-query
后端 (AiController → AiService → IAiProvider)
  ├── BaiduAiServiceImpl   → 百度文心 ERNIE API
  └── TencentAiServiceImpl → 腾讯混元 Hunyuan API
```

后端使用策略模式（`IAiProvider` 接口），根据前端传入的 `provider` 参数动态选择具体实现。配置存放在 `application-dev.yml` 中。

---

## 百度文心一言 API 配置

### 第一步：注册百度智能云

1. 打开 [百度智能云控制台](https://console.bce.baidu.com/)
2. 注册并登录百度账号
3. 完成实名认证（企业或个人均可）

### 第二步：创建应用获取密钥

1. 进入 [千帆大模型平台](https://console.bce.baidu.com/qianfan/overview)
2. 左侧菜单点击 **应用接入** → **创建应用**
3. 填写应用名称（如 "社区养老助手"），其他默认即可
4. 创建完成后在应用列表中可以看到：
   - **API Key**（即 `api-key`）
   - **Secret Key**（即 `secret-key`）

> ⚠️ 免费额度说明：百度文心提供一定的免费调用额度，个人认证后即可使用 ERNIE-Lite 等模型，详见 [计费说明](https://cloud.baidu.com/doc/WENXINWORKSHOP/s/hlrk4akp7)。

### 第三步：记录密钥

```
API Key:    xxxxxxxxxxxxxxxxxxxxxxxx
Secret Key: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

---

## 腾讯混元 API 配置

### 第一步：注册腾讯云

1. 打开 [腾讯云控制台](https://console.cloud.tencent.com/)
2. 注册并登录腾讯账号
3. 完成实名认证

### 第二步：开通混元大模型服务

1. 在控制台搜索 **混元大模型** 或直接访问 [混元控制台](https://console.cloud.tencent.com/hunyuan)
2. 点击 **开通服务**（首次使用需同意服务协议）
3. 开通成功后即可调用 API

### 第三步：获取 API 密钥

1. 进入 [API 密钥管理页面](https://console.cloud.tencent.com/cam/capi)
2. 点击 **新建密钥**
3. 记录以下信息：
   - **SecretId**（即 `secret-id`）
   - **SecretKey**（即 `secret-key`）

> ⚠️ 密钥创建后 SecretKey 仅显示一次，请立即保存。

> ⚠️ 免费额度说明：腾讯混元对新用户提供一定免费 token 额度，详见 [计费概述](https://cloud.tencent.com/document/product/1729/97731)。

---

## 项目配置文件修改

配置文件路径：

```
wechat-elderly-service/src/main/resources/application-dev.yml
```

找到以下配置段，将占位符替换为你获取的真实密钥：

```yaml
# AI provider keys for development
ai:
  default-provider: BAIDU           # 默认引擎：BAIDU 或 TENCENT
  baidu:
    api-key: "YOUR_BAIDU_API_KEY"           # ← 替换为百度 API Key
    secret-key: "YOUR_BAIDU_SECRET_KEY"     # ← 替换为百度 Secret Key
  tencent:
    secret-id: "YOUR_TENCENT_SECRET_ID"     # ← 替换为腾讯 SecretId
    secret-key: "YOUR_TENCENT_SECRET_KEY"   # ← 替换为腾讯 SecretKey
```

### 示例（使用虚拟值）

```yaml
ai:
  default-provider: BAIDU
  baidu:
    api-key: "abc123def456ghi789"
    secret-key: "xyz987uvw654rst321abc123def456ghi"
  tencent:
    secret-id: "AKIDxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
    secret-key: "abcdefghijklmnopqrstuvwxyz123456"
```

> ⚠️ **安全提醒**：切勿将真实 API 密钥提交到 Git 仓库！`application-dev.yml` 已在开发环境使用，如需更高安全性，可使用环境变量或 Spring 的外部化配置。

---

## 切换默认引擎

修改 `ai.default-provider` 即可设置后端默认引擎：

| 值 | 引擎 | 说明 |
|---|---|---|
| `BAIDU` | 百度文心一言 | 调用 ERNIE 系列模型 |
| `TENCENT` | 腾讯混元 | 调用 Hunyuan 系列模型 |

前端在发送请求时也可以通过 `provider` 参数指定引擎（覆盖默认值），在 AI 问答页面顶部可以切换"百度文心"和"腾讯混元"。

---

## 验证配置

### 方式一：Swagger UI 测试

1. 启动后端服务：
   ```bash
   cd wechat-elderly-service
   mvn spring-boot:run
   ```

2. 打开浏览器访问 Swagger UI：
   ```
   http://localhost:8080/swagger-ui.html
   ```

3. 找到 **ai-controller** → `POST /api/ai/text-query`

4. 点击 **Try it out**，输入请求体：
   ```json
   {
     "userId": 1,
     "provider": "BAIDU",
     "intent": "free",
     "text": "你好，请问附近有什么养老服务？"
   }
   ```

5. 点击 **Execute**，如果返回 `code: 0` 且 `data.answer` 有内容，说明百度引擎配置成功。

6. 将 `provider` 改为 `"TENCENT"` 再次测试腾讯引擎。

### 方式二：cURL 命令测试

```bash
# 测试百度文心
curl -X POST http://localhost:8080/api/ai/text-query \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <你的JWT Token>" \
  -d '{"userId":1,"provider":"BAIDU","intent":"free","text":"你好"}'

# 测试腾讯混元
curl -X POST http://localhost:8080/api/ai/text-query \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <你的JWT Token>" \
  -d '{"userId":1,"provider":"TENCENT","intent":"free","text":"你好"}'
```

### 方式三：前端页面测试

1. 启动前端 H5 模式：`npm run dev:h5`
2. 登录后进入首页 → 点击 "AI问答"
3. 在页面顶部选择引擎（百度文心 / 腾讯混元）
4. 输入文字或点击语音按钮提问
5. 如果正常返回回答，配置完成

---

## 常见问题

### Q: 报错 "API Key 无效" 或 "鉴权失败"？
**A:** 检查 `application-dev.yml` 中的密钥是否正确复制，注意不要有多余空格。百度的 API Key 和 Secret Key 缺一不可。

### Q: 腾讯混元报错 "AuthFailure"？
**A:** 确认 SecretId 和 SecretKey 正确，并且已在控制台开通了混元大模型服务。子账户需检查是否有 `QcloudHunyuanFullAccess` 权限。

### Q: 只配置了一个引擎，另一个能用吗？
**A:** 可以。只要不选择未配置的引擎即可。前端切换到未配置的引擎会返回错误。建议将 `default-provider` 设为已配置的引擎。

### Q: 如何控制 AI 回答的长度和风格？
**A:** 当前项目在后端 `BaiduAiServiceImpl` 和 `TencentAiServiceImpl` 中设置了 system prompt，调整相关代码中的 prompt 内容即可改变回答风格。

### Q: API 调用收费吗？
**A:** 两家都提供一定免费额度：
- 百度文心：ERNIE-Lite 等轻量模型有免费额度
- 腾讯混元：新用户赠送一定 token 额度
- 超出免费额度后按调用量计费，详见各平台计费页面

### Q: 如何在生产环境安全地管理密钥？
**A:** 推荐方式：
1. 使用环境变量：`AI_BAIDU_API_KEY` 等
2. 在 `application.yml` 中引用：`api-key: ${AI_BAIDU_API_KEY}`
3. 或使用 Spring Cloud Config / Vault 等配置中心

---

## 相关文件列表

| 文件 | 说明 |
|---|---|
| `application-dev.yml` | AI 密钥配置（开发环境） |
| `application.yml` | 基础配置 + AI 默认值 |
| `AiController.java` | AI 问答 REST 接口 |
| `AiService.java` / `AiServiceImpl.java` | AI 业务逻辑 |
| `IAiProvider.java` | AI 引擎策略接口 |
| `BaiduAiServiceImpl.java` | 百度文心实现 |
| `TencentAiServiceImpl.java` | 腾讯混元实现 |
| `ai-chat.vue` | 前端 AI 问答页面 |
