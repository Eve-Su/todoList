package com.zz.knowledge.infrastructure.llm;

import com.zz.platform.common.util.VectorUtil;
import org.springframework.stereotype.Component;

@Component
public class PlaceholderEmbeddingFacade implements EmbeddingFacade {

    @Override
    public float[] embed(String text) {
        return VectorUtil.placeholderEmbedding(text);
    }
}
