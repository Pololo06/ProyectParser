package org.evilmind.services;

import java.math.BigDecimal;

public class TaxCalculationService {

    private final String serviceIdentifier;
    private final BigDecimal statutoryRate;

    public TaxCalculationService(String serviceIdentifier, BigDecimal statutoryRate) {
        this.serviceIdentifier = serviceIdentifier;
        this.statutoryRate = statutoryRate;
    }

    public String getServiceIdentifier() {
        return serviceIdentifier;
    }

    public BigDecimal getStatutoryRate() {
        return statutoryRate;
    }

    public BigDecimal computeTaxLiability(FiscalTaxpayer taxpayer) {
        if (taxpayer == null || taxpayer.getDeclaredIncome() == null) {
            return BigDecimal.ZERO;
        }
        return taxpayer.getDeclaredIncome().multiply(statutoryRate);
    }

    public FiscalTaxpayer adjustTaxpayerIncome(FiscalTaxpayer originalTaxpayer, BigDecimal adjustmentDelta) {
        if (originalTaxpayer == null || adjustmentDelta == null) {
            return originalTaxpayer;
        }
        BigDecimal newIncome = originalTaxpayer.getDeclaredIncome().add(adjustmentDelta);
        return new FiscalTaxpayer(
                originalTaxpayer.getTaxpayerId(),
                originalTaxpayer.getTaxIdentificationCode(),
                newIncome,
                originalTaxpayer.getRegistrationTimestamp()
        );
    }
}
