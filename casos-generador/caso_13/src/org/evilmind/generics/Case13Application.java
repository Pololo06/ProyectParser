package org.evilmind.generics;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Case13Application {

    public static void main(String[] args) {
        TaxPaymentRepository repo = new TaxPaymentRepository((short) 1);

        TaxPaymentPayload payload = new TaxPaymentPayload(
                9901L,
                new BigDecimal("350000.00"),
                OffsetDateTime.now()
        );

        repo.persist(payload);

        System.out.println("=== Caso 13 ejecutado con exito ===");
        System.out.println("Particion: " + repo.getRepositoryPartition());
        System.out.println("Monto extraido: " + repo.extractAmount(payload));
    }
}
