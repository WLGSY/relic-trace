package com.relictrace.controller;

import com.relictrace.common.PageResult;
import com.relictrace.common.Result;
import com.relictrace.po.RelicExhibitionLoanDO;
import com.relictrace.query.PageQuery;
import com.relictrace.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 展览借调控制层。
 *
 * @author 李丰
 */
@RestController
@RequestMapping("/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @GetMapping
    public Result<PageResult<RelicExhibitionLoanDO>> list(PageQuery query) {
        return Result.success(loanService.page(query));
    }

    @PostMapping
    public Result<String> create(@RequestBody RelicExhibitionLoanDO loan) {
        return Result.success(loanService.create(loan), "新增成功");
    }

    @PostMapping("/{id}/handover")
    public Result<Void> handover(@PathVariable String id) {
        loanService.handover(id);
        return Result.success(null, "交接完成");
    }

    @PostMapping("/{id}/return")
    public Result<Void> returnLoan(@PathVariable String id, @RequestBody RelicExhibitionLoanDO loan) {
        loanService.returnLoan(id, loan);
        return Result.success(null, "归还接收成功");
    }
}
