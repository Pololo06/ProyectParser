package org.evilmind.nested;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class BillingPlan {

    private final Long planId;
    private final String planName;
    private final BigDecimal monthlyFee;
    private final Short billingCycleDays;

    public BillingPlan(Long planId, String planName, BigDecimal monthlyFee, Short billingCycleDays) {
        this.planId = planId;
        this.planName = planName;
        this.monthlyFee = monthlyFee;
        this.billingCycleDays = billingCycleDays;
    }

    public Long getPlanId() {
        return planId;
    }

    public String getPlanName() {
        return planName;
    }

    public BigDecimal getMonthlyFee() {
        return monthlyFee;
    }

    public Short getBillingCycleDays() {
        return billingCycleDays;
    }

    public interface PaymentDiscountPolicy {

        BigDecimal calculateEffectiveAmount(BigDecimal baseAmount, Short discountRatePercentage);
    }

    public record InvoiceSnapshot(
            Long invoiceId,
            BigDecimal chargedAmount,
            OffsetDateTime issuedAt,
            Short taxRatePercentage
            ) {

        public Boolean isHighValue() {
            return chargedAmount != null && chargedAmount.compareTo(new BigDecimal("1000000")) > 0;
        }
    }
}
