package com.relictrace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.relictrace.po.RelicExhibitionLoanDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 展览借调表（relic_exhibition_loan）数据访问层。
 *
 * <p>继承 MyBatis-Plus BaseMapper，获得通用 CRUD 能力。</p>
 *
 * @author 李丰
 */
@Mapper
public interface RelicExhibitionLoanMapper extends BaseMapper<RelicExhibitionLoanDO> {

    /**
     * 查询指定前缀下最大的借调单号。
     *
     * <p>用原生 SQL 绕过逻辑删除过滤，避免与已删除记录的单号冲突。</p>
     *
     * @param prefix 单号前缀，如 {@code JD-2026-}
     * @return 最大单号；无记录时返回 null
     */
    @Select("SELECT MAX(loan_code) FROM relic_exhibition_loan WHERE loan_code LIKE CONCAT(#{prefix}, '%')")
    String selectMaxLoanCode(@Param("prefix") String prefix);
}
