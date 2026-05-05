# 项目交接文档（新对话上下文）

## 项目概览

社区适老化服务小程序，面向老年用户。三大功能：**一呼馨家**（帮扶预约）、**公共服务**（缴费查询）、**AI问答**（双引擎智能对话）。

| 端 | 技术栈 | 目录 |
|---|---|---|
| 微信小程序/H5 | uni-app + Vue 3 + Pinia | `wechat-elderly-mini/` |
| REST API | Spring Boot 3.2 + MyBatis-Plus + MySQL 8 | `wechat-elderly-service/` |
| 服务人员网页 | 纯 HTML/JS（Spring Boot 静态资源） | `resources/static/worker/index.html` |

- **后端端口**：8080 | **DB**：`wechat_elderly`，root/123456
- **JWT**：claims 含 userId/phone，Worker 端额外含 role=worker/workerId
- **API 响应信封**：`{ code, message, data }`，code=0 为成功
- **公开路由白名单**：`/api/auth/login`、`/api/worker/login`、`/api/service/list`、`/api/service/*`、`/api/health`、`/api/ai/faq-list`、`/api/ai/faq-audio/*`

---

## 当前完成状态

### ✅ 已完成（全部功能模块）

**订单全链路**
- orders 表 14 字段：含 service_name / scheduled_date / scheduled_slot / address / remark / rating / worker_id / assigned_time / **contact_phone**
- 状态流：`CREATED → ASSIGNED（接单）→ COMPLETED → RATED` / `CANCELLED`
- 订单列表 4 Tab 筛选（全部/进行中/已完成/已取消）
- OrderCard 显示服务名；detail 页展示预约信息、评分
- **订单详情显示联系电话**：创建时自动存入用户手机号，完成/评价状态可拨打
- **订单详情显示服务人员信息**：接单后关联查询 Worker 姓名与电话，可直接拨打

**AI 问答模块（双引擎）**
- 策略模式：`IAiProvider` → `BaiduAiServiceImpl`（百度文心 ERNIE）+ `TencentAiServiceImpl`（腾讯混元）
- 前端切换引擎（百度文心 / 腾讯混元），5 大功能按钮：政策咨询、健康养生、生活帮助、防诈提醒、自由提问
- 语音问答：录音 → 上传音频 → 后端识别 → AI 回答 → 前端语音播报
- FAQ 常见问题列表 + 预录音频播放
- 问答历史记录查询
- 全站语音操作按钮（VoiceActionSheet 组件，各页面均可唤起）
- AI 密钥配置教程：`md/AI-API配置教程.md`

**服务人员抢单网页端**
- 访问：`http://localhost:8080/worker/index.html`
- 测试账号：`18000000001 / 123456`，`18000000002 / 123456`
- 功能：登录 → 待抢单（10s 轮询）→ 接单 → 完成；卡片展示用户姓名/电话/地址/备注

**个人中心增强**
- 常用地址管理：profile 页编辑/保存常用地址（后端 + localStorage 双存储）
- 创建订单时可一键填入常用地址
- 个人信息编辑（姓名/年龄/社区）

**其他模块**
- 政务代办：`government_tasks` 表 + CRUD + `pages/user/gov-tasks.vue`
- 紧急联系人：`emergency_contacts` 表 + CRUD（含编辑弹窗，已从 mock 改为后端持久化）
- 缴费历史：`GET /api/bill/history?userId=`（列表展示）
- 社区字段：`users.community`，profile 页动态展示

### ⬜ 未完成（论文材料）

- 适老化 UI 优化（字体缩放、高对比度无障碍）
- 论文材料：ER 图、需求分析、测试报告

---

## 数据库表（8张）

