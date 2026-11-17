/**
 * 演示数据（种子）
 * ------------------------------------------------------------------
 * 结构与《数据表设计》一一对应：字段名沿用数据库列名（小驼峰形式），
 * 便于后续把 mock 层替换为真实 Spring Boot 接口时对照 DTO。
 *
 * 说明：编号类字段（relicCode / applyCode / recordCode ...）在真实系统中
 * 由后端按 sys_config 中的编号规则生成，这里给出等价的演示值。
 */

const PAD = (n) => String(n).padStart(2, '0')

/** 相对今天的前 n 天，返回 "YYYY-MM-DD HH:mm:ss" */
function dt(daysAgo = 0, hour = 9, minute = 30) {
  const d = new Date()
  d.setDate(d.getDate() - daysAgo)
  return `${d.getFullYear()}-${PAD(d.getMonth() + 1)}-${PAD(d.getDate())} ${PAD(hour)}:${PAD(minute)}:00`
}

/** 相对今天的前 n 天，返回 "YYYY-MM-DD" */
function day(daysAgo = 0) {
  return dt(daysAgo).slice(0, 10)
}

const YEAR = new Date().getFullYear()

/* ------------------------------------------------------------------ */
/* 基础字典                                                            */
/* ------------------------------------------------------------------ */

const categories = [
  { id: 'cat0000000000000000000000000001', categoryCode: 'TC', categoryName: '陶瓷', fkParentId: null, categoryLevel: 1, isLeaf: 1, sortNo: 1 },
  { id: 'cat0000000000000000000000000002', categoryCode: 'QT', categoryName: '青铜', fkParentId: null, categoryLevel: 1, isLeaf: 1, sortNo: 2 },
  { id: 'cat0000000000000000000000000003', categoryCode: 'SH', categoryName: '书画', fkParentId: null, categoryLevel: 1, isLeaf: 1, sortNo: 3 },
  { id: 'cat0000000000000000000000000004', categoryCode: 'YQ', categoryName: '玉器', fkParentId: null, categoryLevel: 1, isLeaf: 1, sortNo: 4 },
  { id: 'cat0000000000000000000000000005', categoryCode: 'ZX', categoryName: '织绣', fkParentId: null, categoryLevel: 1, isLeaf: 1, sortNo: 5 },
  { id: 'cat0000000000000000000000000006', categoryCode: 'ZM', categoryName: '竹木', fkParentId: null, categoryLevel: 1, isLeaf: 1, sortNo: 6 }
]

const users = [
  {
    id: 'usr0000000000000000000000000001', loginName: 'admin', loginPassword: '123456', realName: '李丰',
    roleType: 1, phone: '13800000001', avatar: '', isEnabled: 1, lastLoginTime: dt(0, 8, 42),
    createUserId: 'system', createUserName: '系统初始化', createTime: dt(320), updateUserName: '李丰', updateTime: dt(3), isDeleted: 0, memo: '部署时预置的系统管理员'
  },
  {
    id: 'usr0000000000000000000000000002', loginName: 'zhangly', loginPassword: '123456', realName: '张录入',
    roleType: 2, phone: '13800000002', avatar: '', isEnabled: 1, lastLoginTime: dt(0, 9, 5),
    createUserId: 'usr0000000000000000000000000001', createUserName: '李丰', createTime: dt(210), updateUserName: '李丰', updateTime: dt(12), isDeleted: 0, memo: '藏品征集与编目'
  },
  {
    id: 'usr0000000000000000000000000003', loginName: 'lishen', loginPassword: '123456', realName: '李审核',
    roleType: 3, phone: '13800000003', avatar: '', isEnabled: 1, lastLoginTime: dt(1, 16, 20),
    createUserId: 'usr0000000000000000000000000001', createUserName: '李丰', createTime: dt(208), updateUserName: '李丰', updateTime: dt(20), isDeleted: 0, memo: '征集 / 出入库 / 修复 / 借调 / 注销审核'
  },
  {
    id: 'usr0000000000000000000000000004', loginName: 'wangkg', loginPassword: '123456', realName: '王库房',
    roleType: 2, phone: '13800000004', avatar: '', isEnabled: 0, lastLoginTime: dt(45, 10, 2),
    createUserId: 'usr0000000000000000000000000001', createUserName: '李丰', createTime: dt(180), updateUserName: '李丰', updateTime: dt(30), isDeleted: 0, memo: '离岗，账号已停用'
  },
  {
    id: 'usr0000000000000000000000000005', loginName: 'zhaogl', loginPassword: '123456', realName: '赵管理',
    roleType: 1, phone: '13800000005', avatar: '', isEnabled: 1, lastLoginTime: dt(6, 14, 12),
    createUserId: 'usr0000000000000000000000000001', createUserName: '李丰', createTime: dt(150), updateUserName: '李丰', updateTime: dt(6), isDeleted: 0, memo: '协助系统配置'
  }
]

const warehouses = [
  { id: 'wh00000000000000000000000000001', warehouseCode: 'W-01', warehouseName: '一号库房', location: '文物库区 A 座 1 层', keeperName: '王库房', isEnabled: 1, createUserName: '李丰', createTime: dt(300), memo: '陶瓷 · 玉器常设库' },
  { id: 'wh00000000000000000000000000002', warehouseCode: 'W-02', warehouseName: '二号库房', location: '文物库区 B 座负 1 层', keeperName: '王库房', isEnabled: 1, createUserName: '李丰', createTime: dt(300), memo: '青铜 · 书画 · 织绣恒温库' },
  { id: 'wh00000000000000000000000000003', warehouseCode: 'W-03', warehouseName: '临时周转库', location: '办公楼 2 层 201', keeperName: '张录入', isEnabled: 1, createUserName: '李丰', createTime: dt(120), memo: '待编目 / 待修复藏品暂存' }
]

