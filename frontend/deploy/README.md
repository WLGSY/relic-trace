# 前端部署说明（Nginx 托管 dist）

本目录用于**生产/演示环境**部署：前端不再依赖 Node 进程（不用 `npm run dev`），
由 Nginx 托管 `npm run build` 产出的静态文件，并把 `/api` 反向代理到 Spring Boot。

## 一、运行架构

```
浏览器
  │  http://localhost            （80 端口，本机调试）
  │  https://10.19.6.8           （443 端口，手机/局域网设备）
  ▼
┌─────────────────────────────────────────┐
│  Nginx (端口 80 / 443)                   │
│  ├─ /            → E:/.../relic-trace/frontend/dist   （静态文件）
│  └─ /api/*       → http://127.0.0.1:8080/api/*   （反向代理）
└─────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────┐
│  Spring Boot (端口 8080, context-path=/api) │
│  └─ MySQL (127.0.0.1:3307 / relic_db)   │
└─────────────────────────────────────────┘
```

两个 server 共用同一份 `conf/snippets/relic-app.conf`（静态托管 + `/api` 反代），
改 location 只需改这一处。

要点：
- 前端 `VITE_API_BASE=/api` 是**相对路径**，打包后天然同源，无需修改任何接口地址。
- 后端 `server.servlet.context-path=/api`，所以 Nginx 把 `/api/xxx` 原样转发到 `8080/api/xxx`。
- 前端使用 **hash 路由**（`createWebHashHistory`），刷新页面不会 404，Nginx 无需额外的 history 回退规则。
- 443 存在的唯一理由是**手机端摄像头**：浏览器规定 `getUserMedia` 只能在安全上下文（HTTPS / localhost）中调用。

## 二、目录结构

```
relic-trace/frontend/
├── dist/                      # 前端打包产物（Nginx 的 root）
├── deploy/
│   ├── nginx-1.28.0/          # Nginx 免安装版
│   │   ├── nginx.exe
│   │   ├── conf/nginx.conf    # 已按本项目配置好（80 + 443）
│   │   ├── conf/snippets/relic-app.conf  # 两个 server 共用的 location 片段
│   │   └── logs/              # 运行日志（error.log / access.log）
│   ├── certs/                 # 自签 HTTPS 证书（relic.crt / relic.key）
│   ├── start-nginx.bat        # 一键启动
│   └── stop-nginx.bat         # 一键停止
└── src/                       # 前端源码
```

## 三、启动步骤

### 1. 启动后端（必须先起）

```bash
cd E:/毕业设计/relic-trace
mvn -pl relic-trace-boot spring-boot:run
```
或在 IDEA 中运行 `relic-trace-boot` 模块的 `RelicTraceApplication`。

确认：`http://localhost:8080/api/auth/login` 可访问（直接访问可能返回 405，属正常）。

### 2. 启动 Nginx

双击 `deploy/start-nginx.bat`，或在命令行执行：

```bash
cd E:/毕业设计/relic-trace/frontend/deploy/nginx-1.28.0
nginx.exe
```

### 3. 访问系统

浏览器打开 **http://localhost**

演示账号（密码均为 `123456`）：

| 账号 | 角色 |
|------|------|
| admin | 系统管理员 |
| zhangly | 录入员 |
| lishen | 审核员 |

停止 Nginx：双击 `deploy/stop-nginx.bat`，或 `nginx.exe -s quit`。

### 4. 手机 / 局域网设备访问（HTTPS）

手机要用**页面内摄像头扫码**，必须走 HTTPS——浏览器规定 `getUserMedia` 只在安全上下文
（HTTPS 或 localhost）可用，`http://局域网IP` 下 `navigator.mediaDevices` 会被整个隐藏。

```bash
# 1) 查电脑 WLAN 的 IPv4（不要用 VMware 虚拟网卡的 192.168.x.x）
ipconfig

# 2) 手机连同一个 WiFi，浏览器打开（首次有证书警告）
https://10.19.6.8
```

首次访问的证书警告是自签证书的正常现象：

- Android Chrome：`高级` → `继续前往 10.19.6.8（不安全）`
- iOS Safari：`显示详细信息` → `访问此网站`

> 微信内置浏览器对自签 HTTPS 拦截较严，扫码演示建议用**手机自带相机**，或复制链接到浏览器打开。

