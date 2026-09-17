package com.universidad.modelo.convertidor;

import com.cleandev.tpa.api.converter.BaseCodedEnumConverter;
import com.universidad.modelo.enumeracion.EstadoEntidad;

public class EstadoEntidadConverter extends BaseCodedEnumConverter<EstadoEntidad>{

    @Override
    protected Class<EstadoEntidad> getEnumClass() {
        return EstadoEntidad.class;
    }
    
}
