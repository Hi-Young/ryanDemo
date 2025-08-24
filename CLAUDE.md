# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

这是一个基于Spring Boot 2.2.6的Java学习项目，主要用于演示各种Java技术栈和编程概念。项目使用Maven作为构建工具，包含了多个学习模块。

## 开发环境

- **Java版本**: 1.8
- **Spring Boot版本**: 2.2.6.RELEASE
- **构建工具**: Maven
- **数据库**: MySQL
- **服务端口**: 18888

## 常用命令

### 构建和运行
```bash
# 编译项目
mvn compile

# 运行应用 (使用JVM参数)
mvn spring-boot:run
```

### 环境配置
项目支持多环境配置：
- `dev` (默认): 开发环境
- `test`: 测试环境  
- `prod`: 生产环境

使用 `-Pdev`, `-Ptest`, `-Pprod` 切换环境

## 项目架构

### 核心模块结构

**com.ryan** - 主要业务模块
- `business/controller/` - REST控制器
- `business/service/` - 业务服务层
- `business/entity/user/` - 用户实体和DTO
- `business/mapper/` - MyBatis映射器
- `common/aspect/` - AOP切面 (性能监控、日志、缓存、重试)
- `common/exceptionhandler/` - 全局异常处理
- `common/base/` - 基础返回对象

**com.geektime** - 学习示例模块
- `algorithm/` - 算法练习
- `basic/generic/` - Java泛型学习
- `concurrent/` - 并发编程示例 (锁、线程池、工具类)
- `designpattern/` - 设计模式示例 (策略、模板、工厂、代理)
- `framework/spring/` - Spring框架学习
- `jvm/` - JVM相关 (类加载器、内存管理)
- `middleware/mq/` - 消息队列 (RocketMQ)
- `network/` - 网络编程 (NIO、Netty)

**com.arthas** - Arthas性能诊断工具示例

### 技术栈

**核心框架**
- Spring Boot 2.2.6 + Spring Web
- MyBatis Plus 3.4.1 (ORM)
- MySQL + Redis

**工具库**
- Lombok - 减少样板代码
- MapStruct 1.4.1 - 对象映射
- FastJSON 1.2.83 - JSON处理
- Swagger2 2.9.2 - API文档
- AspectJ - AOP支持

**中间件集成**
- RocketMQ 4.3.0 - 消息队列
- Netty 4.1.8 - 网络编程
- XXL-Job 2.3.0 - 分布式任务调度
- QLExpress 3.3.1 - 规则引擎

**其他特性**
- JWT认证、POI Excel处理
- 腾讯云COS集成、Jasypt配置加密
- 动态数据源支持

### AOP切面功能

项目包含完整的AOP切面系统：
- `PerformanceAspect` - 性能监控，使用`@MonitorPerformance`注解
- `CacheAspect` - 缓存管理，使用`@Cacheable`注解  
- `RetryAspect` - 重试机制，使用`@Retry`注解
- `OperationLogAspect` - 操作日志记录
- `LoggingAspect` - 方法调用日志

### 数据库集成

- MyBatis Plus配置了ID自动生成策略 (`assign_id`)
- 支持驼峰命名转换
- 包含用户相关的实体类和映射器
- Mapper扫描包: `com.**.mapper`

## 注意事项

- 主应用类: `com.RyanDemoApplication`
- 默认端口: 18888
- 日志文件位置: `logs/` 目录
- 资源文件支持环境变量替换
- JVM启动参数已配置 (Metaspace: 128m-256m, Heap: 512m-1024m)