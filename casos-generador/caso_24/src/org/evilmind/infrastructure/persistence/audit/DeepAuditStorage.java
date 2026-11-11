package org.evilmind.infrastructure.persistence.audit;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class DeepAuditStorage {

    private final Long storagePartitionId;
    private OffsetDateTime lastFlushedTimestamp;

    public DeepAuditStorage(Long storagePartitionId) {
        this.storagePartitionId = storagePartitionId;
        this.lastFlushedTimestamp = OffsetDateTime.now();
    }

    public Long getStoragePartitionId() {
        return storagePartitionId;
    }

    public OffsetDateTime getLastFlushedTimestamp() {
        return lastFlushedTimestamp;
    }

    public Boolean persistLog(String transactionToken, BigDecimal amount) {
        this.lastFlushedTimestamp = OffsetDateTime.now();
        return Boolean.TRUE;
    }
}
