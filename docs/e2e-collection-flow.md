# 藏品征集—审核—建档 端到端流程运行记录

> 运行日期：2026-11-12  
> 运行环境：Spring Boot 3.2.5（8080） + MySQL 8.0（3307 / relic_db） + Nginx（80/443）  
> 运行方式：直接调用后端 REST 接口（与前端页面调用同一套接口），逐角色切换账号执行

---

## 一、测试对象

| 项目 | 内容 |
|------|------|
| 藏品名称 | 唐三彩力士三足盘 |
| 年代 | 唐 |
| 质地 | 陶 |
| 规格 | 高 16.2cm，口径 21.8cm |
| 级别 | 二级 |
| 分类 | 陶瓷（`00000000000000000000000000000101`） |
| 征集来源 | 社会征集（民间捐赠） |
| 征集方式 | 捐赠（字典码 `donate`） |
| 实物照片 | `uploads/relics/TC-0005.jpg`（1000×903，174,662 字节） |

照片取自公开文物影像资料，器物为唐三彩盘类中三足塑作胡人力士的形制；规格与征集背景为演示用示例数据，实际使用时以实测与原始凭证为准。

## 二、角色与状态机

系统角色（`sys_user.role_type`）：

| 账号 | 姓名 | roleType | 职责 |
|------|------|----------|------|
| zhangly | 张录入 | 2 | 征集登记、提交、编目建档 |
| lishen | 李审核 | 3 | 审核工作台、出具审核意见 |
| admin | 系统管理员 | 1 | 全局管理 |

征集申请状态机（`relic_collection_apply.apply_status`）：

```
0 草稿 ──提交──> 1 待审核 ──审核通过──> 2 审核通过
                      └────驳回────> 3 审核驳回 ──重新编辑提交──> 1
```

审核结果映射（`AuditServiceImpl.auditCollection`）：`auditResult=1` → 状态 2；`auditResult=2` → 状态 3，且驳回必须填写意见。

---

## 三、逐步操作记录

### 步骤 1　录入员创建征集申请（草稿）

- 角色：zhangly（录入员）
- 接口：`POST /api/collection-applies`
- 请求体（节选）：

```json
{
  "applyCode": "ZJ-2026-002",
  "batchNo": "2026-B12",
  "relicName": "唐三彩力士三足盘",
  "fkCategoryId": "00000000000000000000000000000101",
  "era": "唐",
  "texture": "陶",
  "specification": "高 16.2cm，口径 21.8cm",
  "relicLevel": 2,
  "collectionSource": "社会征集（民间捐赠）",
  "collectionWay": "donate",
  "collector": "张录入",
  "collectionTime": "2026-11-12 09:30:00",
  "applyStatus": 0
}
```

- 响应：`{"code":200,"message":"新增成功","data":"2120690251072090114"}`
- 回读校验：状态 `0`，编号 `ZJ-2026-002`，创建人自动填充为「张录入」，创建时间 `2026-11-12 09:28:51`

### 步骤 2　录入员提交审核

- 角色：zhangly
- 接口：`POST /api/collection-applies/2120690251072090114/submit`
- 响应：`{"code":200,"message":"提交成功"}`
- 回读校验：状态 `0 → 1`（待审核），更新时间 `09:28:57`

### 步骤 3　审核员查看待办

- 角色：lishen（审核员）
- 接口：`GET /api/audits/pending-count` → `{"collection":1,"stock":0,"repair":0,"loan":0,"cancel":0,"total":1}`
- 接口：`GET /api/audits/pending?pageNum=1&pageSize=5`
- 返回：`bizType=1`、业务号 `ZJ-2026-002`、标题「唐三彩力士三足盘」、提交人「张录入」、提交时间 `09:28:51`

### 步骤 4　审核员提交审批（通过）

- 角色：lishen
- 接口：`POST /api/audits`
- 请求体：

```json
{
  "bizType": 1,
  "fkBizId": "2120690251072090114",
  "bizCode": "ZJ-2026-002",
  "auditResult": 1,
  "auditOpinion": "经核验，该盘胎质坚致、三彩釉面自然，力士足造型与唐代同类器相符，来源说明及捐赠手续齐备，同意入藏。"
}
```

