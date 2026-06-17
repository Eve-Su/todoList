-- knowledge-service owns schema knowledge.
-- pgvector is installed in public schema, so vector columns are declared as public.vector.

CREATE EXTENSION IF NOT EXISTS vector WITH SCHEMA public;

CREATE SCHEMA IF NOT EXISTS knowledge;

CREATE TABLE IF NOT EXISTS knowledge.knowledge_base (
    id BIGSERIAL PRIMARY KEY,
    kb_code VARCHAR(64) NOT NULL UNIQUE,
    kb_name VARCHAR(128) NOT NULL,
    tenant_id VARCHAR(64),
    embedding_model_code VARCHAR(64),
    top_k INT NOT NULL DEFAULT 5,
    status VARCHAR(32) NOT NULL,
    permission_scope_json JSONB,
    ext_json JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_knowledge_base_tenant_id ON knowledge.knowledge_base(tenant_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_base_status ON knowledge.knowledge_base(status);

CREATE TABLE IF NOT EXISTS knowledge.knowledge_document (
    id BIGSERIAL PRIMARY KEY,
    kb_code VARCHAR(64) NOT NULL,
    doc_code VARCHAR(64) NOT NULL UNIQUE,
    doc_name VARCHAR(255) NOT NULL,
    source_type VARCHAR(32) NOT NULL,
    source_uri TEXT,
    content_text TEXT,
    status VARCHAR(32) NOT NULL,
    version_no INT NOT NULL DEFAULT 1,
    ext_json JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_knowledge_document_kb_code ON knowledge.knowledge_document(kb_code);
CREATE INDEX IF NOT EXISTS idx_knowledge_document_status ON knowledge.knowledge_document(status);
CREATE INDEX IF NOT EXISTS idx_knowledge_document_created_at ON knowledge.knowledge_document(created_at DESC);

CREATE TABLE IF NOT EXISTS knowledge.knowledge_chunk (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    kb_code VARCHAR(64) NOT NULL,
    chunk_no INT NOT NULL,
    chunk_text TEXT NOT NULL,
    chunk_tokens INT,
    metadata_json JSONB,
    embedding public.vector(1536),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_knowledge_chunk_document_id ON knowledge.knowledge_chunk(document_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_chunk_kb_code ON knowledge.knowledge_chunk(kb_code);
CREATE UNIQUE INDEX IF NOT EXISTS uk_knowledge_chunk_doc_no ON knowledge.knowledge_chunk(document_id, chunk_no);
CREATE INDEX IF NOT EXISTS idx_knowledge_chunk_embedding_ivfflat
    ON knowledge.knowledge_chunk USING ivfflat (embedding public.vector_cosine_ops)
    WITH (lists = 100);

CREATE TABLE IF NOT EXISTS knowledge.knowledge_import_task (
    id BIGSERIAL PRIMARY KEY,
    task_id VARCHAR(64) NOT NULL UNIQUE,
    kb_code VARCHAR(64) NOT NULL,
    doc_code VARCHAR(64) NOT NULL,
    task_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    error_message TEXT,
    payload_json JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_knowledge_import_task_kb_code ON knowledge.knowledge_import_task(kb_code);
CREATE INDEX IF NOT EXISTS idx_knowledge_import_task_doc_code ON knowledge.knowledge_import_task(doc_code);
CREATE INDEX IF NOT EXISTS idx_knowledge_import_task_status ON knowledge.knowledge_import_task(status);
CREATE INDEX IF NOT EXISTS idx_knowledge_import_task_created_at ON knowledge.knowledge_import_task(created_at DESC);
