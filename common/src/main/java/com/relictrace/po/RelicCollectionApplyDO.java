package com.relictrace.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 藏品征集申请实体（relic_collection_apply）。
 *
 * @author 李丰
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("relic_collection_apply")
public class RelicCollectionApplyDO extends VersionedEntity {

    /** 征集申请 id（主键） */
    @TableId
    private String id;

    /** 征集编号（唯一） */
    private String applyCode;

    /** 征集批次号 */
    private String batchNo;

    /** 藏品名称 */
    private String relicName;

    /** 藏品分类 id（关联 relic_category） */
    private String fkCategoryId;

    /** 年代 */
    private String era;

    /** 质地 */
    private String texture;

    /** 规格（尺寸、重量等） */
    private String specification;

    /** 藏品级别：1 一级 / 2 二级 / 3 三级 / 4 一般 */
    private Integer relicLevel;

    /** 征集来源（捐赠、购买、调拨等） */
    private String collectionSource;

    /** 征集方式（字典项） */
    private String collectionWay;

    /** 藏品描述 */
    private String collectionDesc;

    /** 征集人 */
    private String collector;

    /** 征集时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime collectionTime;

    /** 申请状态：0 草稿 / 1 待审核 / 2 审核通过 / 3 审核驳回 */
    private Integer applyStatus;

    /** 审核意见 */
    private String auditOpinion;
}
