# 部署文档(Docker Compose)

> 适用场景:云服务器(建议 Ubuntu 22.04 / Debian 12,1核2G 起步)上部署"银龄馨家"后端,
> 小程序以**体验版 + 真机调试模式**连接服务器公网 IP,用于毕业答辩演示。
> 不需要域名、不需要备案、不需要 HTTPS(免调试模式上线属可选项,见文末)。

---

## 一、服务器初始化(首次约 15 分钟)

### 1. 安装 Docker 与 Compose 插件

```bash
# 官方脚本(国内服务器若慢,用下面的阿里云镜像脚本)
curl -fsSL https://get.docker.com | bash -s docker

# 国内加速备选:
# curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun

# 启动并设开机自启
systemctl enable --now docker

# 验证
docker --version && docker compose version
```

### 2. 配置国内镜像加速(强烈建议)

```bash
mkdir -p /etc/docker
cat > /etc/docker/daemon.json << 'EOF'
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://dockerproxy.net",
    "https://docker.1ms.run"
  ]
}
EOF
systemctl restart docker
```

### 3. 放行防火墙/安全组

- 云厂商控制台**安全组**:放行 TCP `22`(SSH)、`8080`(后端)
- 服务器系统防火墙(如启用):`ufw allow 22/tcp && ufw allow 8080/tcp`

---

## 二、获取代码并配置密钥

```bash
# 拉代码(或本地打包上传整个 wechat-elderly-service 目录)
git clone https://github.com/SeptThirteen/WeChat-Mini-Program.git
cd WeChat-Mini-Program/wechat-elderly-service

# 配置密钥(复制模板后编辑)
cp .env.example .env
vim .env
```

`.env` 必填项与生成方式:

| 变量 | 说明 | 生成 |
|---|---|---|
| `MYSQL_ROOT_PASSWORD` | 数据库 root 密码 | `openssl rand -base64 18` |
| `JWT_SECRET` | JWT 签名密钥 | `openssl rand -base64 48` |
| `AI_BAIDU_API_KEY` 等 | AI 引擎密钥(可选) | 控制台申请,不填则语音/AI 走降级提示 |
| `SMS_CODE_RETURN_IN_RESPONSE` | 演示期保持 `true` | 接真实短信后才改 `false` |

> ⚠️ `.env` 已被 gitignore,严禁提交或外传。

---

## 三、一键部署

```bash
# 构建镜像并后台启动(app 构建约 3-6 分钟,MySQL 拉镜像约 1-2 分钟)
docker compose up -d --build

# 观察启动进度,直到看到 app 容器 Started
docker compose logs -f app
```

启动顺序:MySQL 先初始化(自动执行 `init.sql` + 迁移脚本建表)→ 健康检查通过 → app 启动。

**验证**:

```bash
curl http://127.0.0.1:8080/api/health
# 期望: {"status":"ok","service":"wechat-elderly-service"}

# 外网验证(本机或手机浏览器):
curl http://<服务器公网IP>:8080/api/health
```

前端侧:把 `wechat-elderly-mini/api/config.js` 的 `BASE_URL` 改为 `http://<服务器公网IP>:8080`,
`npm run build:mp-weixin` 打包,微信开发者工具上传代码 → 版本管理设为**体验版** →
手机(需加为体验成员)扫码后点右上角"···"→ **打开调试**(跳过域名校验),即可真机访问。

---

## 四、日常运维

```bash
# 看实时日志
docker compose logs -f app          # 后端
docker compose logs -f mysql        # 数据库

# 修改代码后更新部署(重新构建 app,数据不丢)
docker compose up -d --build app

# 备份数据库(答辩前建议做一次)
docker compose exec mysql sh -c 'mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" wechat_elderly' > backup-$(date +%F).sql

# 恢复备份
docker compose exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" wechat_elderly' < backup-2026-09-13.sql

# 停止 / 重启
docker compose down        # 停止(数据卷保留)
docker compose up -d       # 再次启动
```

服务器重启后所有容器由 `restart: always` 自动拉起,无需人工干预。

**工人端**(浏览器直访):`http://<服务器公网IP>:8080/worker/index.html`

---

## 五、常见问题

| 现象 | 原因与处理 |
|---|---|
| `docker compose` 拉镜像超时 | 镜像加速未生效:检查 `/etc/docker/daemon.json` 后 `systemctl restart docker`;或换文档中其他镜像源 |
| app 反复重启 | `docker compose logs app` 看异常;最常见是 `.env` 未填/MySQL 未就绪(会自动重试) |
| 手机真机连不上后端 | ①安全组未放行 8080 ②体验版未打开"调试模式" ③`BASE_URL` 写错(要公网 IP) |
| 验证码接口 429 | 60 秒重发冷却,属正常防护 |
| AI/语音功能提示不可用 | `.env` 里 AI 密钥未填或失效,主备引擎会自动尝试另一家 |
| 想重置全部数据 | `docker compose down -v`(⚠️删除数据卷)后重新 `up -d` |

---

## 六、可选项(答辩后如需正式发布)

正式发布小程序要求 HTTPS + 备案域名 + 域名进微信 request 合法域名白名单:
1. 购买域名并 ICP 备案(约 1-2 周)
2. 用 Caddy 反代 8080 自动签发 HTTPS 证书
3. `.env` 中 `SMS_CODE_RETURN_IN_RESPONSE=false` 并接入真实短信网关
4. 微信公众平台配置 request 合法域名,小程序提审发布
