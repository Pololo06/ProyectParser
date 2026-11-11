package org.evilmind.nested;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Case01Application {

    public static void main(String[] args) {
        Long accountId = 1001L;
        String accountNumber = "ACC-2026-X01";
        BigDecimal currentBalance = new BigDecimal("15000000.50");
        OffsetDateTime openingDate = OffsetDateTime.now();
        Short activeStatus = (short) 1;

        FinancialAccount account = new FinancialAccount(
                accountId,
                accountNumber,
                currentBalance,
                openingDate,
                activeStatus
        );

        FinancialAccount.LedgerEntry entry = account.new LedgerEntry(
                5001L,
                new BigDecimal("250000.75"),
                OffsetDateTime.now(), (short) 2,
                Boolean.TRUE
        );

        System.out.println("=== Caso 01 ejecutado con exito ===");
        System.out.println("ID Cuenta: " + account.getId());
        System.out.println("ID Entrada: " + entry.getEntryId());
        System.out.println("Monto Entrada: " + entry.getTransactionAmount());
    }
}
