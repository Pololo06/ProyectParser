package org.evilmind.enums;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public enum TaxPayerType {

    INDIVIDUAL(Short.valueOf((short) 1), new BigDecimal("0.19"), "Persona Natural"),
    CORPORATE(Short.valueOf((short) 2), new BigDecimal("0.35"), "Persona Jurídica"),
    NON_PROFIT(Short.valueOf((short) 3), new BigDecimal("0.00"), "Entidad Sin Ánimo de Lucro");

    private final Short typeCode;
    private final BigDecimal baseRetentionRate;
    private final String description;
    private final OffsetDateTime catalogEpoch;

    TaxPayerType(Short typeCode, BigDecimal baseRetentionRate, String description) {
        this.typeCode = typeCode;
        this.baseRetentionRate = baseRetentionRate;
        this.description = description;
        this.catalogEpoch = OffsetDateTime.now();
    }

    public Short getTypeCode() {
        return typeCode;
    }

    public BigDecimal getBaseRetentionRate() {
        return baseRetentionRate;
    }

    public String getDescription() {
        return description;
    }

    public OffsetDateTime getCatalogEpoch() {
        return catalogEpoch;
    }

    public Boolean hasTaxExemption() {
        return baseRetentionRate != null && baseRetentionRate.compareTo(BigDecimal.ZERO) == 0;
    }
}
