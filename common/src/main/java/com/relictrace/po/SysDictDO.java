package com.relictrace.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 数据字典实体（sys_dict）。
 *
 * @author 李丰
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict")
public class SysDictDO extends BaseEntity {

    /** 字典 id（主键） */
    @TableId
    private String id;

    /** 字典类型（如 collection_way、relic_level） */
    private String dictType;

    /** 字典项编码（同类型内唯一） */
    private String dictCode;

    /** 字典项名称 */
    private String dictLabel;

    /** 排序号 */
    private Integer sortNo;

    /** 是否启用：1 是 / 0 否 */
    private Integer isEnabled;

    /** 说明 */
    private String remark;
}
