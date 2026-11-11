package org.evilmind.api.contracts;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class ExecutionRequest {

    private final String requestId;
    private final BigDecimal requestedAmount;
    private final OffsetDateTime requestTime;

    public ExecutionRequest(String requestId, BigDecimal requestedAmount, OffsetDateTime requestTime) {
        this.requestId = requestId;
        this.requestedAmount = requestedAmount;
        this.requestTime = requestTime;
    }

    public String getRequestId() {
        return requestId;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public OffsetDateTime getRequestTime() {
        return requestTime;
    }
}
