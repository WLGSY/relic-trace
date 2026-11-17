# 文物藏品管理与溯源系统 · 前端

依据《软件学院 2026 届学生毕业设计成果（245102031037_李丰）》实现的前端工程，覆盖文档中全部功能模块与页面。
技术栈：**Vue 3（Composition API + `<script setup>`）+ Vite 5 + Vue Router 4 + Pinia 2 + Element Plus 2 + ECharts 5 + Axios**。

工程内置一套基于 `localStorage` 的 Mock 数据服务，**无需后端即可完整运行与演示**；接口路径、请求参数、响应体结构均与真实 Spring Boot 后端约定保持一致，切换环境变量即可对接真实服务。

---

## 一、快速开始

```bash
npm install     # 安装依赖
npm run dev     # 开发模式，http://localhost:5173
npm run build   # 生产构建，产物在 dist/
npm run preview # 预览构建产物，http://localhost:4173
```

### 演示账号

| 登录账号 | 姓名 | 角色 | 可见范围 |
| --- | --- | --- | --- |
| `admin` | 李丰 | 管理员 | 全部菜单（含用户、字典、参数、日志） |
| `zhangly` | 张录入 | 录入员 | 征集、编目、出入库、修复、借调、注销 |
| `lishen` | 李审核 | 审核员 | 审核工作台、注销审批、统计分析 |
| `wangkg` | 王库房 | 录入员 | 库房、出入库、盘点（库房业务） |
| `zhaogl` | 赵管理 | 管理员 | 全量业务 |

初始密码统一为 `123456`，登录页提供三身份快捷填充。

### Mock 与真实后端的切换

```
.env.development  VITE_USE_MOCK=true    # 走内置 Mock（默认）
.env.production   VITE_USE_MOCK=false   # 走真实后端
```

- `true`：`src/api/request.js` 为 Axios 装配自定义 adapter，请求直接交给 `src/mock/index.js` 处理，数据落 `localStorage`（键 `relic_mock_db_v3`）。
- `false`：请求经 Vite 代理（`/api` → `http://localhost:8080`）转发到 Spring Boot 服务。
- 重置演示数据：在浏览器控制台执行 `localStorage.removeItem('relic_mock_db_v3')` 后刷新。

---

## 二、目录结构

```
relic-trace/frontend/
├─ index.html                  # 入口 HTML（内联 SVG favicon）
├─ vite.config.js              # 别名、代理、分包
├─ .env.development / .env.production
└─ src/
   ├─ main.js                  # 应用装配：Pinia / Router / Element Plus（中文包）
   ├─ App.vue                  # 路由出口 + 页面切换过渡
   ├─ api/                     # 接口层（按业务域拆分）
   │  ├─ request.js            # Axios 实例、Mock adapter 装配、拦截器、Token 存取
   │  └─ auth / relic / collection / stock / warehouse / repair / loan / cancel / audit / system / stats
   ├─ mock/
   │  ├─ db.js                 # 18 张表的种子数据（对应数据库表结构）
   │  └─ index.js              # 路由表式 Mock 服务：鉴权、分页、业务校验、日志与溯源联动
   ├─ router/index.js           # 前台 5 路由 + 登录 + 后台 15 子路由 + 404，含角色守卫
   ├─ stores/
   │  ├─ user.js               # 登录态、菜单、角色能力判断
   │  └─ meta.js               # 分类 / 字典 / 库房 元数据缓存
   ├─ composables/
   │  ├─ useTable.js           # 列表页分页、查询、重置、加载态
   │  └─ useChart.js           # ECharts 初始化、尺寸自适应、统一图表配色
   ├─ layouts/
   │  ├─ FrontLayout.vue       # 前台：粘性页头、移动端抽屉导航、页脚
   │  └─ AdminLayout.vue       # 后台：侧栏分组菜单、面包屑、移动端遮罩
   ├─ components/              # 通用组件（见下表）
   ├─ config/menu.js           # 侧栏菜单单一数据源，同时驱动渲染与权限校验
   ├─ utils/                   # constants（枚举）/ format（日期金额）/ export（CSV、打印）
   ├─ styles/                  # tokens.css → base.css → element.css 三层设计系统
   └─ views/
      ├─ front/                # 首页、检索、藏品详情、数据看板、扫码溯源、404
      └─ admin/                # 登录、工作台 + 13 个业务/系统管理页
```

### 通用组件

| 组件 | 职责 |
| --- | --- |
| `PageHeader` | 页面标题区：衬线标题 + 等宽编号 + 副标题 + 操作插槽 |
| `SectionHead` | 区块小标题 + 发丝线分隔 |
| `StatGrid` | 指标网格（列间发丝线分隔） |
| `StatusTag` | 由枚举表驱动的状态标签，全站同色同义 |
| `RelicCard` | 藏品卡片（4:5 展品影像 + 衬线名称 + 展签式元信息） |
| `ArtFrame` | 离线展品影像：低饱和矿物渐变 + 内联 SVG 线稿 |
| `TraceTimeline` | 溯源时间线（7px 圆点 + 竖线） |
| `RelicPicker` | 弹窗式藏品选择器 |
| `AuditDialog` | 审核弹窗（通过 / 驳回，驳回必填意见） |
| `EmptyState` | 空状态占位 |
| `EChart` | ECharts 容器，封装 `useChart` |

