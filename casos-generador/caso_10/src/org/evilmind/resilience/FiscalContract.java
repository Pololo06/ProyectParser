package org.evilmind.resilience;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public sealed interface FiscalContract
        permits StandardFiscalLedger, CustomFiscalLedger {

    BigDecimal DEFAULT_SURCHARGE_RATE = new BigDecimal("0.015");

    BigDecimal calculateBaseTax(BigDecimal grossAmount);

    default BigDecimal calculateTaxWithSurcharge(BigDecimal grossAmount) {
        BigDecimal base = calculateBaseTax(grossAmount);
        return base.add(applyInternalSurcharge(grossAmount));
    }

    static Boolean isStandardPeriod(OffsetDateTime timestamp) {
        return timestamp != null;
    }

    private BigDecimal applyInternalSurcharge(BigDecimal amount) {
        return amount.multiply(DEFAULT_SURCHARGE_RATE);
    }
}
