package org.evilmind.records;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Case07Application {

    public static void main(String[] args) {
        SettlementBatchRecord batchFull = new SettlementBatchRecord(
                3001L,
                "BATCH-FULL-01",
                new BigDecimal("890000.00"),
                OffsetDateTime.now(), (short) 10);

        SettlementBatchRecord batchReduced = new SettlementBatchRecord(
                3002L,
                "BATCH-REDUCED-02",
                new BigDecimal("150000.00")
        );

        System.out.println("=== Caso 07 ejecutado con exito ===");
        System.out.println("Batch Completo Prioridad: " + batchFull.isHighPriority());
        System.out.println("Batch Reducido Cutoff: " + batchReduced.processingCutoff());
    }
}
