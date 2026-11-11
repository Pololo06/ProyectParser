package org.evilmind.service;

import java.math.BigDecimal;
import org.evilmind.domain.FiscalInvoice;
import org.evilmind.repository.InvoiceRepository;

public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public BigDecimal computeTaxes(FiscalInvoice invoice) {
        if (invoice == null || invoice.getNetTotal() == null) {
            return BigDecimal.ZERO;
        }
        return invoice.getNetTotal().multiply(new BigDecimal("0.19"));
    }

    @Override
    public Boolean registerInvoice(FiscalInvoice invoice) {
        return invoiceRepository.save(invoice);
    }
}