二维码里的地址由后端配置决定，换机器/换网络时要一起改：

| 文件 | 配置项 | 说明 |
|------|--------|------|
| `relic-trace-boot/src/main/resources/application.yml` | `relic.qr.base-url` | 默认 `https://10.19.6.8`，可用环境变量 `RELIC_QR_BASE_URL` 覆盖 |

改完需**重启后端**，再重建全部二维码（否则旧码里还是上一个地址）：

```bash
curl -X POST http://127.0.0.1:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"loginName":"admin","loginPassword":"123456"}'
# 取返回的 data.token，替换下面的 <TOKEN>

curl -X POST http://127.0.0.1:8080/api/relics/qrcodes/rebuild \
     -H "Authorization: Bearer <TOKEN>"
```

更换 IP 时三件事一起做：**重新生成证书（SAN 写入新 IP）→ 改 `base-url` → 重建二维码**。

重新生成证书：

```bash
cd E:/毕业设计/relic-trace/frontend/deploy/certs
MSYS_NO_PATHCONV=1 openssl req -x509 -newkey rsa:2048 -sha256 -days 3650 -nodes \
  -keyout relic.key -out relic.crt \
  -subj "/C=CN/ST=Hunan/L=Changsha/O=RelicTrace/CN=10.19.6.8" \
  -addext "subjectAltName=DNS:localhost,IP:127.0.0.1,IP:10.19.6.8"
```

证书重新生成后执行 `nginx.exe -s reload`。

## 四、前端改动后如何重新发布

```bash
cd E:/毕业设计/relic-trace/frontend
npm run build        # 产出新的 dist
```

**Nginx 无需重启**（每次请求都读磁盘文件），刷新浏览器即可看到新版本。
`index.html` 已配置为不缓存，`/assets/` 下带 hash 的文件长缓存，发版不会出现白屏或旧文件残留。

> 若 `npm run build` 报 `dist` 目录清理失败，可先重命名旧目录：
> `mv dist dist_old && npm run build`

## 五、常见问题

| 现象 | 原因 | 处理 |
|------|------|------|
| 打开 http://localhost 显示 502 | 后端未启动或端口不对 | 先启动 Spring Boot（8080） |
| 页面白屏、控制台 404 | `dist` 不是最新构建 | 重新 `npm run build` |
| 登录返回 401 或"账号或密码错误" | 数据库未连接 / 密码哈希不匹配 | 检查 `application.yml` 的库名 `relic_db` 与账号 |
| Nginx 启动后立即退出 | 80 端口被占用（IIS、迅雷等） | `netstat -ano \| findstr :80` 查占用，或改 `listen` 端口 |
| 修改了 nginx.conf 未生效 | 未重载 | `nginx.exe -s reload` |
| 手机打开提示"您的连接不是私密连接" | 自签证书不受系统信任 | 点「高级 / 显示详细信息」→「继续前往」 |
| 手机扫码页提示"http 明文地址，浏览器已禁用摄像头" | 用 http 访问了 | 改用 `https://电脑IP` |
| 二维码扫出来的还是旧地址 | 改完 `base-url` 未重启后端，或未重建 | 重启后端后调 `POST /api/relics/qrcodes/rebuild` |
| 手机打不开 http://电脑IP | Nginx 未启动 / 防火墙拦截 | 确认 80、443 在监听，防火墙放行 nginx |

## 六、生产环境加固建议（可选）

1. **只允许内网访问 8080**：后端不必对公网暴露，仅由 Nginx 转发。
2. **HTTPS**：本项目已内置自签证书与 443 入口（见第三节第 4 小节），正式对外上线时替换为 CA 签发的证书即可，`nginx.conf` 只需改 `ssl_certificate` 两个路径。
3. **隐藏版本号**：`http` 块加 `server_tokens off;`。
4. **CORS 收紧**：生产环境把后端 `WebConfig` 的 `allowedOriginPatterns("*")` 改为具体域名。
5. **JWT 密钥**：通过环境变量 `RELIC_JWT_SECRET` 覆盖 `application.yml` 中的默认值。

## 七、相关文件

- Nginx 配置：`deploy/nginx-1.28.0/conf/nginx.conf`
- 后端配置：`relic-trace-boot/src/main/resources/application.yml`
- 前端环境变量：`frontend/.env.production`（`VITE_API_BASE=/api`）
