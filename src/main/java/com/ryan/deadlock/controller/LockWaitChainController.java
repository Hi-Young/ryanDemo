package com.ryan.deadlock.controller;

import com.ryan.common.base.ResultVO;
import com.ryan.deadlock.service.LockWaitChainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Demo endpoints for "DB lock wait -> long transaction -> cascading slow requests".
 */
@Slf4j
@RestController
@RequestMapping("/deadlock/lock-wait")
public class LockWaitChainController {

    private final LockWaitChainService service;

    public LockWaitChainController(LockWaitChainService service) {
        this.service = service;
    }

    /**
     * Tx-B: Lock order_locks row and hold it for a while.
     */
    @GetMapping("/hold-order")
    public ResultVO<Map<String, Object>> holdOrder(
            @RequestParam(defaultValue = "ORD20241001001") String orderNo,
            @RequestParam(defaultValue = "20000") long holdMs) {

        long start = System.currentTimeMillis();
        service.holdOrderLock(orderNo, holdMs);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("tx", "B");
        m.put("orderNo", orderNo);
        m.put("holdMs", holdMs);
        m.put("elapsedMs", System.currentTimeMillis() - start);
        return ResultVO.success(m);
    }

    /**
     * Tx-A: Lock account first, then lock the same order row (will wait if Tx-B is running).
     */
    @GetMapping("/chain-a")
    public ResultVO<Map<String, Object>> chainA(
            @RequestParam(defaultValue = "ACC001") String accountNo,
            @RequestParam(defaultValue = "ORD20241001001") String orderNo) {

        long start = System.currentTimeMillis();
        service.chainTransactionA(accountNo, orderNo);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("tx", "A");
        m.put("accountNo", accountNo);
        m.put("orderNo", orderNo);
        m.put("elapsedMs", System.currentTimeMillis() - start);
        return ResultVO.success(m);
    }

    /**
     * Tx-C: Contend on the same account row (will wait if Tx-A is running and holding the lock).
     */
    @GetMapping("/touch-account")
    public ResultVO<Map<String, Object>> touchAccount(
            @RequestParam(defaultValue = "ACC001") String accountNo) {

        long start = System.currentTimeMillis();
        service.touchAccount(accountNo);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("tx", "C");
        m.put("accountNo", accountNo);
        m.put("elapsedMs", System.currentTimeMillis() - start);
        return ResultVO.success(m);
    }
}

