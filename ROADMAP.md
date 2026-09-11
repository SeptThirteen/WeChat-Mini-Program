# 功能开发路线图

## 待开发需求

### ⭐⭐⭐ 高优先级（立即实施）

#### 需求 5：服务名称润色
**工作量**：1 小时  
**风险**：极低  
**影响范围**：数据库 + 前端展示  
**状态**：✅ 已完成（v1.1，提交 68eff6d）

**实施清单**：
- [x] 数据库：`services` 表新增 `display_name` 字段（VARCHAR 50）
- [x] 更新种子数据，为 7 个服务填入友好名称：
  - 日间照护 → 暖心陪伴
  - 外出陪同 → 贴心出行
  - 代购 → 跑腿帮买
  - 维修 → 上门维修
  - 家务帮助 → 洁净到家
  - 基础陪护 → 就医陪护
  - 健康测量 → 健康小站
- [x] 后端：`Service.java` 新增 `displayName` 字段
- [x] 前端：`didi.vue`、`index.vue`、`service/detail.vue` 使用 `displayName` 展示
- [x] `create.vue` 的 `detectServiceKey` 匹配逻辑改为按 `category` 匹配（保持向下兼容）

---

#### 需求 2：简化下单流程（适老化优化）
**工作量**：半天  
**风险**：低  
**影响范围**：前端 `create.vue` + 用户体验  
**状态**：✅ 已完成（v1.1，提交 68eff6d）

**实施清单**：
- [x] 跳过 `service/detail.vue`：从 `didi.vue` 直接跳转 `create.vue`
- [x] 地址预填：从 `useUserStore().address` 自动填入地址输入框
- [x] 日期时间默认值：日期默认今天、时段默认最近可用时段
- [x] 细分选项折叠优化：复杂子表单简化，默认收起
- [x] 按钮优化："立即预约"、按钮加大、触觉反馈

---

### ⭐⭐ 中优先级（第二批）

#### 需求 1：接单人员增加类别
**工作量**：半天  
**风险**：低  
**影响范围**：数据库 + 后端接单逻辑 + 前端工人端  

**实施清单**：
- [x] 数据库：`workers` 表新增 `category` 字段（VARCHAR 20）
  - 可选值：超市老板 / 志愿者 / 社区工作人员 / 物业工作人员 / 其他
- [x] 后端：
  - [x] `Worker.java` 新增 `category` 字段
  - [x] `WorkerRegisterRequest.java` 新增 `category` 校验
  - [x] `WorkerServiceImpl.register()` 保存类别
  - [x] `WorkerServiceImpl.pendingOrders()` 增加智能排序：
    - 代购 → 优先推荐“超市老板”
    - 维修 → 优先推荐“物业工作人员”
    - 日间照护/外出陪同 → 优先推荐“志愿者”或“社区工作人员”
- [x] 前端：
  - [x] `worker/index.html` 注册表单增加类别下拉选择
  - [x] `order/detail.vue` 显示接单人类别标签（带图标）

---

#### 需求 4：长期订单支持
**工作量**：1 天  
**风险**：中（方案设计需确定）  
**影响范围**：数据库表结构 + 前后端全链路  

**方案决策**：
- 存储方案：单条主订单 + recurrence_rule JSON 字段（推荐）
- 或：拆成多条子订单（便于逐一接单）

**实施清单**：
- [x] 数据库：`orders` 表新增字段：
  - `order_type` VARCHAR(10) DEFAULT 'SINGLE'（SINGLE/RECURRING）
  - `date_start` VARCHAR(20)（长期订单起始日期）
  - `date_end` VARCHAR(20)（长期订单结束日期）
  - `recurrence_rule` VARCHAR(50)（重复规则：每天/每周/每周一三五 等）
- [x] 后端：
  - [x] `Order.java` 新增 4 个字段
  - [x] `CreateOrderRequest.java` 新增长期订单参数
  - [x] `OrderServiceImpl.createOrder()` 处理 `orderType` 字段
- [x] 前端：
  - [x] `create.vue` 增加“单次/长期”切换 Tab
  - [x] 长期订单模式下：显示日期区间 picker + 重复频率选择器
  - [x] `confirm.vue` 显示长期订单信息 + 传参到API
  - [x] `order/list.vue` 和 `detail.vue` 显示“长期”标签 + 日期范围
  - [x] 工人端显示长期订单标识

---

### ⭐ 低优先级（第三批）

