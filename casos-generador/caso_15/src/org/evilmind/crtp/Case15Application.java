package org.evilmind.crtp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Case15Application {

    public static void main(String[] args) {
        FinancialLedgerNode nodeA = new FinancialLedgerNode(
                101L,
                OffsetDateTime.now(),
                new BigDecimal("500000.00"), (short) 1).withWeight(new BigDecimal("600000.00"));

        FinancialLedgerNode nodeB = new FinancialLedgerNode(
                102L,
                OffsetDateTime.now(),
                new BigDecimal("450000.00"), (short) 2);

        System.out.println("=== Caso 15 ejecutado con exito ===");
        System.out.println("Nodo A ID: " + nodeA.getNodeId());
        System.out.println("Nodo A Nivel: " + nodeA.getLedgerLevel());
        System.out.println("Nodo A es superior a B?: " + nodeA.isSuperiorTo(nodeB));
    }
}
