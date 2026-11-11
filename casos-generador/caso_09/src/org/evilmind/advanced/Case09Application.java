package org.evilmind.advanced;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Case09Application {

    public static void main(String[] args) {
        TaxAssessmentManager manager = new TaxAssessmentManager(
                901L,
                "DIAN-REGIONAL",
                OffsetDateTime.now(), (short) 1);

        TaxAssessmentManager.TaxPayerRecord payer = new TaxAssessmentManager.TaxPayerRecord(
                55001L,
                "NIT-900800700-1",
                new BigDecimal("50000000.00"),
                OffsetDateTime.now(), (short) 2);

        TaxAssessmentManager.RateStrategy strategy = TaxAssessmentManager.RateStrategy.STANDARD;
        BigDecimal calculatedTax = manager.executeAssessment(payer, strategy);

        System.out.println("=== Caso 09 ejecutado con exito ===");
        System.out.println("Autoridad: " + manager.getAuthorityName());
        System.out.println("Es corporativo: " + payer.isCorporate());
        System.out.println("Estrategia: " + strategy.name() + " (Prioridad: " + strategy.getRulePriority() + ")");
        System.out.println("Impuesto calculado: " + calculatedTax);
    }
}