---

## 三、页面与文档对应关系

**前台（游客 / 馆内展示）**

| 路由 | 页面 | 说明 |
| --- | --- | --- |
| `/` | `HomeView` | Hero 轮播、馆藏精选、流转概览、快速溯源、能力介绍 |
| `/search` | `SearchView` | 关键词 + 分类 / 年代 / 级别多维检索，结果分页 |
| `/relic/:id` | `DetailView` | 藏品详情、档案信息、溯源时间线、影像资料 |
| `/dashboard` | `DashboardView` | 公开数据看板（分类构成、级别分布、月度入库趋势） |
| `/scan` | `ScanView` | 扫码溯源演示：输入藏品编号或二维码内容直达详情 |
| `/login` | `LoginView` | 后台登录，三身份快捷填充 |

**后台（登录后按角色授权）**

| 路由 | 页面 | 对应文档模块 |
| --- | --- | --- |
| `/admin/workbench` | 系统工作台 | 待办汇总、快捷入口、最近流转 |
| `/admin/collect` | 藏品征集 | 征集申请登记、提交审核、进度跟踪 |
| `/admin/catalog` | 编目建档 | 建档信息录入、编号自动生成、影像上传 |
| `/admin/archive` | 藏品档案 | 检索 / 高级查询、详情抽屉、修改、逻辑删除 |
| `/admin/stock` | 出入库管理 | 入库、出库、转移登记与单据打印 |
| `/admin/warehouse` | 库房管理 | 库房 / 仓位 / 盘点任务三个页签 |
| `/admin/repair` | 修复管理 | 修复申请、修复过程记录、完残状况回写 |
| `/admin/loan` | 展览借调 | 借出登记、归还核销、超期提示 |
| `/admin/cancel` | 注销管理 | 注销申请、审批流、状态归档 |
| `/admin/audit` | 审核工作台 | 征集 / 修复 / 注销多业务统一审核 |
| `/admin/stats` | 统计分析 | 五维统计图表 + 明细表 + 报表导出 |
| `/admin/user` | 用户管理 | 账号增改、重置密码、启停用、逻辑删除 |
| `/admin/dict` | 数据字典 | 字典类型导航 + 字典项维护 |
| `/admin/param` | 系统参数 | 布尔开关 / 数值 / 文本三类参数的即时编辑 |
| `/admin/log` | 操作日志 | 按模块、类型、操作人、时间区间检索审计痕迹 |

---

## 四、设计系统

视觉方向：**素白 · 墨黑 · 石青**——以白墙展厅与展签为参照的编辑式现代美术馆语言，刻意避开常见后台的圆角卡片与彩色图标堆叠。

- **仅一个强调色**：石青 `#0e3f39 / #14554c` 只用于可交互焦点与数据强调；陶土 `#a34b2e`、赭石 `#7d6120` 仅承担状态语义。
- **直角 + 发丝线**：全局 `--radius: 2px`，除浮层外不投影，层次由 1px 描边与留白建立。
- **排版即层级**：`--font-display` 宋体衬线用于标题，等宽字体承载编号 / 时间 / 参数键，正文无衬线。
- **双密度作用域**：`body[data-density="front"]` 与 `"admin"` 分别定义前台展览密度与后台信息密度。
- **响应式**：1180 / 980 / 640 三档断点；表格在窄屏横向滚动，后台侧栏转为抽屉；支持 `prefers-reduced-motion`。

设计令牌集中在 `src/styles/tokens.css`，修改主色或字阶只需改一处；Element Plus 的主题覆盖集中在 `src/styles/element.css`。

---

## 五、工程约定

- **接口层**：`src/api/*.js` 只做参数与路径封装，业务逻辑留在页面或 Mock 服务中；所有响应经拦截器解包为 `data`，错误统一 `ElMessage` 提示，401 自动跳登录。
- **列表页**：统一使用 `useTable(fetcher, { defaultQuery })`，返回 `loading / list / total / query / search / reset / onPageChange`。
- **权限**：`config/menu.js` 为菜单唯一来源，路由 `meta.roles` 与菜单 `roles` 同源；守卫在 `router.beforeEach` 中校验，越权 URL 直接重定向。
- **数据联动**：审核通过时 Mock 层同步更新藏品 `stockStatus`、库房仓位，并写入溯源记录与审计记录，保证前台时间线可回溯。
- **离线可用**：不依赖任何外链图片与字体 CDN，`ArtFrame` 以渐变 + SVG 生成展品影像。
