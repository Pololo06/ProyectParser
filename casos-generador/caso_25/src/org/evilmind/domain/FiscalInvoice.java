package org.evilmind.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class FiscalInvoice extends AbstractAuditableEntity {

    private final String invoiceNumber;
    private final BigDecimal netTotal;
    private final TaxpayerProfile issuer;
    private final List<InvoiceItem> items;
    private final InvoiceSecurityDigest securityDigest;

    public FiscalInvoice(Long entityId, OffsetDateTime createdAt, Short revisionVersion, String invoiceNumber, BigDecimal netTotal, TaxpayerProfile issuer, List<InvoiceItem> items, String signature) {
        super(entityId, createdAt, revisionVersion);
        this.invoiceNumber = invoiceNumber;
        this.netTotal = netTotal;
        this.issuer = issuer;
        this.items = items;
        this.securityDigest = new InvoiceSecurityDigest(signature, OffsetDateTime.now());
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public BigDecimal getNetTotal() {
        return netTotal;
    }

    public TaxpayerProfile getIssuer() {
        return issuer;
    }

    public List<InvoiceItem> getItems() {
        return items;
    }

    public InvoiceSecurityDigest getSecurityDigest() {
        return securityDigest;
    }
}
