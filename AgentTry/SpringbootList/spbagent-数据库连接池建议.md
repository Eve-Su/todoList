现在新项目里，尤其是：

* Spring Boot 2.x / 3.x
* Java 17+
* 微服务
* Kubernetes
* 高并发 API

其实大量公司已经默认：

# 用 HikariCP

因为 SpringBoot 默认连接池就是：

HikariCP

---

# 一、Hikari 为什么现在最主流

核心就一句：

> 极致性能 + 极简设计

它跟 Druid 完全不是一个设计方向。

---

# 二、Hikari 的核心优势

## 1）性能极强（行业第一梯队）

Hikari 的设计目标：

```text
减少锁竞争
减少对象创建
减少状态切换
减少额外功能
```

所以：

* 获取连接更快
* 并发能力更强
* GC压力更小

高并发下：

通常比 Druid：

```text
快 10% ~ 40%
```

尤其：

* QPS高
* 短SQL
* 微服务
* 云原生

场景。

---

# 三、为什么 Hikari 快

因为它：

## 不做很多“重功能”

比如：

Druid 内置：

* SQL监控
* SQL解析
* wall防火墙
* SQL统计
* Web控制台
* filter链

这些都要消耗性能。

Hikari：

```text
只做连接池
```

所以特别轻。

---

# 四、Hikari 最大优势

## 低延迟

很多时候：

不是平均速度。

而是：

```text
P99
P999
```

尾延迟。

Hikari 在高并发下：

尾延迟控制非常优秀。

这点在：

* 网关
* 秒杀
* 支付
* API系统

非常重要。

---

# 五、Hikari 的缺点

## 1）监控能力弱

不像 Druid：

直接：

```text
/druid
```

看SQL。

Hikari：

没有。

---

## 2）没有 wall

Druid 有：

```text
wall filter
```

SQL防火墙。

Hikari 没有。

---

## 3）没有慢SQL统计

Hikari：

只负责：

```text
连接池
```

不是数据库监控平台。

---

# 六、那现在企业怎么做？

现代架构已经：

## 不依赖连接池做监控

而是：

| 功能    | 方案                |
| ----- | ----------------- |
| SQL监控 | P6Spy             |
| SQL分析 | SkyWalking        |
| 链路追踪  | OpenTelemetry     |
| 慢SQL  | MySQL本身           |
| 指标    | Micrometer        |
| 可视化   | Grafana           |
| APM   | Pinpoint / Zipkin |

所以：

Hikari 更适合现代微服务。

---

# 七、生产级 Hikari 最终配置

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver

    url: jdbc:mysql://127.0.0.1:3306/your_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&rewriteBatchedStatements=true&cachePrepStmts=true&useServerPrepStmts=true

    username: root
    password: your_password

    hikari:

      # 连接池名称
      pool-name: HikariPool

      # 最大连接数
      maximum-pool-size: 100

      # 最小空闲连接
      minimum-idle: 10

      # 空闲连接超时
      idle-timeout: 300000

      # 连接最大生命周期
      max-lifetime: 1800000

      # 获取连接超时
      connection-timeout: 10000

      # 连接存活检测
      validation-timeout: 5000

      # 自动提交
      auto-commit: true

      # 保活
      keepalive-time: 60000

      # leak检测（非常重要）
      leak-detection-threshold: 60000

      # mysql优化
      data-source-properties:
        cachePrepStmts: true
        prepStmtCacheSize: 250
        prepStmtCacheSqlLimit: 2048
        useServerPrepStmts: true
        useLocalSessionState: true
        rewriteBatchedStatements: true
        cacheResultSetMetadata: true
        cacheServerConfiguration: true
        elideSetAutoCommits: true
        maintainTimeStats: false
```

---

# 八、Hikari 最重要参数

## 1）maximum-pool-size

别乱调大。

很多人：

```yaml
maximum-pool-size: 500
```

然后数据库直接炸。

实际上：

大部分系统：

```text
30~100
```

已经很多。

---

## 2）max-lifetime（极重要）

```yaml
max-lifetime: 1800000
```

30分钟。

原因：

MySQL 会主动断开长连接。

如果你：

```text
连接池连接 > MySQL超时时间
```

就会：

```text
Communications link failure
```

所以：

Hikari 官方强烈建议：

> 小于 MySQL wait_timeout

---

## 3）connection-timeout

```yaml
connection-timeout: 10000
```

拿不到连接：

10 秒快速失败。

高并发系统非常重要。

---

## 4）leak-detection-threshold

```yaml
leak-detection-threshold: 60000
```

连接泄漏检测。

比如：

```java
conn.close();
```

没执行。

它会报警。

生产非常有用。

---

# 九、安全方面要搭配什么

Hikari 自身：

## 不负责安全

它只是连接池。

---

# 十、真正生产安全方案

## 1）必须搭配 SQL 参数化

永远：

```java
PreparedStatement
```

或者：

* MyBatis
* JPA
* MyBatis-Plus

避免：

```java
"select * from user where id=" + id
```

否则：

SQL注入。

---

## 2）必须限制 SQL 权限

数据库账号：

不要：

```sql
GRANT ALL PRIVILEGES
```

而是：

最小权限原则。

例如：

```sql
SELECT
INSERT
UPDATE
DELETE
```

按业务分。

---

## 3）推荐搭配 SQL 防火墙

如果你想要：

Druid wall 那种能力。

现在更推荐：

## 网关层

例如：

* Apache APISIX
* Kong
* Spring Cloud Gateway

做：

* WAF
* 黑名单
* 限流
* SQL注入拦截
* CC防护

而不是连接池层。

---

# 十一、真正高并发安全架构

现代生产：

一般：

```text
Nginx
  ↓
API Gateway
  ↓
限流
  ↓
鉴权
  ↓
Redis
  ↓
应用
  ↓
Hikari
  ↓
MySQL
```

连接池：

只是其中一层。

---

# 十二、Hikari 最佳搭配

推荐组合：

| 功能     | 推荐           |
| ------ | ------------ |
| 连接池    | HikariCP     |
| ORM    | MyBatis      |
| SQL打印  | P6Spy        |
| 监控     | Micrometer   |
| 链路     | SkyWalking   |
| 限流     | Sentinel     |
| 网关     | APISIX       |
| 熔断     | Resilience4j |
| 缓存     | Redis        |
| 防SQL注入 | 参数化SQL       |

---

# 十三、Druid 和 Hikari 最终怎么选

## 选 Hikari

如果：

* 高并发
* 微服务
* K8s
* 云原生
* 极致性能
* SpringBoot3

推荐：

HikariCP

---

## 选 Druid

如果：

* 传统单体
* 要SQL监控
* 要wall
* 要慢SQL统计
* 运维能力一般

推荐：

Druid

---

# 十四、现在行业趋势

现在大量公司：

其实已经：

```text
Hikari + Prometheus + Grafana
```

替代：

```text
Druid监控页
```

因为：

更轻、更云原生、更适合容器化。
