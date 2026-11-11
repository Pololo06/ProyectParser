package org.evilmind.bounds;

import java.time.OffsetDateTime;

public interface AuditableTransaction {

    Long getTransactionSequence();

    OffsetDateTime getExecutionTime();
}
