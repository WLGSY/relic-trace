package com.relictrace.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 溯源记录展示对象。
 *
 * @author 李丰
 */
@Data
public class TraceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 溯源记录 id */
    private String id;

    /** 藏品 id */
    private String fkRelicId;

    /** 溯源类型：1 征集 / 2 入藏 / 3 入库 / 4 出库 / 5 移库 / 6 修复 / 7 借出 / 8 归还 / 9 注销 */
    private Integer traceType;

    /** 关联业务 id */
    private String fkBizId;

    /** 溯源时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime traceTime;

    /** 操作人 */
    private String operatorName;

    /** 溯源内容 */
    private String traceContent;
}
