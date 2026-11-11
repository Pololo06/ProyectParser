package org.evilmind.records;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionEnvelope<K, V>(
        Long envelopeId,
        K routingKey,
        V payloadData,
        BigDecimal commissionAmount,
        OffsetDateTime capturedAt,
        Short payloadVersion
        ) implements AuditablePayload {

    public static final BigDecimal BASE_SURCHARGE = new BigDecimal("15.75");
    public static final Short DEFAULT_PARTITION_KEY = (short) 100;

    @Override
    public BigDecimal evaluateWeight() {
        return null;
    }

    public V extractPayload() {
        return payloadData;
    }
}
