建议你把它做成 `Spring Boot + 分层内核 + 插件式工具体系`，第一版先像“单线程 agent runtime”，不要一开始就卷工作流引擎、多 agent 和复杂记忆系统。对 Java 来说，最重要的是把接口抽稳，把状态和执行链做清楚。

**总体架构**
建议分 6 层：

1. `api`
暴露聊天接口、会话接口、调试接口。

2. `application`
编排 agent 执行流程，负责一轮对话怎么跑完。

3. `domain`
放核心抽象：`Agent`、`Tool`、`PlanStep`、`Session`、`Message`、`Action`。

4. `infrastructure`
接模型、数据库、Redis、向量库、日志、外部 HTTP 工具。

5. `memory`
管理短期记忆、摘要记忆、长期记忆。

6. `runtime`
负责回合循环、工具调度、上下文构建、异常恢复。

**推荐目录**
```text
src/main/java/com/yourcompany/agent/
  api/
    controller/
    dto/
  application/
    service/
    orchestrator/
  domain/
    agent/
    session/
    message/
    tool/
    action/
    memory/
  runtime/
    runner/
    planner/
    context/
    executor/
  infrastructure/
    llm/
    persistence/
    vector/
    config/
    client/
  common/
    exception/
    utils/
    constant/
```

**核心对象先定死**
第一版至少有这些类：

```java
AgentSession
- sessionId
- userId
- status
- currentGoal
- messages
- memorySummary
- metadata

AgentMessage
- role
- content
- toolCalls
- toolResult
- timestamp

AgentAction
- type           // RESPOND, TOOL_CALL, ASK_USER, FINISH
- toolName
- toolArgsJson
- finalAnswer
- reason

ToolDefinition
- name
- description
- inputSchema
- timeoutMs

ToolExecutionResult
- success
- output
- errorMessage
- rawData

ExecutionContext
- session
- availableTools
- requestId
- variables
```

这里最关键的是 `AgentAction`。  
不要让模型自由说一大段文本后你再猜它要干嘛，而是强制它输出结构化动作。

**执行主链路**
推荐把一轮执行统一收口在一个 `AgentRunner` 里：

```text
Controller
 -> AgentApplicationService
 -> AgentRunner.runTurn()
    -> SessionStore.load()
    -> ContextBuilder.build()
    -> Planner.decide()
    -> ToolExecutor.execute()   // if needed
    -> Planner.decide()         // continue if needed
    -> SessionStore.save()
 -> return response
```

对应到 Java 类，大概是：

- `AgentController`
- `AgentApplicationService`
- `AgentRunner`
- `Planner`
- `ContextBuilder`
- `ToolRegistry`
- `ToolExecutor`
- `SessionRepository`
- `MemoryService`
- `LlmClient`

**最小接口设计**
这几个接口建议一开始就抽出来：

```java
public interface LlmClient {
    String chat(List<LlmMessage> messages);
}

public interface Planner {
    AgentAction decide(ExecutionContext context);
}

public interface Tool {
    String name();
    ToolExecutionResult execute(String argumentsJson, ExecutionContext context);
}

public interface ToolRegistry {
    Optional<Tool> getTool(String toolName);
    List<ToolDefinition> listTools();
}

public interface SessionRepository {
    AgentSession findBySessionId(String sessionId);
    void save(AgentSession session);
}

public interface MemoryService {
    String buildSummary(AgentSession session);
}
```

这样后面你换模型、换存储、换工具实现都不会伤筋动骨。

**Spring Boot 里怎么组织最顺**
建议把工具做成 Spring Bean 自动注册：

```java
@Component
public class TimeTool implements Tool {
    @Override
    public String name() {
        return "time_query";
    }

    @Override
    public ToolExecutionResult execute(String argumentsJson, ExecutionContext context) {
        return ToolExecutionResult.success(LocalDateTime.now().toString());
    }
}
```

然后在 `ToolRegistry` 里统一收集：

```java
@Component
public class DefaultToolRegistry implements ToolRegistry {

    private final Map<String, Tool> toolMap;

    public DefaultToolRegistry(List<Tool> tools) {
        this.toolMap = tools.stream().collect(Collectors.toMap(Tool::name, t -> t));
    }

    @Override
    public Optional<Tool> getTool(String toolName) {
        return Optional.ofNullable(toolMap.get(toolName));
    }
}
```

这套在 Spring Boot 里非常顺，扩一个工具几乎零心智负担。

