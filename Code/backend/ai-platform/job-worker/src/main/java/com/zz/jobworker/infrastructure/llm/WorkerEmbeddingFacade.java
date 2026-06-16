package com.zz.jobworker.infrastructure.llm;

public interface WorkerEmbeddingFacade {

    float[] embed(String text);
}
