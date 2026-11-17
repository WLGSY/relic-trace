package com.relictrace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.relictrace.po.SysUserDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户表（sys_user）数据访问层。
 *
 * <p>继承 MyBatis-Plus BaseMapper，获得通用 CRUD 能力。</p>
 *
 * @author 李丰
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUserDO> {
}
