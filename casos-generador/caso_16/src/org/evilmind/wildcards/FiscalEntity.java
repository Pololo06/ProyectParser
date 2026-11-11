package org.evilmind.wildcards;

import java.math.BigDecimal;

public class FiscalEntity {

    private final Long entityId;
    private final BigDecimal declaredWorth;

    public FiscalEntity(Long entityId, BigDecimal declaredWorth) {
        this.entityId = entityId;
        this.declaredWorth = declaredWorth;
    }

    public Long getEntityId() {
        return entityId;
    }

    public BigDecimal getDeclaredWorth() {
        return declaredWorth;
    }
}
