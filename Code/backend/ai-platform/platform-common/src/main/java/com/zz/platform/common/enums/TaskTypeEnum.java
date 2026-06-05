package com.zz.platform.common.enums;

import lombok.Getter;

@Getter
public enum TaskTypeEnum {

    DOC_IMPORT("DOC_IMPORT", "文档导入"),
    EMBEDDING_BUILD("EMBEDDING_BUILD", "向量构建"),
    APPROVAL_APPROVED("APPROVAL_APPROVED", "审批通过"),
    APPROVAL_REJECTED("APPROVAL_REJECTED", "审批驳回"),
    APPROVAL_RESUME("APPROVAL_RESUME", "审批恢复"),
    WORKFLOW_ASYNC_NODE("WORKFLOW_ASYNC_NODE", "流程异步节点"),
    WORKFLOW_RESUME("WORKFLOW_RESUME", "流程恢复"),
    TOOL_ASYNC_EXECUTE("TOOL_ASYNC_EXECUTE", "工具异步执行");

    private final String code;
    private final String desc;

    TaskTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
