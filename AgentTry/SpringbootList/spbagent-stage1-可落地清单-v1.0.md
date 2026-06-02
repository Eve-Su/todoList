下面这版按“能开工、能验收、能逐步扩”的思路来拆，适合你直接排期。

**一期目标**
先做出一个最小可运行版本，具备这 5 个能力：

1. 用户发起一次对话
2. Agent 能保存会话和消息
3. Agent 能调用工具
4. Agent 能输出结构化动作
5. Agent 能做基础知识检索

---

**一、项目初始化清单**

1. 技术栈定版
- `Spring Boot 3.x`
- `Java 17` 或 `21`
- `PostgreSQL 15+`
- `pgvector`
- `MyBatis-Plus` 或 `MyBatis`
- `Jackson`
- `Hibernate Validator`
- `Lombok`
- `OpenAPI/Swagger`

2. 工程结构确定
建议先单体，后面再拆模块：
- `api`
- `application`
- `domain`
- `runtime`
- `infrastructure`
- `common`

3. 配置项先预留
至少预留这些：
- 数据库连接
- pgvector 开关
- LLM 接口地址/密钥
- embedding 模型配置
- agent 最大循环次数
- 工具调用超时时间
- 会话摘要阈值
- 检索 topK

验收标准：
- 工程能启动
- 健康检查可访问
- Swagger 可打开

---

**二、数据库落地清单**

先安装扩展：

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

第一批先建 6 张表：

1. `agent_session`
2. `agent_message`
3. `agent_action_log`
4. `agent_tool_log`
5. `knowledge_document`
6. `knowledge_chunk`

建议你第一阶段先不建太多辅助表，避免过度设计。

验收标准：
- 6 张表建成
- 主键、唯一键、索引齐全
- `knowledge_chunk.embedding` 可正常存取向量
- 能跑一条简单向量查询

---

**三、核心领域模型清单**

先定义这几个核心对象：

1. `AgentSession`
2. `AgentMessage`
3. `AgentAction`
4. `ToolDefinition`
5. `ToolResult`
6. `ExecutionContext`
7. `KnowledgeChunk`
8. `ChatRequest`
9. `ChatResponse`

`AgentAction.type` 建议固定枚举：
- `RESPOND`
- `TOOL_CALL`
- `RETRIEVE`
- `ASK_USER`
- `FINISH`

验收标准：
- 核心对象职责清晰
- 不直接把数据库 Entity 暴露给 controller
- DTO、Domain、Entity 分层明确

---

**四、接口清单**

第一版建议只做这 4 个接口，全部 `POST`：

1. `POST /agent/chat`
主对话入口

2. `POST /agent/session/history`
查询会话历史

3. `POST /agent/knowledge/import`
导入知识文档

4. `POST /agent/knowledge/search`
调试知识检索

如果后面需要再补：
- `POST /agent/tool/execute`
- `POST /agent/memory/query`

验收标准：
- 接口出入参统一
- controller 层带 swagger 注解
- 保存/编辑类接口后续如有日志体系，再统一补日志注解

---

**五、运行时主链路清单**

先实现一条最小执行链：

1. 接收用户请求
2. 加载或创建 `session`
3. 保存用户消息
4. 构建上下文
5. 调用模型生成 `AgentAction`
6. 如果需要工具调用，执行工具
7. 保存工具结果
8. 再次调用模型生成最终答复
9. 保存 assistant 消息
10. 返回响应

建议主入口统一收敛到：
- `AgentRunner.run()`

验收标准：
- 单轮问答能闭环
- 工具调用能闭环
- 异常时有明确返回和日志
- 同一个 `sessionId` 可连续对话

---

**六、工具体系清单**

第一版只做 2 到 3 个工具就够：

1. `TimeTool`
查当前时间

2. `HttpGetTool` 或内部查询工具
验证工具调用链路

3. `KnowledgeSearchTool`
封装知识检索能力

关键组件：
- `Tool`
- `ToolRegistry`
- `ToolExecutor`

必须有的能力：
- 工具注册
- 参数校验
- 超时控制
- 异常捕获
- 执行日志

验收标准：
- 新增一个工具不需要改主流程
- 工具异常不会拖垮整个会话
- 工具日志能查到参数、结果、耗时