**Planner 怎么做**
第一版不要单独搞复杂 AI Planner，直接做成 `LLM + JSON Action 输出`：

模型输出固定结构：

```json
{
  "type": "TOOL_CALL",
  "toolName": "search_weather",
  "toolArgs": {
    "city": "Shanghai"
  },
  "reason": "Need current weather before answering"
}
```

你在 Java 里用 `Jackson` 反序列化成 `AgentAction`。  
如果解析失败，就走兜底逻辑，比如：

- 重试一次
- 要求模型重新输出 JSON
- 或直接返回“本轮解析失败”

**上下文构建一定要单独做**
不要把 prompt 拼接散落在各处。单独搞一个 `ContextBuilder`：

```java
public interface ContextBuilder {
    List<LlmMessage> build(ExecutionContext context);
}
```

上下文建议包含：

1. 系统指令
2. 当前用户问题
3. 最近几轮消息
4. 历史摘要
5. 可用工具定义
6. 当前工具结果
7. 本轮目标

以后你做消息裁剪、摘要压缩、RAG 注入，全在这里扩。

**记忆系统怎么分**
第一版只做两层就够：

1. `Short-term Memory`
存在会话里，保存最近 N 轮消息。

2. `Summary Memory`
当消息太长时，定期把旧消息总结成摘要，替代原始长历史。

先别急着做向量记忆。  
很多人上来就接 Milvus / pgvector，最后发现 agent 连基本回合控制都没做好。

**会话存储建议**
第一版推荐：

- `MySQL` 存会话、消息、执行日志
- `Redis` 做热点上下文缓存
- 后续需要再加向量库

表可以至少有：

- `agent_session`
- `agent_message`
- `agent_execution_log`

如果你后面真要落库，按你的规则，表建议统一：
`ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci`

**工具执行层要重点做好**
`ToolExecutor` 不只是调一下方法，至少要带：

- 参数校验
- 超时控制
- 异常捕获
- 执行日志
- 权限检查
- 幂等控制

建议工具参数定义成 schema，哪怕第一版只是 Java Bean 校验也行。

**接口设计建议**
按你偏好的规范，如果是对话入口，建议统一 `POST`：

- `POST /agent/chat`
- `POST /agent/session/create`
- `POST /agent/session/history`
- `POST /agent/tool/debug`

请求体示例：

```json
{
  "sessionId": "s123",
  "userId": "u1001",
  "message": "帮我查一下今天上海天气"
}
```

响应体：

```json
{
  "sessionId": "s123",
  "answer": "今天上海多云，气温 24-29 度",
  "actionType": "FINISH",
  "traceId": "trace-xxx"
}
```

**第一版开发顺序**
这是最稳的顺序：

1. 搭 `Spring Boot` 基础工程
2. 定义 `Session / Message / Action / Tool` 核心模型
3. 实现 `ToolRegistry`
4. 实现 `LlmClient`
5. 实现 `ContextBuilder`
6. 实现 `Planner`
7. 实现 `AgentRunner`
8. 接 `Controller`
9. 落 `MySQL` 持久化
10. 增加日志、超时、重试

**第一版不要做的东西**
这些很容易把项目拖死：

1. 多 agent 自治协作
2. 图工作流引擎
3. 复杂权限系统
4. 长链路反思机制
5. 全量 RAG
6. 自动任务编排 DSL

先把“用户问一句，agent 能稳定做判断、调工具、返回答案、保存状态”跑通，已经很强了。

**最值得你提前留扩展点的地方**
1. `LlmClient`
后面可能接 OpenAI、阿里、DeepSeek、本地模型。

2. `Tool`
后面你会不断加新工具。

3. `SessionRepository`
后面可能从单库切到 Redis + DB。

4. `MemoryService`
后面可能接摘要模型和向量检索。

5. `Planner`
后面你可能从单轮判断升级到 plan-and-execute。

**一个很实用的版本路线**
你可以按这三期推进：

1. `v0.1`
单 agent + 单模型 + 本地工具 + MySQL 会话存储

2. `v0.2`
支持摘要记忆、工具超时、执行日志、traceId

3. `v0.3`
支持 RAG、插件式工具包、异步任务、人工接管

如果你愿意，我下一步可以直接给你出一版 `Java + Spring Boot` 的最小骨架设计，粒度会到：

1. Maven 依赖
2. 包结构
3. 核心类清单
4. 关键接口代码
5. `POST /agent/chat` 的完整时序

如果你想继续，我建议我下一条就直接给你“可开工的项目骨架”。