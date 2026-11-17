package com.relictrace.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体公共基类。
 *
 * <p>封装各业务表通用的审计字段：创建人、创建时间、修改人、修改时间、逻辑删除、乐观锁、备注。</p>
 *
 * @author 李丰
 */
@Data
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 创建人 id */
    @TableField(fill = FieldFill.INSERT)
    private String createUserId;

    /** 创建人姓名 */
    @TableField(fill = FieldFill.INSERT)
    private String createUserName;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;

    /** 修改人 id */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;

    /** 修改人姓名 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateUserName;

    /** 修改时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;

    /** 是否删除：0 未删除 / 1 已删除 */
    @TableLogic
    private Integer isDeleted;
}
