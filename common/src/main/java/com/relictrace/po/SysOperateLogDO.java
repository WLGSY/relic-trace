package com.relictrace.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体（sys_operate_log）。
 *
 * @author 李丰
 */
@Data
@TableName("sys_operate_log")
public class SysOperateLogDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日志 id（主键） */
    @TableId
    private String id;

    /** 操作人 id */
    private String operateUserId;

    /** 操作人 */
    private String operateUserName;

    /** 操作模块（如藏品征集、出入库管理） */
    private String module;

    /** 操作类型（如登录、新增、修改、删除、审核） */
    private String operateType;

    /** 操作内容 */
    private String operateContent;

    /** 请求参数 */
    private String requestParam;

    /** 操作 IP */
    private String ip;

    /** 耗时（毫秒） */
    private Long costTime;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime operateTime;
}