#### 需求 3：语音下单（大模型）
**工作量**：1-2 天  
**风险**：中（依赖大模型输出稳定性）  
**影响范围**：AI 模块 + 下单流程  
**状态**：✅ 后端+前端已完成（v1.3，2026-09-12）；LLM/ASR 真实调用需在 application-dev.yml 配置有效 AI Key，真机流程待验证

**实施清单**：
- [x] 后端：`POST /api/ai/parse-order-intent`（音频可选 + text 调试参数）
  - [x] System Prompt 设计：限定返回 JSON Schema，动态注入服务目录与当前日期
  - [x] JSON 结构：`{serviceKey, date, timeSlot, address, remark, error?}`
  - [x] 走 IAiProvider 抽象（百度文心/腾讯混元均可，前端默认 BAIDU）
  - [x] 异常处理：provider 降级文案识别；JSON 解析失败/服务未识别 → `{error}` + queryText 兜底；过去日期/非法时段置 null
- [x] 前端：
  - [x] `didi.vue` 头部增加"🎤 语音下单"入口按钮
  - [x] 新建 `pages/order/voice-create.vue`：点击录音(60s上限) → 上传解析 → "我听到您说"结果确认卡（服务/日期/时段/地址/备注，缺项显示"到下一步再选"）→ 确认后跳 `create.vue`（带 serviceId/serviceName/servicePrice/date/slot/address/remark 预填）→ 失败显示友好提示 + 重试/手动选服务
  - [x] `create.vue` onLoad 支持 date/slot/address/remark 预填参数；语音带地址时不被用户档案地址覆盖
- [x] 测试：
  - [x] Mock LLM 单元测试 10 项全通过（`AiServiceImplTest`：围栏JSON提取/模糊服务匹配/中文时段映射/过去日期兜底/非法JSON/降级文案/ASR失败）
  - [x] 接口实测：文本模式、TTS合成语音模式、参数校验 400、无Key降级路径
  - [ ] 真实 AI Key 下的 10 条口语用例回归（待用户配置有效 Key 后执行）

---

## 技术债务清单

> 2026-09-12 安全与数据质量治理：除"账单真实数据对接"外全部处理完毕。

- [x] Worker 密码加密（BCrypt + `spring-security-crypto`；历史明文账号登录时自动比对并升级为哈希，init.sql 种子已替换为哈希）
- [x] 验证码登录真实校验（内存版 `SmsCodeService`：6位随机码、5分钟有效、60秒重发冷却、连错5次作废、一次性使用；演示模式接口回显 `devCode`，生产需关闭 `sms.code.return-in-response` 并接入真实短信网关）
- [x] FAQ 音频文件补全（Windows SAPI 中文TTS 预生成 `faq-001.wav` ~ `faq-006.wav`，接口改为 WAV 流；重新生成脚本 `generate_faq_audio.ps1`）
- [x] 账单查询明细化（按 用户+户号+账单月 确定性生成明细账单，前端结构化展示并标注演示数据；真实数据对接需公用事业API授权，替换 `BillQueryServiceImpl` 即可）
- [x] Git 历史密钥清理（2026-09-12 全历史扫描：历史 `application-dev.yml` 仅含 `YOUR_*` 占位符，教程文档中密钥为虚拟示例，**未发现真实密钥泄露，无需清理**）

---

## 开发规范提醒

### 后端开发检查清单
- [ ] DTO 参数校验（`@Valid` + `@NotNull` / `@NotBlank`）
- [ ] 异常统一抛 `BusinessException`
- [ ] 接口文档更新（Swagger 注解）
- [ ] 数据库脚本同步更新 `init.sql`

### 前端开发检查清单
- [ ] 适老化规范：按钮高度 ≥ 88rpx，字号 ≥ 32rpx
- [ ] 所有接口调用通过 `api/request.js`
- [ ] 用户反馈：加载中提示 + 成功/失败 toast
- [ ] 测试多场景：无网络、Token 过期、401 跳转

---

## 里程碑

| 版本 | 功能 | 目标日期 |
|------|------|----------|
| v1.1 | 服务名称润色 + 下单流程简化 | ✅ 已完成 |
| v1.2 | 接单人员类别 + 长期订单 | ✅ 已完成 |
| v1.2.x | 全站UI改版（银龄馨家青绿主题）+ 安全治理（BCrypt/短信验证码/FAQ音频/账单明细化） | ✅ 2026-09-12 |
| v1.3 | 语音下单（大模型意图解析，后端+前端完成，LLM真实调用待配置有效Key） | ✅ 2026-09-12 |
