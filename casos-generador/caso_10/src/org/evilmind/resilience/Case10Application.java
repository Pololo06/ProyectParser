package org.evilmind.resilience;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Case10Application {

    public static void main(String[] args) {
        FiscalContract standard = new StandardFiscalLedger(
                11001L,
                "FOL-STD-99", (short) 1,
                new BigDecimal("50000.00")
        );

        FiscalContract custom = new CustomFiscalLedger(
                new BigDecimal("0.12"),
                OffsetDateTime.now()
        );

        BigDecimal gross = new BigDecimal("1000000.00");

        System.out.println("=== Caso 10 (Acid Test) ejecutado con exito ===");
        System.out.println("Impuesto Estandar con Recargo (default method): " + standard.calculateTaxWithSurcharge(gross));
        System.out.println("Impuesto Personalizado Base: " + custom.calculateBaseTax(gross));
        System.out.println("Periodo valido (static method): " + FiscalContract.isStandardPeriod(OffsetDateTime.now()));
    }
}