- 响应：`{"code":200,"message":"审核完成"}`
- 回读校验：申请状态 `1 → 2`，审核意见已写入；待办数归零
- 审核日志（`sys_audit_record`）：业务号 `ZJ-2026-002`、审核人「李审核」、结果 `1`、时间 `2026-11-12 09:29:22`

### 步骤 5　录入员编目建档

- 角色：zhangly
- 接口：`POST /api/relics`
- 请求体（节选）：

```json
{
  "relicCode": "TC-0005",
  "relicName": "唐三彩力士三足盘",
  "fkCollectionApplyId": "2120690251072090114",
  "fkCategoryId": "00000000000000000000000000000101",
  "fkWarehouseId": "00000000000000000000000000010001",
  "fkPositionId": "00000000000000000000000000020001",
  "stockStatus": 1,
  "isPublic": 1
}
```

- 响应：`{"code":200,"message":"编目建档成功","data":"2120690424397508610"}`
- 建档时自动完成两件事：
  - 写入溯源记录：类型 `2`（入藏）、`2026-11-12 09:29:32`、「藏品编目入藏：唐三彩力士三足盘」
  - 生成溯源二维码：内容 `https://10.19.6.8/#/scan?code=TC-0005`，图片 `/uploads/qrcodes/TC-0005.png`

### 步骤 6　关联实物照片

- 说明：本次通过数据脚本写入 `relic_attachment`（原因见问题 2）
- 写入内容：`唐三彩力士三足盘_正视.jpg`、`relics/TC-0005.jpg`、类型 `1`（藏品影像）、174,662 字节、上传人「张录入」
- 详情回读：`cover = /uploads/relics/TC-0005.jpg`，附件列表 1 条

### 步骤 7　溯源链路验证

| 验证项 | 接口 | 结果 |
|--------|------|------|
| 图片可访问 | `GET /api/uploads/relics/TC-0005.jpg` | HTTP 200 |
| 扫码溯源 | `GET /api/relics/by-code/TC-0005` | 命中「唐三彩力士三足盘」，年代唐、质地陶 |
| 扫码计数 | `GET /api/relics/{id}/qrcode` | `scanCount 0 → 1`，`lastScanTime 09:29:47` |
| 前台公开检索 | `GET /api/relics/public/search?keyword=三彩` | 命中 2 条，含 `TC-0005`（带封面） |

---

## 四、状态流转汇总

| 环节 | 操作角色 | 接口 | 状态变化 | 时间 |
|------|----------|------|----------|------|
| 征集登记 | 张录入 | `POST /collection-applies` | — → 0 草稿 | 09:28:51 |
| 提交审核 | 张录入 | `POST /collection-applies/{id}/submit` | 0 → 1 待审核 | 09:28:57 |
| 审核通过 | 李审核 | `POST /audits` | 1 → 2 审核通过 | 09:29:22 |
| 编目建档 | 张录入 | `POST /relics` | 生成藏品 `TC-0005` | 09:29:32 |
| 扫码溯源 | 匿名 | `GET /relics/by-code/TC-0005` | 扫码计数 +1 | 09:29:47 |

最终数据：征集申请 `ZJ-2026-002`（状态 2）↔ 藏品 `TC-0005`（经 `fk_collection_apply_id` 关联），溯源记录 1 条，二维码 1 张，附件 1 条。

---

## 五、遇到的问题与处理

### 问题 1　征集编号必须由调用方提供，前端表单却没有该字段

- 现象：`relic_collection_apply.apply_code` 为 `NOT NULL UNIQUE`，但 `CollectionServiceImpl.create()` 不生成编号，`CollectView.vue` 的表单也不含 `applyCode`。若照页面填写直接提交，会因编号为空触发数据库约束错误。
- 处理：接口调用时显式传入 `ZJ-2026-002`。
- 建议：后端在 `create()` 中按 `ZJ-yyyy-NNN` 规则生成，或前端补只读编号展示。

### 问题 2　系统没有附件上传接口

- 现象：全工程检索无 `MultipartFile` 相关接口，前端也无上传 API，`relic_attachment` 仅有查询代码。图片只能靠直接写库/脚本挂接。
- 处理：按既有数据格式（`file_path` 存 `relics/TC-0005.jpg` 相对路径、`file_type=1`）写入一条附件记录。
- 建议：补 `POST /relics/{id}/attachments` 上传接口，落盘到 `relic.upload-dir` 并写附件表。

