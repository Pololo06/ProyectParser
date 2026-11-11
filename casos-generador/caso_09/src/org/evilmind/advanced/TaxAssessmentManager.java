package org.evilmind.advanced;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TaxAssessmentManager {

    private final Long managerId;
    private final String authorityName;
    private final OffsetDateTime auditTimestamp;
    private final Short jurisdictionTier;

    public TaxAssessmentManager(Long managerId, String authorityName, OffsetDateTime auditTimestamp, Short jurisdictionTier) {
        this.managerId = managerId;
        this.authorityName = authorityName;
        this.auditTimestamp = auditTimestamp;
        this.jurisdictionTier = jurisdictionTier;
    }

    public Long getManagerId() {
        return managerId;
    }

    public String getAuthorityName() {
        return authorityName;
    }

    public OffsetDateTime getAuditTimestamp() {
        return auditTimestamp;
    }

    public Short getJurisdictionTier() {
        return jurisdictionTier;
    }

    public BigDecimal executeAssessment(TaxPayerRecord payer, RateStrategy strategy) {
        return strategy.evaluate(payer);
    }

    public interface TaxRuleContract {

        BigDecimal evaluate(TaxPayerRecord payer);

        Short getRulePriority();
    }

    public record TaxPayerRecord(
            Long taxpayerId,
            String taxCode,
            BigDecimal declaredRevenue,
            OffsetDateTime declaredAt,
            Short complianceStatus
            ) {

        public static final BigDecimal REVENUE_LIMIT = new BigDecimal("10000000.00");

        public Boolean isCorporate() {
            return complianceStatus != null && complianceStatus.compareTo((short) 2) == 0;
        }
    }

    public enum RateStrategy implements TaxRuleContract {

        STANDARD(Short.valueOf((short) 100), new BigDecimal("0.19")) {
            @Override
            public BigDecimal evaluate(TaxPayerRecord payer) {
                return payer.declaredRevenue().multiply(getBaseRate());
            }
        },
        REDUCED(Short.valueOf((short) 200), new BigDecimal("0.05")) {
            @Override
            public BigDecimal evaluate(TaxPayerRecord payer) {
                return payer.declaredRevenue().multiply(getBaseRate());
            }
        },
        EXEMPT(Short.valueOf((short) 300), new BigDecimal("0.00")) {
            @Override
            public BigDecimal evaluate(TaxPayerRecord payer) {
                return BigDecimal.ZERO;
            }
        };

        private final Short rulePriority;
        private final BigDecimal baseRate;

        RateStrategy(Short rulePriority, BigDecimal baseRate) {
            this.rulePriority = rulePriority;
            this.baseRate = baseRate;
        }

        public BigDecimal getBaseRate() {
            return baseRate;
        }

        @Override
        public Short getRulePriority() {
            return rulePriority;
        }
    }
}
