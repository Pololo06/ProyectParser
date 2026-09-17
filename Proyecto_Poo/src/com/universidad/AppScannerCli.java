package com.universidad;

import com.cleandev.cli.config.CliConfig;
import com.universidad.vista.constante.ConfiguracionCli;

public final class AppScannerCli {

    private AppScannerCli() {
    }

    public static CliConfig configuracionCompleta() {
        return CliConfig.defaultConfig()
                .toBuilder()
                .systemTitle(ConfiguracionCli.TITULO_SISTEMA)
                .exitMessage(ConfiguracionCli.MENSAJE_SALIDA)
                .separatorChar(ConfiguracionCli.CARACTER_SEPARADOR)
                .separatorLength(ConfiguracionCli.LONGITUD_SEPARADOR)
                .errorPrefix(ConfiguracionCli.PREFIJO_ERROR)
                .defaultNullText(ConfiguracionCli.TEXTO_NULO)
                .defaultYesText(ConfiguracionCli.TEXTO_SI)
                .defaultNoText(ConfiguracionCli.TEXTO_NO)
                .inputDateFormat(ConfiguracionCli.FORMATO_FECHA_ENTRADA)
                .visualDateFormat(ConfiguracionCli.FORMATO_FECHA_VISUAL)
                .visualDateTimeFormat(ConfiguracionCli.FORMATO_FECHA_HORA_VISUAL)
                .locale(ConfiguracionCli.LOCALE)
                .build();
    }
}
