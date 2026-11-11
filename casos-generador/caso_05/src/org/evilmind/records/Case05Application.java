package org.evilmind.records;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Case05Application {

    public static void main(String[] args) {
        FinancialAuditRecord record = new FinancialAuditRecord(
                88001L,
                "AUD-TRK-992",
                new BigDecimal("450000.00"),
                new BigDecimal("85500.00"),
                OffsetDateTime.now(), (short) 3);

        System.out.println("=== Caso 05 ejecutado con exito ===");
        System.out.println("ID Auditoria: " + record.auditId());
        System.out.println("Token: " + record.trackingToken());
        System.out.println("Version Compliance: " + FinancialAuditRecord.COMPLIANCE_VERSION);
        System.out.println("Requiere revision: " + record.requiresManualReview());
    }
}
