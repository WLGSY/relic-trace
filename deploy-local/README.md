# 本机演示环境（内网穿透方案）

没有云服务器时，用这套脚本把整套系统在本机跑起来，再通过 Cloudflare 隧道对外提供一个
临时 HTTPS 域名。**不需要备案、不需要公网 IP、不需要路由器端口映射。**

## 一次运行的结构

```
手机 / 外网浏览器
      │  https://xxxx.trycloudflare.com
      ▼
Cloudflare 边缘节点
      │  隧道（本机主动外连，无需入站端口）
      ▼
cloudflared.exe（本机）
      │  http://127.0.0.1:80
      ▼
Nginx（80）── 静态文件 dist
      │  /api 反向代理
      ▼
Spring Boot（8080）── MySQL（3307）＋ uploads 目录
```

## 前置条件

| 依赖 | 要求 |
|---|---|
| MySQL | 本机服务已启动，监听 `3307`，库 `relic_db` 已导入 |
| 后端产物 | `relic-trace-boot/target/relic-trace-boot.jar` |
| 前端产物 | `frontend/dist/index.html` |
| JDK | 17 及以上（`java -version` 可用） |
| Nginx | `frontend/deploy/nginx-1.28.0/`（脚本会自动拉起） |
| cloudflared | `deploy-local/bin/cloudflared.exe`（已随目录提供） |

构建命令：

```bash
mvn clean package -DskipTests
cd frontend && npm run build
```

## 使用

1. 双击 `start-demo.bat`
2. 等 20~40 秒，窗口会打印一个 `https://xxx.trycloudflare.com` 地址
3. 把这个地址发给老师或自己用手机打开
4. 演示结束后双击 `stop-demo.bat` 释放进程

脚本会自动完成：

- 检查 MySQL / Nginx / 构建产物
- 启动后端，并把二维码跳转地址指向本次的隧道域名
- 用 admin 账号登录后调用 `/api/relics/qrcodes/rebuild` **重建全部二维码**
- 自检公网 `/api` 是否可达

## 演示账号

| 账号 | 密码 | 角色 |
|---|---|---|
| admin | 123456 | 系统管理员 |
| zhangly | 123456 | 录入员 |
| lishen | 123456 | 审核员 |

## 必须知道的限制

**1. 域名每次启动都会变。** 免费快速隧道不固定域名，所以每次启动后二维码内容都不同。
脚本会自动重建，但如果你的演示材料里印了旧网址，会打不开。想要固定域名有两种做法：
注册 Cloudflare 账号做 named tunnel（需要自己有个域名），或换成 cpolar/花生壳的固定二级域名。

**2. 电脑不能关机、不能休眠。** 隧道是反向连接，本机进程一停，公网地址立刻失效。
答辩前记得把电源计划里的休眠关掉。

**3. 首次打开可能等几秒。** Cloudflare 边缘节点需要一点时间同步新域名，遇到 502/530 刷新一次即可。

**4. 上传的图片存在本机。** 换电脑演示要连 `uploads/` 目录一起拷走。

**5. 这是演示环境，不是生产部署。** 内存只有 `-Xmx512m`，也没有 HTTPS 证书管理、
日志轮转、开机自启。正式上线请走服务器部署方案。

## 常见问题

**后端启动超时**
看 `deploy-local/logs/backend.log`。常见原因是 MySQL 没起，或 `relic_db` 没导入。

**页面能打开但接口 404**
说明 `/api` 没转到后端。检查 8080 是否在监听，以及 Nginx 的 `relic-app.conf` 里
`proxy_pass http://127.0.0.1:8080` 是否正确。

**手机扫码打不开摄像头**
必须是 `https://` 地址（本项目隧道天然是 HTTPS）。用 `http://` 局域网 IP 访问时，
浏览器会直接禁用 `getUserMedia`。

**提示端口 8080 被占用**
脚本会复用已有后端，但那个后端可能还指向旧的二维码域名。先在任务管理器里结束
`java.exe` 再重跑脚本。

**隧道地址一直不出来**
看 `deploy-local/logs/cloudflared.log`。公司网络封 UDP 时会退回 HTTP/2，属正常；
若完全连不上，说明出站 7844 端口被拦，需要换网络。
