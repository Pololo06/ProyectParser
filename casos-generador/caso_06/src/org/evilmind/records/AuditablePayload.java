package org.evilmind.records;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface AuditablePayload {

    BigDecimal MIN_WEIGHT_THRESHOLD = new BigDecimal("0.05");
    Short SCHEMA_REVISION = (short) 2;

    OffsetDateTime capturedAt();

    Short payloadVersion();

    BigDecimal evaluateWeight();
}
