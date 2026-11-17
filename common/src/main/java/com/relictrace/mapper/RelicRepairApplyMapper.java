package com.relictrace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.relictrace.po.RelicRepairApplyDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 修复申请表（relic_repair_apply）数据访问层。
 *
 * <p>继承 MyBatis-Plus BaseMapper，获得通用 CRUD 能力。</p>
 *
 * @author 李丰
 */
@Mapper
public interface RelicRepairApplyMapper extends BaseMapper<RelicRepairApplyDO> {

    /**
     * 查询指定前缀下最大的修复单号。
     *
     * <p>用原生 SQL 绕过逻辑删除过滤：单号列有唯一索引，
     * 已删除记录仍占用编号，过滤后新单号会与历史记录冲突。</p>
     *
     * @param prefix 单号前缀，如 {@code XF-2026-}
     * @return 最大单号；无记录时返回 null
     */
    @Select("SELECT MAX(apply_code) FROM relic_repair_apply WHERE apply_code LIKE CONCAT(#{prefix}, '%')")
    String selectMaxApplyCode(@Param("prefix") String prefix);
}
