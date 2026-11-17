package com.relictrace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.relictrace.po.SysOperateLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志表（sys_operate_log）数据访问层。
 *
 * <p>继承 MyBatis-Plus BaseMapper，获得通用 CRUD 能力。</p>
 *
 * @author 李丰
 */
@Mapper
public interface SysOperateLogMapper extends BaseMapper<SysOperateLogDO> {
}