| 表 | 关键字段 / 说明 |
|---|---|
| `users` | user_id / phone / name / age / address / **community** |
| `services` | service_id / category / description / price（7条种子数据）|
| `orders` | 14字段，含 **worker_id / assigned_time / contact_phone**；FK→users,services,workers |
| `bill_queries` | query_id / user_id / query_type / result_snapshot（返回模拟金额）|
| `government_tasks` | task_id / user_id / task_type / task_desc / status（SUBMITTED/PROCESSING/COMPLETED/REJECTED）|
| `emergency_contacts` | contact_id / user_id / name / phone / relation（每用户最多2条）|
| `workers` | worker_id / phone（唯一）/ name / password（明文，测试用）|
| `ai_query_logs` | log_id / user_id / provider / intent / query_text / response_text / created_time（AI 问答日志）|

**⚠️ 重要：init.sql 导入必须用 cmd，不能用 PowerShell 管道（会损坏中文字符）：**

```bash
cmd /c "mysql -uroot -p123456 --default-character-set=utf8mb4 < wechat-elderly-service\src\main\resources\db\init.sql"
```

---

## 关键文件路径

```
wechat-elderly-mini/
  api/config.js             ← BASE_URL（局域网调试需改为实际 IP）
  api/request.js            ← 统一请求封装，code≠0 reject，401 清 token
  api/order.js / gov.js / contact.js / bill.js / user.js / auth.js / service.js
  store/user.js             ← Pinia，token/userId，uni.storage 持久化
  styles/theme.scss         ← 适老化变量（$color-primary: #e76f51，$btn-height: 56px）
  pages/home/ai-chat.vue    ← AI 问答页（双引擎切换 + 5 功能按钮 + 语音录入 + FAQ）

wechat-elderly-service/src/main/
  java/.../common/ApiResponse.java         ← success(data) / error(code, message)
  java/.../common/BusinessException.java   ← throw new BusinessException(code, "中文")
  java/.../config/AuthInterceptor.java     ← 仅验签，不注入 userId 到 request
  java/.../config/WebConfig.java           ← 白名单路由（修改这里添加公开路由）
  java/.../controller/AiController.java    ← AI 问答 5 个端点（text/voice/faq/history）
  java/.../service/ai/IAiProvider.java     ← AI 引擎策略接口
  java/.../service/ai/BaiduAiServiceImpl.java  ← 百度文心 ERNIE 实现
  java/.../service/ai/TencentAiServiceImpl.java ← 腾讯混元实现
  java/.../util/JwtUtil.java               ← generateToken(Map) / parseToken(token)
  resources/application.yml               ← port:8080，server.servlet.encoding.force:true
  resources/application-dev.yml           ← DB 连接 + AI 密钥配置（占位符，需替换）
  resources/db/init.sql                   ← 8张表 + 种子数据
  resources/static/worker/index.html      ← 服务人员网页端
```

---

## 常用命令

```bash
# 重新初始化数据库（用 cmd，非 PowerShell）
cmd /c "mysql -uroot -p123456 --default-character-set=utf8mb4 < wechat-elderly-service\src\main\resources\db\init.sql"

# 启动后端（先 cd 进子目录）
cd wechat-elderly-service
mvn spring-boot:run

# 前端小程序开发
cd wechat-elderly-mini
npm run dev:mp-weixin

# 前端 H5 开发
npm run dev:h5

# 健康检查
curl http://localhost:8080/api/health

# 服务人员网页
http://localhost:8080/worker/index.html
```

---

## 已知注意事项

| 问题 | 说明 |
|---|---|
| PowerShell 终端中文乱码 | 仅显示问题，DB 和 API 数据均为正确 UTF-8（HEX 已验证）|
| Worker 密码明文 | 测试项目简化，不引入 BCrypt |
| auth 验证码任意通过 | 跳过真实 OTP，输入任意数字即可 |
| Bill 金额模拟 | `/api/bill/query` 返回 ¥128.50，非真实账单 |
| AI 密钥为占位符 | `application-dev.yml` 中 AI Key 需替换为真实值，参见 `md/AI-API配置教程.md` |
| 启动后端必须 cd 进子目录 | 在项目根目录 `mvn spring-boot:run` 会报 No plugin found 错误 |

---

## Git 状态

- **分支**：`develop`
- **远程**：`https://github.com/SeptThirteen/WeChat-Mini-Program`
- **本地 git 用户**：sept_thirteen / 1423845155@qq.com
