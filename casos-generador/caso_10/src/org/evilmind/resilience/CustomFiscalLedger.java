package org.evilmind.resilience;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public non-sealed class CustomFiscalLedger implements FiscalContract {

    private final BigDecimal customMultiplier;
    private final OffsetDateTime registeredDate;

    public CustomFiscalLedger(BigDecimal customMultiplier, OffsetDateTime registeredDate) {
        this.customMultiplier = customMultiplier;
        this.registeredDate = registeredDate;
    }

    @Override
    public BigDecimal calculateBaseTax(BigDecimal grossAmount) {
        return grossAmount.multiply(customMultiplier);
    }

    public BigDecimal getCustomMultiplier() {
        return customMultiplier;
    }

    public OffsetDateTime getRegisteredDate() {
        return registeredDate;
    }
}
