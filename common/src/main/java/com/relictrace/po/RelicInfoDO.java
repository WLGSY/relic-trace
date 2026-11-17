package com.relictrace.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 藏品信息实体（relic_info）。
 *
 * @author 李丰
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("relic_info")
public class RelicInfoDO extends VersionedEntity {

    /** 藏品 id（主键） */
    @TableId
    private String id;

    /** 藏品编号（编目建档时生成，全局唯一） */
    private String relicCode;

    /** 藏品名称 */
    private String relicName;

    /** 藏品分类 id（关联 relic_category） */
    private String fkCategoryId;

    /** 征集申请 id（关联 relic_collection_apply） */
    private String fkCollectionApplyId;

    /** 年代 */
    private String era;

    /** 质地 */
    private String texture;

    /** 规格 */
    private String specification;

    /** 藏品级别：1 一级 / 2 二级 / 3 三级 / 4 一般 */
    private Integer relicLevel;

    /** 数量（默认为 1） */
    private Integer quantity;

    /** 完残状况 */
    private String integrityStatus;

    /** 所在库房 id（关联 relic_warehouse） */
    private String fkWarehouseId;

    /** 所在仓位 id（关联 relic_storage_position） */
    private String fkPositionId;

    /** 库存状态：1 在库 / 2 出库 / 3 借出 / 4 修复中 / 5 已注销 */
    private Integer stockStatus;

    /** 溯源二维码内容（内嵌藏品编号） */
    private String qrcodeContent;

    /** 是否公开：1 公开 / 0 保密 */
    private Integer isPublic;

    /** 藏品详情描述 */
    private String relicDesc;
}
