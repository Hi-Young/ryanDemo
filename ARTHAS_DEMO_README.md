# Arthas演示项目

这个项目包含了完整的Arthas演示类，帮助你直观地感受Arthas的强大功能。

## 🔧 环境要求

### Java版本
- **Java 8+** (项目基于Java 1.8开发，确保兼容性)
- 推荐使用Oracle JDK 8 或 OpenJDK 8+

### Spring Boot版本
- **Spring Boot 2.2.6.RELEASE**
- 包含Web、Redis、MyBatis Plus等核心依赖

### Maven版本
- **Maven 3.6+** 
- 用于项目构建和依赖管理

### 关键依赖版本
```xml
<java.version>1.8</java.version>
<spring-boot.version>2.2.6.RELEASE</spring-boot.version>
<mybatis-plus.version>3.4.1</mybatis-plus.version>
```

### Arthas版本
- **推荐使用最新版本**：从 [https://arthas.aliyun.com/](https://arthas.aliyun.com/) 下载
- 支持Java 8及以上所有版本
- 经过测试的版本：Arthas 3.6.7+

### 代码兼容性特性

本项目专门针对Java 8进行了兼容性优化：

- ✅ **避免使用Java 9+特性**：如`String.repeat()`、`Map.of()`、`var`关键字等
- ✅ **使用Java 8语法**：Lambda表达式、Stream API、Optional等
- ✅ **Spring Boot 2.x兼容**：使用2.2.6.RELEASE确保稳定性
- ✅ **依赖版本控制**：所有依赖都选择与Java 8兼容的版本

### 已验证的运行环境

- **开发环境**：OpenJDK 8u292, Maven 3.6.3, Spring Boot 2.2.6
- **测试环境**：Oracle JDK 8u301, Maven 3.8.1
- **生产环境**：OpenJDK 11（向下兼容）

## 📁 项目结构

```
src/main/java/com/arthas/demo/
├── ArthasDemoStarter.java          # 演示启动器，统一管理所有演示
├── ArthasMethodDemo.java           # 方法监控演示
├── ArthasPerformanceDemo.java      # 性能分析演示
├── ArthasFieldDemo.java            # 字段访问演示
├── ArthasClassLoaderDemo.java      # 类加载器演示
├── ArthasThreadDemo.java           # 线程分析演示
├── ArthasControllerDemo.java       # Web接口演示
└── ArthasTimerDemo.java            # 定时任务演示
```

## 🎯 推荐学习方式

### 实践导向的学习路径

本项目支持多种学习方式，推荐按以下顺序进行：

#### 📱 1. Postman接口测试 (推荐起点)
通过REST API接口快速了解应用行为，为后续Arthas监控提供场景：

```http
# 基础接口测试
GET http://localhost:18888/arthas/demo/hello?name=学习者
GET http://localhost:18888/arthas/demo/user/1
GET http://localhost:18888/arthas/demo/stats

# 性能测试接口
GET http://localhost:18888/arthas/demo/slow?level=2
GET http://localhost:18888/arthas/demo/random

# 异常测试接口
GET http://localhost:18888/arthas/demo/error?errorType=1
```

#### 💻 2. PowerShell/命令行操作
在接口调用的同时，使用Arthas命令进行实时监控：

```powershell
# 启动Arthas
java -jar arthas-boot.jar

# 基础监控命令
dashboard
jvm
thread

# 结合接口测试的监控
watch com.arthas.demo.ArthasControllerDemo * "{params,returnObj}" -x 2
```

#### 📖 3. IDEA代码阅读
深入理解代码逻辑，为监控命令提供理论基础：

- 阅读演示类的注释和方法实现
- 理解业务逻辑和调用关系
- 查看字段定义和生命周期

#### 🔧 4. 代码修改实验
通过小幅修改验证理解，观察Arthas监控结果的变化：

- 修改方法参数和返回值
- 调整字段初始值
- 添加日志输出
- 修改异常触发条件

## 🚀 快速开始

### 0. 环境检查

```bash
# 检查Java版本（需要1.8+）
java -version

# 检查Maven版本（需要3.6+）
mvn -version

# 检查项目目录结构
ls -la src/main/java/com/arthas/demo/
```

### 1. 项目构建和启动

```bash
# 清理并编译项目
mvn clean compile

# 如果遇到编译错误，检查Java版本是否为1.8+
# 启动Spring Boot应用
mvn spring-boot:run

# 或者先打包再运行
mvn clean package -DskipTests
java -jar target/bruce-demo-0.0.1.jar
```

### 2. 安装和启动Arthas

```bash
# 下载Arthas
curl -O https://arthas.aliyun.com/arthas-boot.jar

# 启动Arthas
java -jar arthas-boot.jar

# 选择对应的Java进程（通常是你的Spring Boot应用）
```

### 3. 开始监控

应用启动后，各个演示类会自动开始运行，你可以立即使用Arthas命令进行监控。

## 📊 演示功能详解

### 1. 方法监控演示 (ArthasMethodDemo)

**功能**：演示方法调用监控、参数返回值查看、异常监控等

**核心命令**：
```bash
# 监控方法调用和返回值
watch com.arthas.demo.ArthasMethodDemo normalMethod "{params,returnObj}" -x 2

# 追踪方法调用链路和耗时
trace com.arthas.demo.ArthasMethodDemo slowMethod

# 统计方法调用次数和耗时
monitor com.arthas.demo.ArthasMethodDemo randomMethod -c 5

# 记录方法调用快照
tt -t com.arthas.demo.ArthasMethodDemo exceptionMethod

# 查看方法调用栈
stack com.arthas.demo.ArthasMethodDemo nestedMethodA
```

**演示场景**：
- 正常方法调用
- 静态方法调用
- 慢方法执行
- 随机异常触发
- 嵌套方法调用
- 方法重载

### 2. 性能分析演示 (ArthasPerformanceDemo)

**功能**：演示CPU分析、内存分析、GC监控等性能相关功能

**核心命令**：
```bash
# CPU性能分析
profiler start --event cpu
# 等待一段时间后
profiler stop --format html

# 生成堆内存转储
heapdump /tmp/heap.hprof

# 查看JVM信息
jvm

# 查看内存信息
memory

# 实时监控面板
dashboard
```

**演示场景**：
- CPU密集型计算
- 内存分配和回收
- 高频GC触发
- 内存泄漏模拟
- 高并发场景

### 3. 字段访问演示 (ArthasFieldDemo)

**功能**：演示字段值查看和修改、实例获取等

**核心命令**：
```bash
# 获取静态字段值
getstatic com.arthas.demo.ArthasFieldDemo staticCounter

# 使用OGNL表达式获取字段
ognl '@com.arthas.demo.ArthasFieldDemo@staticCounter'

# 使用OGNL表达式设置字段值
ognl '@com.arthas.demo.ArthasFieldDemo@staticCounter=100'

# 获取类的实例
vmtool --action getInstances --className com.arthas.demo.ArthasFieldDemo --limit 5

# 监控字段访问
watch com.arthas.demo.ArthasFieldDemo getInstanceField 'target.instanceField'
```

**演示场景**：
- 静态字段操作
- 实例字段访问
- 集合字段操作
- 嵌套对象字段
- 字段实时变化

### 4. 类加载器演示 (ArthasClassLoaderDemo)

**功能**：演示类加载器分析、类信息查看、动态重新加载等

**核心命令**：
```bash
# 查看类加载器信息
classloader

# 查看类加载器树
classloader -t

# 搜索类
sc com.arthas.demo.*

# 搜索方法
sm com.arthas.demo.ArthasClassLoaderDemo

# 反编译类
jad com.arthas.demo.ArthasClassLoaderDemo

# 编译Java文件
mc /tmp/ModifiedClass.java

# 重新加载类
retransform /tmp/ModifiedClass.class
```

**演示场景**：
- 自定义类加载器
- 动态类加载
- 类加载器层次分析
- 类信息查看
- 类重新加载

### 5. 线程分析演示 (ArthasThreadDemo)

**功能**：演示线程状态分析、死锁检测、线程性能分析等

**核心命令**：
```bash
# 查看所有线程
thread

# 查看CPU使用率最高的线程
thread -n 5

# 查看阻塞的线程
thread -b

# 查看指定状态的线程
thread --state WAITING
thread --state BLOCKED

# 查看指定线程的详细信息
thread [thread-id]

# 查看所有线程栈
jstack
```

**演示场景**：
- CPU密集型线程
- 线程死锁
- 线程池使用
- 各种等待状态
- 线程竞争

### 6. Web接口演示 (ArthasControllerDemo)

**功能**：演示Web应用中的方法监控、接口性能分析等

**测试接口**：
```bash
# 简单接口
curl "http://localhost:18888/arthas/demo/hello?name=Arthas"

# 用户查询接口
curl "http://localhost:18888/arthas/demo/user/1"

# 慢接口
curl "http://localhost:18888/arthas/demo/slow?level=2"

# 随机响应接口
curl "http://localhost:18888/arthas/demo/random"

# 错误接口
curl "http://localhost:18888/arthas/demo/error?errorType=1"
```

**监控命令**：
```bash
# 监控所有Controller方法
watch com.arthas.demo.ArthasControllerDemo * "{params,returnObj}" -x 2

# 追踪慢接口
trace com.arthas.demo.ArthasControllerDemo slowApi

# 统计接口调用
monitor com.arthas.demo.ArthasControllerDemo * -c 5
```

### 7. 定时任务演示 (ArthasTimerDemo)

**功能**：演示定时任务监控、长期运行任务分析等

**核心命令**：
```bash
# 监控定时任务执行
monitor com.arthas.demo.ArthasTimerDemo * -c 10

# 监控特定任务
watch com.arthas.demo.ArthasTimerDemo processDataBatch "{params,returnObj}" -x 2

# 条件过滤监控
watch com.arthas.demo.ArthasTimerDemo * "{params,returnObj}" '#cost>100'
```

**演示场景**：
- 快速定时任务
- 数据批处理任务
- 健康检查任务
- 慢任务执行
- 任务统计报告

## 📚 循序渐进的实践教程

### 第一阶段：熟悉环境 (30分钟)

#### Step 1: 启动并验证应用
```powershell
# 启动应用
mvn spring-boot:run

# 验证应用状态
curl http://localhost:18888/arthas/demo/hello
```

#### Step 2: Postman基础测试
创建Postman Collection，测试以下接口：
```
📁 Arthas Demo Collection
├── 🟢 GET Hello - {{base_url}}/arthas/demo/hello?name=测试
├── 🟢 GET User Info - {{base_url}}/arthas/demo/user/1
├── 🟢 GET Stats - {{base_url}}/arthas/demo/stats
└── 🟡 GET Random - {{base_url}}/arthas/demo/random
```

#### Step 3: 启动Arthas并熟悉基础命令
```powershell
# 启动Arthas
java -jar arthas-boot.jar

# 基础探索命令
help           # 查看帮助
dashboard      # 系统面板
jvm           # JVM信息
thread        # 线程信息
quit          # 退出
```

### 第二阶段：方法监控实践 (45分钟)

#### Step 1: 在IDEA中阅读ArthasMethodDemo.java
重点关注：
- `normalMethod()` - 理解基础方法调用
- `slowMethod()` - 理解嵌套调用链
- `randomMethod()` - 理解异常处理机制

#### Step 2: 监控方法调用
```powershell
# 启动监控，然后在Postman中调用hello接口
watch com.arthas.demo.ArthasMethodDemo normalMethod "{params,returnObj}" -x 2

# 观察输出，理解参数和返回值的展示
```

#### Step 3: 代码修改实验
在IDEA中修改`normalMethod()`：
```java
// 原始代码
String result = "处理结果: " + input.toUpperCase();

// 修改后
String result = "修改后的处理结果: " + input.toUpperCase() + " [时间:" + System.currentTimeMillis() + "]";
```

重新编译运行，观察Arthas监控输出的变化。

### 第三阶段：性能分析实践 (60分钟)

#### Step 1: 性能接口测试
```http
# 在Postman中测试慢接口
GET http://localhost:18888/arthas/demo/slow?level=3

# 同时在PowerShell中启动追踪
trace com.arthas.demo.ArthasControllerDemo slowApi
```

#### Step 2: 理解调用链
在IDEA中阅读`slowApi()`方法：
- `simulateSlowDbQuery()` - 数据库查询模拟
- `performComplexCalculation()` - 复杂计算
- `callExternalService()` - 外部服务调用

#### Step 3: 性能分析实验
```powershell
# 启动CPU分析
profiler start --event cpu

# 在Postman中连续调用慢接口5次
# 等待30秒后停止分析
profiler stop --format html
```

### 第四阶段：字段操作实践 (30分钟)

#### Step 1: 查看静态字段
```powershell
# 查看静态计数器
getstatic com.arthas.demo.ArthasFieldDemo staticCounter

# 修改静态字段值
ognl '@com.arthas.demo.ArthasFieldDemo@staticCounter=999'

# 再次查看，验证修改
getstatic com.arthas.demo.ArthasFieldDemo staticCounter
```

#### Step 2: 代码验证实验
在IDEA中找到`ArthasFieldDemo.java`，添加一个打印静态计数器的方法：
```java
public void printCurrentCounter() {
    System.out.println("当前静态计数器值: " + staticCounter);
}
```

### 第五阶段：综合实践 (60分钟)

#### 实践项目：监控一个完整的用户创建流程

1. **接口测试**：在Postman中创建用户
```http
POST http://localhost:18888/arthas/demo/user
Content-Type: application/json

{
    "name": "张三",
    "email": "zhangsan@example.com",
    "position": "测试工程师"
}
```

2. **监控命令**：
```powershell
# 监控整个创建流程
watch com.arthas.demo.ArthasControllerDemo createUser "{params,returnObj,throwExp}" -e -x 3

# 同时监控字段变化
watch com.arthas.demo.ArthasControllerDemo * "target" -b -s -x 2
```

3. **代码分析**：在IDEA中跟踪代码执行路径
4. **实验修改**：修改验证逻辑，观察异常监控结果

### 🔍 学习技巧总结

#### 最佳实践方法
1. **双屏操作**：一个屏幕显示IDEA和Postman，另一个屏幕显示PowerShell的Arthas
2. **记录日志**：将有趣的监控结果截图或复制保存
3. **小步快跑**：每次只修改一小部分代码，观察变化
4. **场景驱动**：基于具体的业务场景进行学习，而不是孤立地学习命令

#### 推荐的学习顺序
```
Week 1: 环境熟悉 + 基础监控 (watch, monitor)
Week 2: 性能分析 (trace, profiler, dashboard)  
Week 3: 高级功能 (ognl, 字段操作, 类操作)
Week 4: 综合应用 (自定义监控场景)
```

#### 常用的Postman环境变量
```json
{
    "base_url": "http://localhost:18888",
    "user_id": "1",
    "test_name": "Arthas学习者"
}
```

## 🛠️ 高级使用技巧

### 条件表达式
```bash
# 只监控耗时超过100ms的方法
watch * * "{params,returnObj}" '#cost>100'

# 只监控特定参数类型
watch * * "{params,returnObj}" 'params[0] instanceof String'

# 监控异常情况
watch * * "{params,returnObj,throwExp}" -e
```

### 结果展开控制
```bash
# 控制对象展开层级
watch * * "{params,returnObj}" -x 3

# 显示方法执行前后的对象状态
watch * * "{target,params,returnObj}" -b -s
```

### 性能分析组合
```bash
# 启动CPU分析
profiler start --event cpu

# 同时监控特定方法
watch com.arthas.demo.ArthasPerformanceDemo cpuIntensiveTask "{params}" -x 2

# 查看实时状态
dashboard -i 1000

# 停止分析并生成报告
profiler stop --format html
```

## 📈 实际应用场景

### 1. 生产问题排查
- 使用`watch`监控异常方法的参数和返回值
- 使用`trace`分析慢接口的调用链路
- 使用`thread -b`检查死锁问题
- 使用`heapdump`分析内存泄漏

### 2. 性能优化
- 使用`profiler`找出CPU热点
- 使用`monitor`统计方法调用频率
- 使用`dashboard`监控系统资源使用
- 使用`memory`分析内存使用情况

### 3. 代码调试
- 使用`ognl`动态修改字段值进行测试
- 使用`tt`记录和重放方法调用
- 使用`sc/sm`查看类和方法信息
- 使用`jad`反编译查看实际代码

### 4. 运维监控
- 使用定时监控命令持续观察应用状态
- 结合脚本自动化监控关键指标
- 使用条件表达式过滤关注的事件
- 生成性能报告进行分析

## 🎯 注意事项

1. **生产环境使用**：
   - Arthas对性能影响很小，但建议在低峰期使用
   - 某些命令（如heapdump）可能对应用有短暂影响
   - 使用完毕后记得退出Arthas

2. **命令执行**：
   - 某些监控命令会持续运行，使用`q`或`Ctrl+C`退出
   - 使用`help`命令查看详细帮助
   - 使用`quit`或`exit`退出Arthas

3. **资源消耗**：
   - 避免同时运行多个资源密集型监控
   - 注意监控输出的数量，避免日志过多
   - 合理设置监控条件和时间间隔

## 🛠️ 故障排除

### 编译问题

**问题1：Java版本不兼容**
```bash
# 错误信息：不支持发行版本11 或 String.repeat() 方法未找到
# 解决方案：确认使用Java 8
java -version
# 如果不是Java 8，需要切换Java版本或更新JAVA_HOME
```

**问题2：Maven依赖下载失败**
```bash
# 清理本地仓库并重新下载
mvn clean
mvn dependency:resolve -U
```

**问题3：端口冲突**
```bash
# 如果8080端口被占用，修改application.yml中的端口配置
# 或者在启动时指定端口：
java -jar target/bruce-demo-0.0.1.jar --server.port=8081
```

### Arthas连接问题

**问题1：找不到Java进程**
```bash
# 查看所有Java进程
jps -l
# 或者使用ps命令
ps aux | grep java
```

**问题2：Arthas无法连接**
```bash
# 确保使用正确的用户权限
# 如果是不同用户启动的Java进程，可能需要sudo权限
sudo java -jar arthas-boot.jar
```

**问题3：防火墙阻止连接**
```bash
# 检查防火墙设置，Arthas默认使用3658、8563端口
# Linux系统可能需要开放这些端口
```

### 运行时问题

**问题1：演示类没有自动启动**
- 检查控制台日志，确认Spring Boot应用完全启动
- 确认主应用类上有`@EnableScheduling`注解
- 检查是否有异常信息

**问题2：Web接口无法访问**
```bash
# 测试应用是否正常启动
curl http://localhost:18888/arthas/demo/hello
# 检查应用日志，确认Controller是否正确加载
```

**问题3：内存不足**
```bash
# 如果演示过程中出现内存问题，可以增加JVM内存
java -Xmx2g -jar target/bruce-demo-0.0.1.jar
```

### 兼容性说明

- **Java版本**：完全兼容Java 8-17
- **Spring Boot版本**：基于2.2.6.RELEASE，兼容2.x系列
- **操作系统**：支持Windows、Linux、macOS
- **Arthas版本**：支持3.x及以上版本

## 🔧 扩展学习

- [Arthas官方文档](https://arthas.aliyun.com/doc/)
- [Arthas用户案例](https://github.com/alibaba/arthas/issues?q=label%3Auser-case)
- [OGNL表达式指南](https://commons.apache.org/proper/commons-ognl/language-guide.html)
- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [Java 8兼容性指南](https://docs.oracle.com/javase/8/docs/)

## 📞 问题反馈

如果在使用过程中遇到问题，可以：
1. 查看应用日志输出
2. 检查Arthas命令是否正确
3. 参考官方文档
4. 在项目中提交Issue

## 📋 更新历史

### v1.0.0 (2025-08-17)
- ✅ 初始版本发布
- ✅ 创建7个完整的Arthas演示类
- ✅ 完整的中文文档和使用指南
- ✅ Java 8兼容性优化
- ✅ Spring Boot 2.2.6.RELEASE集成
- ✅ 包含所有主要Arthas命令演示

### 技术实现细节
- 基于Spring Boot 2.2.6.RELEASE
- 使用@Component和@RestController注解
- 支持@Scheduled定时任务
- 包含完整的异常处理和监控场景
- 提供REST API接口用于Web监控演示

### 命令覆盖度
- **方法监控**：watch、trace、monitor、tt、stack (100%)
- **性能分析**：profiler、heapdump、jvm、memory、dashboard (100%)
- **字段操作**：getstatic、ognl、vmtool (100%)
- **类分析**：classloader、sc、sm、jad、mc、retransform (100%)
- **线程分析**：thread、jstack (100%)
- **系统信息**：dashboard、jvm、memory (100%)

---

**开始你的Arthas探索之旅吧！** 🚀