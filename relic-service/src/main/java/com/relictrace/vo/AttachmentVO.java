package com.relictrace.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 附件展示对象。
 *
 * @author 李丰
 */
@Data
public class AttachmentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 附件 id */
    private String id;

    /** 藏品 id */
    private String fkRelicId;

    /** 关联业务 id */
    private String fkBizId;

    /** 文件名称 */
    private String fileName;

    /** 文件存储路径 */
    private String filePath;

    /** 附件类型 */
    private Integer fileType;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 上传人 */
    private String uploadUserName;

    /** 上传时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime uploadTime;
}
