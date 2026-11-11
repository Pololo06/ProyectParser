package org.evilmind.wildcards;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TaxVarianceProcessor {

    private final Short processorCode;

    public TaxVarianceProcessor(Short processorCode) {
        this.processorCode = processorCode;
    }

    public Short getProcessorCode() {
        return processorCode;
    }

    public BigDecimal processProducer(Supplier<? extends FiscalEntity> entityProducer) {
        if (entityProducer == null) {
            return BigDecimal.ZERO;
        }
        FiscalEntity entity = entityProducer.get();
        return entity != null ? entity.getDeclaredWorth() : BigDecimal.ZERO;
    }

    public Boolean supplyToConsumer(Consumer<? super TaxHoldingEntity> entityConsumer, TaxHoldingEntity payload) {
        if (entityConsumer != null && payload != null) {
            entityConsumer.accept(payload);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public Boolean inspectUnknown(Supplier<?> unknownSupplier) {
        return unknownSupplier != null && unknownSupplier.get() != null;
    }
}
