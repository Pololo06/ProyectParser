package org.evilmind.enums;

public class Case08Application {

    public static void main(String[] args) {
        TaxPayerType pType = TaxPayerType.CORPORATE;

        System.out.println("=== Caso 08 ejecutado con exito ===");
        System.out.println("Tipo: " + pType.name());
        System.out.println("Codigo: " + pType.getTypeCode());
        System.out.println("Tasa Retencion: " + pType.getBaseRetentionRate());
        System.out.println("Exento?: " + pType.hasTaxExemption());
    }
}