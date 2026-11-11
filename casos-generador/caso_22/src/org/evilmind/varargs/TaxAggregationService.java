package org.evilmind.varargs;

import java.math.BigDecimal;

public class TaxAggregationService {

    private final String serviceName;

    public TaxAggregationService(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public BigDecimal aggregateValues() {
        return BigDecimal.ZERO;
    }

    public BigDecimal aggregateValues(BigDecimal initialSeed) {
        return initialSeed != null ? initialSeed : BigDecimal.ZERO;
    }

    public BigDecimal aggregateValues(BigDecimal initialSeed, BigDecimal... additionalAmounts) {
        BigDecimal total = aggregateValues(initialSeed);
        if (additionalAmounts != null) {
            for (BigDecimal amount : additionalAmounts) {
                if (amount != null) {
                    total = total.add(amount);
                }
            }
        }
        return total;
    }

    public BigDecimal combineTiers(TaxRateTier... tiers) {
        if (tiers == null || tiers.length == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal combined = BigDecimal.ZERO;
        for (TaxRateTier tier : tiers) {
            if (tier != null && tier.getRateMultiplier() != null) {
                combined = combined.add(tier.getRateMultiplier());
            }
        }
        return combined;
    }

    public Short calculateMaxTierCode(int... tierCodes) {
        if (tierCodes == null || tierCodes.length == 0) {
            return Short.valueOf((short) 0);
        }
        int max = tierCodes[0];
        for (int code : tierCodes) {
            if (code > max) {
                max = code;
            }
        }
        return Short.valueOf((short) max);
    }
}
