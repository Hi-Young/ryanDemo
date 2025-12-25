package com.geektime.basic.spi;

/**
 * 日志服务接口 - SPI 服务定义
 *
 * 这是 SPI 机制的第一步：定义服务接口
 * 接口定义者只关心"做什么"，不关心"怎么做"
 */
public interface LogService {

    /**
     * 记录日志
     * @param message 日志内容
     */
    void log(String message);

    /**
     * 获取日志服务名称
     * @return 服务名称
     */
    String getName();
}
