package org.evilmind.repository;

import org.evilmind.domain.FiscalInvoice;

public interface InvoiceRepository {

    FiscalInvoice findById(Long id);

    Boolean save(FiscalInvoice invoice);
}