const positions = [
  { id: 'pos0000000000000000000000000001', positionCode: 'A-01', positionName: 'A 区 01 号柜', fkWarehouseId: warehouses[0].id, isEnabled: 1 },
  { id: 'pos0000000000000000000000000002', positionCode: 'A-02', positionName: 'A 区 02 号柜', fkWarehouseId: warehouses[0].id, isEnabled: 1 },
  { id: 'pos0000000000000000000000000003', positionCode: 'A-03', positionName: 'A 区 03 号柜', fkWarehouseId: warehouses[0].id, isEnabled: 1 },
  { id: 'pos0000000000000000000000000004', positionCode: 'A-04', positionName: 'A 区 04 号柜', fkWarehouseId: warehouses[0].id, isEnabled: 1 },
  { id: 'pos0000000000000000000000000005', positionCode: 'A-05', positionName: 'A 区 05 号柜', fkWarehouseId: warehouses[0].id, isEnabled: 1 },
  { id: 'pos0000000000000000000000000006', positionCode: 'B-01', positionName: 'B 区 01 号柜', fkWarehouseId: warehouses[1].id, isEnabled: 1 },
  { id: 'pos0000000000000000000000000007', positionCode: 'B-02', positionName: 'B 区 02 号柜', fkWarehouseId: warehouses[1].id, isEnabled: 1 },
  { id: 'pos0000000000000000000000000008', positionCode: 'B-03', positionName: 'B 区 03 号柜', fkWarehouseId: warehouses[1].id, isEnabled: 1 },
  { id: 'pos0000000000000000000000000009', positionCode: 'B-04', positionName: 'B 区 04 号柜', fkWarehouseId: warehouses[1].id, isEnabled: 1 },
  { id: 'pos0000000000000000000000000010', positionCode: 'B-05', positionName: 'B 区 05 号柜', fkWarehouseId: warehouses[1].id, isEnabled: 1 },
  { id: 'pos0000000000000000000000000011', positionCode: 'C-01', positionName: 'C 区 01 号架', fkWarehouseId: warehouses[2].id, isEnabled: 1 },
  { id: 'pos0000000000000000000000000012', positionCode: 'C-02', positionName: 'C 区 02 号架', fkWarehouseId: warehouses[2].id, isEnabled: 1 },
  { id: 'pos0000000000000000000000000013', positionCode: 'C-03', positionName: 'C 区 03 号架', fkWarehouseId: warehouses[2].id, isEnabled: 1 }
]

/* ------------------------------------------------------------------ */
/* 藏品档案                                                            */
/* ------------------------------------------------------------------ */

