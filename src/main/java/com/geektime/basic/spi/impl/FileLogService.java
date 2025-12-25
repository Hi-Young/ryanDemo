package com.geektime.basic.spi.impl;

import com.geektime.basic.spi.LogService;

/**
 * 文件日志服务 - SPI 服务实现
 *
 * 模拟将日志写入文件（实际只是打印，方便演示）
 */
public class FileLogService implements LogService {

    private final String fileName = "app.log";

    public FileLogService() {
    }

    @Override
    public void log(String message) {
        // 模拟写入文件，实际场景会使用 FileWriter 等
        System.out.println("[File] " + message + " -> 写入到 " + fileName);
    }

    @Override
    public String getName() {
        return "File";
    }
}
