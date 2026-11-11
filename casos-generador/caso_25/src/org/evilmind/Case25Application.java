package org.evilmind;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.evilmind.controller.InvoiceController;
import org.evilmind.controller.InvoiceResponseDto;
import org.evilmind.domain.FiscalInvoice;
import org.evilmind.domain.InvoiceItem;
import org.evilmind.domain.TaxpayerProfile;
import org.evilmind.repository.InMemoryInvoiceRepository;
import org.evilmind.repository.InvoiceRepository;
import org.evilmind.service.InvoiceService;
import org.evilmind.service.InvoiceServiceImpl;

public class Case25Application {

    public static void main(String[] args) {
        InvoiceRepository repo = new InMemoryInvoiceRepository();
        InvoiceService service = new InvoiceServiceImpl(repo);
        InvoiceController controller = new InvoiceController(service);

        TaxpayerProfile issuer = new TaxpayerProfile(1L, "ACME CORP S.A.", (short) 170);
        InvoiceItem item = new InvoiceItem(101L, "Licencia Enterprise", new BigDecimal("1000000.00"), (short) 1);

        FiscalInvoice invoice = new FiscalInvoice(
                5001L,
                OffsetDateTime.now(), (short) 1,
                "FAC-2026-FINAL",
                new BigDecimal("1000000.00"),
                issuer,
                List.of(item),
                "SHA256-DIGEST-TOKEN-XYZ"
        );

        InvoiceResponseDto response = controller.processInvoiceEntry(invoice);

        System.out.println("=== Caso 25 ejecutado con exito ===");
        System.out.println("Factura: " + response.invoiceNumber());
        System.out.println("Total con Impuestos: " + response.totalWithTaxes());
        System.out.println("Firma Seguridad: " + invoice.getSecurityDigest().getDigitalSignature());
    }
}
