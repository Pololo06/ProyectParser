package org.evilmind.relations;

import java.math.BigDecimal;
import java.util.Collections;

public class Case19Application {

    public static void main(String[] args) {
        TaxCategory root = new TaxCategory(
                1L,
                "IMPUESTOS_NACIONALES",
                new BigDecimal("0.00"), (short) 1,
                null,
                Collections.emptyList()
        );

        TaxCategory child = new TaxCategory(
                2L,
                "IVA_CORPORATIVO",
                new BigDecimal("0.19"), (short) 2,
                root,
                Collections.emptyList()
        );

        System.out.println("=== Caso 19 ejecutado con exito ===");
        System.out.println("Categoria Raiz: " + root.getCategoryName() + " (Es raiz?: " + root.isRootCategory() + ")");
        System.out.println("Subcategoria: " + child.getCategoryName() + " -> Padre: " + child.getParentCategory().getCategoryName());
    }
}
