package org.evilmind.statics;

import java.math.BigDecimal;

public class Case23Application {

    public static void main(String[] args) {

        TaxCurrencyRegistry eurCurrency = TaxCurrencyRegistry.ofCustomRate("EUR", new BigDecimal("1.0850"));

        System.out.println("=== Caso 23 ejecutado con exito ===");
        System.out.println("Base ISO: " + TaxCurrencyRegistry.DEFAULT_BASE_ISO);
        System.out.println("Moneda Creada: " + eurCurrency.getCurrencyIsoCode() + " -> Tasa: " + eurCurrency.getCurrentExchangeRate());
        System.out.println("Contador Global de Instancias: " + TaxCurrencyRegistry.getGlobalRegistryCounter());
    }
}
