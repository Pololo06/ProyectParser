package org.evilmind.nested;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Case02Application {

    public static void main(String[] args) {
        // Nivel 1
        EnterpriseHierarchy enterprise = new EnterpriseHierarchy(
                100L,
                "CORP-HQ",
                OffsetDateTime.now()
        );

        // Nivel 2: Al ser static, no requiere una instancia previa de EnterpriseHierarchy
        EnterpriseHierarchy.DivisionUnit division = new EnterpriseHierarchy.DivisionUnit(
                200L,
                "FINANCE_OPS",
                new BigDecimal("7500000.00")
        );

        // Nivel 3: Acceso estático calificado en cadena
        EnterpriseHierarchy.DivisionUnit.DepartmentNode department = new EnterpriseHierarchy.DivisionUnit.DepartmentNode(
                300L, (short) 10,
                new BigDecimal("120000.50"),
                OffsetDateTime.now()
        );

        System.out.println("=== Caso 02 ejecutado con exito ===");
        System.out.println("Empresa: " + enterprise.getEnterpriseCode());
        System.out.println("Division: " + division.getDivisionName());
        System.out.println("Departamento: " + department.getDepartmentId());
    }
}