/** 藏品基础定义（便于批量派生其他业务数据） */
const relicSeed = [
  { name: '青釉莲瓣纹碗', cat: 0, era: '五代', texture: '陶瓷', level: 1, spec: '口径 18.2cm，高 7.6cm，重 0.42kg', integrity: '完好', wh: 0, pos: 0, stock: 1, public: 1, cover: 'a', desc: '敞口，深腹，圈足。通体施青釉，釉色青中泛灰，外壁刻划莲瓣纹，瓣尖微翘，刀法流畅，为五代越窑系典型器物。' },
  { name: '青铜饕餮纹鼎', cat: 1, era: '商周', texture: '青铜', level: 1, spec: '通高 32cm，口径 26cm，重 5.8kg', integrity: '基本完好', wh: 0, pos: 1, stock: 3, public: 1, cover: 'f', desc: '双立耳，深腹，三柱足。腹部饰饕餮纹一周，云雷纹衬地，扉棱分明，铸造工艺精湛，为商代晚期青铜礼器代表。' },
  { name: '青玉螭龙佩', cat: 3, era: '汉代', texture: '玉器', level: 2, spec: '长 9.4cm，宽 5.1cm，厚 0.6cm', integrity: '完好', wh: 0, pos: 2, stock: 1, public: 1, cover: 'c', desc: '青玉质，局部有褐色沁。透雕螭龙盘绕，龙首回顾，线条圆润，穿孔位于龙脊，为汉代组玉佩构件。' },
  { name: '三彩骆驼俑', cat: 0, era: '唐代', texture: '陶瓷', level: 2, spec: '通高 58cm，长 46cm', integrity: '局部残损', wh: 1, pos: 5, stock: 1, public: 1, cover: 'd', desc: '双峰驼昂首嘶鸣，背铺菱格纹毯，釉色以黄、绿、白为主，流淌自然，是唐代丝绸之路贸易的实物见证。' },
  { name: '汝窑天青釉洗', cat: 0, era: '宋代', texture: '陶瓷', level: 1, spec: '口径 13.5cm，高 3.8cm', integrity: '完好', wh: 1, pos: 6, stock: 1, public: 1, cover: 'e', desc: '敞口浅腹，平底。通体施天青釉，釉面开细密冰裂纹，底有五枚支钉痕，为汝窑天青釉典型器。' },
  { name: '错金银云纹犀尊', cat: 1, era: '汉代', texture: '青铜', level: 1, spec: '高 22cm，长 34cm，重 3.2kg', integrity: '基本完好', wh: 1, pos: 7, stock: 1, public: 1, cover: 'b', desc: '犀牛形酒器，体态敦厚，通体错金银云气纹，背开圆口带盖，为汉代错金银工艺巅峰之作。' },
  { name: '缂丝花鸟图轴', cat: 4, era: '宋代', texture: '织绣', level: 2, spec: '纵 96cm，横 42cm', integrity: '局部残损', wh: 1, pos: 8, stock: 4, public: 1, cover: 'c', desc: '缂丝工艺织花鸟小景，通经断纬，色阶过渡自然，存宋代院体画意，正在开展除尘与装裱修复。' },
  { name: '董其昌山水册页', cat: 2, era: '明清', texture: '书画', level: 2, spec: '八开，每开 28×22cm', integrity: '基本完好', wh: 1, pos: 9, stock: 1, public: 1, cover: 'b', desc: '纸本水墨，八开册页，仿倪瓒笔意，山石以披麻皴写就，墨色清润，钤"宗伯学士"白文印。' },
  { name: '玉兽面纹璧', cat: 3, era: '商周', texture: '玉器', level: 3, spec: '直径 15.2cm，厚 1.1cm', integrity: '完好', wh: 0, pos: 4, stock: 1, public: 1, cover: 'a', desc: '青玉质，一面光素，一面琢兽面纹，双阴线勾勒，形制规整，为西周礼玉。' },
  { name: '黑釉油滴盏', cat: 0, era: '宋代', texture: '陶瓷', level: 3, spec: '口径 12.4cm，高 6.1cm', integrity: '局部残损', wh: 1, pos: 9, stock: 2, public: 1, cover: 'f', desc: '束口，斜壁，小圈足。黑釉地满布银灰色油滴结晶，随光线变换闪烁，出库用于国内巡展研究。' },
  { name: '铜鎏金佛像', cat: 1, era: '唐代', texture: '青铜', level: 2, spec: '通高 26cm，重 2.4kg', integrity: '基本完好', wh: 2, pos: 10, stock: 1, public: 1, cover: 'e', desc: '结跏趺坐于莲台，螺发，面容丰润，衣纹流畅，通体鎏金，为唐代小型金铜造像。' },
  { name: '织锦龙纹袍', cat: 4, era: '明清', texture: '织绣', level: 4, spec: '衣长 132cm，通袖 198cm', integrity: '严重残损', wh: 2, pos: 11, stock: 1, public: 1, cover: 'd', desc: '石青地织金云龙纹，圆领大襟，下摆饰海水江崖，丝线多处断裂，待评估是否申报修复。' },
  { name: '龙泉窑青瓷瓶', cat: 0, era: '明清', texture: '陶瓷', level: 3, spec: '高 34cm，口径 9cm', integrity: '严重残损', wh: 0, pos: 3, stock: 5, public: 1, cover: 'a', desc: '盘口，长颈，鼓腹。釉色粉青，因库房意外碰损致口沿残缺，已完成注销审批并归档。' },
  { name: '竹雕笔筒', cat: 5, era: '明清', texture: '竹木', level: 4, spec: '高 14cm，口径 9cm', integrity: '基本完好', wh: 2, pos: 12, stock: 1, public: 0, cover: 'f', desc: '竹节形，浮雕松下高士图，刀法深峻。该件暂列为保密藏品，不在前台公开展示。' }
]

const relics = relicSeed.map((item, index) => {
  const no = String(index + 1).padStart(4, '0')
  return {
    id: `rel${String(index + 1).padStart(29, '0')}`,
    relicCode: `${YEAR}-${no}`,
    relicName: item.name,
    fkCategoryId: categories[item.cat].id,
    fkCollectionApplyId: `col${String(index + 1).padStart(29, '0')}`,
    era: item.era,
    texture: item.texture,
    specification: item.spec,
    relicLevel: item.level,
    quantity: 1,
    integrityStatus: item.integrity,
    fkWarehouseId: warehouses[item.wh].id,
    fkPositionId: positions[item.pos].id,
    stockStatus: item.stock,
    qrcodeContent: `relic://trace/${YEAR}-${no}`,
    isPublic: item.public,
    relicDesc: item.desc,
    collectionWay: ['捐赠', '购买', '调拨', '出土', '移交'][index % 5],
    collectionSource: ['湖南博物院调拨', '民间捐赠', '长沙市公安局移交', '考古发掘', '拍卖征集'][index % 5],
    collectionPrice: [0, 120000, 0, 0, 86000][index % 5],
    storageTime: day(300 - index * 9),
    cover: item.cover,
    createUserId: 'usr0000000000000000000000000001',
    createUserName: '张录入',
    createTime: dt(300 - index * 9, 10, 20),
    updateUserName: '张录入',
    updateTime: dt(20 - (index % 10), 15, 40),
    isDeleted: 0,
    memo: ''
  }
})

/* ------------------------------------------------------------------ */
/* 征集申请                                                            */
/* ------------------------------------------------------------------ */

