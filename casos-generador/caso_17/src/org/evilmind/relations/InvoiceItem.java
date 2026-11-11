package org.evilmind.relations;

import java.math.BigDecimal;

public class InvoiceItem {

    private final Long itemId;
    private final String itemDescription;
    private final BigDecimal unitPrice;
    private final Short quantity;

    public InvoiceItem(Long itemId, String itemDescription, BigDecimal unitPrice, Short quantity) {
        this.itemId = itemId;
        this.itemDescription = itemDescription;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public Long getItemId() {
        return itemId;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public Short getQuantity() {
        return quantity;
    }

    public BigDecimal computeLineTotal() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(new BigDecimal(quantity.toString()));
    }
}
