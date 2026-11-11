package org.evilmind.template;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public abstract class AbstractRiskEngine {

    protected Long engineId;
    protected BigDecimal baseThreshold;

    protected int[][][] riskMatrixCube;

    protected BigDecimal[][] factorWeightGrid;

    private final OffsetDateTime[] checkpointTimestamps;

    protected AbstractRiskEngine(Long engineId, BigDecimal baseThreshold, int[][][] riskMatrixCube, BigDecimal[][] factorWeightGrid, OffsetDateTime[] checkpointTimestamps) {
        this.engineId = engineId;
        this.baseThreshold = baseThreshold;
        this.riskMatrixCube = riskMatrixCube;
        this.factorWeightGrid = factorWeightGrid;
        this.checkpointTimestamps = checkpointTimestamps;
    }

    public Long getEngineId() {
        return engineId;
    }

    public BigDecimal getBaseThreshold() {
        return baseThreshold;
    }

    public int[][][] getRiskMatrixCube() {
        return riskMatrixCube;
    }

    public BigDecimal[][] getFactorWeightGrid() {
        return factorWeightGrid;
    }

    public OffsetDateTime[] getCheckpointTimestamps() {
        return checkpointTimestamps;
    }

    public final BigDecimal executeEvaluation(int[][][] inputCube) {
        if (!validateInput(inputCube)) {
            return BigDecimal.ZERO;
        }
        return computeRiskScore(inputCube, this.factorWeightGrid);
    }

    protected Boolean validateInput(int[][][] inputCube) {
        return inputCube != null && inputCube.length > 0;
    }

    // Método abstracto que las subclases DEBEN implementar
    protected abstract BigDecimal computeRiskScore(int[][][] rawMetrics, BigDecimal[][] weights);
}
