package org.evilmind.relations;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class AuditVoucher {

    private final Long voucherId;
    private final OffsetDateTime voucherDate;
    private final BigDecimal totalAmount;
    private final VoucherEntry primaryEntry;
    private final List<VoucherEntry> voucherEntries;

    public AuditVoucher(Long voucherId, OffsetDateTime voucherDate, BigDecimal totalAmount, VoucherEntry primaryEntry, List<VoucherEntry> voucherEntries) {
        this.voucherId = voucherId;
        this.voucherDate = voucherDate;
        this.totalAmount = totalAmount;
        this.primaryEntry = primaryEntry;
        this.voucherEntries = voucherEntries;
    }

    public Boolean verifyEntryConsistency(VoucherEntry externalCandidate) {
        return externalCandidate != null && externalCandidate.getCreditAmount() != null;
    }

    public Long getVoucherId() {
        return voucherId;
    }

    public OffsetDateTime getVoucherDate() {
        return voucherDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public VoucherEntry getPrimaryEntry() {
        return primaryEntry;
    }

    public List<VoucherEntry> getVoucherEntries() {
        return voucherEntries;
    }
}