const collectionSeed = [
  { name: '青釉莲瓣纹碗', cat: 0, era: '五代', texture: '陶瓷', way: '调拨', source: '湖南博物院调拨', price: 0, status: 2, collector: '张录入', days: 96 },
  { name: '青铜饕餮纹鼎', cat: 1, era: '商周', texture: '青铜', way: '购买', source: '长沙文物商店', price: 180000, status: 2, collector: '张录入', days: 88 },
  { name: '三彩骆驼俑', cat: 0, era: '唐代', texture: '陶瓷', way: '捐赠', source: '民间捐赠（陈氏家族）', price: 0, status: 2, collector: '张录入', days: 74 },
  { name: '龙泉窑青瓷瓶', cat: 0, era: '明清', texture: '陶瓷', way: '移交', source: '长沙市公安局移交', price: 0, status: 2, collector: '张录入', days: 60 },
  { name: '青花缠枝莲纹罐', cat: 0, era: '明清', texture: '陶瓷', way: '购买', source: '湘潭古玩市场', price: 96000, status: 1, collector: '张录入', days: 6 },
  { name: '四乳四神铜镜', cat: 1, era: '汉代', texture: '青铜', way: '移交', source: '考古发掘（望城坡 M3）', price: 0, status: 1, collector: '张录入', days: 4 },
  { name: '彩绘骑马俑', cat: 0, era: '唐代', texture: '陶瓷', way: '捐赠', source: '民间捐赠（刘氏）', price: 0, status: 0, collector: '张录入', days: 2 },
  { name: '青白玉璧', cat: 3, era: '汉代', texture: '玉器', way: '购买', source: '私人藏家转让', price: 260000, status: 3, collector: '张录入', days: 14 }
]

const collectionApplies = collectionSeed.map((item, index) => ({
  id: `col${String(index + 1).padStart(29, '0')}`,
  applyCode: `C-${YEAR}-${String(71 + index).padStart(3, '0')}`,
  batchNo: `B-${YEAR}-${String(Math.floor(index / 2) + 1).padStart(3, '0')}`,
  relicName: item.name,
  fkCategoryId: categories[item.cat].id,
  era: item.era,
  texture: item.texture,
  specification: '待补充实测数据',
  relicLevel: [1, 1, 2, 3, 2, 2, 3, 2][index],
  collectionSource: item.source,
  collectionWay: item.way,
  collectionPrice: item.price,
  collectionDesc: `${item.era}${item.texture}器，${item.source}，具体完残状况待入藏核验。`,
  collector: item.collector,
  collectionTime: day(item.days),
  applyStatus: item.status,
  auditOpinion: item.status === 3 ? '征集来源材料不完整，请补充权属证明后重新提交。' : item.status === 2 ? '材料齐全，同意征集。' : '',
  createUserId: 'usr0000000000000000000000000002',
  createUserName: '张录入',
  createTime: dt(item.days, 9, 20),
  updateTime: dt(item.days - 1 > 0 ? item.days - 1 : 0, 11, 5),
  isDeleted: 0,
  memo: ''
}))

/* ------------------------------------------------------------------ */
/* 出入库 / 修复 / 借调 / 注销                                          */
/* ------------------------------------------------------------------ */

const stockRecords = [
  { type: 1, relic: 0, from: null, to: 0, target: '湖南博物院调拨入库', operator: '王库房', days: 300, status: 1, reason: '征集审核通过，办理入藏入库' },
  { type: 1, relic: 1, from: null, to: 0, target: '长沙文物商店', operator: '王库房', days: 286, status: 1, reason: '征集入藏' },
  { type: 1, relic: 2, from: null, to: 0, target: '民间捐赠', operator: '王库房', days: 270, status: 1, reason: '捐赠入藏' },
  { type: 2, relic: 9, from: 1, to: null, target: '湖南省博物馆巡展', operator: '王库房', days: 26, status: 1, reason: '参加"宋瓷雅韵"巡展' },
  { type: 2, relic: 1, from: 0, to: null, target: '长沙市博物馆', operator: '张录入', days: 14, status: 1, reason: '展览借调出库交接' },
  { type: 3, relic: 3, from: 0, to: 1, target: '二号库房 B-01', operator: '王库房', days: 12, status: 1, reason: '调整至恒温库房存放' },
  { type: 1, relic: 6, from: null, to: 1, target: '修复室', operator: '王库房', days: 9, status: 0, reason: '修复前入库暂存' },
  { type: 2, relic: 6, from: 1, to: null, target: '修复室', operator: '张录入', days: 8, status: 0, reason: '送修复室开展装裱修复' },
  { type: 1, relic: 10, from: null, to: 2, target: '临时周转库', operator: '张录入', days: 5, status: 0, reason: '待编目暂存' },
  { type: 2, relic: 11, from: 2, to: null, target: '湖南省文物考古研究所', operator: '张录入', days: 3, status: 2, reason: '合作研究借用（未获审批）' }
]

stockRecords.forEach((item, index) => {
  const relic = relics[item.relic]
  item.id = `sr${String(index + 1).padStart(30, '0')}`
  item.recordCode = `S-${YEAR}-${String(index + 1).padStart(3, '0')}`
  item.recordType = item.type
  item.fkRelicId = relic.id
  item.fkFromWarehouseId = item.from === null ? null : warehouses[item.from].id
  item.fkToWarehouseId = item.to === null ? null : warehouses[item.to].id
  item.sourceTarget = item.target
  item.operatorName = item.operator
  item.operateTime = dt(item.days, 10, 30)
  item.operateReason = item.reason
  item.applyStatus = item.status
  item.auditOpinion = item.status === 2 ? '藏品状态与申请事由不符，暂不予出库。' : item.status === 1 ? '同意。' : ''
  item.createUserName = item.operator
  item.createTime = dt(item.days, 9, 50)
  item.isDeleted = 0
})

