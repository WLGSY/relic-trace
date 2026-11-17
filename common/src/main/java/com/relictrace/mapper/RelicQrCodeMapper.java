package com.relictrace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.relictrace.po.RelicQrCodeDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 溯源二维码表（relic_qr_code）数据访问层。
 *
 * <p>继承 MyBatis-Plus BaseMapper，获得通用 CRUD 能力。</p>
 *
 * @author 李丰
 */
@Mapper
public interface RelicQrCodeMapper extends BaseMapper<RelicQrCodeDO> {
}
