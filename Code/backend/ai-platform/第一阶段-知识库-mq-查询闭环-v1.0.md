下面按“小白接手视角”把整条链路讲清楚。你可以把它理解成：`knowledge-service` 负责接收文档和发任务，`RocketMQ` 负责把任务排队，`job-worker` 负责异步干活，PostgreSQL/pgvector 负责保存结果和向量检索。

**一、整体闭环**
当前最小闭环是：

```text
1. 用户调用 /knowledge/document/import
2. knowledge-service 保存文档 knowledge_document
3. knowledge-service 保存导入任务 knowledge_import_task
4. knowledge-service 发送 RocketMQ 消息 ai_knowledge_import_topic
5. job-worker 监听并消费这个 topic
6. job-worker 读取消息 payload，执行切片、生成 embedding
7. job-worker 写入 knowledge.knowledge_chunk
8. job-worker 更新任务状态 READY，写 job_work.job_task_log
9. 用户调用 /knowledge/search
10. knowledge-service 用 query 生成 embedding，再从 knowledge_chunk 做向量召回
```

对应关系可以先记成一句话：

```text
导入入口在 knowledge-service，耗时任务交给 RocketMQ，真正异步处理在 job-worker，检索再回到 knowledge-service。
```

**二、服务和类职责**
`knowledge-service`：知识库服务，负责“入口”和“检索”。

[KnowledgeDocumentController.java](D:/zzz/todoList/Code/backend/ai-platform/knowledge-service/src/main/java/com/zz/knowledge/api/controller/KnowledgeDocumentController.java:19)

这个是文档导入接口层，暴露：

```text
POST /knowledge/document/import
```

它不做复杂业务，只接收请求，然后调用应用服务。

[KnowledgeDocumentImportAppService.java](D:/zzz/todoList/Code/backend/ai-platform/knowledge-service/src/main/java/com/zz/knowledge/application/service/KnowledgeDocumentImportAppService.java:21)

这是导入主业务类，职责最多：

```text
生成 docCode
生成 taskId
写 knowledge.knowledge_document
写 knowledge.knowledge_import_task
组装 BaseMqMessage
调用 RocketMqProducer 发送 MQ
返回 taskId、docCode、status
```

里面这段就是把任务丢给 MQ：

```java
rocketMqProducer.syncSend(MqTopicConstant.AI_KNOWLEDGE_IMPORT_TOPIC, BaseMqMessage.builder()
        .taskId(taskId)
        .taskType(MqTagConstant.DOC_IMPORT)
        .traceId(TraceUtil.currentTraceId())
        .businessId(docCode)
        .sourceService("knowledge-service")
        .payloadJson(task.getPayloadJson())
        .timestamp(System.currentTimeMillis())
        .build());
```

[KnowledgeSearchController.java](D:/zzz/todoList/Code/backend/ai-platform/knowledge-service/src/main/java/com/zz/knowledge/api/controller/KnowledgeSearchController.java:19)

这是检索接口，暴露：

```text
POST /knowledge/search
```

[KnowledgeSearchAppService.java](D:/zzz/todoList/Code/backend/ai-platform/knowledge-service/src/main/java/com/zz/knowledge/application/service/KnowledgeSearchAppService.java:13)

这是检索业务类。它会：

```text
拿 query
调用 EmbeddingFacade 生成查询向量
调用 KnowledgeChunkRepository.searchByEmbedding 做向量召回
如果向量召回为空，再走文本 like 兜底
```

[KnowledgeChunkRepository.java](D:/zzz/todoList/Code/backend/ai-platform/knowledge-service/src/main/java/com/zz/knowledge/infrastructure/persistence/repository/KnowledgeChunkRepository.java:11)

这是知识切片查询仓储。核心是：

```sql
ORDER BY embedding OPERATOR(public.<=>) ?::public.vector
```

`<=>` 是 pgvector 的余弦/距离类操作符。因为你的连接串用了 `currentSchema=knowledge`，所以这里显式写了 `public.<=>` 和 `public.vector`，否则 PostgreSQL 找不到 pgvector 的类型和操作符。

**三、job-worker 做什么**
`job-worker`：异步任务执行器。它不提供 HTTP 接口，只监听 MQ。