---

**七、模型接入清单**

先统一封装一个 `LlmClient`，不要把模型调用散在各处。

至少拆 2 类能力：

1. `chat`
用于 action 决策和最终回答

2. `embedding`
用于知识检索

建议模型返回结构化 JSON，例如：

```json
{
  "type": "TOOL_CALL",
  "toolName": "knowledge_search",
  "toolArgs": {
    "query": "pgvector 怎么用"
  },
  "reason": "need retrieval before answer"
}
```

验收标准：
- 模型输出能稳定反序列化
- 解析失败有兜底策略
- chat 和 embedding 接口解耦

---

**八、知识库与向量检索清单**

第一版知识库最小流程：

1. 上传或录入文档
2. 文档切 chunk
3. 生成 embedding
4. 存入 `knowledge_chunk`
5. 查询时生成 query embedding
6. 相似度检索 topK
7. 拼装结果给模型

第一版参数建议：
- chunk 大小：`500-800` 字
- overlap：`50-100`
- topK：`3-5`

验收标准：
- 能成功导入一篇文档
- 能落 `knowledge_document` 和 `knowledge_chunk`
- 能根据 query 查出相近 chunk
- 检索结果可拼进模型上下文

---

**九、记忆机制清单**

第一版只做短期记忆和摘要记忆，不急着做长期记忆表。

先做：

1. 最近 N 轮消息窗口
2. 历史摘要字段 `agent_session.summary`

触发摘要建议：
- 消息数超过阈值
- 或 token 估算超过阈值

验收标准：
- 长对话不会无限堆上下文
- 摘要后仍能保持基本上下文连续性

---

**十、日志与观测清单**

这块别省，后面排问题全靠它。

至少落：
1. `traceId`
2. `sessionId`
3. 模型请求耗时
4. 工具耗时
5. action 决策日志
6. 错误堆栈日志

数据库表至少记录：
- `agent_action_log`
- `agent_tool_log`

验收标准：
- 一次对话能串起完整 trace
- 能查出“为什么调用了某个工具”
- 能查出“哪一步失败了”

---

**十一、异常处理清单**

至少覆盖这几类异常：

1. 模型调用失败
2. 模型输出 JSON 解析失败
3. 工具不存在
4. 工具参数非法
5. 工具执行超时
6. 知识检索为空
7. 会话不存在

验收标准：
- controller 层统一异常返回
- 不把底层异常直接抛给前端
- 日志里能定位根因

---

**十二、第一阶段开发顺序**

建议按这个顺序做，最稳：

1. 搭工程和配置
2. 建库建表
3. 定义 DTO / Domain / Entity
4. 实现 `Session` 和 `Message` 持久化
5. 实现 `LlmClient`
6. 实现 `AgentAction` 结构化输出
7. 实现 `ToolRegistry + ToolExecutor`
8. 实现 `AgentRunner`
9. 打通 `POST /agent/chat`
10. 实现知识导入和检索
11. 增加摘要记忆
12. 完善日志和异常处理

---

**十三、每阶段验收节点**

**节点 1：会话闭环**
- 能创建/复用会话
- 能保存用户消息和回复消息

**节点 2：工具闭环**
- Agent 能判断并调用工具
- 工具结果能回注到模型上下文

**节点 3：RAG 闭环**
- 文档可切片、向量化、存储、召回
- Agent 能基于召回结果回答

**节点 4：稳定性闭环**
- 有 action 日志
- 有 tool 日志
- 有统一异常处理
- 有摘要机制

---

**十四、第二阶段再做的东西**
这些别放第一期：

1. 多 Agent
2. 工作流 DSL
3. 长期记忆 `memory_fact`
4. 重排模型
5. 权限隔离
6. 异步任务调度
7. 插件市场式工具体系

---

**十五、你现在可以直接开工的最小范围**
如果你想最快落地，第一周只盯这 4 件事：

1. 建 6 张表
2. 做 `POST /agent/chat`
3. 做 2 个工具
4. 做知识检索最小闭环

这 4 个通了，你这个框架就不是 PPT 了。

如果你愿意，我下一步可以继续给你一版更细的“开发任务拆解表”，直接按：
- `Day 1`
- `Day 2`
- `Day 3`
- `Day 4`
来排。