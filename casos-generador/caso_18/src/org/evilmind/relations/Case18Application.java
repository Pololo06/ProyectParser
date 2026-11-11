package org.evilmind.relations;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

public class Case18Application {

    public static void main(String[] args) {
        JurisdictionZone zone = new JurisdictionZone("BOG", "Bogota D.C.", (short) 170);
        FiscalPolicy policy = new FiscalPolicy(5001L, new BigDecimal("0.19"), OffsetDateTime.now());

        NationalFiscalDirectory directory = new NationalFiscalDirectory(
                10L,
                OffsetDateTime.now(),
                Map.of(zone, policy)
        );

        System.out.println("=== Caso 18 ejecutado con exito ===");
        System.out.println("Directorio: " + directory.getDirectoryId());
        System.out.println("Politica resuelta: " + directory.resolvePolicy(zone).getDefaultTaxRate());
    }
}
