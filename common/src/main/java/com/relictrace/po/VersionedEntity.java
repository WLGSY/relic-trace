package com.relictrace.po;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 带乐观锁与备注的实体基类。
 *
 * <p>在 {@link BaseEntity} 基础上追加 {@code version}（乐观锁版本号）与 {@code memo}（备注），
 * 仅用于数据库表实际存在这两列的实体（如 relic_info、sys_user 等）。</p>
 *
 * @author 李丰
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class VersionedEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 乐观锁版本号 */
    private Integer version;

    /** 备注 */
    private String memo;
}
