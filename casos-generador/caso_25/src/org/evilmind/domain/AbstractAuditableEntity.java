package org.evilmind.domain;

import java.time.OffsetDateTime;

public abstract class AbstractAuditableEntity {

    protected Long entityId;
    protected OffsetDateTime createdAt;
    Short revisionVersion;
    private OffsetDateTime lastModifiedAt;

    protected AbstractAuditableEntity(Long entityId, OffsetDateTime createdAt, Short revisionVersion) {
        this.entityId = entityId;
        this.createdAt = createdAt;
        this.revisionVersion = revisionVersion;
        this.lastModifiedAt = OffsetDateTime.now();
    }

    public Long getEntityId() {
        return entityId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    Short getRevisionVersion() {
        return revisionVersion;
    }
}
