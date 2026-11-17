package com.relictrace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.relictrace.po.RelicStockRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 出入库记录表（relic_stock_record）数据访问层。
 *
 * <p>继承 MyBatis-Plus BaseMapper，获得通用 CRUD 能力。</p>
 *
 * @author 李丰
 */
@Mapper
public interface RelicStockRecordMapper extends BaseMapper<RelicStockRecordDO> {

    /**
     * 查询指定前缀下最大的出入库单号。
     *
     * <p>入库 RK-、出库 CK-、移库 YK- 三类前缀各自独立流水，
     * 用原生 SQL 绕过逻辑删除过滤，避免与已删除记录的单号冲突。</p>
     *
     * @param prefix 单号前缀，如 {@code CK-2026-}
     * @return 最大单号；无记录时返回 null
     */
    @Select("SELECT MAX(record_code) FROM relic_stock_record WHERE record_code LIKE CONCAT(#{prefix}, '%')")
    String selectMaxRecordCode(@Param("prefix") String prefix);
}
