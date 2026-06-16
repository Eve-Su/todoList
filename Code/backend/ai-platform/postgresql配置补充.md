已只读测试 PostgreSQL 连通性，结果是：

- `postgresql / 1234`：认证失败
- `postgres / 1234`：连接成功
- 数据库：`ag_platform`
- 当前用户：`postgres`
- `pgvector` 扩展：已安装，版本 `0.8.2`

因此我按实测可用的 `postgres / 1234` 给需要数据库的三个模块补了默认 HikariCP 数据源配置：

- [ai-platform-api/application.yml](D:/zzz/todoList/Code/backend/ai-platform/ai-platform-api/src/main/resources/application.yml:1)
- [knowledge-service/application.yml](D:/zzz/todoList/Code/backend/ai-platform/knowledge-service/src/main/resources/application.yml:1)
- [job-worker/application.yml](D:/zzz/todoList/Code/backend/ai-platform/job-worker/src/main/resources/application.yml:1)

配置内容是 Spring Boot 默认数据源方式，没有显式切换连接池，所以会使用默认 `HikariCP`。同时给 `knowledge-service` 和 `job-worker` 也补了 `mybatis-plus` 下划线转驼峰配置。

最后已执行 `mvn -q -DskipTests package`，构建通过。