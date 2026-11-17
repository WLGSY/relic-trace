package com.relictrace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.relictrace.po.RelicCancelApplyDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 注销申请表（relic_cancel_apply）数据访问层。
 *
 * <p>继承 MyBatis-Plus BaseMapper，获得通用 CRUD 能力。</p>
 *
 * @author 李丰
 */
@Mapper
public interface RelicCancelApplyMapper extends BaseMapper<RelicCancelApplyDO> {

    /**
     * 查询指定前缀下最大的注销单号。
     *
     * <p>用原生 SQL 绕过逻辑删除过滤，避免与已删除记录的单号冲突。</p>
     *
     * @param prefix 单号前缀，如 {@code ZX-2026-}
     * @return 最大单号；无记录时返回 null
     */
    @Select("SELECT MAX(apply_code) FROM relic_cancel_apply WHERE apply_code LIKE CONCAT(#{prefix}, '%')")
    String selectMaxApplyCode(@Param("prefix") String prefix);
}
