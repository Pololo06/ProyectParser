package com.universidad.dto.validacion;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class ReglasValidacion {

    private ReglasValidacion() {
    }

    public static String limpiarRequerido(String valor, String mensajeError) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensajeError);
        }
        return valor.trim();
    }

    public static UUID limpiarUuidRequerido(UUID valor, String mensajeError) {
        if (valor == null) {
            throw new IllegalArgumentException(mensajeError);
        }
        return valor;
    }

    public static Integer limpiarEnteroRequerido(Integer valor, String mensajeError) {
        if (valor == null) {
            throw new IllegalArgumentException(mensajeError);
        }
        return valor;
    }

    public static Long limpiarLongRequerido(Long valor, String mensajeError) {
        if (valor == null) {
            throw new IllegalArgumentException(mensajeError);
        }
        return valor;
    }

    public static String limpiarCorreo(String correo) {
        if (correo == null || !correo.contains("@")) {
            throw new IllegalArgumentException("El correo debe tener un formato válido");
        }
        return correo.trim();
    }

    public static String limpiarCelular(String celular) {
        if (celular == null || celular.trim().length() < 7) {
            throw new IllegalArgumentException("El celular debe tener al menos 7 dígitos");
        }
        return celular.trim();
    }

    public static Integer limpiarEnteroEnRango(Integer valor, int min, int max, String mensajeError) {
        if (valor == null) {
            throw new IllegalArgumentException(mensajeError);
        }
        if (valor < min || valor > max) {
            throw new IllegalArgumentException(mensajeError);
        }
        return valor;
    }

    public static Integer limpiarEnteroPositivo(Integer valor, String mensajeError) {
        if (valor == null) {
            throw new IllegalArgumentException(mensajeError);
        }
        if (valor <= 0) {
            throw new IllegalArgumentException(mensajeError);
        }
        return valor;
    }

    public static BigDecimal limpiarBigDecimalPositivo(BigDecimal valor, String mensajeError) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(mensajeError);
        }
        return valor;
    }

    public static BigDecimal limpiarBigDecimalNoNegativo(BigDecimal valor, String mensajeError) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(mensajeError);
        }
        return valor;
    }

    public static <T> List<T> limpiarListaRequerida(List<T> lista, String mensajeError) {
        if (lista == null || lista.isEmpty()) {
            throw new IllegalArgumentException(mensajeError);
        }
        for (T item : lista) {
            if (item == null) {
                throw new IllegalArgumentException("Los elementos de la lista no pueden ser nulos");
            }
        }
        return lista;
    }
}
