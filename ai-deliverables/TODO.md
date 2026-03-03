# WeChat Mini Program — AI Improvement To-Do List

## High Priority

- [x] Fix `localStorage` → `uni.setStorageSync` in `store/user.js` and `api/request.js`
      → Already correct in original code; verified.
- [x] Add 401 handling + auto-logout in `api/request.js`
      → Clears token and redirects to login on 401/403.
- [x] Add logout button to profile page (`pages/user/profile.vue`)
      → Confirmation dialog + `userStore.logout()` call added.
- [x] Implement bill query page (`pages/query/index.vue`) — backend is ready
      → Full UI: type selector (电费/水费/有线电视), account input, result display, query history.
- [x] Add loading / error / empty states to all pages
      → Added to index, order list, order detail, profile pages.

## Medium Priority

- [x] Order cancel UI (`pages/order/list.vue` + `api/order.js`)
      → Confirmation modal + `cancelOrder()` API call on CREATED orders.
- [x] Order rate UI (`pages/order/list.vue` + `api/order.js`)
      → Action sheet (1–5 stars) + `rateOrder()` API call on COMPLETED orders.
- [x] Order detail page (`pages/order/detail.vue` + `api/order.js`)
      → New page showing full order info with cancel/rate actions; registered in pages.json.
- [x] Input validation on login (`pages/login/login.vue`)
      → Validates 11-digit phone (regex ^1\d{10}$) and 6-digit code; loading state added.
- [x] Use `ServiceCard` and `OrderCard` components properly
      → `index.vue` now uses <ServiceCard> with @click. `order/list.vue` uses <OrderCard>
         with @click-detail, @cancel, @rate. Inline duplication removed.
- [x] Externalize backend URL config
      → Created `api/config.js`; `request.js` imports BASE_URL from it.

## Low Priority

- [x] Match tab bar colors to warm theme in `pages.json`
      → selectedColor: #e76f51, color: #7d6b66, backgroundColor: #fff7f3.
      → Also added "查询" as 4th tab so the bill query page is directly reachable.
- [x] Add pagination to order and service lists
      → Client-side pagination (page size 5) on both service list and order list.
         Prev/next buttons; resets to page 1 on fresh load.
- [x] Profile editing (`pages/user/profile.vue`)
      → Inline edit mode for name and age; validates input before calling
         PUT /api/user/profile via new updateUserProfile() in api/user.js.

## AI Module & Recent Features

- [x] AI 双引擎问答（百度文心 ERNIE + 腾讯混元 Hunyuan）
      → IAiProvider 策略模式，前端可切换引擎；文本问答 + 语音问答全链路。
- [x] 全站语音操作按钮（VoiceActionSheet 组件）
      → 首页/订单/查询/个人中心均可唤起语音操作。
- [x] FAQ 常见问题列表 + 音频播放
      → 6 条 FAQ 硬编码 + 预录 MP3 音频端点（/api/ai/faq-audio/{id}）。
- [x] AI 问答历史记录（ai_query_logs 表 + GET /api/ai/history）
      → 后端存储文本问答日志，前端可查询历史。
- [x] 订单创建时保存联系电话（contact_phone 字段）
      → OrderServiceImpl 创建订单时自动存入下单用户手机号。
- [x] 订单详情显示联系电话 + 拨打按钮
      → COMPLETED/RATED 状态下展示 contactPhone，可直接拨打。
- [x] 订单详情显示服务人员信息（workerName + workerPhone）
      → 接单后关联查询 Worker 姓名与电话，detail.vue 展示并可拨打。
- [x] 常用地址管理（profile 编辑保存 + 创建订单快捷填入）
      → profile 页新增常用地址卡片，后端 + localStorage 双存储。
- [x] AI API 配置教程文档
      → 新增 `md/AI-API配置教程.md`，覆盖百度/腾讯密钥获取与配置流程。
