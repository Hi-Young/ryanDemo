# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

这是一个基于Spring Boot 2.2.6的Java学习项目，主要用于演示各种Java技术栈和编程概念。项目使用Maven作为构建工具，包含了多个学习模块。

## 开发环境

- **Java版本**: 1.8
- **Spring Boot版本**: 2.2.6.RELEASE
- **构建工具**: Maven
- **数据库**: MySQL 5.7
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

## 文档导航

详细的知识点文档按模块归档在 `docs/` 目录：

| 目录 | 内容 | 文件 |
|------|------|------|
| `docs/arthas/` | Arthas性能诊断工具 | `README.md` 演示指南, `学习路径.md` 交互式教程, `实践指南.md` 实战场景 |
| `docs/deadlock/` | 数据库死锁实战 | `README.md` 死锁理论+6种场景+监控诊断 |
| `docs/es/` | Elasticsearch搜索 | `README.md` 券搜索Demo, `nested-vs-object.md` nested/object存储原理 |
| `docs/aeon-demo/` | AEON促销引擎 | `aeon-coupon-promo-demo.md` 促销+优惠券计算流程 |
| `docs/notes/` | 技术笔记 | `slf4j-static-binding.md` SLF4J静态绑定机制 |

## 项目架构

### 核心模块结构

**com.ryan** - 主要业务模块
- `business/controller/` - REST控制器
- `business/service/` - 业务服务层 (促销策略、事务回滚演示)
- `business/entity/` - 实体和DTO
- `business/mapper/` - MyBatis映射器
- `business/strategy/` - 策略模式实现 (折扣、满减、新用户)
- `common/aspect/` - AOP切面 (性能监控、日志、缓存、重试)
- `common/exceptionhandler/` - 全局异常处理
- `common/base/` - 基础返回对象
- `deadlock/` - 数据库死锁实战模块 (转账/库存/索引死锁场景 + 监控切面)
- `es/` - Elasticsearch集成 (优惠券搜索、查询模式演示、HTTP客户端)
- `experiment/` - 实验性代码 (深拷贝、QLExpress规则引擎)

**com.geektime** - 学习示例模块
- `algorithm/` - 算法练习
- `basic/generic/` - Java泛型学习 (含5天系统培训课程)
- `basic/spi/` - SPI服务发现机制
- `concurrent/` - 并发编程示例 (基础同步、锁实现、线程池、并发工具)
- `designpattern/` - 设计模式示例 (策略、模板、工厂、代理)
- `framework/spring/` - Spring框架学习 (IoC、Import机制)
- `jvm/` - JVM相关 (类加载器、内存管理、内存泄漏模拟)
- `middleware/mq/` - 消息队列 (RocketMQ生产者/消费者)
- `network/` - 网络编程 (TCP、NIO、Netty)

**com.arthas** - Arthas性能诊断工具示例 (方法监控、性能分析、字段访问、线程分析、死锁检测等14个演示类)

**com.aeon** - AEON促销引擎Demo (优惠券/促销/运费规则引擎)

**com.bruce** - 优惠券和促销演示模块

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
- Elasticsearch - 搜索引擎

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
- `DeadlockMonitorAspect` - 死锁监控，使用`@MonitorDeadlock`注解

### 数据库集成

- MyBatis Plus配置了ID自动生成策略 (`assign_id`)
- 支持驼峰命名转换
- 包含用户相关的实体类和映射器
- Mapper扫描包: `com.**.mapper`
- SQL脚本: `src/main/resources/sql/` (deadlock_tables.sql, promotion_strategy_tables.sql)

## 注意事项

- 主应用类: `com.RyanDemoApplication`
- 默认端口: 18888
- 日志文件位置: `logs/` 目录
- 资源文件支持环境变量替换
- JVM启动参数已配置 (Metaspace: 128m-256m, Heap: 512m-1024m)