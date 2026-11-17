package com.relictrace.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 待审核项展示对象（跨业务类型统一结构）。
 *
 * @author 李丰
 */
@Data
public class AuditItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务类型：1 征集 / 2 出入库 / 3 修复 / 4 借调 / 5 注销 */
    private Integer bizType;

    /** 业务单据 id */
    private String fkBizId;

    /** 业务单号 */
    private String bizCode;

    /** 业务标题（藏品名称 / 事项描述） */
    private String title;

    /** 提交人 */
    private String submitUser;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime submitTime;
}
