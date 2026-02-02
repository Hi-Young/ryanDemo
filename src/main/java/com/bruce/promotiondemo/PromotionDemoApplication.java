package com.bruce.promotiondemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 促销计算新旧架构对比 Demo 应用
 *
 * 演示三个核心差异：
 * 1. 水平叠加 vs 垂直叠加（parallelCalculate 参数）
 * 2. 贪心选择 vs 穷举最优（规则选取策略）
 * 3. 回滚机制的 bug（旧架构的核心问题）
 */
@SpringBootApplication
public class PromotionDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PromotionDemoApplication.class, args);
    }
}
