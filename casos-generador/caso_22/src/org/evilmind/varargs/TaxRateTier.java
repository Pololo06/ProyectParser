package org.evilmind.varargs;

import java.math.BigDecimal;

public class TaxRateTier {

    private final Short tierLevel;
    private final BigDecimal rateMultiplier;

    public TaxRateTier(Short tierLevel, BigDecimal rateMultiplier) {
        this.tierLevel = tierLevel;
        this.rateMultiplier = rateMultiplier;
    }

    public Short getTierLevel() {
        return tierLevel;
    }

    public BigDecimal getRateMultiplier() {
        return rateMultiplier;
    }
}
