package com.universidad.modelo;

import com.cleandev.tpa.api.annotations.TpaConvert;
import com.cleandev.tpa.api.annotations.TpaId;
import com.universidad.modelo.convertidor.EstadoEntidadConverter;
import com.universidad.modelo.enumeracion.EstadoEntidad;

import java.math.BigDecimal;

public class AsignaturaCosto {

    @TpaId
    private Integer idAsignaturaCosto;

    private String nombreAsignatura;
    private Short semanasDuracion;
    private BigDecimal costoBase;
    @TpaConvert(converter = EstadoEntidadConverter.class)
    private EstadoEntidad estadoAsignatura;

    // Reflexión
    protected AsignaturaCosto() {
    }

    // Creación
    public AsignaturaCosto(String nombreAsignatura, Short semanasDuracion, BigDecimal costoBase) {
        this.nombreAsignatura = nombreAsignatura;
        this.semanasDuracion = semanasDuracion;
        this.costoBase = costoBase;
        this.estadoAsignatura = EstadoEntidad.ACTIVO;
    }

    // Hidratación
    public AsignaturaCosto(
            Integer idAsignaturaCosto,
            String nombreAsignatura,
            Short semanasDuracion,
            BigDecimal costoBase,
            EstadoEntidad estadoAsignatura
    ) {

        if (idAsignaturaCosto == null) {
            throw new IllegalArgumentException("El ID de la asignatura costo es obligatorio en hidratación");
        }

        this.idAsignaturaCosto = idAsignaturaCosto;
        this.nombreAsignatura = nombreAsignatura;
        this.semanasDuracion = semanasDuracion;
        this.costoBase = costoBase;
        this.estadoAsignatura = (estadoAsignatura != null) ? estadoAsignatura : EstadoEntidad.ACTIVO;
    }

    // Métodos de comportamiento
    public void actualizarNombre(String nuevoNombre) {
        if (nuevoNombre.equalsIgnoreCase(this.nombreAsignatura)) {
            throw new IllegalArgumentException("El nuevo nombre es igual al actual");
        }
        this.nombreAsignatura = nuevoNombre;
    }

    public void actualizarSemanas(Short nuevasSemanas) {
        if (nuevasSemanas.equals(this.semanasDuracion)) {
            throw new IllegalArgumentException("Las nuevas semanas son iguales a las actuales");
        }
        this.semanasDuracion = nuevasSemanas;
    }

    public void actualizarCosto(BigDecimal nuevoCosto) {
        if (nuevoCosto.compareTo(this.costoBase) == 0) {
            throw new IllegalArgumentException("El nuevo costo es igual al actual");
        }
        this.costoBase = nuevoCosto;
    }

    public void suspender() {
        this.estadoAsignatura = EstadoEntidad.INACTIVO;
    }

    public void reactivar() {
        this.estadoAsignatura = EstadoEntidad.ACTIVO;
    }

    public boolean estaActiva() {
        return this.estadoAsignatura == EstadoEntidad.ACTIVO;
    }

    // Getters
    public Integer getIdAsignaturaCosto() {
        return idAsignaturaCosto;
    }

    public String getNombreAsignatura() {
        return nombreAsignatura;
    }

    public Short getSemanasDuracion() {
        return semanasDuracion;
    }

    public BigDecimal getCostoBase() {
        return costoBase;
    }

    public EstadoEntidad getEstadoAsignatura() {
        return estadoAsignatura;
    }
}
