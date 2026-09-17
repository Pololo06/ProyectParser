package com.universidad.modelo;

import com.cleandev.tpa.api.annotations.TpaConvert;
import com.cleandev.tpa.api.annotations.TpaId;
import com.universidad.modelo.convertidor.EstadoEntidadConverter;
import com.universidad.modelo.enumeracion.EstadoEntidad;

public class Profesor {

    @TpaId
    private Long idProfesor;
    private String nombreProfesor;
    private String celularProfesor;
    @TpaConvert(converter = EstadoEntidadConverter.class)
    private EstadoEntidad estadoProfesor;

    // Reflexion de otras librerias
    protected Profesor() {
    }

    
    
    public Profesor(
            String nombreProfesor,
            String celularProfesor
    ) {
        this.nombreProfesor = nombreProfesor;
        this.celularProfesor = celularProfesor;
        this.estadoProfesor = estadoProfesor.ACTIVO;
    }

    // Hidratacion
    public Profesor(
            Long idProfesor,
            String nombreProfesor,
            String celularProfesor,
            EstadoEntidad estadoProfesor
    ) {
        this.idProfesor = idProfesor;
        this.nombreProfesor = nombreProfesor;
        this.celularProfesor = celularProfesor;
        this.estadoProfesor = (estadoProfesor != null) ? estadoProfesor : EstadoEntidad.ACTIVO;

        if (idProfesor == null) {
            throw new IllegalArgumentException("ID profesor es obligatorio");
        }
        this.idProfesor = idProfesor;
    }

    public void actualizarNombre(String nuevoNombre) {
        if (nuevoNombre.equalsIgnoreCase(this.nombreProfesor)) {
            throw new IllegalArgumentException("Los nombre son iguales");
        }
        this.nombreProfesor = nuevoNombre;
    }

    public void actualizarCelular(String nuevoCelular) {
        if (nuevoCelular.equalsIgnoreCase(this.celularProfesor)) {
            throw new IllegalArgumentException("Celulares iguales");
        }
        this.celularProfesor = nuevoCelular;
    }

    public void cambiarEstado(EstadoEntidad nuevoEstado) {
        this.estadoProfesor = this.estadoProfesor.cambiarEstadoA(nuevoEstado);
    }

    public boolean estaActivo() {
        return this.estadoProfesor == EstadoEntidad.ACTIVO;
    }
    
    // Getters

    public Long getIdProfesor() {
        return idProfesor;
    }

    public String getNombreProfesor() {
        return nombreProfesor;
    }

    public String getCelularProfesor() {
        return celularProfesor;
    }

    public EstadoEntidad getEstadoProfesor() {
        return estadoProfesor;
    }
    
}
