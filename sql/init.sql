-- =====================================================================
-- 文物藏品管理与溯源系统 · 数据库初始化脚本
-- 命名规则遵循《设计文档》2.4.2 数据库命名规则：
--   1) 表名、字段名使用小写字母或数字，下划线分隔，禁止数字开头；
--   2) 表达「是否」概念的字段使用 is_xxx，类型 unsigned tinyint（1 是 / 0 否）；
--   3) 非负数字段必须 unsigned；
--   4) 表名不使用复数名词。
-- 数据库：MySQL 8.0
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `relic_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `relic_db`;

-- ---------------------------------------------------------------------
-- 1. 系统用户表 sys_user
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`               varchar(32)  NOT NULL COMMENT '用户id，主键，UUID',
    `login_name`       varchar(50)  NOT NULL COMMENT '登录账号，唯一',
    `login_password`   varchar(100) NOT NULL COMMENT '登录密码，BCrypt加密存储',
    `real_name`        varchar(50)  NOT NULL COMMENT '真实姓名',
    `role_type`        tinyint unsigned NOT NULL COMMENT '角色类型：1系统管理员，2录入员，3审核员',
    `phone`            varchar(20)  DEFAULT NULL COMMENT '联系电话',
    `avatar`           varchar(200) DEFAULT NULL COMMENT '头像地址',
    `is_enabled`       tinyint unsigned NOT NULL DEFAULT 1 COMMENT '是否启用：1是，0否',
    `last_login_time`  datetime     DEFAULT NULL COMMENT '最后登录时间',
    `create_user_id`   varchar(32)  NOT NULL DEFAULT '' COMMENT '创建人id',
    `create_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '创建人',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id`   varchar(32)  NOT NULL DEFAULT '' COMMENT '修改人id',
    `update_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '修改人姓名',
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted`       tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除：0否，1是',
    `version`          int unsigned DEFAULT 0 COMMENT '乐观锁',
    `memo`             varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_login_name` (`login_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ---------------------------------------------------------------------
