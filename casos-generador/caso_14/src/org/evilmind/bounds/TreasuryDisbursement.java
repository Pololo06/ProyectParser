package org.evilmind.bounds;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public final class TreasuryDisbursement implements FinancialValuable, AuditableTransaction {

    private final Long transactionSequence;
    private final BigDecimal netAssetValue;
    private final OffsetDateTime executionTime;

    public TreasuryDisbursement(Long transactionSequence, BigDecimal netAssetValue, OffsetDateTime executionTime) {
        this.transactionSequence = transactionSequence;
        this.netAssetValue = netAssetValue;
        this.executionTime = executionTime;
    }

    @Override
    public Long getTransactionSequence() {
        return transactionSequence;
    }

    @Override
    public BigDecimal getNetAssetValue() {
        return netAssetValue;
    }

    @Override
    public OffsetDateTime getExecutionTime() {
        return executionTime;
    }
}
