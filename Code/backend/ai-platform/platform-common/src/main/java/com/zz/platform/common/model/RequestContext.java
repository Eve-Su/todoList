package com.zz.platform.common.model;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String traceId;
    private String tenantId;
    private String userId;
}
