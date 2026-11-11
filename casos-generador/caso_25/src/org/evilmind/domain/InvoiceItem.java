package org.evilmind.domain;

import java.math.BigDecimal;

public class InvoiceItem {

    private final Long itemId;
    private final String description;
    private final BigDecimal unitPrice;
    private final Short quantity;

    public InvoiceItem(Long itemId, String description, BigDecimal unitPrice, Short quantity) {
        this.itemId = itemId;
        this.description = description;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public Long getItemId() {
        return itemId;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public Short getQuantity() {
        return quantity;
    }
}
