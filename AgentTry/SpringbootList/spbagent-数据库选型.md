建议你把第一版目标定成：`单 Agent + 多轮会话 + 工具调用 + 会话记忆 + 基础 RAG`。这样库表不会过重，但后面扩展到长期记忆、知识库、工作流都接得住。

**模块设计**
建议按这 7 个模块拆：

1. `agent-api`
对外接口层，提供聊天、会话、知识库检索、调试入口。  
接口建议统一 `POST`。

2. `agent-core`
核心领域模型，放 `Session`、`Message`、`Action`、`Tool`、`Memory`、`Chunk` 这些抽象。

3. `agent-runtime`
运行时编排层，负责一轮对话的执行循环：
`加载会话 -> 构建上下文 -> 模型决策 -> 工具调用 -> 检索记忆 -> 输出结果 -> 持久化`

4. `agent-llm`
模型适配层，统一封装大模型调用，屏蔽不同厂商差异。

5. `agent-memory`
记忆与检索层，负责短期记忆、摘要记忆、向量召回、知识片段拼装。

6. `agent-tool`
工具体系，负责工具注册、参数校验、执行、超时、异常日志。

7. `agent-infra`
基础设施层，放 PostgreSQL、pgvector、Redis、对象存储、异步任务、trace 等实现。

如果你是单体项目，也可以先只做包结构，不急着拆 Maven 多模块。

**推荐包结构**
```text
com.xxx.agent
  api
    controller
    dto
  application
    service
    orchestrator
  domain
    session
    message
    action
    tool
    memory
    knowledge
  runtime
    runner
    planner
    context
    executor
  infrastructure
    persistence
      entity
      mapper
      repository
    llm
    vector
    config
    client
  common
    enums
    utils
    exception
```

**核心流程**
一轮 Agent 执行建议固定成下面这条链路：

1. 接收用户请求
2. 加载 `session`
3. 读取最近消息和摘要记忆
4. 如果需要，检索知识库和长期记忆
5. 构建模型上下文
6. 模型输出结构化 `action`
7. 若为工具调用，则执行工具并记录结果
8. 再次进入模型判断，直到 `FINISH`
9. 保存消息、动作、工具日志、上下文摘要
10. 返回响应

建议把模型输出收敛成统一动作：

- `RESPOND`
- `TOOL_CALL`
- `RETRIEVE`
- `ASK_USER`
- `FINISH`

---

**库表设计**
下面这版够你做第一版 Agent 框架，而且后面接 RAG 很顺。

**1. agent_session**
存会话主信息。