[KnowledgeImportConsumer.java](D:/zzz/todoList/Code/backend/ai-platform/job-worker/src/main/java/com/zz/jobworker/consumer/KnowledgeImportConsumer.java:11)

这是 MQ 消费者，监听：

```text
topic = ai_knowledge_import_topic
consumerGroup = job-worker-knowledge-import-group
```

它收到消息后调用 `KnowledgeImportHandler`。

[AbstractMqConsumer.java](D:/zzz/todoList/Code/backend/ai-platform/platform-starter-mq/src/main/java/com/zz/platform/mq/consumer/AbstractMqConsumer.java:1)

这是公共 MQ 消费模板，负责统一日志、异常封装。业务消费者只需要实现 `doConsume`。

[KnowledgeImportHandler.java](D:/zzz/todoList/Code/backend/ai-platform/job-worker/src/main/java/com/zz/jobworker/handler/KnowledgeImportHandler.java:1)

这是异步导入真正干活的地方。它做：

```text
解析 MQ message.payloadJson
记录 job_task_log RUNNING
更新 knowledge_import_task 为 IMPORTING
删除旧 chunk
对 contentText 切片
生成 placeholder embedding
写 knowledge.knowledge_chunk
更新 knowledge_import_task 为 READY
更新 knowledge_document 为 READY
记录 job_task_log SUCCESS
失败时更新 FAILED 和 error_message
```

[KnowledgeChunkWriteRepository.java](D:/zzz/todoList/Code/backend/ai-platform/job-worker/src/main/java/com/zz/jobworker/infrastructure/persistence/repository/KnowledgeChunkWriteRepository.java:8)

这个类负责往 `knowledge.knowledge_chunk` 写切片和向量：

```sql
INSERT INTO knowledge.knowledge_chunk
(document_id, kb_code, chunk_no, chunk_text, chunk_tokens, metadata_json, embedding)
VALUES (?, ?, ?, ?, ?, '{}'::jsonb, ?::public.vector)
```

[KnowledgeImportStatusRepository.java](D:/zzz/todoList/Code/backend/ai-platform/job-worker/src/main/java/com/zz/jobworker/infrastructure/persistence/repository/KnowledgeImportStatusRepository.java:1)

负责更新导入任务和文档状态，比如：

```text
PENDING
IMPORTING
READY
FAILED
```

[JobTaskLogRepository.java](D:/zzz/todoList/Code/backend/ai-platform/job-worker/src/main/java/com/zz/jobworker/infrastructure/persistence/repository/JobTaskLogRepository.java:8)

负责写 `job_work.job_task_log`，也就是异步任务执行日志。

**四、公共模块怎么参与**
[BaseMqMessage.java](D:/zzz/todoList/Code/backend/ai-platform/platform-starter-mq/src/main/java/com/zz/platform/mq/message/BaseMqMessage.java:1)

所有 MQ 消息的统一结构，里面有：

```text
taskId
taskType
businessId
traceId
tenantId
sourceService
payloadJson
timestamp
```

[MqTopicConstant.java](D:/zzz/todoList/Code/backend/ai-platform/platform-starter-mq/src/main/java/com/zz/platform/mq/constant/MqTopicConstant.java:1)

统一管理 topic 名称，目前核心是：

```text
ai_knowledge_import_topic
```

[MqTagConstant.java](D:/zzz/todoList/Code/backend/ai-platform/platform-starter-mq/src/main/java/com/zz/platform/mq/constant/MqTagConstant.java:1)

统一管理 tag/taskType，目前文档导入是：

```text
DOC_IMPORT
```

[RocketMqProducer.java](D:/zzz/todoList/Code/backend/ai-platform/platform-starter-mq/src/main/java/com/zz/platform/mq/producer/RocketMqProducer.java:12)

统一 MQ 生产者封装。`knowledge-service` 不直接用 RocketMQTemplate，而是通过它发送消息。

[PlatformMqAutoConfiguration.java](D:/zzz/todoList/Code/backend/ai-platform/platform-starter-mq/src/main/java/com/zz/platform/mq/config/PlatformMqAutoConfiguration.java:9)

自动创建 `RocketMqProducer`。这里注意，它要在 RocketMQ 官方自动配置之后执行，否则拿不到 `RocketMQTemplate`。

