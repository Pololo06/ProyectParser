package com.universidad.infrastructure.cli.constante;

import java.util.Locale;

public final class ConfiguracionCli {

    private ConfiguracionCli() {
        throw new UnsupportedOperationException(
                "No se pueden crear objetos de esta clase"
        );
    }

    public static final String TITULO_SISTEMA = "POO - UNIMAG";
    public static final String MENSAJE_SALIDA = "Bai Bai beibi";

    public static final String CARACTER_SEPARADOR = "*";
    public static final int LONGITUD_SEPARADOR = 80;
    public static final String PREFIJO_ERROR = "ERROR: ";
    public static final String TEXTO_NULO = "No registrado";
    public static final String TEXTO_SI = "Aceptar";
    public static final String TEXTO_NO = "Cancelar";
    public static final String FORMATO_FECHA_ENTRADA = "yyyy-MM-dd";
    public static final String FORMATO_FECHA_VISUAL = "dd/MM/yyyy";
    public static final String FORMATO_FECHA_HORA_VISUAL = "dd/MM/yyyy HH:mm";
    public static final Locale LOCALE = Locale.forLanguageTag("es-CO");

}
