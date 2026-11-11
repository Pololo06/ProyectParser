package org.evilmind.relations;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class Case20Application {

    public static void main(String[] args) {
        VoucherEntry entry1 = new VoucherEntry(10L, "CTA-1105", new BigDecimal("75000.00"), (short) 1);
        VoucherEntry entry2 = new VoucherEntry(20L, "CTA-2205", new BigDecimal("125000.00"), (short) 2);

        AuditVoucher voucher = new AuditVoucher(
                999L,
                OffsetDateTime.now(),
                new BigDecimal("200000.00"),
                entry1,
                List.of(entry1, entry2)
        );

        System.out.println("=== Caso 20 (Torneo de Pesos) ejecutado con exito ===");
        System.out.println("Voucher ID: " + voucher.getVoucherId());
        System.out.println("Entrada Primaria: " + voucher.getPrimaryEntry().getAccountCode());
        System.out.println("Total Entradas: " + voucher.getVoucherEntries().size());
        System.out.println("Verificacion: " + voucher.verifyEntryConsistency(entry2));
    }
}
