package com.relictrace.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 溯源二维码展示对象。
 *
 * <p>供藏品详情、档案抽屉、二维码下载等场景使用。</p>
 *
 * @author 李丰
 */
@Data
public class QrCodeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 二维码记录 id */
    private String id;

    /** 关联藏品 id */
    private String fkRelicId;

    /** 二维码内容（内嵌溯源页面地址与藏品编号） */
    private String content;

    /** 二维码图片访问地址，形如 /uploads/qrcodes/TC-0001.png */
    private String imageUrl;

    /** 生成人 */
    private String generateUserName;

    /** 生成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime generateTime;

    /** 累计扫码次数 */
    private Integer scanCount;

    /** 最近扫码时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime lastScanTime;
}