### 问题 3　审核接口未做角色鉴权

- 现象：用录入员账号（roleType=2）调用 `POST /api/audits`（bizType=99 触发业务校验），返回 `{"code":500,"message":"未知的业务类型"}`，而非 403。说明接口只校验 JWT，未校验角色。
- 附带验证：该异常请求未在 `sys_audit_record` 留下脏数据（`@Transactional` 回滚正常）。
- 处理：流程验证未受影响（实际操作仍由审核员执行）。
- 建议：在审核类接口加角色校验（仅 roleType=3/1 可调用），对应论文"权限控制"章节。

### 问题 4　首轮选品与库内既有藏品重复

- 现象：首次按「三彩载乐骆驼俑」走流程，建档后发现与既有 `TC-0002 三彩骆驼载乐俑` 为同一器物，属重复著录。
- 处理：物理清除该轮产生的征集单、藏品、溯源、二维码记录，改选「唐三彩力士三足盘」重新完整走一遍。本文档记录的是重跑后的最终结果。
- 启示：建档前的编号与品名查重应作为流程卡点。

### 问题 5　征集方式字典码前后端不一致

- 现象：数据库 `sys_dict` 中 `collection_way` 的 `dict_code` 为 `donate / purchase / transfer / borrow`；前端 `utils/constants.js` 的 `DICT_FALLBACK` 却是 `'1'~'5'`。
- 影响：字典接口正常时以数据库为准，不影响提交；一旦字典接口失败走兜底，下拉项的值会与库中数据对不上。
- 建议：统一字典码，或去掉兜底硬编码。

### 问题 6　征集列表直接显示字典码

- 现象：`CollectView.vue` 的「征集方式」列用 `prop="collectionWay"` 直接渲染，未调用 `dictLabel()` 转换，列表中会显示 `donate` 而非「捐赠」。
- 建议：与其它列一致，改用 `dictLabel('collection_way', row.collectionWay)`。

---

## 六、问题修复记录（2026-11-12 第二轮）

上述 6 个问题已在代码中修复，并新增 1 处测试中暴露的阻断性缺陷（问题 7）。

| 编号 | 修复内容 | 涉及文件 |
|---|---|---|
| 1 | `create()` 自动生成 `ZJ-yyyy-NNN`，取当年最大流水号 +1；并发抢占编号时重试 3 次；`update()` 拒绝改写编号与状态 | `CollectionServiceImpl`、`RelicCollectionApplyMapper` |
| 2 | 新增 `POST /relics/{id}/attachments` 与 `DELETE /relics/attachments/{id}`，落盘 `relic.upload-dir/relics/`、写附件表、删除时同步清理磁盘文件 | `RelicController`、`RelicService(Impl)`、`application.yml` |
| 3 | 新增 `@RequireRole` 注解 + `RoleInterceptor`，审核提交/批量审核限定 roleType 1、3；服务层再兜一层校验 | `RequireRole`、`RoleInterceptor`、`WebConfig`、`AuditController`、`AuditServiceImpl` |
| 5 | `DICT_FALLBACK.collection_way` 改为 `donate/purchase/transfer/borrow`，与 `sys_dict` 一致 | `utils/constants.js` |
| 6 | 征集方式列改用 `dictLabel('collection_way', …)` 渲染 | `CollectView.vue` |
| 7 | 日期字段统一容错：纯日期按当日 00:00:00 解析 | `JacksonConfig`（新增） |
| 8 | 征集 / 档案列表筛选条件真正落到 SQL | `CollectionQuery`（新增）、`RelicQuery`、`CollectionServiceImpl`、`ArchiveView.vue` |

### 问题 7　日期选择器取值导致接口 500（测试中新发现）

