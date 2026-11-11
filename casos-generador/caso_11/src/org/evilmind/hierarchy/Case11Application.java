package org.evilmind.hierarchy;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Case11Application {

    public static void main(String[] args) {
        AssetContract asset = new CommercialRealEstate(
                88001L,
                OffsetDateTime.now(),
                new BigDecimal("1250000000.00"), (short) 840,
                OffsetDateTime.now(), (short) 1);

        System.out.println("=== Caso 11 ejecutado con exito ===");
        System.out.println("Activo ID: " + asset.getEntityId());
        System.out.println("Valor Tasado: " + asset.getAssessedValue());
        System.out.println("Tasa Depreciacion: " + asset.calculateDepreciationRate());
    }
}
