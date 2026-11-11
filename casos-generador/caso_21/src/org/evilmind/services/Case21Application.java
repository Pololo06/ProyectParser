package org.evilmind.services;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Case21Application {

    public static void main(String[] args) {
        TaxCalculationService service = new TaxCalculationService("SRV-TAX-CALC", new BigDecimal("0.19"));

        FiscalTaxpayer taxpayer = new FiscalTaxpayer(
                77001L,
                "TAX-ID-99",
                new BigDecimal("5000000.00"),
                OffsetDateTime.now()
        );

        BigDecimal liability = service.computeTaxLiability(taxpayer);
        FiscalTaxpayer adjusted = service.adjustTaxpayerIncome(taxpayer, new BigDecimal("250000.00"));

        System.out.println("=== Caso 21 ejecutado con exito ===");
        System.out.println("Servicio: " + service.getServiceIdentifier());
        System.out.println("Impuesto Calculado: " + liability);
        System.out.println("Ingreso Ajustado: " + adjusted.getDeclaredIncome());
    }
}
