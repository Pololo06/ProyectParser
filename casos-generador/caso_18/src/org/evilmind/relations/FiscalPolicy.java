package org.evilmind.relations;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class FiscalPolicy {

    private final Long policyId;
    private final BigDecimal defaultTaxRate;
    private final OffsetDateTime effectiveFrom;

    public FiscalPolicy(Long policyId, BigDecimal defaultTaxRate, OffsetDateTime effectiveFrom) {
        this.policyId = policyId;
        this.defaultTaxRate = defaultTaxRate;
        this.effectiveFrom = effectiveFrom;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public BigDecimal getDefaultTaxRate() {
        return defaultTaxRate;
    }

    public OffsetDateTime getEffectiveFrom() {
        return effectiveFrom;
    }
}
