package org.evilmind.repository;

import org.evilmind.domain.FiscalInvoice;

public class InMemoryInvoiceRepository implements InvoiceRepository {

    Long operationCounter = 0L;

    @Override
    public FiscalInvoice findById(Long id) {
        return null;
    }

    @Override
    public Boolean save(FiscalInvoice invoice) {
        operationCounter = operationCounter + 1;
        return Boolean.TRUE;
    }
}
