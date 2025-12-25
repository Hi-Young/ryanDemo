package com.geektime.basic.spi;

import java.util.ServiceLoader;

/**
 * Java SPI 机制演示
 *
 * SPI（Service Provider Interface）是 Java 提供的一种服务发现机制。
 *
 * 核心组件：
 * 1. 服务接口（LogService）         - 定义服务规范
 * 2. 服务实现（ConsoleLogService）  - 提供具体实现
 * 3. 配置文件（META-INF/services/） - 声明实现类
 * 4. ServiceLoader                  - 加载服务实现
 *
 * 运行方式：
 * 在 IDEA 中直接运行 main 方法
 */
public class SPIDemo {

    public static void main(String[] args) {
        System.out.println("========== Java SPI Demo ==========\n");

        // ==================== 1. 使用 ServiceLoader 加载所有实现 ====================
        // 这是 SPI 的核心 API
        // 它会读取 META-INF/services/com.geektime.basic.spi.LogService 文件
        // 并通过反射创建文件中声明的所有实现类实例
        ServiceLoader<LogService> loader = ServiceLoader.load(LogService.class);

        // ==================== 2. 展示发现的所有服务实现 ====================
        System.out.println("发现的日志服务实现:");
        int index = 1;
        for (LogService service : loader) {
            System.out.println("  " + index + ". " + service.getName());
            index++;
        }

        // ==================== 3. 使用所有实现记录日志 ====================
        System.out.println("\n使用所有实现记录日志:");
        // 注意：ServiceLoader 是懒加载的，每次迭代都会重新创建实例
        // 如果需要复用，可以先转成 List
        for (LogService service : loader) {
            service.log("Hello, SPI!");
        }

        // ==================== 4. 获取指定的服务实现 ====================
        System.out.println("\n只使用 Console 实现:");
        LogService consoleService = getServiceByName(loader, "Console");
        if (consoleService != null) {
            consoleService.log("这条日志只输出到控制台");
        }

        // ==================== 5. SPI 原理说明 ====================
        System.out.println("\n========== SPI 加载原理 ==========");
        System.out.println("配置文件位置: META-INF/services/" + LogService.class.getName());
        System.out.println("配置文件内容: 实现类的全限定名，每行一个");
        System.out.println("加载过程:");
        System.out.println("  1. ServiceLoader.load(LogService.class)");
        System.out.println("  2. 查找 classpath 下的配置文件");
        System.out.println("  3. 读取文件，获取所有实现类名");
        System.out.println("  4. 通过 Class.forName() 加载类");
        System.out.println("  5. 通过 newInstance() 创建实例（需要无参构造函数）");

        // ==================== 6. 真实应用场景 ====================
        System.out.println("\n========== 真实应用场景 ==========");
        System.out.println("1. JDBC 驱动加载 - DriverManager 自动发现数据库驱动");
        System.out.println("2. SLF4J 日志框架 - 自动发现日志实现（Logback/Log4j）");
        System.out.println("3. Dubbo 扩展点 - 通过 SPI 实现可插拔的组件");
        System.out.println("4. Spring Boot - 自动配置（类似 SPI 的思想）");
    }

    /**
     * 根据名称获取指定的服务实现
     */
    private static LogService getServiceByName(ServiceLoader<LogService> loader, String name) {
        for (LogService service : loader) {
            if (service.getName().equals(name)) {
                return service;
            }
        }
        return null;
    }
}
