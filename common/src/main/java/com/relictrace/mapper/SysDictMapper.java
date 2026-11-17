package com.relictrace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.relictrace.po.SysDictDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据字典表（sys_dict）数据访问层。
 *
 * <p>继承 MyBatis-Plus BaseMapper，获得通用 CRUD 能力。</p>
 *
 * @author 李丰
 */
@Mapper
public interface SysDictMapper extends BaseMapper<SysDictDO> {
}
