package org.evilmind.generics;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public final class TaxPaymentPayload implements IdentifiablePayload {

    private final Long payloadId;
    private final BigDecimal declaredAmount;
    private final OffsetDateTime emissionDate;

    public TaxPaymentPayload(Long payloadId, BigDecimal declaredAmount, OffsetDateTime emissionDate) {
        this.payloadId = payloadId;
        this.declaredAmount = declaredAmount;
        this.emissionDate = emissionDate;
    }

    @Override
    public Long getPayloadId() {
        return payloadId;
    }

    @Override
    public BigDecimal getDeclaredAmount() {
        return declaredAmount;
    }

    @Override
    public OffsetDateTime getEmissionDate() {
        return emissionDate;
    }
}
