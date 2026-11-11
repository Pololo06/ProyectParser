package org.evilmind.resilience;

import java.math.BigDecimal;

public final class StandardFiscalLedger implements FiscalContract {

    // Matriz de visibilidad en atributos
    public Long ledgerId;
    protected String ledgerFolio;
    Short revisionNumber;
    private BigDecimal accumulatedTotal;

    public StandardFiscalLedger(Long ledgerId, String ledgerFolio, Short revisionNumber, BigDecimal accumulatedTotal) {
        this.ledgerId = ledgerId;
        this.ledgerFolio = ledgerFolio;
        this.revisionNumber = revisionNumber;
        this.accumulatedTotal = accumulatedTotal;
    }

    @Override
    public BigDecimal calculateBaseTax(BigDecimal grossAmount) {
        return grossAmount.multiply(new BigDecimal("0.19"));
    }

    public Long getLedgerId() {
        return ledgerId;
    }

    protected String resolveFolioHeader() {
        return ledgerFolio;
    }

    Short retrieveRevision() {
        return revisionNumber;
    }

    private BigDecimal inspectAccumulated() {
        return accumulatedTotal;
    }
}
