package org.evilmind.nested;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Case04Application {

    public static void main(String[] args) {
        TaxExecutionService service = new TaxExecutionService(
                401L,
                "CO-BOG",
                new BigDecimal("0.19"),
                OffsetDateTime.now()
        );

        BigDecimal result = service.processTaxesWithInternalConstructs(new BigDecimal("100000.00"), (short) 2);

        System.out.println("=== Caso 04 ejecutado con exito ===");
        System.out.println("Resultado de ejecucion: " + result);
    }
}
