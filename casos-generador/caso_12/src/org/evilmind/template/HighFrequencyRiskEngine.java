package org.evilmind.template;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class HighFrequencyRiskEngine extends AbstractRiskEngine {

    private final Short latencyThresholdMs;

    public HighFrequencyRiskEngine(Long engineId, BigDecimal baseThreshold, int[][][] riskMatrixCube, BigDecimal[][] factorWeightGrid, OffsetDateTime[] checkpointTimestamps, Short latencyThresholdMs) {
        super(engineId, baseThreshold, riskMatrixCube, factorWeightGrid, checkpointTimestamps);
        this.latencyThresholdMs = latencyThresholdMs;
    }

    public Short getLatencyThresholdMs() {
        return latencyThresholdMs;
    }

    @Override
    protected BigDecimal computeRiskScore(int[][][] rawMetrics, BigDecimal[][] weights) {
        return this.baseThreshold.multiply(new BigDecimal(latencyThresholdMs.toString()));
    }
}
