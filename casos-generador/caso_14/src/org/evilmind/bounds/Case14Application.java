package org.evilmind.bounds;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Case14Application {

    public static void main(String[] args) {
        TransactionBatchSorter<TreasuryDisbursement> sorter = new TransactionBatchSorter<>(
                Short.valueOf((short) 10)
        );

        TreasuryDisbursement item = new TreasuryDisbursement(
                882001L,
                new BigDecimal("7500000.00"),
                OffsetDateTime.now()
        );

        System.out.println("=== Caso 14 ejecutado con exito ===");
        System.out.println("Prioridad del batch: " + sorter.getBatchPriority());
        System.out.println("Valor ponderado: " + sorter.calculateWeightedValue(item));
        System.out.println("Secuencia resuelta: " + sorter.resolveSequence(item));
    }
}
