package com.ryan.business.controller;

import com.ryan.business.service.RollbackTestService;
import com.ryan.common.base.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rollback-test")
public class RollbackTestController {

    @Autowired
    private RollbackTestService rollbackTestService;

    @GetMapping("/nested-transaction-error")
    public ResultVO<String> nestedTransactionError(@RequestParam(defaultValue = "false") boolean triggerError) {
        try {
            rollbackTestService.outerTransaction(triggerError);
            ResultVO<String> result = ResultVO.success("操作成功");
            System.out.println("模拟远程接口调用");
            // 调用远程接口
            return result;
        } catch (Exception e) {
            return ResultVO.error(500, e.getMessage());
        }
    }

    @GetMapping("/checked-exception-rollback")
    public ResultVO<String> checkedExceptionRollback() {
        try {
            rollbackTestService.checkedExceptionWithRollback();
            return ResultVO.success("操作成功");
        } catch (Exception e) {
            return ResultVO.error(500, e.getMessage());
        }
    }
}