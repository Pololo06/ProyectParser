package org.evilmind.wildcards;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Case16Application {

    public static void main(String[] args) {
        TaxVarianceProcessor processor = new TaxVarianceProcessor((short) 99);

        TaxHoldingEntity holding = new TaxHoldingEntity(
                3301L,
                new BigDecimal("12000000.00"), (short) 4,
                OffsetDateTime.now()
        );

        BigDecimal worth = processor.processProducer(() -> holding);

        Boolean consumed = processor.supplyToConsumer(entity -> {
            System.out.println("Entidad procesada: " + entity.getEntityId());
        }, holding);

        System.out.println("=== Caso 16 ejecutado con exito ===");
        System.out.println("Codigo Procesador: " + processor.getProcessorCode());
        System.out.println("Valor producido: " + worth);
        System.out.println("Consumo exitoso?: " + consumed);
    }
}