const repairs = [
  { relic: 6, item: '除尘、揭裱与局部补绢', user: '周修复', start: 8, end: null, before: '画心多处断裂，绢丝酥脆，边缘有霉斑。', after: '', status: 2 },
  { relic: 9, item: '口沿金缮修复', user: '周修复', start: 4, end: null, before: '口沿一处磕缺，釉面开片加剧。', after: '', status: 1 },
  { relic: 2, item: '包浆清理与裂隙加固', user: '周修复', start: 60, end: 42, before: '表面附着硬结物，龙尾处有细微裂隙。', after: '完成清理，裂隙以可逆材料加固，包浆保留完好。', status: 2 },
  { relic: 13, item: '虫蛀除治', user: '周修复', start: 20, end: null, before: '器身有活虫蛀孔。', after: '', status: 3 }
]

repairs.forEach((item, index) => {
  const relic = relics[item.relic]
  item.id = `rp${String(index + 1).padStart(30, '0')}`
  item.applyCode = `R-${YEAR}-${String(index + 1).padStart(3, '0')}`
  item.fkRelicId = relic.id
  item.repairItem = item.item
  item.repairUserName = item.user
  item.startTime = day(item.start)
  item.endTime = item.end === null ? null : day(item.end)
  item.conditionBefore = item.before
  item.conditionAfter = item.after
  item.applyStatus = item.status
  item.auditOpinion = item.status === 3 ? '该件已列入注销流程，暂不安排修复。' : item.status === 2 ? '同意修复，请做好过程记录。' : ''
  item.createUserName = '张录入'
  item.createTime = dt(item.start, 9, 10)
  item.isDeleted = 0
})

const loans = [
  { relic: 1, unit: '长沙市博物馆', user: '张录入', time: 14, plan: -20, actual: null, status: 2, opinion: '同意借展，请按期归还。' },
  { relic: 8, unit: '湖南博物院', user: '张录入', time: 3, plan: -30, actual: null, status: 1, opinion: '' },
  { relic: 3, unit: '湘潭市博物馆', user: '张录入', time: 120, plan: 60, actual: 58, status: 3, opinion: '同意借展。' },
  { relic: 10, unit: '岳阳楼景区管理处', user: '张录入', time: 6, plan: -45, actual: null, status: 4, opinion: '借展单位不具备恒温恒湿条件，不予借出。' }
]

loans.forEach((item, index) => {
  const relic = relics[item.relic]
  item.id = `ln${String(index + 1).padStart(30, '0')}`
  item.loanCode = `L-${YEAR}-${String(index + 1).padStart(3, '0')}`
  item.fkRelicId = relic.id
  item.loanUnit = item.unit
  item.loanUserName = item.user
  item.loanTime = day(item.time)
  item.planReturnTime = day(item.plan)
  item.actualReturnTime = item.actual === null ? null : day(item.actual)
  item.loanStatus = item.status
  item.auditOpinion = item.opinion
  item.agreementPath = item.status === 2 || item.status === 3 ? `/files/loan/${item.loanCode}.pdf` : null
  item.createUserName = '张录入'
  item.createTime = dt(item.time, 9, 0)
  item.isDeleted = 0
})

const cancels = [
  { relic: 12, reason: '藏品口沿严重残缺，修复价值低，经专家论证予以注销。', user: '张录入', status: 1, archiveUser: '李丰', archive: 1 },
  { relic: 11, reason: '重复入藏，与馆藏同型器物重叠。', user: '张录入', status: 1, archiveUser: null, archive: null },
  { relic: 13, reason: '器身受虫蛀严重，拟申请注销。', user: '张录入', status: 2, archiveUser: null, archive: null }
]

cancels.forEach((item, index) => {
  const relic = relics[item.relic]
  item.id = `cc${String(index + 1).padStart(30, '0')}`
  item.applyCode = `X-${YEAR}-${String(index + 1).padStart(3, '0')}`
  item.fkRelicId = relic.id
  item.cancelReason = item.reason
  item.cancelUserName = item.user
  item.applyStatus = item.status
  item.auditOpinion = item.status === 2 ? '注销理由不充分，建议先申报修复。' : item.status === 1 ? '同意注销，按规定归档。' : ''
  item.archiveUserName = item.archiveUser
  item.archiveTime = item.archive === null ? null : day(item.archive)
  item.createUserName = '张录入'
  item.createTime = dt(30 - index * 5, 14, 20)
  item.isDeleted = 0
})

/* ------------------------------------------------------------------ */
/* 修复 / 借调 / 注销审核通过后，同步更新藏品状态                        */
/* ------------------------------------------------------------------ */
relics[6].stockStatus = 4 // 缂丝花鸟图轴：修复中

/* ------------------------------------------------------------------ */
/* 溯源记录（按藏品组织的时间线）                                       */
/* ------------------------------------------------------------------ */

