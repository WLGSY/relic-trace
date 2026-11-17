package com.relictrace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.relictrace.po.RelicInfoDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 藏品信息表（relic_info）数据访问层。
 *
 * <p>继承 MyBatis-Plus BaseMapper，获得通用 CRUD 能力。</p>
 *
 * @author 李丰
 */
@Mapper
public interface RelicInfoMapper extends BaseMapper<RelicInfoDO> {

    /**
     * 查询指定前缀下最大的藏品编号。
     *
     * <p>用原生 SQL 绕过逻辑删除过滤，避免与已删除记录的编号冲突。</p>
     *
     * @param prefix 编号前缀，如 {@code RW-}
     * @return 最大编号；无记录时返回 null
     */
    @Select("SELECT MAX(relic_code) FROM relic_info WHERE relic_code LIKE CONCAT(#{prefix}, '%')")
    String selectMaxRelicCode(@Param("prefix") String prefix);
}
