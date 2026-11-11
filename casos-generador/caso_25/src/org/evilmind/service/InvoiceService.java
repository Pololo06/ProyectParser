package org.evilmind.service;

import java.math.BigDecimal;
import org.evilmind.domain.FiscalInvoice;

public interface InvoiceService {

    BigDecimal computeTaxes(FiscalInvoice invoice);

    Boolean registerInvoice(FiscalInvoice invoice);
}
