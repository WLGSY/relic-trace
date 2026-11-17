package com.relictrace.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息展示对象（登录返回 / 个人资料）。
 *
 * @author 李丰
 */
@Data
public class UserInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户 id */
    private String id;

    /** 登录账号 */
    private String loginName;

    /** 真实姓名 */
    private String realName;

    /** 角色类型：1 系统管理员 / 2 录入员 / 3 审核员 */
    private Integer roleType;

    /** 联系电话 */
    private String phone;

    /** 头像地址 */
    private String avatar;

    /** 是否启用 */
    private Integer isEnabled;
}
