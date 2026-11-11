package org.evilmind.nested;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TaxExecutionService {

    private final Long serviceId;
    private final String jurisdictionCode;
    private final BigDecimal flatRate;
    private final OffsetDateTime activatedAt;

    public TaxExecutionService(Long serviceId, String jurisdictionCode, BigDecimal flatRate, OffsetDateTime activatedAt) {
        this.serviceId = serviceId;
        this.jurisdictionCode = jurisdictionCode;
        this.flatRate = flatRate;
        this.activatedAt = activatedAt;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public String getJurisdictionCode() {
        return jurisdictionCode;
    }

    public BigDecimal getFlatRate() {
        return flatRate;
    }

    public OffsetDateTime getActivatedAt() {
        return activatedAt;
    }

    public BigDecimal processTaxesWithInternalConstructs(BigDecimal rawAmount, Short multiplier) {

        class LocalTaxCalculator {

            private final BigDecimal base;

            LocalTaxCalculator(BigDecimal base) {
                this.base = base;
            }

            BigDecimal compute() {
                return base.multiply(new BigDecimal(multiplier.toString()));
            }
        }

        LocalTaxCalculator localCalc = new LocalTaxCalculator(rawAmount);
        BigDecimal subTotal = localCalc.compute();

        Runnable auditLogger = new Runnable() {
            @Override
            public void run() {
                System.out.println("Auditoria ejecutada para: " + subTotal);
            }
        };
        auditLogger.run();

        return subTotal;
    }
}
