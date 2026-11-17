package com.relictrace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.relictrace.po.SysAuditRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审核记录表（sys_audit_record）数据访问层。
 *
 * <p>继承 MyBatis-Plus BaseMapper，获得通用 CRUD 能力。</p>
 *
 * @author 李丰
 */
@Mapper
public interface SysAuditRecordMapper extends BaseMapper<SysAuditRecordDO> {
}
