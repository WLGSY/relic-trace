-- =====================================================================
-- 文物藏品管理与溯源系统 · 初始化种子数据
-- 说明：演示账号密码统一为 123456（BCrypt 加密后写入）。
-- =====================================================================

USE `relic_db`;

-- ---------------------------------------------------------------------
-- 系统用户（演示账号密码均为 123456）
-- 说明：用户账号由应用启动时 DataInitializer 自动创建（BCrypt 现场加密），
--       无需在此脚本中硬编码密码哈希；此处保留 SQL 以说明表结构填充方式。
--       role_type：1 系统管理员 / 2 录入员 / 3 审核员
-- ---------------------------------------------------------------------
-- 演示账号：admin（管理员）、zhangly（录入员）、lishen（审核员），密码 123456
-- 由 DataInitializer 在首次启动时自动初始化，无需手动执行。

-- ---------------------------------------------------------------------
-- 藏品分类（三级分类示例）
-- ---------------------------------------------------------------------
INSERT INTO `relic_category` (`id`, `category_code`, `category_name`, `fk_parent_id`, `category_level`, `is_leaf`, `sort_no`, `create_user_id`, `create_user_name`, `create_time`, `update_user_id`, `update_user_name`, `update_time`, `is_deleted`, `version`) VALUES
('c-0001', 'TC', '陶瓷', NULL, 1, 0, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, 0),
('c-0002', 'QT', '青铜', NULL, 1, 0, 2, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, 0),
('c-0003', 'YQ', '玉器', NULL, 1, 0, 3, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, 0),
('c-0004', 'SH', '书画', NULL, 1, 1, 4, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, 0),
('c-0005', 'TC-CQ', '瓷器', 'c-0001', 2, 1, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, 0),
('c-0006', 'TC-TQ', '陶器', 'c-0001', 2, 1, 2, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, 0),
('c-0007', 'QT-LD', '礼器', 'c-0002', 2, 1, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, 0),
('c-0008', 'QT-BQ', '兵器', 'c-0002', 2, 1, 2, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, 0);

-- ---------------------------------------------------------------------
-- 数据字典（对应前端 DICT_FALLBACK）
-- ---------------------------------------------------------------------
INSERT INTO `sys_dict` (`id`, `dict_type`, `dict_code`, `dict_label`, `sort_no`, `is_enabled`, `create_user_id`, `create_user_name`, `create_time`, `update_user_id`, `update_user_name`, `update_time`, `is_deleted`, `remark`) VALUES
('d-0001', 'relic_level', '1', '一级', 1, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, '藏品级别'),
('d-0002', 'relic_level', '2', '二级', 2, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, '藏品级别'),
('d-0003', 'relic_level', '3', '三级', 3, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, '藏品级别'),
('d-0004', 'relic_level', '4', '一般', 4, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, '藏品级别'),
('d-0005', 'collection_way', '1', '捐赠', 1, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, '征集方式'),
('d-0006', 'collection_way', '2', '购买', 2, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, '征集方式'),
('d-0007', 'collection_way', '3', '调拨', 3, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, '征集方式'),
('d-0008', 'collection_way', '4', '出土', 4, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, '征集方式'),
('d-0009', 'collection_way', '5', '移交', 5, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, '征集方式'),
('d-0010', 'era', '1', '新石器时代', 1, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, '年代'),
('d-0011', 'era', '2', '商周', 2, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, '年代'),
('d-0012', 'era', '3', '汉代', 3, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, '年代'),
('d-0013', 'era', '4', '唐代', 4, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, '年代'),
('d-0014', 'era', '5', '宋代', 6, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, '年代'),
('d-0015', 'era', '6', '明清', 7, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, '年代'),
('d-0016', 'era', '7', '近现代', 8, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, '年代'),
('d-0017', 'texture', '1', '陶瓷', 1, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, '质地'),
('d-0018', 'texture', '2', '青铜', 2, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, '质地'),
('d-0019', 'texture', '3', '玉器', 3, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, '质地'),
('d-0020', 'texture', '4', '书画', 4, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, '质地'),
('d-0021', 'integrity_status', '1', '完好', 1, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, '完残状况'),
('d-0022', 'integrity_status', '2', '基本完好', 2, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, '完残状况'),
('d-0023', 'integrity_status', '3', '局部残损', 3, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, '完残状况'),
('d-0024', 'integrity_status', '4', '严重残损', 4, 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, '完残状况');

-- ---------------------------------------------------------------------
-- 系统参数配置
-- ---------------------------------------------------------------------
INSERT INTO `sys_config` (`id`, `config_key`, `config_name`, `config_value`, `description`, `create_user_id`, `create_user_name`, `create_time`, `update_user_id`, `update_user_name`, `update_time`, `is_deleted`, `version`) VALUES
('cfg-0001', 'relic_code.prefix', '藏品编号前缀', 'WW', '编目建档时生成藏品编号的前缀', 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, 0),
('cfg-0002', 'qrcode.rule', '二维码生成规则', 'http://localhost:5173/#/scan/{relicCode}', '溯源二维码内容生成规则，内嵌藏品编号', 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, 0);

-- ---------------------------------------------------------------------
-- 库房与仓位（示例数据）
-- ---------------------------------------------------------------------
INSERT INTO `relic_warehouse` (`id`, `warehouse_code`, `warehouse_name`, `location`, `keeper_name`, `is_enabled`, `create_user_id`, `create_user_name`, `create_time`, `update_user_id`, `update_user_name`, `update_time`, `is_deleted`, `version`) VALUES
('w-0001', 'WH-001', '一号库房', '主楼一层东侧', '王保管', 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, 0),
('w-0002', 'WH-002', '二号库房', '主楼一层西侧', '赵保管', 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, 0),
('w-0003', 'WH-003', '精品库房', '地下恒温库', '孙保管', 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, 0);

INSERT INTO `relic_storage_position` (`id`, `position_code`, `position_name`, `fk_warehouse_id`, `is_enabled`, `create_user_id`, `create_user_name`, `create_time`, `update_user_id`, `update_user_name`, `update_time`, `is_deleted`, `version`) VALUES
('p-0001', 'A-01', 'A区01柜', 'w-0001', 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, 0),
('p-0002', 'A-02', 'A区02柜', 'w-0001', 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, 0),
('p-0003', 'B-01', 'B区01柜', 'w-0002', 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, 0),
('p-0004', 'J-01', '精品柜01', 'w-0003', 1, 'u-admin-0001', '系统管理员', NOW(), 'u-admin-0001', '系统管理员', NOW(), 0, 0);
