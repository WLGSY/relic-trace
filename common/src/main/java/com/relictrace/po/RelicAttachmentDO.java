package com.relictrace.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 藏品附件实体（relic_attachment）。
 *
 * @author 李丰
 */
@Data
@TableName("relic_attachment")
public class RelicAttachmentDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 附件 id（主键） */
    @TableId
    private String id;

    /** 藏品 id（关联 relic_info） */
    private String fkRelicId;

    /** 关联业务 id（修复申请、借调申请等） */
    private String fkBizId;

    /** 文件名称 */
    private String fileName;

    /** 文件存储路径 */
    private String filePath;

    /** 附件类型：1 藏品影像 / 2 修复前照片 / 3 修复后照片 / 4 借调协议 / 5 其他 */
    private Integer fileType;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 上传人 id */
    private String uploadUserId;

    /** 上传人 */
    private String uploadUserName;

    /** 上传时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime uploadTime;

    /** 是否删除：0 未删除 / 1 已删除 */
    private Integer isDeleted;
}
