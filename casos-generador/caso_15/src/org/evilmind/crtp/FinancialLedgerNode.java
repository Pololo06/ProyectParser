package org.evilmind.crtp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class FinancialLedgerNode extends AbstractAuditedNode<FinancialLedgerNode> {

    private final Short ledgerLevel;

    public FinancialLedgerNode(Long nodeId, OffsetDateTime nodeTimestamp, BigDecimal cumulativeWeight, Short ledgerLevel) {
        super(nodeId, nodeTimestamp, cumulativeWeight);
        this.ledgerLevel = ledgerLevel;
    }

    public Short getLedgerLevel() {
        return ledgerLevel;
    }

    @Override
    public Boolean isSuperiorTo(FinancialLedgerNode siblingNode) {
        if (siblingNode == null || this.cumulativeWeight == null) {
            return Boolean.FALSE;
        }
        return this.cumulativeWeight.compareTo(siblingNode.getCumulativeWeight()) > 0;
    }
}
