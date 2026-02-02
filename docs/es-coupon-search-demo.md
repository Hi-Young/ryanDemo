# ES 券搜索 Demo（可写进简历的那种）

本 Demo 模拟「券搜索服务」里最常见的 ES 用法：索引设计（mapping）、批量写入（bulk）、全文检索 + 过滤（bool/must/filter）、运营权重（function_score）、高亮、聚合分面（aggs）、地理位置筛选/排序（geo_point + _geo_distance）、联想词（completion suggest）。

## 1. 启动 Elasticsearch

本仓库提供了 docker compose：

```bash
cd docker/elasticsearch
docker compose up -d
```

- ES: `http://127.0.0.1:9200`
- Kibana: `http://127.0.0.1:5601`

## 2. 创建索引（mapping）

标准分词版本（无需插件）：

```bash
curl -X PUT "http://127.0.0.1:9200/coupon_demo_v1" \
  -H "Content-Type: application/json" \
  --data-binary @src/main/resources/es/coupon/coupon-index-standard.json
```

可选：IK 分词版本（需要 ES 安装 IK 插件）：

```bash
curl -X PUT "http://127.0.0.1:9200/coupon_demo_v1" \
  -H "Content-Type: application/json" \
  --data-binary @src/main/resources/es/coupon/coupon-index-ik.json
```

## 3. 批量导入样例数据

```bash
curl -X POST "http://127.0.0.1:9200/_bulk?refresh=true" \
  -H "Content-Type: application/x-ndjson" \
  --data-binary @src/main/resources/es/coupon/coupon-sample-bulk.ndjson
```

## 4. 通过 Spring Boot 接口练手

代码位置：

- `src/main/java/com/ryan/es/coupon/CouponEsController.java`
- `src/main/java/com/ryan/es/coupon/CouponEsService.java`
- `src/main/java/com/ryan/es/coupon/CouponEsQueryBuilder.java`

### 4.1 配置（可选）

`src/main/resources/application-dev.yml` 里可以加（也可以不加，默认就是 `127.0.0.1:9200`）：

```yaml
demo:
  elasticsearch:
    base-url: http://127.0.0.1:9200
    coupon-index: coupon_demo_v1
    coupon-index-mapping: standard
```

### 4.2 快速验证

```bash
curl "http://127.0.0.1:18888/api/es/coupon/ping"

curl -X POST "http://127.0.0.1:18888/api/es/coupon/index?recreate=true"

curl -X POST "http://127.0.0.1:18888/api/es/coupon/sample/bulk?refresh=true"
```

### 4.3 查询（支持高亮/聚合/权重/geo）

查看生成的 DSL（不请求 ES，只生成 JSON）：

```bash
curl -X POST "http://127.0.0.1:18888/api/es/coupon/dsl" \
  -H "Content-Type: application/json" \
  -d '{"keyword":"满减","cityCode":"310000","sort":"default","pageNo":1,"pageSize":10}'
```

真实查询（会请求 ES 并解析成业务返回结构）：

```bash
curl -X POST "http://127.0.0.1:18888/api/es/coupon/search" \
  -H "Content-Type: application/json" \
  -d '{"keyword":"星巴克","sort":"distance","lat":31.2304,"lon":121.4737,"distanceKm":5}'
```

联想词（completion suggest）：

```bash
curl "http://127.0.0.1:18888/api/es/coupon/suggest?prefix=%E6%98%9F&size=10"
```

## 5. 简历可写点（基于这个 Demo）

- 设计券搜索 ES 索引：区分 `text/keyword`、日期/数值字段、`geo_point`，并为 title 增加 `completion` 用于联想词。
- 负责搜索 DSL：`bool(must + filter)` 实现「关键词检索 + 多维过滤」，并使用 `function_score + field_value_factor(weight)` 融合运营权重排序。
- 支持高亮、分面聚合（merchant/city/category）、地理位置过滤/排序（`geo_distance` / `_geo_distance`）。
- 使用 `_bulk` 批量写入，提供 upsert（`_update + doc_as_upsert`）与 refresh 控制，方便联调/压测。

