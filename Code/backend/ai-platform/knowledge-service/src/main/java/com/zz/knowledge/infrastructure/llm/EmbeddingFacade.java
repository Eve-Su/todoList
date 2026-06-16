package com.zz.knowledge.infrastructure.llm;

public interface EmbeddingFacade {

    float[] embed(String text);
}
