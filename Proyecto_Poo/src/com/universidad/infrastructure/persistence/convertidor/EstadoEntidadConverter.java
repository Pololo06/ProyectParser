package com.universidad.infrastructure.persistence.convertidor;

import com.cleandev.tpa.api.converter.BaseCodedEnumConverter;
import com.universidad.domain.model.enumeracion.EstadoEntidad;

public class EstadoEntidadConverter extends BaseCodedEnumConverter<EstadoEntidad>{

    @Override
    protected Class<EstadoEntidad> getEnumClass() {
        return EstadoEntidad.class;
    }
    
}