- 现象：`CollectView` 的征集时间用 `value-format="YYYY-MM-DD"`，提交 `"2026-11-12"`；而实体字段 `collectionTime` 标注 `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")`，Jackson 反序列化抛 `DateTimeParseException`，接口返回 `{"code":500,"message":"系统繁忙，请稍后重试"}`。即**按页面默认值填写征集单必然保存失败**，同类问题还影响编目建档（入藏时间）、借调（借出/归还日期）、修复（起止时间）。
- 处理：新增 `JacksonConfig`，注册容错的 `LocalDateTime` 反序列化器，依次尝试 `yyyy-MM-dd HH:mm:ss`、ISO、`yyyy-MM-dd HH:mm`，纯日期补 `00:00:00`；序列化仍固定输出 `yyyy-MM-dd HH:mm:ss`，前端展示不变。
- 附带：`GlobalExceptionHandler` 新增 `MaxUploadSizeExceededException` 处理，超过 20MB 返回「文件过大，单个附件不能超过 20 MB」而非统一兜底文案。

### 修复后的回归验证（8081 端口临时实例，不影响 8080）

| 用例 | 结果 |
|---|---|
| 不传编号创建征集 | 自动生成 `ZJ-2026-003`、`004`、`005`，流水号连续递增 |
| 纯日期提交征集时间 | `"2026-11-12"` → 落库 `2026-11-12 00:00:00`，返回 200 |
| 重复提交同一单 | `{"code":500,"message":"当前状态不可提交审核，请刷新后重试"}` |
| 编辑时提交伪造编号/状态 | 编号仍为 `ZJ-2026-004`、状态仍为 1，未被覆盖 |
| 录入员调用审核接口 | `{"code":403,"message":"当前角色无权执行该操作"}` |
| 录入员调用批量审核 | `403` |
| 审核员审核通过 | 200，申请状态 1 → 2，审核意见回写 |
| 上传 jpg 附件 | 200，落盘 `relics/TC-0005_20261112094717287.jpg`，`/api/uploads/...` 可访问（200），详情封面切换为新图 |
| 上传 .exe | `不支持的文件类型，仅允许 jpg / jpeg / png / webp / gif / bmp / pdf` |
| 上传 21MB / 16MB | 21MB 返回「文件过大，单个附件不能超过 20 MB」；16MB 上传成功 |
| 删除附件 | 200，数据库逻辑删除 + 磁盘文件移除，封面回落到原图 |

> 验证用的临时征集单、附件记录与文件均已清理，`relic_collection_apply` 仅保留 `ZJ-2026-002`。

### 问题 8　列表筛选条件未生效（测试中新发现）

- 现象：后台征集、档案页面的高级查询/筛选下拉看似可用，实际不起作用。原因有两层：
  1. 后端 `CollectionServiceImpl.page()` 只处理 `keyword`，状态、分类、年代、征集方式、时间区间全被丢弃；
  2. 档案页前端传 `categoryId`，而 `RelicQuery` 的字段是 `fkCategoryId`，参数名对不上，分类筛选同样失效；`isPublic` 甚至没有对应字段。
- 处理：新增 `CollectionQuery`（关键字 + 状态 + 分类 + 年代 + 征集方式 + 时间区间）并落到 `LambdaQueryWrapper`；`RelicQuery` 补 `isPublic` 字段并参与过滤；前端档案页查询键由 `categoryId` 改为 `fkCategoryId`。
- 回归：`/relics` 全量 13 条 → 按分类 5 条 → 按在库 11 条 → 按保密 0 条；`/collection-applies` 按状态 2 为 1 条、状态 0 为 0 条，按 `donate` 1 条、`purchase` 0 条，时间区间 2026 年 1 条，均符合预期。

> 同类问题仍存在于借调、修复、出入库、注销、日志、用户等页面（后端仍只支持 `keyword`），本次未改动；如需一并补齐，可按 `CollectionQuery` 的方式为各业务补查询对象。

---

## 七、结论

征集 → 提交 → 审核 → 建档 → 溯源 的完整链路已跑通，状态流转与角色职责符合设计：录入员负责登记与建档，审核员负责审批，审批结果回写申请状态并留痕，建档后自动生成溯源记录与可扫二维码，前台公开检索与扫码接口均能命中新藏品。

上一轮列出的三处工程缺口（征集编号自动生成、附件上传接口、审核接口角色鉴权）及两处体验缺陷（字典码、列表显示）已全部修复，并额外修掉了日期反序列化这一阻断性问题。前端 `dist` 已重新构建，后端需在 IDEA 重启后生效。