[JsonbTypeHandler.java](D:/zzz/todoList/Code/backend/ai-platform/knowledge-service/src/main/java/com/zz/knowledge/infrastructure/persistence/typehandler/JsonbTypeHandler.java:1)

处理 PostgreSQL `jsonb` 字段，否则 MyBatis-Plus 会把 JSON 字符串当 `varchar` 写，导致你之前看到的：

```text
字段 payload_json 的类型为 jsonb，但表达式的类型为 character varying
```

**五、RocketMQ 在这里怎么用**
RocketMQ 这里承担的是“异步削峰”和“任务解耦”。

不用 MQ 的话，`/knowledge/document/import` 会在 HTTP 请求里直接完成：

```text
导入文档 -> 切片 -> embedding -> 写向量
```

这会导致接口很慢，而且如果 embedding 或切片失败，用户请求也会失败。

用了 MQ 后变成：

```text
HTTP 接口只负责落任务和发消息
job-worker 后台慢慢消费处理
```

所以 `knowledge-service` 和 `job-worker` 不需要互相 HTTP 调用。

当前 MQ 角色：

```text
NameServer：路由中心，告诉生产者和消费者 broker 在哪里
Broker：真正存消息的服务
Topic：消息分类，这里是 ai_knowledge_import_topic
Producer：knowledge-service
Consumer：job-worker
Consumer Group：job-worker-knowledge-import-group
```

**六、本地需要启动哪些 CMD**
建议开 4 个窗口：

```text
CMD 1：RocketMQ NameServer
CMD 2：RocketMQ Broker
CMD 3：knowledge-service
CMD 4：job-worker
```

前提路径按你本机当前环境：

```text
RocketMQ: D:\W\Environment\rocketmq-all-5.5.0-bin-release
JDK17: D:\W\Environment\Java\jdk17
Project: D:\zzz\todoList\Code\backend\ai-platform
```

CMD 1：启动 NameServer

```cmd
set JAVA_HOME=D:\W\Environment\Java\jdk17
set ROCKETMQ_HOME=D:\W\Environment\rocketmq-all-5.5.0-bin-release
cd /d %ROCKETMQ_HOME%\bin
mqnamesrv.cmd
```

看到类似这个就说明成功：

```text
The Name Server boot success
```

CMD 2：启动 Broker

```cmd
set JAVA_HOME=D:\W\Environment\Java\jdk17
set ROCKETMQ_HOME=D:\W\Environment\rocketmq-all-5.5.0-bin-release
set JAVA_OPT_EXT=-Xms256m -Xmx512m -XX:MaxDirectMemorySize=512m
cd /d %ROCKETMQ_HOME%\bin
mqbroker.cmd -n localhost:9876 -c ..\conf\broker.conf autoCreateTopicEnable=true
```

看到类似这个就说明成功：

```text
The broker[broker-a, xxx:10911] boot success
```

CMD 3：启动 knowledge-service

```cmd
set JAVA_HOME=D:\W\Environment\Java\jdk17
cd /d D:\zzz\todoList\Code\backend\ai-platform
mvn -q -DskipTests package
java -jar knowledge-service\target\knowledge-service-1.0.0-SNAPSHOT.jar
```

默认端口是：

```text
18081
```

如果 `18081` 被占用，可以临时用：

```cmd
java -jar knowledge-service\target\knowledge-service-1.0.0-SNAPSHOT.jar --server.port=18091
```

CMD 4：启动 job-worker

```cmd
set JAVA_HOME=D:\W\Environment\Java\jdk17
cd /d D:\zzz\todoList\Code\backend\ai-platform
java -jar job-worker\target\job-worker-1.0.0-SNAPSHOT.jar
```

`job-worker` 没有 HTTP 端口，看到消费者注册成功即可：

```text
running container: DefaultRocketMQListenerContainer
topic='ai_knowledge_import_topic'
consumerGroup='job-worker-knowledge-import-group'
```

**七、怎么创建 topic**
如果 broker 启动时带了：

```cmd
autoCreateTopicEnable=true
```

第一次发送时通常能自动创建 topic。

更推荐你显式创建，命令如下：

```cmd
set JAVA_HOME=D:\W\Environment\Java\jdk17
set ROCKETMQ_HOME=D:\W\Environment\rocketmq-all-5.5.0-bin-release
cd /d %ROCKETMQ_HOME%\bin

mqadmin.cmd updateTopic -n localhost:9876 -c DefaultCluster -t ai_knowledge_import_topic -r 8 -w 8
```

