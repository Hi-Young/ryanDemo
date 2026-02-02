package com.ryan.deadlock.controller;

import com.ryan.common.base.ResultVO;
import com.ryan.deadlock.service.TransactionLeakDemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Endpoints to reproduce: manual transaction forgot commit/close -> lock held + pool exhaustion.
 */
@RestController
@RequestMapping("/deadlock/tx-leak")
public class TransactionLeakDemoController {

    private final TransactionLeakDemoService service;

    public TransactionLeakDemoController(TransactionLeakDemoService service) {
        this.service = service;
    }

    /**
     * Leak a connection (autoCommit=false, no close). Mainly used to exhaust the pool quickly.
     */
    @GetMapping("/leak-conn")
    public ResultVO<Map<String, Object>> leakConn() {
        return ResultVO.success(service.leakConnectionOnly());
    }

    /**
     * Leak a transaction that holds an InnoDB row lock on table `account`.
     *
     * <p>After calling this, try calling:
     * <ul>
     *   <li>/deadlock/lock-wait/touch-account?accountNo=ACC001 (will block)</li>
     * </ul>
     */
    @GetMapping("/leak-account-lock")
    public ResultVO<Map<String, Object>> leakAccountLock(
            @RequestParam(defaultValue = "ACC001") String accountNo,
            @RequestParam(defaultValue = "0.01") BigDecimal delta) {
        return ResultVO.success(service.leakAccountRowLock(accountNo, delta));
    }

    @GetMapping("/list")
    public ResultVO<List<Map<String, Object>>> list() {
        return ResultVO.success(service.listLeaks());
    }

    @GetMapping("/commit")
    public ResultVO<Map<String, Object>> commit(@RequestParam String leakId) {
        return ResultVO.success(service.commitAndClose(leakId));
    }

    @GetMapping("/rollback")
    public ResultVO<Map<String, Object>> rollback(@RequestParam String leakId) {
        return ResultVO.success(service.rollbackAndClose(leakId));
    }

    /**
     * Emergency stop: rollback and close all leaked transactions/connections.
     */
    @GetMapping("/close-all")
    public ResultVO<Map<String, Object>> closeAll() {
        return ResultVO.success(service.closeAll());
    }
}

