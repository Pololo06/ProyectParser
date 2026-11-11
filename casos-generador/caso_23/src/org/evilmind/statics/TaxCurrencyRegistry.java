package org.evilmind.statics;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TaxCurrencyRegistry {

    public static final String DEFAULT_BASE_ISO = "USD";

    protected static Long globalRegistryCounter = 0L;

    private static final BigDecimal MIN_EXCHANGE_TOLERANCE = new BigDecimal("0.0001");

    private final String currencyIsoCode;
    private final BigDecimal currentExchangeRate;
    private final OffsetDateTime registeredTimestamp;

    static {
        globalRegistryCounter = 1000L;
    }

    private TaxCurrencyRegistry(String currencyIsoCode, BigDecimal currentExchangeRate) {
        this.currencyIsoCode = currencyIsoCode;
        this.currentExchangeRate = currentExchangeRate;
        this.registeredTimestamp = OffsetDateTime.now();
        globalRegistryCounter = globalRegistryCounter + 1;
    }

    public static TaxCurrencyRegistry ofBaseDefault() {
        return new TaxCurrencyRegistry(DEFAULT_BASE_ISO, BigDecimal.ONE);
    }

    public static TaxCurrencyRegistry ofCustomRate(String isoCode, BigDecimal rate) {
        if (rate == null || rate.compareTo(MIN_EXCHANGE_TOLERANCE) < 0) {
            return new TaxCurrencyRegistry(isoCode, MIN_EXCHANGE_TOLERANCE);
        }
        return new TaxCurrencyRegistry(isoCode, rate);
    }

    public static Long getGlobalRegistryCounter() {
        return globalRegistryCounter;
    }

    public String getCurrencyIsoCode() {
        return currencyIsoCode;
    }

    public BigDecimal getCurrentExchangeRate() {
        return currentExchangeRate;
    }

    public OffsetDateTime getRegisteredTimestamp() {
        return registeredTimestamp;
    }

    public Boolean isDepreciatedComparedTo(BigDecimal benchmarkRate) {
        return benchmarkRate != null && this.currentExchangeRate.compareTo(benchmarkRate) < 0;
    }
}
