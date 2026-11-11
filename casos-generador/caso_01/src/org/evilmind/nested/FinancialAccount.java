package org.evilmind.nested;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class FinancialAccount {

    private Long id;
    private String accountNumber;
    private BigDecimal currentBalance;
    private OffsetDateTime openingDate;
    private Short accountStatus;

    public FinancialAccount(Long id, String accountNumber, BigDecimal currentBalance, OffsetDateTime openingDate, Short accountStatus) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.currentBalance = currentBalance;
        this.openingDate = openingDate;
        this.accountStatus = accountStatus;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public OffsetDateTime getOpeningDate() {
        return openingDate;
    }

    public Short getAccountStatus() {
        return accountStatus;
    }

    public LedgerEntry createEntry(BigDecimal amount, Short entryType) {
        return null;
    }

    public class LedgerEntry {

        private Long entryId;
        private BigDecimal transactionAmount;
        private OffsetDateTime registeredAt;
        private Short entryTypeCode;
        private Boolean isAudited;

        public LedgerEntry(Long entryId, BigDecimal transactionAmount, OffsetDateTime registeredAt, Short entryTypeCode, Boolean isAudited) {
            this.entryId = entryId;
            this.transactionAmount = transactionAmount;
            this.registeredAt = registeredAt;
            this.entryTypeCode = entryTypeCode;
            this.isAudited = isAudited;
        }

        public Long getEntryId() {
            return entryId;
        }

        public BigDecimal getTransactionAmount() {
            return transactionAmount;
        }

        public OffsetDateTime getRegisteredAt() {
            return registeredAt;
        }

        public Short getEntryTypeCode() {
            return entryTypeCode;
        }

        public Boolean getIsAudited() {
            return isAudited;
        }

        public BigDecimal computeProjectedBalance() {
            return null;
        }
    }
}
