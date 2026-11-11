package org.evilmind.records;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

public record SettlementBatchRecord(
        Long batchId,
        String batchReference,
        BigDecimal totalBatchAmount,
        OffsetDateTime processingCutoff,
        Short priorityLevel
        ) {

    public SettlementBatchRecord {
        Objects.requireNonNull(batchId, "batchId no puede ser nulo");
        Objects.requireNonNull(totalBatchAmount, "totalBatchAmount no puede ser nulo");

        if (priorityLevel == null) {
            priorityLevel = (short) 1;
        }
    }

    public SettlementBatchRecord(Long batchId, String batchReference, BigDecimal totalBatchAmount) {
        this(batchId,
                batchReference,
                totalBatchAmount,
                OffsetDateTime.now(), (short) 1);
    }

    public Boolean isHighPriority() {
        return priorityLevel != null && priorityLevel.compareTo((short) 5) >= 0;
    }
}
