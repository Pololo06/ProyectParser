package org.evilmind.hierarchy;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public final class CommercialRealEstate implements AssetContract {

    private final Long entityId;
    private final OffsetDateTime createdAt;
    private final BigDecimal assessedValue;
    private final Short currencyCode;
    private final OffsetDateTime lastModifiedAt;
    private final Short lifecycleStatusCode;

    public CommercialRealEstate(Long entityId, OffsetDateTime createdAt, BigDecimal assessedValue, Short currencyCode, OffsetDateTime lastModifiedAt, Short lifecycleStatusCode) {
        this.entityId = entityId;
        this.createdAt = createdAt;
        this.assessedValue = assessedValue;
        this.currencyCode = currencyCode;
        this.lastModifiedAt = lastModifiedAt;
        this.lifecycleStatusCode = lifecycleStatusCode;
    }

    @Override
    public Long getEntityId() {
        return entityId;
    }

    @Override
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public BigDecimal getAssessedValue() {
        return assessedValue;
    }

    @Override
    public Short getCurrencyCode() {
        return currencyCode;
    }

    @Override
    public OffsetDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }

    @Override
    public Short getLifecycleStatusCode() {
        return lifecycleStatusCode;
    }

    @Override
    public BigDecimal calculateDepreciationRate() {
        return new BigDecimal("0.05");
    }
}
