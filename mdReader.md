这段代码展示了SLF4J 1.7的**静态绑定机制**的核心问题。

**诡异的地方**：

```java
StaticLoggerBinder.getSingleton();
```

SLF4J源码里调用了这个类，但**SLF4J自己的jar包里没有这个类**。

**那编译怎么过的？**

SLF4J源码里其实有一个占位用的`StaticLoggerBinder`，仅用于编译通过，打包时**故意不打进去**。

**运行时怎么工作的？**

```
你的项目引入：
├── slf4j-api.jar        → 有LoggerFactory，没有StaticLoggerBinder
└── logback-classic.jar  → 有StaticLoggerBinder实现

ClassLoader加载时：
LoggerFactory调用StaticLoggerBinder.getSingleton()
    → 去classpath找这个类
    → 找到logback提供的那个
    → 绑定成功
```

**这设计的问题**：

1. **不透明**：靠ClassLoader碰运气找类，不是显式的服务发现
2. **多实现冲突**：引入两个日志实现，谁先被加载到不可控
3. **编译期hack**：要靠"编译时有、打包时删"这种trick

所以1.8改成了标准SPI——用`ServiceLoader`显式扫描，不再玩这种ClassLoader的隐式查找。