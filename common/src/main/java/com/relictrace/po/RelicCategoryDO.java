package com.relictrace.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 藏品分类实体（relic_category）。
 *
 * @author 李丰
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("relic_category")
public class RelicCategoryDO extends VersionedEntity {

    /** 分类 id（主键） */
    @TableId
    private String id;

    /** 分类编码（唯一） */
    private String categoryCode;

    /** 分类名称 */
    private String categoryName;

    /** 父级分类 id（顶级分类为空） */
    private String fkParentId;

    /** 分类层级：1 一级 / 2 二级 / 3 三级 */
    private Integer categoryLevel;

    /** 是否叶子节点：1 是 / 0 否 */
    private Integer isLeaf;

    /** 排序号（数值越小越靠前） */
    private Integer sortNo;
}
