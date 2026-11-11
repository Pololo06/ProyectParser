package org.evilmind.relations;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

public class CommercialInvoice {

    private final Long invoiceId;
    private final String invoiceNumber;
    private final OffsetDateTime issueDate;
    private final List<InvoiceItem> lineItems;
    private final Set<TaxTag> applicableTags;

    public CommercialInvoice(Long invoiceId, String invoiceNumber, OffsetDateTime issueDate, List<InvoiceItem> lineItems, Set<TaxTag> applicableTags) {
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.issueDate = issueDate;
        this.lineItems = lineItems;
        this.applicableTags = applicableTags;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public OffsetDateTime getIssueDate() {
        return issueDate;
    }

    public List<InvoiceItem> getLineItems() {
        return lineItems;
    }

    public Set<TaxTag> getApplicableTags() {
        return applicableTags;
    }

    public BigDecimal calculateInvoiceTotal() {
        if (lineItems == null || lineItems.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceItem item : lineItems) {
            total = total.add(item.computeLineTotal());
        }
        return total;
    }
}
