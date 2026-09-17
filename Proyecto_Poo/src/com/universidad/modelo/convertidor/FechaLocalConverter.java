package com.universidad.modelo.convertidor;

import com.cleandev.tpa.api.converter.AttributeConverter;
import java.time.LocalDate;

/** Guarda la fecha como texto ISO: yyyy-MM-dd. */
public class FechaLocalConverter implements AttributeConverter<LocalDate> {
    @Override
    public String convertToDatabaseColumn(LocalDate fecha) {
        return fecha == null ? null : fecha.toString();
    }

    @Override
    public LocalDate convertToEntityAttribute(String texto) {
        return texto == null || texto.isBlank() ? null : LocalDate.parse(texto);
    }
}
