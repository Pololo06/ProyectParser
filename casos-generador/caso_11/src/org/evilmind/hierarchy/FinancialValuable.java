package org.evilmind.hierarchy;

import java.math.BigDecimal;

public interface FinancialValuable extends IdentifiableEntity {

    BigDecimal getAssessedValue();

    Short getCurrencyCode();
}