查看 topic 是否存在：

```cmd
mqadmin.cmd topicList -n localhost:9876
```

查看 topic 路由：

```cmd
mqadmin.cmd topicRoute -n localhost:9876 -t ai_knowledge_import_topic
```

如果这里没有路由，应用发送消息时就会报：

```text
No route info of this topic: ai_knowledge_import_topic
```

**八、怎么验证闭环**
导入文档：

```powershell
$body = @{
  kbCode = 'demo_kb'
  docName = 'closure-test.txt'
  sourceType = 'TEXT'
  sourceUri = 'local-test'
  contentText = '企业 AI Agent 平台一期闭环验证。knowledge-service 接收文档导入请求，写入任务表并发送 RocketMQ 消息。job-worker 消费消息后执行切片、生成 embedding，并写入 knowledge_chunk。'
} | ConvertTo-Json -Depth 5

Invoke-WebRequest `
  -UseBasicParsing `
  -Uri http://localhost:18081/knowledge/document/import `
  -Method POST `
  -ContentType 'application/json; charset=utf-8' `
  -Body $body
```

查数据库：

```powershell
$env:PGPASSWORD='1234'

psql -h localhost -p 5432 -U postgres -d ag_platform -c "
select id, doc_code, status, updated_at
from knowledge.knowledge_document
order by id desc limit 3;

select id, task_id, status, jsonb_typeof(payload_json), updated_at
from knowledge.knowledge_import_task
order by id desc limit 3;

select document_id, kb_code, count(*) chunk_count, max(created_at) last_chunk_at
from knowledge.knowledge_chunk
group by document_id, kb_code
order by document_id desc limit 3;

select task_id, status, jsonb_typeof(result_json), error_message, updated_at
from job_work.job_task_log
order by id desc limit 3;
"
```

成功标准：

```text
knowledge_document.status = READY
knowledge_import_task.status = READY
knowledge_chunk 有数据
job_task_log.status = SUCCESS
```

检索：

```powershell
$search = @{
  kbCode = 'demo_kb'
  query = 'job-worker 消费消息后做什么'
  topK = 3
} | ConvertTo-Json -Depth 5

Invoke-WebRequest `
  -UseBasicParsing `
  -Uri http://localhost:18081/knowledge/search `
  -Method POST `
  -ContentType 'application/json; charset=utf-8' `
  -Body $search
```

**九、有没有可视化界面**
有，官方叫 RocketMQ Dashboard。官方文档说明它可以查看 broker、topic、consumer、消息等信息，也支持用可视化方式替代部分命令行操作，比如 topic 配置和 broker 管理。[RocketMQ Dashboard 官方文档](https://rocketmq.apache.org/docs/deploymentOperations/04Dashboard/)；项目源码在 [apache/rocketmq-dashboard](https://github.com/apache/rocketmq-dashboard)。

如果你有 Docker，可以这样启动：

```cmd
docker run -d --name rocketmq-dashboard ^
  -p 8088:8080 ^
  -e "JAVA_OPTS=-Drocketmq.namesrv.addr=host.docker.internal:9876" ^
  apacherocketmq/rocketmq-dashboard:latest
```

然后访问：

```text
http://localhost:8088
```

如果 Docker 访问不到宿主机 RocketMQ，可以把 `host.docker.internal:9876` 改成你本机局域网 IP，比如：

```text
10.124.168.201:9876
```

Dashboard 里你主要看这几个页面：

```text
Cluster：看 broker 是否在线
Topic：看 ai_knowledge_import_topic 是否存在
Consumer：看 job-worker-knowledge-import-group 是否在线
Message：按 topic 查询消息
```

**十、新人最容易混的点**
`NameServer` 不是存消息的，它只是路由中心。

`Broker` 才是存消息的。只启动 NameServer 不启动 Broker，发送消息会报：

```text
No route info of this topic
```

`Topic` 是消息分类。这里固定用：

```text
ai_knowledge_import_topic
```

`Producer` 是发消息的人，这里是：

```text
knowledge-service
```

`Consumer` 是消费消息的人，这里是：

```text
job-worker
```

`job-worker` 不提供 HTTP 接口，所以别去访问它的端口，它启动成功看日志就行。