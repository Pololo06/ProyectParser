package org.evilmind.hierarchy;

import java.time.OffsetDateTime;

public interface IdentifiableEntity {

    Long getEntityId();

    OffsetDateTime getCreatedAt();
}
