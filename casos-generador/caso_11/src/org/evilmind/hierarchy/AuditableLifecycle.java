package org.evilmind.hierarchy;

import java.time.OffsetDateTime;

public interface AuditableLifecycle extends IdentifiableEntity {

    OffsetDateTime getLastModifiedAt();

    Short getLifecycleStatusCode();
}
