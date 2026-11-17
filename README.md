# 文物藏品管理与溯源系统 · 聚合工程

前后端同仓管理。后端采用 Maven 聚合多模块组织，前端为 Vue 3 独立工程。

## 一、目录结构

```
relic-trace/
├── pom.xml                     聚合工程（packaging=pom），统一依赖版本
├── common/                     公共基础模块
│   └── src/main/java/com/relictrace/
│       ├── common/             统一响应、异常、JWT、登录上下文、分页、操作日志切面
│       ├── po/                 数据库实体（BaseEntity、VersionedEntity + 18 张表对应 DO）
│       ├── mapper/             MyBatis-Plus 数据访问接口
│       ├── query/              PageQuery 通用分页条件
│       └── config/             MyBatis-Plus、对象填充、Web MVC、密码加密配置
├── user-service/               用户服务模块
│   └── src/main/java/com/relictrace/
│       ├── controller/         AuthController、SystemController
│       ├── service/ + impl/    AuthService、SystemService
│       ├── dto/ vo/            LoginDTO、LoginVO、UserInfoVO
│       └── config/             DataInitializer（预置管理员账号）
├── relic-service/              藏品服务模块
│   └── src/main/java/com/relictrace/
│       ├── controller/         征集、藏品、库房、修复、借调、注销（6 个）
│       ├── service/ + impl/    对应 6 个业务服务
│       ├── query/              RelicQuery
│       └── vo/                 RelicVO、AttachmentVO、TraceVO
├── trace-service/              溯源服务模块
│   └── src/main/java/com/relictrace/
│       ├── controller/         AuditController、StatsController
│       ├── service/ + impl/    AuditService、StatsService
│       ├── dto/ vo/            AuditDTO、AuditItemVO、PendingCountVO
├── relic-trace-boot/           启动模块（唯一可运行入口）
│   ├── src/main/java/com/relictrace/RelicTraceApplication.java
│   └── src/main/resources/application.yml
├── frontend/                   Vue 3 + Element Plus 前端工程（含 deploy 部署脚本）
├── sql/                        数据库初始化脚本（init.sql、data.sql）
└── uploads/                    附件上传目录
```

## 二、模块职责与依赖关系

| 模块 | 职责 | 依赖 |
|---|---|---|
| `common` | 与业务无关的公共能力：统一响应体、全局异常、JWT 签发校验、登录上下文、分页对象、实体基类、全部 Mapper 接口、通用配置 | 无内部依赖 |
| `user-service` | 登录认证、令牌签发、用户管理、数据字典、系统参数、初始管理员预置 | common |
| `relic-service` | 藏品征集、编目建档、库房仓位、出入库、库存盘点、修复、展览借调、注销 | common |
| `trace-service` | 溯源记录与时间线、二维码、业务审核、统计分析、操作日志切面 | common |
| `relic-trace-boot` | 唯一启动入口，聚合全部模块并对外提供 HTTP 服务（端口 8080，上下文 `/api`） | 上述四个模块 |

依赖方向始终单向：`boot → 业务模块 → common`，不存在循环依赖。

**设计说明**：本项目为单体应用，模块划分依据是业务域而非部署单元。全部 Mapper 与实体下沉到 `common`，使三个业务模块之间无需相互引用即可访问跨域数据；因此各业务模块保持零交叉依赖，包名与改造前完全一致。

## 三、构建

```bash
# 在 relic-trace 目录下，全量构建
mvn clean package -DskipTests

# 产物
relic-trace-boot/target/relic-trace-boot.jar   # 可执行 fat jar
```

单模块构建（首次执行需先把根 pom 与依赖模块装入本地仓库）：

```bash
mvn -N install -DskipTests                                                    # ① 安装聚合工程根 pom
mvn -pl common,user-service,relic-service,trace-service install -DskipTests   # ② 安装依赖模块
mvn -pl relic-trace-boot package -DskipTests                                  # ③ 构建启动模块
```

> 第 ① 步不可省略。子模块的 `<parent>` 指向 `com.relictrace:relic-trace:1.0.0`，
> 用 `-pl` 单独构建某个模块时，Maven 会从**本地仓库**读取被依赖模块的 pom，
> 而该 pom 的 parent 若无本地副本，就会去远程仓库查找并报
> `Could not find artifact com.relictrace:relic-trace:pom:1.0.0`。
> 若直接执行全量 `mvn clean package`，根 pom 在同一个 reactor 内，不受此影响。

## 四、运行

```bash
# 方式一：直接运行 jar
java -jar relic-trace-boot/target/relic-trace-boot.jar

# 方式二：Maven 插件运行（开发期）
mvn -pl relic-trace-boot spring-boot:run

# 指定端口（默认 8080）
java -jar relic-trace-boot/target/relic-trace-boot.jar --server.port=8080
```

后端接口前缀为 `/api`，例如 `http://localhost:8080/api/auth/login`。

## 五、前端与部署

```bash
cd frontend
npm install
npm run dev      # 开发：http://localhost:5173，自动代理 /api 到 8080

npm run build    # 产物输出到 frontend/dist
```

生产环境由 Nginx 托管 `frontend/dist` 并把 `/api` 反向代理到 8080：

```bash
frontend/deploy/start-nginx.bat   # 启动（80 端口）
frontend/deploy/stop-nginx.bat    # 停止
```

访问 `http://localhost` 即为系统首页。

## 六、扫码溯源（真实二维码）

二维码由后端用 ZXing 生成 PNG，落盘在 `uploads/qrcodes/<藏品编号>.png`，
记录写入 `relic_qr_code`（内容、路径、生成人、扫码次数、最近扫码时间）。

**二维码内容**：`{base-url}/#/scan?code={藏品编号}`，手机相机扫码后直接打开前台
“扫码溯源”页并自动跳转到该藏品详情。

**配置**（`application.yml`）：

```yaml
relic:
  qr:
    base-url: ${RELIC_QR_BASE_URL:http://localhost}
    size: ${RELIC_QR_SIZE:360}
```

- 本机演示保持 `http://localhost`；
- **用手机扫码**时，把 `base-url` 改为电脑局域网地址（本机当前为 `http://10.19.6.8`），
  重启后端后调用批量重建接口刷新二维码内容，并确保防火墙放行 80 端口。

**接口**：

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/relics/{id}/qrcode` | 查询二维码（缺失时自动补建，需登录） |
| POST | `/api/relics/{id}/qrcode/rebuild` | 重新生成单件二维码（需登录） |
| POST | `/api/relics/qrcodes/rebuild` | 批量重建全部二维码，返回数量（需登录） |
| GET | `/api/uploads/qrcodes/{编号}.png` | 二维码图片（免登录，可直接下载打印） |

批量重建示例：

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"loginName":"admin","loginPassword":"123456"}' | python -c "import sys,json;print(json.load(sys.stdin)['data']['token'])")

curl -X POST http://localhost:8080/api/relics/qrcodes/rebuild -H "Authorization: Bearer $TOKEN"
```

前台“扫码溯源”页支持三种方式：摄像头实时扫码（浏览器 `getUserMedia` + jsQR 解码，
需 `localhost` 或 HTTPS）、手动输入编号、演示模式随机取一件公开藏品。

## 七、数据库

- 库名：`relic_db`，端口 `3307`（见 `relic-trace-boot/src/main/resources/application.yml`）
- 初始化：依次执行 `sql/init.sql`、`sql/data.sql`，示例藏品执行 `sql/seed_relics.sql`
- 预置账号：`admin / 123456`（系统管理员）