const traceTemplates = {
  0: [
    { type: 1, offset: 300, content: '由湖南博物院调拨征集，征集编号 C-' + YEAR + '-071，经审核员核验通过。', operator: '张录入' },
    { type: 2, offset: 298, content: '完成编目建档，生成藏品编号 ' + YEAR + '-0001 与溯源二维码。', operator: '张录入' },
    { type: 3, offset: 297, content: '验收入库，存放于一号库房 A-01 号柜。', operator: '王库房' }
  ],
  1: [
    { type: 1, offset: 286, content: '自长沙文物商店征集，成交价 180,000 元，经审核通过。', operator: '张录入' },
    { type: 2, offset: 284, content: '完成编目建档，定级一级文物，生成溯源二维码。', operator: '张录入' },
    { type: 3, offset: 283, content: '入藏一号库房 A-02 号柜。', operator: '王库房' },
    { type: 7, offset: 14, content: '借调至长沙市博物馆参加"青铜礼乐"特展，预计 ' + day(-20) + ' 归还。', operator: '张录入' }
  ],
  2: [
    { type: 1, offset: 270, content: '接受陈氏家族捐赠，征集审核通过。', operator: '张录入' },
    { type: 2, offset: 268, content: '编目建档完成，定级二级文物。', operator: '张录入' },
    { type: 3, offset: 267, content: '入藏一号库房 A-03 号柜。', operator: '王库房' },
    { type: 6, offset: 60, content: '完成包浆清理与裂隙加固，修复档案归档。', operator: '周修复' },
    { type: 7, offset: 120, content: '借调至湘潭市博物馆，借期 60 天。', operator: '张录入' },
    { type: 8, offset: 58, content: '按期归还，验收无新增损伤，恢复在库状态。', operator: '王库房' }
  ],
  3: [
    { type: 1, offset: 74, content: '接受民间捐赠，征集编号 C-' + YEAR + '-073。', operator: '张录入' },
    { type: 2, offset: 72, content: '编目建档，定级二级文物。', operator: '张录入' },
    { type: 3, offset: 70, content: '入藏二号库房 B-01 号柜。', operator: '王库房' },
    { type: 5, offset: 12, content: '由二号库房 B-01 移库至 B-03（恒温柜检修）。', operator: '王库房' }
  ],
  6: [
    { type: 1, offset: 200, content: '自湖南省文物考古研究所调拨征集。', operator: '张录入' },
    { type: 2, offset: 198, content: '编目建档，定级二级文物。', operator: '张录入' },
    { type: 3, offset: 197, content: '入藏二号库房 B-03 号柜。', operator: '王库房' },
    { type: 4, offset: 9, content: '出库送修复室，开展除尘、揭裱与局部补绢。', operator: '张录入' },
    { type: 6, offset: 8, content: '修复申请审核通过，修复人周修复开始作业，预计工期 30 天。', operator: '李审核' }
  ],
  9: [
    { type: 1, offset: 240, content: '考古发掘出土，移交入藏。', operator: '张录入' },
    { type: 2, offset: 236, content: '编目建档，定级三级文物。', operator: '张录入' },
    { type: 3, offset: 235, content: '入藏二号库房 B-05 号柜。', operator: '王库房' },
    { type: 4, offset: 26, content: '出库参加"宋瓷雅韵"巡展，去向湖南省博物馆。', operator: '王库房' }
  ],
  12: [
    { type: 1, offset: 260, content: '由长沙市公安局移交征集。', operator: '张录入' },
    { type: 2, offset: 258, content: '编目建档，定级三级文物。', operator: '张录入' },
    { type: 3, offset: 257, content: '入藏一号库房 A-03 号柜。', operator: '王库房' },
    { type: 9, offset: 2, content: '因口沿严重残缺，经专家论证与审核通过，完成注销归档。', operator: '李丰' }
  ]
}

const defaultTrace = (relic) => [
  { type: 1, offset: 180, content: `完成征集登记，来源：${relic.collectionSource}。`, operator: '张录入' },
  { type: 2, offset: 178, content: `编目建档完成，藏品编号 ${relic.relicCode}，生成溯源二维码。`, operator: '张录入' },
  { type: 3, offset: 176, content: '验收入库，登记存放库房与仓位。', operator: '王库房' }
]

const traces = []
let traceSeq = 0
relics.forEach((relic, index) => {
  const items = traceTemplates[index] || defaultTrace(relic)
  items.forEach((t) => {
    traceSeq += 1
    traces.push({
      id: `tr${String(traceSeq).padStart(30, '0')}`,
      fkRelicId: relic.id,
      traceType: t.type,
      fkBizId: null,
      traceTime: dt(t.offset, 9 + (traceSeq % 8), (traceSeq * 7) % 60),
      operatorName: t.operator,
      traceContent: t.content,
      createUserName: t.operator,
      createTime: dt(t.offset, 9, 30),
      isDeleted: 0
    })
  })
})
// 时间线按时间倒序展示，这里统一升序保存，读取时由接口排序
traces.sort((a, b) => (a.traceTime > b.traceTime ? 1 : -1))

/* ------------------------------------------------------------------ */
/* 二维码 / 附件 / 盘点 / 审核记录 / 日志                               */
/* ------------------------------------------------------------------ */

const qrCodes = relics
  .filter((r) => r.isPublic === 1)
  .map((r, index) => ({
    id: `qr${String(index + 1).padStart(30, '0')}`,
    fkRelicId: r.id,
    qrContent: r.qrcodeContent,
    qrPath: `/files/qrcode/${r.relicCode}.png`,
    generateUserName: '张录入',
    generateTime: r.createTime,
    scanCount: [42, 18, 26, 7, 33, 12, 9, 21, 5, 15, 4, 3][index] || 0,
    lastScanTime: dt(index % 12, 14, 10)
  }))