```sql
CREATE TABLE agent_session (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL UNIQUE,
    user_id VARCHAR(64) NOT NULL,
    session_name VARCHAR(255),
    agent_code VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    current_goal TEXT,
    summary TEXT,
    last_message_at TIMESTAMP,
    ext JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

字段建议：
- `session_id`：对外会话号
- `agent_code`：后面支持多种 agent
- `summary`：历史摘要，控制上下文长度
- `ext`：预留元数据

**2. agent_message**
存会话消息。

```sql
CREATE TABLE agent_message (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL,
    message_role VARCHAR(32) NOT NULL,
    message_type VARCHAR(32) NOT NULL,
    content TEXT NOT NULL,
    tool_name VARCHAR(128),
    tool_call_id VARCHAR(64),
    reply_to_id BIGINT,
    tokens_used INT,
    ext JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

建议：
- `message_role`：`system/user/assistant/tool`
- `message_type`：`text/action/tool_result/summary`
- `reply_to_id`：关联上一条工具调用或消息
- `ext`：保存结构化动作原文、模型返回片段等

索引建议：
```sql
CREATE INDEX idx_agent_message_session_id ON agent_message(session_id);
CREATE INDEX idx_agent_message_session_time ON agent_message(session_id, created_at DESC);
```

**3. agent_action_log**
存每一步决策动作，方便排障。

```sql
CREATE TABLE agent_action_log (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL,
    trace_id VARCHAR(64) NOT NULL,
    step_no INT NOT NULL,
    action_type VARCHAR(32) NOT NULL,
    action_reason TEXT,
    tool_name VARCHAR(128),
    tool_args JSONB,
    action_result TEXT,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

这个表非常重要。后面你排查“为什么 agent 会这样回答”基本靠它。

**4. agent_tool_log**
存工具执行日志。

```sql
CREATE TABLE agent_tool_log (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL,
    trace_id VARCHAR(64) NOT NULL,
    tool_name VARCHAR(128) NOT NULL,
    tool_args JSONB,
    success BOOLEAN NOT NULL,
    output_text TEXT,
    error_message TEXT,
    latency_ms INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

建议记录：
- 工具参数
- 执行结果
- 错误信息
- 耗时

**5. knowledge_document**
存知识文档主表。

```sql
CREATE TABLE knowledge_document (
    id BIGSERIAL PRIMARY KEY,
    knowledge_code VARCHAR(64) NOT NULL,
    doc_name VARCHAR(255) NOT NULL,
    source_type VARCHAR(32) NOT NULL,
    source_uri TEXT,
    content_text TEXT,
    status VARCHAR(32) NOT NULL,
    ext JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

**6. knowledge_chunk**
存文档切片和向量。

先启用扩展：

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

建表：

```sql
CREATE TABLE knowledge_chunk (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    knowledge_code VARCHAR(64) NOT NULL,
    chunk_no INT NOT NULL,
    chunk_text TEXT NOT NULL,
    chunk_tokens INT,
    metadata JSONB,
    embedding VECTOR(1536),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

如果你后面模型 embedding 维度不是 `1536`，这里改成对应维度，比如 `768`、`1024`、`3072`。

索引建议：
```sql
CREATE INDEX idx_knowledge_chunk_document_id ON knowledge_chunk(document_id);
CREATE INDEX idx_knowledge_chunk_knowledge_code ON knowledge_chunk(knowledge_code);
```

向量索引建议后面数据量起来再加。比如：

```sql
CREATE INDEX idx_knowledge_chunk_embedding_cosine
ON knowledge_chunk
USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);
```

注意：
- `ivfflat` 适合有一定数据量后使用
- 小数据量阶段可以先不建向量索引

**7. memory_fact**
存长期记忆，和知识库分开。

```sql
CREATE TABLE memory_fact (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    session_id VARCHAR(64),
    memory_type VARCHAR(32) NOT NULL,
    fact_text TEXT NOT NULL,
    importance_score NUMERIC(5,2) DEFAULT 0,
    metadata JSONB,
    embedding VECTOR(1536),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

这里适合存：
- 用户偏好
- 已确认事实
- 历史任务结论
- 持久化上下文信息

索引建议：
```sql
CREATE INDEX idx_memory_fact_user_id ON memory_fact(user_id);
CREATE INDEX idx_memory_fact_session_id ON memory_fact(session_id);
```

**8. agent_prompt_template**
存 prompt 模板，方便热更新。

```sql
CREATE TABLE agent_prompt_template (
    id BIGSERIAL PRIMARY KEY,
    agent_code VARCHAR(64) NOT NULL,
    prompt_type VARCHAR(32) NOT NULL,
    prompt_name VARCHAR(128) NOT NULL,
    prompt_content TEXT NOT NULL,
    version_no INT NOT NULL DEFAULT 1,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

适合存：
- system prompt
- planner prompt
- summary prompt
- retrieval rewrite prompt

---

**表关系建议**
关系可以这样理解：

- `agent_session` 1 对多 `agent_message`
- `agent_session` 1 对多 `agent_action_log`
- `agent_session` 1 对多 `agent_tool_log`
- `knowledge_document` 1 对多 `knowledge_chunk`
- `memory_fact` 归属 `user_id` 或 `session_id`

---

**Java 核心类设计**
推荐优先定这几个核心接口。

**1. AgentRunner**
```java
public interface AgentRunner {
    AgentChatResult run(AgentChatCommand command);
}
```

职责：
- 一轮完整执行
- 控制循环次数
- 汇总最终输出

**2. Planner**
```java
public interface Planner {
    AgentAction decide(ExecutionContext context);
}
```

职责：
- 基于上下文决定下一步动作
- 输出结构化 `AgentAction`

**3. ContextBuilder**
```java
public interface ContextBuilder {
    AgentPromptContext build(ExecutionContext context);
}
```

职责：
- 拼系统提示词
- 拼会话历史
- 拼知识召回结果
- 控制上下文长度

**4. ToolRegistry / ToolExecutor**
```java
public interface Tool {
    String name();
    ToolResult execute(String argumentsJson, ExecutionContext context);
}
```

职责：
- 工具注册
- 参数校验
- 超时和异常封装

**5. RetrievalService**
```java
public interface RetrievalService {
    List<RetrievedChunk> searchKnowledge(String query, String knowledgeCode, int topK);
}
```

职责：
- embedding 生成
- 向量检索
- 返回 chunk

**6. MemoryService**
```java
public interface MemoryService {
    List<MemoryFact> recall(String userId, String query, int topK);
    void saveFact(MemoryFact fact);
    String summarizeSession(String sessionId);
}
```

---

**建议的实体边界**
领域对象和表实体分开，不要直接把数据库 Entity 到处传。

建议分三层：
- `Entity`：数据库对象
- `Domain`：业务核心对象
- `DTO`：接口出入参

这样后面库表调整，不会把上层全带崩。

---

**RAG 检索链路设计**
后面你做知识库时，建议检索链路固定为：

1. 用户问题改写
2. 生成 query embedding
3. 按 `knowledge_code` 过滤
4. 向量召回 topK
5. 可选重排
6. 拼接 chunk 进入上下文
7. 模型回答并引用来源

第一版先不做重排器也可以。

`PostgreSQL + pgvector` 的基础查询一般像这样：

```sql
SELECT id, chunk_text
FROM knowledge_chunk
WHERE knowledge_code = #{knowledgeCode}
ORDER BY embedding <=> #{queryEmbedding}
LIMIT 5;
```

如果用余弦距离，`<=>` 很常见；具体也要看你选的操作符和索引方式。

---

**接口建议**
按你的约束，统一 `POST` 比较合适：

1. `POST /agent/chat`
聊天主入口

2. `POST /agent/session/history`
查历史消息

3. `POST /agent/knowledge/import`
导入知识文档

4. `POST /agent/knowledge/search`
调试检索结果

5. `POST /agent/memory/query`
调试长期记忆召回

6. `POST /agent/tool/execute`
工具调试入口

如果你有 controller 层规范，后面实际落代码时可以统一补：
- `swagger` 注解
- 删除/保存/编辑类接口的日志注解

---

**第一版最值得先做的 5 个能力**
1. `POST /agent/chat`
先打通主链路

2. 会话持久化
至少能保存 session 和 message

3. 工具调用
先做 2 到 3 个简单工具

4. 摘要记忆
控制上下文长度

5. 知识库召回
先支持一个 `knowledge_code`

---

**一些很关键的取舍**
1. `Session summary` 一定要有  
否则多轮会话很快爆 token。

2. `knowledge_chunk` 和 `memory_fact` 分开  
知识库是外部资料，长期记忆是 agent 自己沉淀，两者生命周期不一样。

3. 动作日志和工具日志不要省  
这不是“可选优化”，是 agent 可维护性的底座。

4. 先单 Agent，别急多 Agent  
数据库一旦围绕多 Agent 过早设计，会把简单问题搞复杂。

---

**我建议你的第一版数据库就这么定**
如果你现在就准备开工，我建议第一批只建这 6 张表：

- `agent_session`
- `agent_message`
- `agent_action_log`
- `agent_tool_log`
- `knowledge_document`
- `knowledge_chunk`

等你做长期记忆时，再补：
- `memory_fact`
- `agent_prompt_template`

这样节奏最稳。

如果你愿意，我下一步可以继续给你一版更落地的内容：  
直接出 `Spring Boot` 里的 `DDD 风格包结构 + 核心类清单 + chat 主链路时序图 + 建表 SQL 初稿`。