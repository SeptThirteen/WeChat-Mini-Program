# 社区适老化服务小程序

> 面向老年用户的社区综合服务平台，提供"滴滴摇人"帮扶预约、公共服务缴费查询、AI 智能问答三大核心功能。
> 
> **技术栈**：uni-app (Vue 3 + Pinia) + Spring Boot 3 + MyBatis-Plus + MySQL 8

---

## 整体架构

```
wechat-elderly-mini         wechat-elderly-service
 (uni-app / Vue 3)    HTTP    (Spring Boot 3.2 / Java 17)
                    ──────►
  api/request.js              controller → service → mapper
  store/user.js               JWT Auth (AuthInterceptor)
  pages/**                    MySQL: wechat_elderly
```

**数据流**：前端通过 `api/request.js` 统一发起 HTTP 请求，自动附加 `Authorization: Bearer <token>` header；后端 `AuthInterceptor` 拦截 `/api/**` 路径进行 JWT 校验；响应统一为 `{ code, message, data }` 信封。

---

## 目录结构

```
WeChat-Mini-Program/
├── wechat-elderly-mini/        # 前端（uni-app）
│   ├── api/                    # API 模块（auth/service/order/bill/user）
│   ├── pages/
│   │   ├── index/index.vue     # 首页（3 大分类入口）
│   │   ├── home/               # 一级分类子页
│   │   │   ├── didi.vue        # 滴滴摇人服务选择
│   │   │   ├── public-service.vue  # 公共服务入口
│   │   │   └── ai-chat.vue     # AI 问答入口
│   │   ├── order/              # 订单流程（create→confirm→success→list→detail）
│   │   ├── query/index.vue     # 便民缴费查询
│   │   └── user/               # 个人中心（profile/bills/emergency/gov-tasks）
│   ├── store/user.js           # Pinia 用户状态（token/userId）
│   ├── styles/theme.scss       # 适老化设计 Token（字号/颜色/按钮尺寸）
│   └── api/config.js           # BASE_URL 配置（本地调试需修改）
│
└── wechat-elderly-service/     # 后端（Spring Boot）
    ├── controller/             # REST API 控制器
    ├── service/impl/           # 业务逻辑实现
    ├── mapper/                 # MyBatis-Plus BaseMapper
    ├── entity/                 # 实体（User/ServiceItem/Order/BillQuery）
    ├── dto/                    # 请求 DTO（含 @Valid 校验）
    ├── common/                 # ApiResponse / BusinessException / GlobalExceptionHandler
    ├── config/                 # AuthInterceptor / JwtUtil / WebConfig
    └── resources/db/init.sql   # 数据库初始化脚本
```

---

## 快速启动

### 前置条件

- JDK 17+、Maven 3.6+
- Node.js 16+、npm
- MySQL 8.0+
- HBuilderX（运行微信小程序）或微信开发者工具

### 1. 初始化数据库

```sql
-- 执行初始化脚本（含建表 + 种子数据）
mysql -u root -p < wechat-elderly-service/src/main/resources/db/init.sql
```

### 2. 启动后端

```bash
cd wechat-elderly-service
mvn spring-boot:run
```

启动成功后访问：
- 健康检查：`http://localhost:8080/api/health`
- Swagger UI：`http://localhost:8080/swagger-ui.html`

### 3. 配置前端接口地址

修改 `wechat-elderly-mini/api/config.js` 中的 `BASE_URL`，将 IP 改为本机局域网地址（微信小程序不支持 localhost）：

```js
const BASE_URL = 'http://192.168.x.x:8080';  // 改为实际 IP
```

### 4. 启动前端

```bash
cd wechat-elderly-mini
npm install
npm run dev:mp-weixin   # 微信小程序
# 或
npm run dev:h5          # H5 浏览器调试
```

在 HBuilderX 中打开 `wechat-elderly-mini` 目录（注意不是仓库根目录），右键"重新识别项目类型"后运行到微信开发者工具。

---

## API 接口一览