-- 2. 藏品分类表 relic_category
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `relic_category`;
CREATE TABLE `relic_category` (
    `id`               varchar(32)  NOT NULL COMMENT '分类id，主键，UUID',
    `category_code`    varchar(20)  NOT NULL COMMENT '分类编码，唯一',
    `category_name`    varchar(50)  NOT NULL COMMENT '分类名称',
    `fk_parent_id`     varchar(32)  DEFAULT NULL COMMENT '父级分类id，顶级分类为空',
    `category_level`   tinyint unsigned NOT NULL COMMENT '分类层级：1一级，2二级，3三级',
    `is_leaf`          tinyint unsigned NOT NULL DEFAULT 1 COMMENT '是否叶子节点：1是，0否',
    `sort_no`          int unsigned DEFAULT 0 COMMENT '排序号，数值越小越靠前',
    `create_user_id`   varchar(32)  NOT NULL DEFAULT '' COMMENT '创建人id',
    `create_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '创建人',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id`   varchar(32)  NOT NULL DEFAULT '' COMMENT '修改人id',
    `update_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '修改人姓名',
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted`       tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除：0否，1是',
    `version`          int unsigned DEFAULT 0 COMMENT '乐观锁',
    `memo`             varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_code` (`category_code`),
    KEY `idx_parent_id` (`fk_parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='藏品分类表';

-- ---------------------------------------------------------------------
-- 3. 藏品征集申请表 relic_collection_apply
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `relic_collection_apply`;
CREATE TABLE `relic_collection_apply` (
    `id`               varchar(32)  NOT NULL COMMENT '征集申请id，主键，UUID',
    `apply_code`       varchar(50)  NOT NULL COMMENT '征集编号，唯一',
    `batch_no`         varchar(50)  DEFAULT NULL COMMENT '征集批次号',
    `relic_name`       varchar(100) NOT NULL COMMENT '藏品名称',
    `fk_category_id`   varchar(32)  NOT NULL COMMENT '藏品分类id，关联relic_category表',
    `era`              varchar(50)  DEFAULT NULL COMMENT '年代',
    `texture`          varchar(50)  DEFAULT NULL COMMENT '质地',
    `specification`    varchar(100) DEFAULT NULL COMMENT '规格',
    `relic_level`      tinyint unsigned DEFAULT NULL COMMENT '藏品级别：1一级，2二级，3三级，4一般',
    `collection_source` varchar(100) DEFAULT NULL COMMENT '征集来源',
    `collection_way`   varchar(50)  DEFAULT NULL COMMENT '征集方式',
    `collection_desc`  varchar(1000) DEFAULT NULL COMMENT '藏品描述',
    `collector`        varchar(50)  DEFAULT NULL COMMENT '征集人',
    `collection_time`  datetime     DEFAULT NULL COMMENT '征集时间',
    `apply_status`     tinyint unsigned NOT NULL DEFAULT 0 COMMENT '申请状态：0草稿，1待审核，2审核通过，3审核驳回',
    `audit_opinion`    varchar(500) DEFAULT NULL COMMENT '审核意见',
    `create_user_id`   varchar(32)  NOT NULL DEFAULT '' COMMENT '创建人id',
    `create_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '创建人',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id`   varchar(32)  NOT NULL DEFAULT '' COMMENT '修改人id',
    `update_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '修改人姓名',
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted`       tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除：0否，1是',
    `version`          int unsigned DEFAULT 0 COMMENT '乐观锁',
    `memo`             varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_apply_code` (`apply_code`),
    KEY `idx_category_id` (`fk_category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='藏品征集申请表';

-- ---------------------------------------------------------------------
-- 4. 藏品信息表 relic_info
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `relic_info`;
CREATE TABLE `relic_info` (
    `id`                  varchar(32)  NOT NULL COMMENT '藏品id，主键，UUID',
    `relic_code`          varchar(32)  NOT NULL COMMENT '藏品编号，全局唯一',
    `relic_name`          varchar(100) NOT NULL COMMENT '藏品名称',
    `fk_category_id`      varchar(32)  NOT NULL COMMENT '藏品分类id，关联relic_category表',
    `fk_collection_apply_id` varchar(32) DEFAULT NULL COMMENT '征集申请id，关联relic_collection_apply表',
    `era`                 varchar(50)  DEFAULT NULL COMMENT '年代',
    `texture`             varchar(50)  DEFAULT NULL COMMENT '质地',
    `specification`       varchar(100) DEFAULT NULL COMMENT '规格',
    `relic_level`         tinyint unsigned DEFAULT NULL COMMENT '藏品级别',
    `quantity`            int unsigned DEFAULT 1 COMMENT '数量，默认为1',
    `integrity_status`    varchar(200) DEFAULT NULL COMMENT '完残状况',
    `fk_warehouse_id`     varchar(32)  DEFAULT NULL COMMENT '所在库房id，关联relic_warehouse表',
    `fk_position_id`      varchar(32)  DEFAULT NULL COMMENT '所在仓位id，关联relic_storage_position表',
    `stock_status`        tinyint unsigned NOT NULL DEFAULT 1 COMMENT '库存状态：1在库，2出库，3借出，4修复中，5已注销',
    `qrcode_content`      varchar(200) DEFAULT NULL COMMENT '溯源二维码内容',
    `is_public`           tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否公开：1公开，0保密',
    `relic_desc`          text         COMMENT '藏品详情描述',
    `create_user_id`      varchar(32)  NOT NULL DEFAULT '' COMMENT '创建人id',
    `create_user_name`    varchar(50)  NOT NULL DEFAULT '' COMMENT '创建人',
    `create_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id`      varchar(32)  NOT NULL DEFAULT '' COMMENT '修改人id',
    `update_user_name`    varchar(50)  NOT NULL DEFAULT '' COMMENT '修改人姓名',
    `update_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted`          tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除：0否，1是',
    `version`             int unsigned DEFAULT 0 COMMENT '乐观锁',
    `memo`                varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_relic_code` (`relic_code`),
    KEY `idx_category_id` (`fk_category_id`),
    KEY `idx_warehouse_id` (`fk_warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='藏品信息表';

-- ---------------------------------------------------------------------
-- 5. 藏品附件表 relic_attachment
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `relic_attachment`;
CREATE TABLE `relic_attachment` (
    `id`               varchar(32)  NOT NULL COMMENT '附件id，主键，UUID',
    `fk_relic_id`      varchar(32)  NOT NULL COMMENT '藏品id，关联relic_info表',
    `fk_biz_id`        varchar(32)  DEFAULT NULL COMMENT '关联业务id',
    `file_name`        varchar(200) NOT NULL COMMENT '文件名称',
    `file_path`        varchar(500) NOT NULL COMMENT '文件存储路径',
    `file_type`        tinyint unsigned NOT NULL COMMENT '附件类型：1藏品影像，2修复前照片，3修复后照片，4借调协议，5其他',
    `file_size`        bigint unsigned DEFAULT NULL COMMENT '文件大小，单位字节',
    `upload_user_id`   varchar(32)  NOT NULL DEFAULT '' COMMENT '上传人id',
    `upload_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '上传人',
    `upload_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `is_deleted`       tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除：0否，1是',
    PRIMARY KEY (`id`),
    KEY `idx_relic_id` (`fk_relic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='藏品附件表';

-- ---------------------------------------------------------------------
-- 6. 库房信息表 relic_warehouse
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `relic_warehouse`;
CREATE TABLE `relic_warehouse` (
    `id`               varchar(32)  NOT NULL COMMENT '库房id，主键，UUID',
    `warehouse_code`   varchar(20)  NOT NULL COMMENT '库房编号，唯一',
    `warehouse_name`   varchar(50)  NOT NULL COMMENT '库房名称',
    `location`         varchar(200) DEFAULT NULL COMMENT '库房位置',
    `keeper_name`      varchar(50)  DEFAULT NULL COMMENT '保管员',
    `is_enabled`       tinyint unsigned NOT NULL DEFAULT 1 COMMENT '是否启用：1是，0否',
    `create_user_id`   varchar(32)  NOT NULL DEFAULT '' COMMENT '创建人id',
    `create_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '创建人',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id`   varchar(32)  NOT NULL DEFAULT '' COMMENT '修改人id',
    `update_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '修改人姓名',
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted`       tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除：0否，1是',
    `version`          int unsigned DEFAULT 0 COMMENT '乐观锁',
    `memo`             varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_warehouse_code` (`warehouse_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库房信息表';

-- ---------------------------------------------------------------------
-- 7. 仓位信息表 relic_storage_position
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `relic_storage_position`;
CREATE TABLE `relic_storage_position` (
    `id`               varchar(32)  NOT NULL COMMENT '仓位id，主键，UUID',
    `position_code`    varchar(20)  NOT NULL COMMENT '仓位编号，库房内唯一',
    `position_name`    varchar(50)  NOT NULL COMMENT '仓位名称',
    `fk_warehouse_id`  varchar(32)  NOT NULL COMMENT '所属库房id，关联relic_warehouse表',
    `is_enabled`       tinyint unsigned NOT NULL DEFAULT 1 COMMENT '是否启用：1是，0否',
    `create_user_id`   varchar(32)  NOT NULL DEFAULT '' COMMENT '创建人id',
    `create_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '创建人',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id`   varchar(32)  NOT NULL DEFAULT '' COMMENT '修改人id',
    `update_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '修改人姓名',
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted`       tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除：0否，1是',
    `version`          int unsigned DEFAULT 0 COMMENT '乐观锁',
    `memo`             varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_warehouse_id` (`fk_warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓位信息表';

-- ---------------------------------------------------------------------
-- 8. 出入库记录表 relic_stock_record
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `relic_stock_record`;
CREATE TABLE `relic_stock_record` (
    `id`                  varchar(32)  NOT NULL COMMENT '出入库记录id，主键，UUID',
    `record_code`         varchar(50)  NOT NULL COMMENT '单据编号，唯一',
    `record_type`         tinyint unsigned NOT NULL COMMENT '业务类型：1入库，2出库，3移库',
    `fk_relic_id`         varchar(32)  NOT NULL COMMENT '藏品id，关联relic_info表',
    `source_target`       varchar(200) DEFAULT NULL COMMENT '来源或去向',
    `fk_from_warehouse_id` varchar(32) DEFAULT NULL COMMENT '原库房id',
    `fk_to_warehouse_id`  varchar(32)  DEFAULT NULL COMMENT '目标库房id',
    `operator_name`       varchar(50)  NOT NULL DEFAULT '' COMMENT '经办人',
    `operate_time`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '经办时间',
    `operate_reason`      varchar(500) DEFAULT NULL COMMENT '事由',
    `apply_status`        tinyint unsigned NOT NULL DEFAULT 0 COMMENT '申请状态：0待审核，1审核通过，2审核驳回',
    `audit_opinion`       varchar(500) DEFAULT NULL COMMENT '审核意见',
    `create_user_id`      varchar(32)  NOT NULL DEFAULT '' COMMENT '创建人id',
    `create_user_name`    varchar(50)  NOT NULL DEFAULT '' COMMENT '创建人',
    `create_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id`      varchar(32)  NOT NULL DEFAULT '' COMMENT '修改人id',
    `update_user_name`    varchar(50)  NOT NULL DEFAULT '' COMMENT '修改人姓名',
    `update_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted`          tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除：0否，1是',
    `version`             int unsigned DEFAULT 0 COMMENT '乐观锁',
    `memo`                varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_record_code` (`record_code`),
    KEY `idx_relic_id` (`fk_relic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出入库记录表';

-- ---------------------------------------------------------------------
-- 9. 库存盘点表 relic_stock_check
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `relic_stock_check`;
CREATE TABLE `relic_stock_check` (
    `id`               varchar(32)  NOT NULL COMMENT '盘点id，主键，UUID',
    `check_code`       varchar(50)  NOT NULL COMMENT '盘点单号，唯一',
    `fk_warehouse_id`  varchar(32)  NOT NULL COMMENT '盘点库房id，关联relic_warehouse表',
    `check_user_name`  varchar(50)  NOT NULL DEFAULT '' COMMENT '盘点人',
    `check_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '盘点时间',
    `total_count`      int unsigned NOT NULL DEFAULT 0 COMMENT '应盘数量',
    `real_count`       int unsigned NOT NULL DEFAULT 0 COMMENT '实盘数量',
    `abnormal_count`   int unsigned DEFAULT 0 COMMENT '异常数量，默认为0',
    `check_status`     tinyint unsigned NOT NULL DEFAULT 1 COMMENT '盘点状态：1盘点中，2已完成',
    `check_result`     varchar(500) DEFAULT NULL COMMENT '盘点结果说明',
    `create_user_id`   varchar(32)  NOT NULL DEFAULT '' COMMENT '创建人id',
    `create_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '创建人',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id`   varchar(32)  NOT NULL DEFAULT '' COMMENT '修改人id',
    `update_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '修改人姓名',
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted`       tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除：0否，1是',
    `version`          int unsigned DEFAULT 0 COMMENT '乐观锁',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_check_code` (`check_code`),
    KEY `idx_warehouse_id` (`fk_warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点表';

-- ---------------------------------------------------------------------
-- 10. 修复申请表 relic_repair_apply
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `relic_repair_apply`;
CREATE TABLE `relic_repair_apply` (
    `id`               varchar(32)  NOT NULL COMMENT '修复申请id，主键，UUID',
    `apply_code`       varchar(50)  NOT NULL COMMENT '修复单号，唯一',
    `fk_relic_id`      varchar(32)  NOT NULL COMMENT '藏品id，关联relic_info表',
    `repair_item`      varchar(200) NOT NULL COMMENT '修复项目',
    `repair_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '修复人',
    `start_time`       datetime     DEFAULT NULL COMMENT '修复开始时间',
    `end_time`         datetime     DEFAULT NULL COMMENT '修复结束时间',
    `condition_before` varchar(500) DEFAULT NULL COMMENT '修复前状况',
    `condition_after`  varchar(500) DEFAULT NULL COMMENT '修复后状况',
    `apply_status`     tinyint unsigned NOT NULL DEFAULT 0 COMMENT '申请状态：0草稿，1待审核，2审核通过，3审核驳回',
    `audit_opinion`    varchar(500) DEFAULT NULL COMMENT '审核意见',
    `create_user_id`   varchar(32)  NOT NULL DEFAULT '' COMMENT '创建人id',
    `create_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '创建人',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id`   varchar(32)  NOT NULL DEFAULT '' COMMENT '修改人id',
    `update_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '修改人姓名',
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted`       tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除：0否，1是',
    `version`          int unsigned DEFAULT 0 COMMENT '乐观锁',
    `memo`             varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_apply_code` (`apply_code`),
    KEY `idx_relic_id` (`fk_relic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='修复申请表';

-- ---------------------------------------------------------------------
-- 11. 展览借调表 relic_exhibition_loan
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `relic_exhibition_loan`;
CREATE TABLE `relic_exhibition_loan` (
    `id`                  varchar(32)  NOT NULL COMMENT '借调id，主键，UUID',
    `loan_code`           varchar(50)  NOT NULL COMMENT '借调单号，唯一',
    `fk_relic_id`         varchar(32)  NOT NULL COMMENT '藏品id，关联relic_info表',
    `loan_unit`           varchar(200) NOT NULL COMMENT '借调单位',
    `loan_user_name`      varchar(50)  NOT NULL DEFAULT '' COMMENT '经办人',
    `loan_time`           datetime     DEFAULT NULL COMMENT '借出时间',
    `plan_return_time`    datetime     DEFAULT NULL COMMENT '预计归还时间',
    `actual_return_time`  datetime     DEFAULT NULL COMMENT '实际归还时间',
    `loan_status`         tinyint unsigned NOT NULL DEFAULT 1 COMMENT '借调状态：1待审核，2借出中，3已归还，4审核驳回',
    `audit_opinion`       varchar(500) DEFAULT NULL COMMENT '审核意见',
    `agreement_path`      varchar(200) DEFAULT NULL COMMENT '借调协议路径',
    `create_user_id`      varchar(32)  NOT NULL DEFAULT '' COMMENT '创建人id',
    `create_user_name`    varchar(50)  NOT NULL DEFAULT '' COMMENT '创建人',
    `create_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id`      varchar(32)  NOT NULL DEFAULT '' COMMENT '修改人id',
    `update_user_name`    varchar(50)  NOT NULL DEFAULT '' COMMENT '修改人姓名',
    `update_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted`          tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除：0否，1是',
    `version`             int unsigned DEFAULT 0 COMMENT '乐观锁',
    `memo`                varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_loan_code` (`loan_code`),
    KEY `idx_relic_id` (`fk_relic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='展览借调表';

-- ---------------------------------------------------------------------
-- 12. 注销申请表 relic_cancel_apply
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `relic_cancel_apply`;
CREATE TABLE `relic_cancel_apply` (
    `id`               varchar(32)  NOT NULL COMMENT '注销申请id，主键，UUID',
    `apply_code`       varchar(50)  NOT NULL COMMENT '注销单号，唯一',
    `fk_relic_id`      varchar(32)  NOT NULL COMMENT '藏品id，关联relic_info表',
    `cancel_reason`    varchar(500) NOT NULL COMMENT '注销原因',
    `cancel_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '经办人',
    `apply_status`     tinyint unsigned NOT NULL DEFAULT 0 COMMENT '申请状态：0待审核，1审核通过，2审核驳回',
    `audit_opinion`    varchar(500) DEFAULT NULL COMMENT '审核意见',
    `archive_user_name` varchar(50) DEFAULT NULL COMMENT '归档人',
    `archive_time`     datetime     DEFAULT NULL COMMENT '归档时间',
    `create_user_id`   varchar(32)  NOT NULL DEFAULT '' COMMENT '创建人id',
    `create_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '创建人',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id`   varchar(32)  NOT NULL DEFAULT '' COMMENT '修改人id',
    `update_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '修改人姓名',
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted`       tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除：0否，1是',
    `version`          int unsigned DEFAULT 0 COMMENT '乐观锁',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_apply_code` (`apply_code`),
    KEY `idx_relic_id` (`fk_relic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='注销申请表';

-- ---------------------------------------------------------------------
-- 13. 溯源记录表 relic_trace_record
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `relic_trace_record`;
CREATE TABLE `relic_trace_record` (
    `id`               varchar(32)  NOT NULL COMMENT '溯源记录id，主键，UUID',
    `fk_relic_id`      varchar(32)  NOT NULL COMMENT '藏品id，关联relic_info表',
    `trace_type`       tinyint unsigned NOT NULL COMMENT '溯源类型：1征集，2入藏，3入库，4出库，5移库，6修复，7借出，8归还，9注销',
    `fk_biz_id`        varchar(32)  DEFAULT NULL COMMENT '关联业务id',
    `trace_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '溯源时间',
    `operator_name`    varchar(50)  NOT NULL DEFAULT '' COMMENT '操作人',
    `trace_content`    varchar(1000) NOT NULL COMMENT '溯源内容',
    `create_user_id`   varchar(32)  NOT NULL DEFAULT '' COMMENT '创建人id',
    `create_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '创建人',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted`       tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除：0否，1是',
    PRIMARY KEY (`id`),
    KEY `idx_relic_id` (`fk_relic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='溯源记录表';

-- ---------------------------------------------------------------------
-- 14. 溯源二维码表 relic_qr_code
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `relic_qr_code`;
CREATE TABLE `relic_qr_code` (
    `id`                  varchar(32)  NOT NULL COMMENT '二维码id，主键，UUID',
    `fk_relic_id`         varchar(32)  NOT NULL COMMENT '藏品id，关联relic_info表，一藏品一二维码',
    `qr_content`          varchar(200) NOT NULL COMMENT '二维码内容，唯一',
    `qr_path`             varchar(200) DEFAULT NULL COMMENT '二维码图片路径',
    `generate_user_name`  varchar(50)  NOT NULL DEFAULT '' COMMENT '生成人',
    `generate_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    `scan_count`          int unsigned DEFAULT 0 COMMENT '扫码次数，默认为0',
    `last_scan_time`      datetime     DEFAULT NULL COMMENT '最近扫码时间',
    `create_user_id`      varchar(32)  NOT NULL DEFAULT '' COMMENT '创建人id',
    `create_user_name`    varchar(50)  NOT NULL DEFAULT '' COMMENT '创建人',
    `create_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted`          tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除：0否，1是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_qr_content` (`qr_content`),
    KEY `idx_relic_id` (`fk_relic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='溯源二维码表';

-- ---------------------------------------------------------------------
-- 15. 审核记录表 sys_audit_record
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_audit_record`;
CREATE TABLE `sys_audit_record` (
    `id`               varchar(32)  NOT NULL COMMENT '审核记录id，主键，UUID',
    `biz_type`         tinyint unsigned NOT NULL COMMENT '业务类型：1征集审核，2出入库审核，3修复审核，4借调审核，5注销审核',
    `fk_biz_id`        varchar(32)  NOT NULL COMMENT '业务单据id',
    `biz_code`         varchar(50)  DEFAULT NULL COMMENT '业务单号',
    `audit_user_id`    varchar(32)  NOT NULL DEFAULT '' COMMENT '审核人id',
    `audit_user_name`  varchar(50)  NOT NULL DEFAULT '' COMMENT '审核人',
    `audit_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    `audit_result`     tinyint unsigned NOT NULL COMMENT '审核结果：1通过，2驳回',
    `audit_opinion`    varchar(500) DEFAULT NULL COMMENT '审核意见，驳回时必填',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted`       tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除：0否，1是',
    PRIMARY KEY (`id`),
    KEY `idx_biz_id` (`fk_biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核记录表';

-- ---------------------------------------------------------------------
-- 16. 数据字典表 sys_dict
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict` (
    `id`               varchar(32)  NOT NULL COMMENT '字典id，主键，UUID',
    `dict_type`        varchar(50)  NOT NULL COMMENT '字典类型',
    `dict_code`        varchar(50)  NOT NULL COMMENT '字典项编码，同类型内唯一',
    `dict_label`       varchar(100) NOT NULL COMMENT '字典项名称',
    `sort_no`          int unsigned DEFAULT 0 COMMENT '排序号',
    `is_enabled`       tinyint unsigned NOT NULL DEFAULT 1 COMMENT '是否启用：1是，0否',
    `create_user_id`   varchar(32)  NOT NULL DEFAULT '' COMMENT '创建人id',
    `create_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '创建人',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id`   varchar(32)  NOT NULL DEFAULT '' COMMENT '修改人id',
    `update_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '修改人姓名',
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted`       tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除：0否，1是',
    `remark`           varchar(200) DEFAULT NULL COMMENT '说明',
    PRIMARY KEY (`id`),
    KEY `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';

-- ---------------------------------------------------------------------
-- 17. 系统参数配置表 sys_config
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
    `id`               varchar(32)  NOT NULL COMMENT '配置id，主键，UUID',
    `config_key`       varchar(100) NOT NULL COMMENT '参数键，唯一',
    `config_name`      varchar(100) NOT NULL COMMENT '参数名称',
    `config_value`     varchar(500) NOT NULL COMMENT '参数值',
    `description`      varchar(500) DEFAULT NULL COMMENT '参数说明',
    `create_user_id`   varchar(32)  NOT NULL DEFAULT '' COMMENT '创建人id',
    `create_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '创建人',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id`   varchar(32)  NOT NULL DEFAULT '' COMMENT '修改人id',
    `update_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '修改人姓名',
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted`       tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除：0否，1是',
    `version`          int unsigned DEFAULT 0 COMMENT '乐观锁',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数配置表';

-- ---------------------------------------------------------------------
-- 18. 操作日志表 sys_operate_log
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_operate_log`;
CREATE TABLE `sys_operate_log` (
    `id`                varchar(32)  NOT NULL COMMENT '日志id，主键，UUID',
    `operate_user_id`   varchar(32)  NOT NULL DEFAULT '' COMMENT '操作人id',
    `operate_user_name` varchar(50)  NOT NULL DEFAULT '' COMMENT '操作人',
    `module`            varchar(50)  NOT NULL COMMENT '操作模块',
    `operate_type`      varchar(50)  NOT NULL COMMENT '操作类型',
    `operate_content`   varchar(1000) NOT NULL COMMENT '操作内容',
    `request_param`     text         COMMENT '请求参数',
    `ip`                varchar(50)  DEFAULT NULL COMMENT '操作IP',
    `cost_time`         bigint unsigned DEFAULT NULL COMMENT '耗时，单位毫秒',
    `operate_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
