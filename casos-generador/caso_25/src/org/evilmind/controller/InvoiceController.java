package org.evilmind.controller;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.evilmind.domain.FiscalInvoice;
import org.evilmind.service.InvoiceService;

public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    public InvoiceResponseDto processInvoiceEntry(FiscalInvoice rawInvoice) {
        BigDecimal tax = invoiceService.computeTaxes(rawInvoice);
        BigDecimal total = rawInvoice.getNetTotal().add(tax);
        invoiceService.registerInvoice(rawInvoice);
        return new InvoiceResponseDto(rawInvoice.getInvoiceNumber(), total, OffsetDateTime.now());
    }
}
