package com.zz.jobworker.infrastructure.llm;

import com.zz.platform.common.util.VectorUtil;
import org.springframework.stereotype.Component;

@Component
public class PlaceholderWorkerEmbeddingFacade implements WorkerEmbeddingFacade {

    @Override
    public float[] embed(String text) {
        return VectorUtil.placeholderEmbedding(text);
    }
}
