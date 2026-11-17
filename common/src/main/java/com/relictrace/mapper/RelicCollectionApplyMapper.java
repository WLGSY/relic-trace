package com.relictrace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.relictrace.po.RelicCollectionApplyDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 藏品征集申请表（relic_collection_apply）数据访问层。
 *
 * <p>继承 MyBatis-Plus BaseMapper，获得通用 CRUD 能力。</p>
 *
 * @author 李丰
 */
@Mapper
public interface RelicCollectionApplyMapper extends BaseMapper<RelicCollectionApplyDO> {

    /**
     * 查询指定前缀下最大的征集编号。
     *
     * <p>使用原生 SQL 而非 QueryWrapper，是为了绕过 MyBatis-Plus 的逻辑删除过滤：
     * 编号列有唯一索引，已逻辑删除的记录仍占用编号，若过滤掉会导致新编号与历史记录冲突。</p>
     *
     * @param prefix 编号前缀，如 {@code ZJ-2026-}
     * @return 最大编号；无记录时返回 null
     */
    @Select("SELECT MAX(apply_code) FROM relic_collection_apply WHERE apply_code LIKE CONCAT(#{prefix}, '%')")
    String selectMaxApplyCode(@Param("prefix") String prefix);
}
