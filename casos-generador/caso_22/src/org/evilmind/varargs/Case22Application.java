package org.evilmind.varargs;

import java.math.BigDecimal;

public class Case22Application {

    public static void main(String[] args) {
        TaxAggregationService service = new TaxAggregationService("SRV-AGGREGATOR");

        TaxRateTier tier1 = new TaxRateTier((short) 1, new BigDecimal("0.05"));
        TaxRateTier tier2 = new TaxRateTier((short) 2, new BigDecimal("0.19"));

        BigDecimal totalAmounts = service.aggregateValues(
                new BigDecimal("100.00"),
                new BigDecimal("250.00"),
                new BigDecimal("50.00")
        );

        BigDecimal combinedTiers = service.combineTiers(tier1, tier2);

        Short maxCode = service.calculateMaxTierCode(1, 4, 2);

        System.out.println("=== Caso 22 ejecutado con exito ===");
        System.out.println("Total Agregado: " + totalAmounts);
        System.out.println("Tasa Combinada: " + combinedTiers);
        System.out.println("Codigo Maximo: " + maxCode);
    }
}
