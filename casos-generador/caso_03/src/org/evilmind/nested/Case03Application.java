package org.evilmind.nested;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Case03Application {

    public static void main(String[] args) {
        BillingPlan plan = new BillingPlan(
                10L,
                "ENTERPRISE_GOLD",
                new BigDecimal("990.00"), (short) 30);

        BillingPlan.PaymentDiscountPolicy discountPolicy = (baseAmount, discountRatePercentage)
                -> baseAmount.multiply(new BigDecimal(discountRatePercentage.toString()));

        BillingPlan.InvoiceSnapshot snapshot = new BillingPlan.InvoiceSnapshot(
                9001L,
                new BigDecimal("5000000.00"),
                OffsetDateTime.now(), (short) 19);

        System.out.println("=== Caso 03 ejecutado con exito ===");
        System.out.println("Plan: " + plan.getPlanName());
        System.out.println("Snapshot Factura: " + snapshot.invoiceId() + " - Es alto valor: " + snapshot.isHighValue());
    }
}
