# 功能开发路线图

## 待开发需求

### ⭐⭐⭐ 高优先级（立即实施）

#### 需求 5：服务名称润色
**工作量**：1 小时  
**风险**：极低  
**影响范围**：数据库 + 前端展示  

**实施清单**：
- [ ] 数据库：`services` 表新增 `display_name` 字段（VARCHAR 50）
- [ ] 更新种子数据，为 7 个服务填入友好名称：
  - 日间照护 → 暖心陪伴
  - 外出陪同 → 贴心出行
  - 代购 → 跑腿帮买
  - 维修 → 上门维修
  - 家务帮助 → 洁净到家
  - 基础陪护 → 就医陪护
  - 健康测量 → 健康小站
- [ ] 后端：`Service.java` 新增 `displayName` 字段
- [ ] 前端：`didi.vue`、`index.vue`、`service/detail.vue` 使用 `displayName` 展示
- [ ] `create.vue` 的 `detectServiceKey` 匹配逻辑改为按 `category` 匹配（保持向下兼容）

---

#### 需求 2：简化下单流程（适老化优化）
**工作量**：半天  
**风险**：低  
**影响范围**：前端 `create.vue` + 用户体验  

**实施清单**：
- [ ] 跳过 `service/detail.vue`：从 `didi.vue` 直接跳转 `create.vue`（已部分支持，验证并优化）
- [ ] 地址预填：从 `useUserStore().address` 自动填入地址输入框
- [ ] 日期时间默认值：
  - 日期默认选中"今天"
  - 时间段默认选中当前时间之后的最近时段（09:00 前 → 上午，14:00 前 → 中午，否则 → 下午）
  - 精确时间滚轮默认值同步时段
- [ ] 细分选项折叠优化：
  - 日间照护/外出陪同/代购/维修 4 个区域默认收起
  - 增加"更多需求"展开按钮（可选）
  - 或简化为"补充说明"单一备注框，移除复杂子表单
- [ ] 按钮优化：
  - "下一步"按钮改为"立即预约"
  - 按钮高度增加到 100rpx
  - 字号增加到 36rpx
  - 增加触觉反馈（`uni.vibrateShort()`）

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

**实施清单**：
- [ ] 后端：新增 `AiController.parseOrderIntent(audioFile)` 接口
  - [ ] System Prompt 设计：限定返回 JSON Schema
  - [ ] JSON 结构：`{serviceKey, date, timeSlot, address, remark, error?}`
  - [ ] 调用腾讯混元 API，约束 response_format（如支持）
  - [ ] 异常处理：返回 `{error: "无法理解"}`
- [ ] 前端：
  - [ ] `didi.vue` 或 `index.vue` 增加"语音下单"大按钮入口
  - [ ] 新建 `pages/order/voice-create.vue` 页面：
    - [ ] 长按录音组件（复用 ai-chat.vue 逻辑）
    - [ ] 提交音频到 `/api/ai/parseOrderIntent`
    - [ ] 解析返回 JSON，自动填充 `create.vue` 表单
    - [ ] 展示识别结果供用户确认："我听到您说：明天上午修水龙头"
    - [ ] 确认后跳转 `create.vue`（带预填参数）
    - [ ] 识别失败显示友好提示 + 重试按钮
- [ ] 测试：
  - [ ] 准备 10 条测试语音文本
  - [ ] 验证 JSON 输出格式稳定性
  - [ ] 兜底逻辑覆盖率测试

---

## 技术债务清单

- [ ] Worker 密码加密（当前明文存储，生产环境需用 BCrypt）
- [ ] 验证码登录真实校验（当前任意 6 位数字通过）
- [ ] FAQ 音频文件补全（当前 `/static/audio/` 目录为空）
- [ ] 账单查询真实数据对接（当前返回模拟金额）
- [ ] Git 历史密钥清理（考虑 git filter-branch 或重新生成腾讯密钥）

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
| v1.1 | 服务名称润色 + 下单流程简化 | 当前 Sprint |
| v1.2 | 接单人员类别 + 长期订单 | 下一 Sprint |
| v1.3 | 语音下单（大模型） | 未来规划 |