const attachments = []
relics.forEach((r, index) => {
  attachments.push({
    id: `at${String(index * 2 + 1).padStart(30, '0')}`,
    fkRelicId: r.id,
    fkBizId: null,
    fileName: `${r.relicName}-正面影像.jpg`,
    filePath: `/files/relic/${r.relicCode}-01.jpg`,
    fileType: 1,
    fileSize: 1024 * 1024 * (2 + (index % 3)),
    uploadUserName: '张录入',
    uploadTime: r.createTime,
    isDeleted: 0
  })
  attachments.push({
    id: `at${String(index * 2 + 2).padStart(30, '0')}`,
    fkRelicId: r.id,
    fkBizId: null,
    fileName: `${r.relicName}-细部影像.jpg`,
    filePath: `/files/relic/${r.relicCode}-02.jpg`,
    fileType: 1,
    fileSize: 1024 * 1024 * (1 + (index % 4)),
    uploadUserName: '张录入',
    uploadTime: r.createTime,
    isDeleted: 0
  })
})
// 修复前后照片
attachments.push(
  {
    id: 'at90000000000000000000000000001', fkRelicId: relics[2].id, fkBizId: repairs[2].id,
    fileName: '青玉螭龙佩-修复前.jpg', filePath: '/files/relic/repair-before-01.jpg', fileType: 2,
    fileSize: 1024 * 1024 * 3, uploadUserName: '周修复', uploadTime: dt(62, 10, 0), isDeleted: 0
  },
  {
    id: 'at90000000000000000000000000002', fkRelicId: relics[2].id, fkBizId: repairs[2].id,
    fileName: '青玉螭龙佩-修复后.jpg', filePath: '/files/relic/repair-after-01.jpg', fileType: 3,
    fileSize: 1024 * 1024 * 3, uploadUserName: '周修复', uploadTime: dt(42, 16, 0), isDeleted: 0
  }
)

const stockChecks = [
  {
    id: 'sc0000000000000000000000000001', checkCode: `K-${YEAR}-001`, fkWarehouseId: warehouses[0].id,
    checkUserName: '王库房', checkTime: day(18), totalCount: 6, realCount: 6, abnormalCount: 0,
    checkStatus: 2, checkResult: '账实相符，无异常。', createUserName: '王库房', createTime: dt(18, 9, 0), isDeleted: 0
  },
  {
    id: 'sc0000000000000000000000000002', checkCode: `K-${YEAR}-002`, fkWarehouseId: warehouses[1].id,
    checkUserName: '王库房', checkTime: day(2), totalCount: 5, realCount: 4, abnormalCount: 1,
    checkStatus: 1, checkResult: '1 件藏品处于修复中，实物不在库，待复核。', createUserName: '王库房', createTime: dt(2, 9, 0), isDeleted: 0
  }
]

const auditRecords = []
let auditSeq = 0
collectionApplies
  .filter((c) => c.applyStatus === 2 || c.applyStatus === 3)
  .forEach((c) => {
    auditSeq += 1
    auditRecords.push({
      id: `au${String(auditSeq).padStart(30, '0')}`,
      bizType: 1,
      fkBizId: c.id,
      bizCode: c.applyCode,
      auditUserId: 'usr0000000000000000000000000003',
      auditUserName: '李审核',
      auditTime: c.updateTime,
      auditResult: c.applyStatus === 2 ? 1 : 2,
      auditOpinion: c.auditOpinion,
      createTime: c.updateTime,
      isDeleted: 0
    })
  })
stockRecords
  .filter((s) => s.applyStatus !== 0)
  .forEach((s) => {
    auditSeq += 1
    auditRecords.push({
      id: `au${String(auditSeq).padStart(30, '0')}`,
      bizType: 2,
      fkBizId: s.id,
      bizCode: s.recordCode,
      auditUserId: 'usr0000000000000000000000000003',
      auditUserName: '李审核',
      auditTime: s.operateTime,
      auditResult: s.applyStatus === 1 ? 1 : 2,
      auditOpinion: s.auditOpinion,
      createTime: s.operateTime,
      isDeleted: 0
    })
  })
repairs
  .filter((r) => r.applyStatus !== 1)
  .forEach((r) => {
    auditSeq += 1
    auditRecords.push({
      id: `au${String(auditSeq).padStart(30, '0')}`,
      bizType: 3,
      fkBizId: r.id,
      bizCode: r.applyCode,
      auditUserId: 'usr0000000000000000000000000003',
      auditUserName: '李审核',
      auditTime: r.createTime,
      auditResult: r.applyStatus === 2 ? 1 : 2,
      auditOpinion: r.auditOpinion,
      createTime: r.createTime,
      isDeleted: 0
    })
  })
loans
  .filter((l) => l.loanStatus === 2 || l.loanStatus === 3 || l.loanStatus === 4)
  .forEach((l) => {
    auditSeq += 1
    auditRecords.push({
      id: `au${String(auditSeq).padStart(30, '0')}`,
      bizType: 4,
      fkBizId: l.id,
      bizCode: l.loanCode,
      auditUserId: 'usr0000000000000000000000000003',
      auditUserName: '李审核',
      auditTime: l.createTime,
      auditResult: l.loanStatus === 4 ? 2 : 1,
      auditOpinion: l.auditOpinion,
      createTime: l.createTime,
      isDeleted: 0
    })
  })
cancels
  .filter((c) => c.applyStatus !== 0)
  .forEach((c) => {
    auditSeq += 1
    auditRecords.push({
      id: `au${String(auditSeq).padStart(30, '0')}`,
      bizType: 5,
      fkBizId: c.id,
      bizCode: c.applyCode,
      auditUserId: 'usr0000000000000000000000000003',
      auditUserName: '李审核',
      auditTime: c.createTime,
      auditResult: c.applyStatus === 1 ? 1 : 2,
      auditOpinion: c.auditOpinion,
      createTime: c.createTime,
      isDeleted: 0
    })
  })

