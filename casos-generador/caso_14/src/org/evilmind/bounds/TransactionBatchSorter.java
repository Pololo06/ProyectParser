package org.evilmind.bounds;

import java.math.BigDecimal;

public class TransactionBatchSorter<T extends FinancialValuable & AuditableTransaction> {

    private final Short batchPriority;

    public TransactionBatchSorter(Short batchPriority) {
        this.batchPriority = batchPriority;
    }

    public Short getBatchPriority() {
        return batchPriority;
    }

    public BigDecimal calculateWeightedValue(T item) {
        BigDecimal value = item.getNetAssetValue();
        return value != null ? value : BigDecimal.ZERO;
    }

    public Long resolveSequence(T item) {
        return item.getTransactionSequence();
    }
}
