package org.evilmind.services;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class FiscalTaxpayer {

    private final Long taxpayerId;
    private final String taxIdentificationCode;
    private final BigDecimal declaredIncome;
    private final OffsetDateTime registrationTimestamp;

    public FiscalTaxpayer(Long taxpayerId, String taxIdentificationCode, BigDecimal declaredIncome, OffsetDateTime registrationTimestamp) {
        this.taxpayerId = taxpayerId;
        this.taxIdentificationCode = taxIdentificationCode;
        this.declaredIncome = declaredIncome;
        this.registrationTimestamp = registrationTimestamp;
    }

    public Long getTaxpayerId() {
        return taxpayerId;
    }

    public String getTaxIdentificationCode() {
        return taxIdentificationCode;
    }

    public BigDecimal getDeclaredIncome() {
        return declaredIncome;
    }

    public OffsetDateTime getRegistrationTimestamp() {
        return registrationTimestamp;
    }
}