const dicts = [
  ['collection_way', '1', '捐赠'], ['collection_way', '2', '购买'], ['collection_way', '3', '调拨'],
  ['collection_way', '4', '出土'], ['collection_way', '5', '移交'],
  ['era', '1', '新石器时代'], ['era', '2', '商周'], ['era', '3', '汉代'], ['era', '4', '唐代'],
  ['era', '5', '五代'], ['era', '6', '宋代'], ['era', '7', '明清'], ['era', '8', '近现代'],
  ['texture', '1', '陶瓷'], ['texture', '2', '青铜'], ['texture', '3', '玉器'],
  ['texture', '4', '书画'], ['texture', '5', '织绣'], ['texture', '6', '竹木'],
  ['relic_level', '1', '一级'], ['relic_level', '2', '二级'], ['relic_level', '3', '三级'], ['relic_level', '4', '一般'],
  ['integrity_status', '1', '完好'], ['integrity_status', '2', '基本完好'], ['integrity_status', '3', '局部残损'], ['integrity_status', '4', '严重残损']
].map(([type, code, label], index) => ({
  id: `dict${String(index + 1).padStart(28, '0')}`,
  dictType: type,
  dictCode: code,
  dictLabel: label,
  sortNo: index + 1,
  isEnabled: 1,
  createUserName: '李丰',
  createTime: dt(200),
  updateTime: dt(30),
  remark: '',
  isDeleted: 0
}))

const configs = [
  { configKey: 'system.name', configName: '系统名称', configValue: '文物藏品管理与溯源系统', description: '用于登录页与页头展示' },
  { configKey: 'relic_code.prefix', configName: '藏品编号前缀', configValue: String(YEAR), description: '编目建档时生成藏品编号的前缀，如 2026-0001' },
  { configKey: 'qrcode.rule', configName: '溯源二维码规则', configValue: 'relic://trace/{relicCode}', description: '二维码内容模板，{relicCode} 会被替换为藏品编号' },
  { configKey: 'apply.batch.approval', configName: '审核批量通过', configValue: 'true', description: '审核工作台是否允许批量通过' },
  { configKey: 'loan.overdue.remind', configName: '借调到期提醒（天）', configValue: '7', description: '借调到期前多少天在首页提示' },
  { configKey: 'attachment.max.size', configName: '附件大小上限（MB）', configValue: '20', description: '影像资料上传的单个文件大小限制' },
  { configKey: 'stock.check.auto', configName: '自动生成盘点任务', configValue: 'false', description: '是否每月 1 日自动生成全库盘点单' }
].map((item, index) => ({
  id: `cfg${String(index + 1).padStart(29, '0')}`,
  configKey: item.configKey,
  configName: item.configName,
  configValue: item.configValue,
  description: item.description,
  createUserName: '李丰',
  createTime: dt(200),
  updateTime: dt(20),
  isDeleted: 0,
  version: 0
}))

const operateLogs = [
  ['张录入', '藏品征集', '新增', '新增征集申请 C-' + YEAR + '-075「彩绘骑马俑」'],
  ['李审核', '审核工作台', '审核', '审核征集申请 C-' + YEAR + '-074，结果：通过'],
  ['王库房', '库房管理', '新增', '创建盘点任务 K-' + YEAR + '-002（二号库房）'],
  ['张录入', '修复管理', '新增', '提交修复申请 R-' + YEAR + '-002「黑釉油滴盏」'],
  ['李审核', '审核工作台', '审核', '审核修复申请 R-' + YEAR + '-001，结果：通过'],
  ['李丰', '用户管理', '修改', '重置用户「张录入」登录密码'],
  ['李丰', '系统参数', '修改', '参数 qrcode.rule 调整为 relic://trace/{relicCode}'],
  ['张录入', '出入库管理', '新增', '登记出库单 S-' + YEAR + '-010「织锦龙纹袍」'],
  ['李丰', '藏品档案', '删除', '逻辑删除藏品 ' + YEAR + '-0013「龙泉窑青瓷瓶」'],
  ['李审核', '审核工作台', '审核', '审核注销申请 X-' + YEAR + '-001，结果：通过'],
  ['王库房', '库房管理', '新增', '新增仓位 B-05（二号库房）'],
  ['李丰', '数据字典', '新增', '新增字典项 era / 近现代']
].map(([user, module, type, content], index) => ({
  id: `lg${String(index + 1).padStart(30, '0')}`,
  operateUserId: users.find((u) => u.realName === user)?.id || 'usr0000000000000000000000000001',
  operateUserName: user,
  module,
  operateType: type,
  operateContent: content,
  operateTime: dt(index, 9 + (index % 9), (index * 11) % 60),
  createTime: dt(index, 9, 0),
  isDeleted: 0
}))

/** 汇总导出，供 mock 服务初始化 */
export function createDatabase() {
  return {
    version: 3,
    users,
    categories,
    warehouses,
    positions,
    relics,
    collectionApplies,
    stockRecords,
    repairs,
    loans,
    cancels,
    traces,
    qrCodes,
    attachments,
    stockChecks,
    auditRecords,
    dicts,
    configs,
    operateLogs
  }
}

export { categories, warehouses, positions }
