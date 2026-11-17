package com.relictrace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.relictrace.po.RelicStoragePositionDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 仓位信息表（relic_storage_position）数据访问层。
 *
 * <p>继承 MyBatis-Plus BaseMapper，获得通用 CRUD 能力。</p>
 *
 * @author 李丰
 */
@Mapper
public interface RelicStoragePositionMapper extends BaseMapper<RelicStoragePositionDO> {
}
