package com.relictrace.service;

import com.relictrace.common.PageResult;
import com.relictrace.po.RelicExhibitionLoanDO;
import com.relictrace.query.PageQuery;

/**
 * 展览借调服务接口。
 *
 * @author 李丰
 */
public interface LoanService {

    /** 分页查询借调记录 */
    PageResult<RelicExhibitionLoanDO> page(PageQuery query);

    /** 新增借调 */
    String create(RelicExhibitionLoanDO loan);

    /** 交接借出（待审核 -> 借出中） */
    void handover(String id);

    /** 归还接收 */
    void returnLoan(String id, RelicExhibitionLoanDO loan);
}
