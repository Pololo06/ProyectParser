package org.evilmind.generics;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface IdentifiablePayload {

    Long getPayloadId();

    BigDecimal getDeclaredAmount();

    OffsetDateTime getEmissionDate();
}
