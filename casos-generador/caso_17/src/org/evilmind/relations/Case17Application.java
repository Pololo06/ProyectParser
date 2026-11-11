package org.evilmind.relations;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

public class Case17Application {

    public static void main(String[] args) {
        InvoiceItem item1 = new InvoiceItem(1L, "Servicio Cloud", new BigDecimal("1500.00"), (short) 2);
        TaxTag tagVat = new TaxTag((short) 19, "IVA_GENERAL");

        CommercialInvoice invoice = new CommercialInvoice(
                901L,
                "FAC-2026-901",
                OffsetDateTime.now(),
                List.of(item1),
                Set.of(tagVat)
        );

        System.out.println("=== Caso 17 ejecutado con exito ===");
        System.out.println("Factura: " + invoice.getInvoiceNumber());
        System.out.println("Total Lineas: " + invoice.getLineItems().size());
        System.out.println("Total Calculado: " + invoice.calculateInvoiceTotal());
    }
}
