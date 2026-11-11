package org.evilmind.template;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Case12Application {

    public static void main(String[] args) {
        int[][][] cube = new int[2][2][2];
        cube[0][0][0] = 42;

        BigDecimal[][] weights = new BigDecimal[2][2];
        weights[0][0] = new BigDecimal("1.25");

        OffsetDateTime[] timestamps = new OffsetDateTime[]{
            OffsetDateTime.now()
        };

        HighFrequencyRiskEngine engine = new HighFrequencyRiskEngine(
                7001L,
                new BigDecimal("500.00"),
                cube,
                weights,
                timestamps, (short) 15);

        BigDecimal score = engine.executeEvaluation(cube);

        System.out.println("=== Caso 12 ejecutado con exito ===");
        System.out.println("Engine ID: " + engine.getEngineId());
        System.out.println("Dimension cubo: " + engine.getRiskMatrixCube().length);
        System.out.println("Score Calculado: " + score);
    }
}
