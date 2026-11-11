package org.evilmind.relations;

import java.math.BigDecimal;

public class VoucherEntry {

    private final Long entryId;
    private final String accountCode;
    private final BigDecimal creditAmount;
    private final Short entrySequence;

    public VoucherEntry(Long entryId, String accountCode, BigDecimal creditAmount, Short entrySequence) {
        this.entryId = entryId;
        this.accountCode = accountCode;
        this.creditAmount = creditAmount;
        this.entrySequence = entrySequence;
    }

    public Long getEntryId() {
        return entryId;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    public Short getEntrySequence() {
        return entrySequence;
    }
}
