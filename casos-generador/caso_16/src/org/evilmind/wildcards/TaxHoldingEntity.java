package org.evilmind.wildcards;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TaxHoldingEntity extends FiscalEntity {

    private final Short holdingJurisdiction;
    private final OffsetDateTime registeredDate;

    public TaxHoldingEntity(Long entityId, BigDecimal declaredWorth, Short holdingJurisdiction, OffsetDateTime registeredDate) {
        super(entityId, declaredWorth);
        this.holdingJurisdiction = holdingJurisdiction;
        this.registeredDate = registeredDate;
    }

    public Short getHoldingJurisdiction() {
        return holdingJurisdiction;
    }

    public OffsetDateTime getRegisteredDate() {
        return registeredDate;
    }
}
