package org.evilmind.records;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Case06Application {

    public static void main(String[] args) {
        Long id = 7710L;
        String key = "ROUTING-KEY-ALPHA";
        BigDecimal commission = new BigDecimal("12.50");
        OffsetDateTime now = OffsetDateTime.now();
        Short version = (short) 1;

        TransactionEnvelope<String, BigDecimal> envelope = new TransactionEnvelope<>(
            id,
            key,
            new BigDecimal("9999.99"),
            commission,
            now,
            version
        );

        System.out.println("=== Caso 06 ejecutado con exito ===");
        System.out.println("Envelope ID: " + envelope.envelopeId());
        System.out.println("Recargo Base Record: " + TransactionEnvelope.BASE_SURCHARGE);
        System.out.println("Revision Esquema Interfaz: " + AuditablePayload.SCHEMA_REVISION);
        System.out.println("Particion por Defecto: " + TransactionEnvelope.DEFAULT_PARTITION_KEY);
    }
}