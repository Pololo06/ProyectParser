package org.evilmind.hierarchy;

import java.math.BigDecimal;

public interface AssetContract extends FinancialValuable, AuditableLifecycle {

    BigDecimal calculateDepreciationRate();
}
