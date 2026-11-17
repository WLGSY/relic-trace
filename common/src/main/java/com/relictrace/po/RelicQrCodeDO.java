package com.relictrace.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 溯源二维码实体（relic_qr_code）。
 *
 * @author 李丰
 */
@Data
@TableName("relic_qr_code")
public class RelicQrCodeDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 二维码 id（主键） */
    @TableId
    private String id;

    /** 藏品 id（关联 relic_info，一藏品一二维码） */
    private String fkRelicId;

    /** 二维码内容（内嵌藏品编号与溯源地址） */
    private String qrContent;

    /** 二维码图片路径 */
    private String qrPath;

    /** 生成人 */
    private String generateUserName;

    /** 生成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime generateTime;

    /** 扫码次数（默认为 0） */
    private Integer scanCount;

    /** 最近扫码时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime lastScanTime;

    /** 创建人 id */
    private String createUserId;

    /** 创建人 */
    private String createUserName;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;

    /** 是否删除：0 未删除 / 1 已删除 */
    private Integer isDeleted;
}
