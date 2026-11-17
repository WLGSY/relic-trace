package com.relictrace.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统参数配置实体（sys_config）。
 *
 * @author 李丰
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class SysConfigDO extends BaseEntity {

    /** 配置 id（主键） */
    @TableId
    private String id;

    /** 参数键（唯一，如 qrcode.rule、relic_code.prefix） */
    private String configKey;

    /** 参数名称 */
    private String configName;

    /** 参数值 */
    private String configValue;

    /** 参数说明 */
    private String description;

    /** 乐观锁版本号 */
    private Integer version;
}
