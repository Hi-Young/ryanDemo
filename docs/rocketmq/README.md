# bruceDemo RocketMQ 接入说明

## 1. 目标

- 在本地启动 RocketMQ NameServer、Broker、Dashboard
- 在 Spring Boot 中提供消息发送和消费示例
- 支持主动构造死信消息，便于在控制台查看重试与死信队列

## 2. 本地启动 RocketMQ 与 Dashboard

在项目根目录执行：

```bash
docker compose -f docker/rocketmq/docker-compose.yml up -d
```

当前 compose 使用镜像：

- `apache/rocketmq:5.3.2`
- `apacherocketmq/rocketmq-dashboard:latest`

服务端口：

- NameServer: `9876`
- Broker: `10911`
- Dashboard: `18080`

Dashboard 地址：

`http://127.0.0.1:18080`

## 3. 应用配置

`application-dev.yml` 已新增以下配置项：

- `rocketmq.name-server`
- `rocketmq.producer.group`
- `demo.rocketmq.topic`
- `demo.rocketmq.tag`
- `demo.rocketmq.consumer-group`
- `demo.rocketmq.dashboard-url`

默认值：

- Topic: `bruce-demo-topic`
- Consumer Group: `bruce-demo-consumer`
- DLQ Topic: `%DLQ%bruce-demo-consumer`

## 4. 演示接口

### 4.1 查看元信息

```bash
curl "http://127.0.0.1:18888/rocketmq/demo/meta"
```

### 4.2 发送普通消息

```bash
curl -X POST "http://127.0.0.1:18888/rocketmq/demo/send?body=hello"
```

### 4.3 发送死信演示消息

```bash
curl -X POST "http://127.0.0.1:18888/rocketmq/demo/send-dlq?body=force-dlq"
```

## 5. 如何在 Dashboard 观察

1. 在 `Topic` 页面可看到 `bruce-demo-topic`
2. 发送普通消息后，可在消息轨迹中看到投递与消费成功
3. 发送死信演示消息后，消费者会故意失败并重试
4. 超过重试次数后，消息进入 `%DLQ%bruce-demo-consumer`

## 6. 常见问题

### 6.1 镜像 tag 不存在

如果看到类似报错：

`docker.io/apache/rocketmq:4.9.8: not found`

说明使用了不存在的 tag。请使用当前文档的 compose（`5.3.2`）重试。

---
作者：codex
