package com.relictrace.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 待审核数量汇总展示对象。
 *
 * @author 李丰
 */
@Data
public class PendingCountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 征集待审核数 */
    private Long collection;

    /** 出入库待审核数 */
    private Long stock;

    /** 修复待审核数 */
    private Long repair;

    /** 借调待审核数 */
    private Long loan;

    /** 注销待审核数 */
    private Long cancel;

    /** 待审核总数 */
    private Long total;
}
