package com.relictrace.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统用户实体（sys_user）。
 *
 * <p>字段命名遵循「数据库命名规则」：表名小写下划线，实体字段驼峰。</p>
 *
 * @author 李丰
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUserDO extends VersionedEntity {

    /** 用户 id（主键，UUID/雪花） */
    @TableId
    private String id;

    /** 登录账号（唯一） */
    private String loginName;

    /** 登录密码（BCrypt 加密存储） */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String loginPassword;

    /** 真实姓名 */
    private String realName;

    /** 角色类型：1 系统管理员 / 2 录入员 / 3 审核员 */
    private Integer roleType;

    /** 联系电话 */
    private String phone;

    /** 头像地址 */
    private String avatar;

    /** 是否启用：1 是 / 0 否 */
    private Integer isEnabled;

    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime lastLoginTime;
}
