package com.relictrace.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 藏品展示对象。
 *
 * <p>在实体基础上补充分类名称、库房名称、仓位名称、溯源记录、附件等关联信息。</p>
 *
 * @author 李丰
 */
@Data
public class RelicVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 藏品 id */
    private String id;

    /** 藏品编号 */
    private String relicCode;

    /** 藏品名称 */
    private String relicName;

    /** 藏品分类 id */
    private String fkCategoryId;

    /** 藏品分类名称 */
    private String categoryName;

    /** 征集申请 id */
    private String fkCollectionApplyId;

    /** 年代 */
    private String era;

    /** 质地 */
    private String texture;

    /** 规格 */
    private String specification;

    /** 藏品级别 */
    private Integer relicLevel;

    /** 数量 */
    private Integer quantity;

    /** 完残状况 */
    private String integrityStatus;

    /** 所在库房 id */
    private String fkWarehouseId;

    /** 所在库房名称 */
    private String warehouseName;

    /** 所在仓位 id */
    private String fkPositionId;

    /** 所在仓位名称 */
    private String positionName;

    /** 库存状态 */
    private Integer stockStatus;

    /** 溯源二维码内容 */
    private String qrcodeContent;

    /** 溯源二维码完整信息（图片地址、扫码次数、生成时间） */
    private QrCodeVO qrcode;

    /** 是否公开：1 公开 / 0 保密 */
    private Integer isPublic;

    /** 藏品详情描述 */
    private String relicDesc;

    /**
     * 封面影像地址。
     *
     * <p>取该藏品首张“藏品影像”类附件（relic_attachment.file_type = 1）的相对路径，
     * 形如 /uploads/relics/TC-0001.jpg；无影像时为空，前端回退到抽象占位图。</p>
     */
    private String cover;

    /** 扫码次数 */
    private Integer scanCount;

    /** 创建人 */
    private String createUserName;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;

    /** 溯源记录列表（详情接口返回） */
    private List<TraceVO> traceList;

    /** 附件列表（详情接口返回） */
    private List<AttachmentVO> attachmentList;
}