| 接口 | 方法 | 需要 Token | 说明 |
|------|------|-----------|------|
| `/api/auth/login` | POST | ❌ | 手机号+验证码登录（测试：任意6位数字） |
| `/api/health` | GET | ❌ | 健康检查 |
| `/api/service/list` | GET | ❌ | 获取服务列表 |
| `/api/service/{id}` | GET | ❌ | 获取服务详情 |
| `/api/order/create` | POST | ✅ | 创建订单（`userId` + `serviceId`） |
| `/api/order/list` | GET | ✅ | 获取订单列表（`?userId=`） |
| `/api/order/{id}` | GET | ✅ | 获取订单详情 |
| `/api/order/{id}/cancel` | POST | ✅ | 取消订单 |
| `/api/order/{id}/rate` | POST | ✅ | 评价订单 |
| `/api/bill/query` | POST | ✅ | 费用查询（`userId` + `queryType`） |
| `/api/user/profile` | GET | ✅ | 获取用户信息（`?userId=`） |
| `/api/user/update` | PUT | ✅ | 更新用户信息（`userId` + 可选字段） |
| `/api/ai/faq-list` | GET | ❌ | 获取常见问题列表（6 条） |
| `/api/ai/faq-audio/{id}` | GET | ❌ | 获取 FAQ 音频流（需放置 MP3 文件） |
| `/api/ai/text-query` | POST | ✅ | 文本 AI 问答（`userId/provider/intent/text`） |
| `/api/ai/voice-query` | POST | ✅ | 语音 AI 问答（multipart 音频 + 参数） |
| `/api/ai/history` | GET | ✅ | 查询历史记录（`?userId=&limit=`） |

> **注意**：`/api/user/update` 是更新接口的实际路径（非 `/api/user/profile`）。

### 登录响应示例

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "token": "eyJ...",
    "userId": 1,
    "phone": "13800000000",
    "name": "张三"
  }
}
```

---

## 数据库表结构

| 表名 | 主要字段 | 说明 |
|------|---------|------|
| `users` | `user_id` / `phone` / `name` / `age` / `address` | 用户信息，登录时自动创建 |
| `services` | `service_id` / `category` / `description` / `price` | 服务项目（滴滴摇人4类+基础3类） |
| `orders` | `order_id` / `user_id` / `service_id` / `status` / `contact_phone` / `worker_id` | 订单，status: `CREATED/ASSIGNED/COMPLETED/RATED/CANCELLED`；含联系电话和服务人员关联 |
| `bill_queries` | `query_id` / `user_id` / `query_type` / `result_snapshot` | 缴费查询记录（当前为模拟数据） |
| `ai_query_logs` | `log_id` / `user_id` / `provider` / `intent` / `query_text` / `response_text` | AI 问答日志（仅存文本，不存音频） |

---

## 适老化设计规范

所有 UI 样式变量定义于 `styles/theme.scss`：

```scss
$fontSize-title: 34px;  // 页面大标题
$fontSize-lg:    24px;  // 卡片标题
$fontSize-md:    20px;  // 按钮/导航文字
$fontSize-base:  18px;  // 正文基础字号
$fontSize-sm:    16px;  // 说明文字

$color-primary: #e76f51;  // 主色（橙红）
$color-bg:      #fff7f3;  // 页面背景（暖白）
$btn-height:    56px;     // 按钮最小高度
```

---

## 开发规范

### 后端
- 所有接口返回 `ApiResponse<T>` 信封（`code/message/data`），`code=0` 表示成功
- 业务异常使用 `throw new BusinessException(code, "中文消息")`，由 `GlobalExceptionHandler` 统一处理
- 新增接口需同时更新 controller / service / mapper 三层
- JWT claims 包含 `userId` 和 `phone`，通过 `JwtUtil` 生成和解析

### 前端
- 所有接口调用必须通过 `api/request.js` 中的 `request()` 函数，不得使用裸 `uni.request`
- 登录态通过 `useUserStore()` 获取，持久化在 `uni.storage`（key: `token`, `userId`）
- 新增全局状态只扩展 `useUserStore`，不引入新的 store

---

## 已知限制

- 账单查询（`/api/bill/query`）返回模拟金额（¥128.50），不是真实账单数据
- AI 问答已接入双引擎策略（百度文心 / 腾讯混元），需在 `application-dev.yml` 中填入真实 API Key 方可使用（详见 `md/AI-API配置教程.md`）
- FAQ 语音播报需在 `src/main/resources/static/audio/` 放置 `faq-001.mp3` ~ `faq-006.mp3` 文件
- 验证码登录跳过实际校验，测试时输入任意 6 位数字即可
- Worker 密码为明文存储（测试项目简化）
