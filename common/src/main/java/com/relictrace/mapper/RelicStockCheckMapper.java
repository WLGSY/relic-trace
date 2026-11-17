package com.relictrace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.relictrace.po.RelicStockCheckDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存盘点表（relic_stock_check）数据访问层。
 *
 * <p>继承 MyBatis-Plus BaseMapper，获得通用 CRUD 能力。</p>
 *
 * @author 李丰
 */
@Mapper
public interface RelicStockCheckMapper extends BaseMapper<RelicStockCheckDO> {
}
