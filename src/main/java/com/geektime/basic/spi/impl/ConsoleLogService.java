package com.geektime.basic.spi.impl;

import com.geektime.basic.spi.LogService;

/**
 * 控制台日志服务 - SPI 服务实现
 *
 * 这是 SPI 机制的第二步：提供服务实现
 *
 * 注意：SPI 实现类必须有无参构造函数，因为 ServiceLoader 通过反射创建实例
 */
public class ConsoleLogService implements LogService {

    // 必须有无参构造函数（这里是默认的）
    public ConsoleLogService() {
    }

    @Override
    public void log(String message) {
        System.out.println("[Console] " + message);
    }

    @Override
    public String getName() {
        return "Console";
    }
}
