# v1.2.x 测试报告 — 安全治理 + 数据质量(2026-09-12)

> 对应提交:2c5eaf1..efb9272(BCrypt / 短信验证码 / FAQ音频 / 账单明细化 / 治愈青绿主题)
> 测试方式:后端启动 + HTTP 接口实测(curl) + MySQL 数据核对
> 结论:**后端 24 项检查全部通过;联调中发现并修复 3 个存量问题(见第四节)**

---

## 1. 短信验证码登录(新)

| # | 验证点 | 结果 |
|---|--------|------|
| 1 | POST /api/auth/sms-code 返回 devCode(演示模式) | ✅ |
| 2 | 错误验证码登录 → 401 验证码错误 | ✅ |
| 3 | 60 秒内重发 → 429 验证码发送太频繁 | ✅ |
| 4 | 非 6 位格式 → 401(格式由客户端/长度校验拦截) | ✅ |
| 5 | 正确验证码登录 → 返回 token/userId,新用户自动建档 | ✅ |
| 6 | 无 Token 访问受保护接口 → 401 未登录 | ✅ |
| 7 | Bearer Token 访问 /api/user/profile → 正常返回 | ✅ |
| 8 | 验证码一次性:复用已用验证码 → 401 已失效 | ✅ |

## 2. Worker 密码 BCrypt(新)

| # | 验证点 | 结果 |
|---|--------|------|
| 9 | 种子账号(18000000001/123456)登录成功,返回 category | ✅ |
| 10 | 错误密码 → 401 密码错误 | ✅ |
| 11 | Token 访问 /api/worker/orders/pending | ✅ |
| 12 | **明文自动升级:首登后查库,密码由 6 位明文变为 60 位 `$2a$10$` 哈希** | ✅ |

## 3. FAQ 语音 + 账单明细化 + 长期订单(新/回归)

| # | 验证点 | 结果 |
|---|--------|------|
| 13 | GET /api/ai/faq-list 返回 6 条 | ✅ |
| 14 | GET /api/ai/faq-audio/001 → `audio/wav`、728KB、`RIFF` 魔数 | ✅ |
| 15 | 不存在的音频 id → 404 | ✅ |
| 16 | 账单查询:同输入两次结果完全一致(确定性) | ✅ |
| 17 | 电费/水费/有线电视三类型均返回明细(用量×单价) + 户号打码 + 截止日期 + `simulated` 标记 | ✅ |
| 18 | 紧急联系人 save/list | ✅ |
| 19 | 政务事项 submit/list | ✅ |
| 20 | 长期订单创建:order_type=RECURRING + 日期区间 + 重复规则正确落库 | ✅ |

## 4. 联调中发现并修复的存量问题

1. **v1.2 迁移脚本从未在本地库生效**:本地 `workers` 缺 `category`、`orders` 缺 4 个长期订单列。根因:脚本使用 `ADD COLUMN IF NOT EXISTS`(MariaDB 语法,MySQL 8.0 不支持)。已手工补列 + 将迁移脚本重写为 information_schema 幂等版。
2. **Spring `-parameters` 编译标志缺失**(项目未用 spring-boot-starter-parent):Spring 6.1 起无法解析未写显式名称的 `@PathVariable`,导致 `/api/ai/faq-audio/{id}` 500。已在 pom 补 `maven.compiler.parameters=true`,并为 `faqAudio` 补显式参数名。
3. **displayName 被"旧库兼容"查询白名单过滤**:服务列表/详情/下单三处 `.select(...)` 排除了 `display_name` 列,导致 `/api/service/list` 返回 null、订单服务名回退为类目名(如"家务帮助"而非"洁净到家")。已移除垫片、create/update DTO 支持 displayName、didi.vue 传参改用友好名。

## 5. 待微信开发者工具/真机验证(自动化无法覆盖)

- [ ] 小程序登录页:获取验证码按钮 + 60 秒倒计时 + 演示模式弹窗展示 devCode
- [ ] FAQ 面板点播:确认微信 `InnerAudioContext` 正常播放 WAV(重点验证 iOS)
- [ ] 查询页:账单结果卡片结构化渲染(大字号金额/明细列表/演示数据标注)
- [ ] 治愈青绿新主题:导航栏/tabBar/渐变头图在真机上的观感(尤其 #A5F3FC 边框)
- [ ] didi.vue → 下单页:服务名传参为友好名(如"洁净到家")

## 6. 回归确认

- [x] 全量 `mvn clean compile` 通过
- [x] 联调产生的测试数据(测试用户/订单/联系人/政务/账单记录)已清理,库中仅保留 BCrypt 升级结果(属预期修复)
