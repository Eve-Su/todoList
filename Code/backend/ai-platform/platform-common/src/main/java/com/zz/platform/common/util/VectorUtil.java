package com.zz.platform.common.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class VectorUtil {

    public static final int DEFAULT_DIMENSION = 1536;

    private VectorUtil() {
    }

    public static float[] placeholderEmbedding(String text) {
        float[] vector = new float[DEFAULT_DIMENSION];
        byte[] bytes = text == null ? new byte[0] : text.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < bytes.length; i++) {
            int index = i % DEFAULT_DIMENSION;
            vector[index] += (bytes[i] & 0xff) / 255.0F;
        }
        normalize(vector);
        return vector;
    }

    public static String toPgVector(float[] vector) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(vector[i]);
        }
        return builder.append(']').toString();
    }

    private static void normalize(float[] vector) {
        double sum = 0;
        for (float value : vector) {
            sum += value * value;
        }
        if (sum == 0) {
            Arrays.fill(vector, 0);
            vector[0] = 1.0F;
            return;
        }
        double norm = Math.sqrt(sum);
        for (int i = 0; i < vector.length; i++) {
            vector[i] = (float) (vector[i] / norm);
        }
    }
}
