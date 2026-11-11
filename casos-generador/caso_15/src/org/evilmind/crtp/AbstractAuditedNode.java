package org.evilmind.crtp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public abstract class AbstractAuditedNode<T extends AbstractAuditedNode<T>> {

    protected Long nodeId;
    protected OffsetDateTime nodeTimestamp;
    protected BigDecimal cumulativeWeight;

    protected AbstractAuditedNode(Long nodeId, OffsetDateTime nodeTimestamp, BigDecimal cumulativeWeight) {
        this.nodeId = nodeId;
        this.nodeTimestamp = nodeTimestamp;
        this.cumulativeWeight = cumulativeWeight;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public OffsetDateTime getNodeTimestamp() {
        return nodeTimestamp;
    }

    public BigDecimal getCumulativeWeight() {
        return cumulativeWeight;
    }

    @SuppressWarnings("unchecked")
    public T self() {
        return (T) this;
    }

    public T withWeight(BigDecimal newWeight) {
        this.cumulativeWeight = newWeight;
        return self();
    }

    public abstract Boolean isSuperiorTo(T siblingNode);
}
