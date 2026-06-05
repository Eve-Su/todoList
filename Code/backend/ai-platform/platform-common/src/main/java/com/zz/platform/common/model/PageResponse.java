package com.zz.platform.common.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private long total;
    private long pageNo;
    private long pageSize;
    private List<T> records;

    public static <T> PageResponse<T> empty(long pageNo, long pageSize) {
        return PageResponse.<T>builder()
                .total(0)
                .pageNo(pageNo)
                .pageSize(pageSize)
                .records(Collections.emptyList())
                .build();
    }
}
