package org.evilmind.records;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record FinancialAuditRecord(
        Long auditId,
        String trackingToken,
        BigDecimal grossAmount,
        BigDecimal taxAmount,
        OffsetDateTime auditTimestamp,
        Short riskLevelCode
        ) {

    public static final String COMPLIANCE_VERSION = "ISO-20022";

    public BigDecimal calculateNetAmount() {
        return null;
    }

    public Boolean requiresManualReview() {
        return null;
    }

    public static Boolean isValidRiskCode(Short code) {
        return null;
    }
}
