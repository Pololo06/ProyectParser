package com.universidad.modelo;

import com.cleandev.tpa.api.annotations.TpaConvert;
import com.cleandev.tpa.api.annotations.TpaId;
import com.universidad.modelo.convertidor.EstadoEntidadConverter;
import com.universidad.modelo.enumeracion.EstadoEntidad;
import java.util.UUID;

public class Curso {

    @TpaId
    private UUID idCurso;

    private String codigoCurso;
    private String nombreCurso;
    private Integer creditosCurso;
    private Integer cupoMaximoCurso;

    @TpaConvert(converter = EstadoEntidadConverter.class)
    private EstadoEntidad estadoCurso;

    protected Curso() {
    }

    public Curso(
            String codigoCurso,
            String nombreCurso,
            Integer creditosCurso,
            Integer cupoMaximoCurso
    ) {
        this.codigoCurso = codigoCurso;
        this.nombreCurso = nombreCurso;
        this.creditosCurso = creditosCurso;
        this.cupoMaximoCurso = cupoMaximoCurso;
        this.estadoCurso = EstadoEntidad.ACTIVO;
    }

    public Curso(
            UUID idCurso,
            String codigoCurso,
            String nombreCurso,
            Integer creditosCurso,
            Integer cupoMaximoCurso,
            EstadoEntidad estadoCurso
    ) {
        if (idCurso == null) {
            throw new IllegalArgumentException(
                    "El ID del curso es obligatorio en hidratacion");
        }

        this.idCurso = idCurso;
        this.codigoCurso = codigoCurso;
        this.nombreCurso = nombreCurso;
        this.creditosCurso = creditosCurso;
        this.cupoMaximoCurso = cupoMaximoCurso;
        this.estadoCurso = estadoCurso != null
                ? estadoCurso
                : EstadoEntidad.ACTIVO;
    }

    public void actualizarNombre(String nuevoNombre) {
        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre es obligatorio");
        }

        String nombreLimpio = nuevoNombre.trim();

        if (nombreLimpio.equalsIgnoreCase(this.nombreCurso)) {
            throw new IllegalArgumentException(
                    "El nuevo nombre es igual al actual");
        }

        this.nombreCurso = nombreLimpio;
    }

    public void actualizarCupo(Integer nuevoCupo) {
        if (nuevoCupo == null || nuevoCupo <= 0) {
            throw new IllegalArgumentException(
                    "El cupo debe ser mayor que cero");
        }

        if (nuevoCupo.equals(this.cupoMaximoCurso)) {
            throw new IllegalArgumentException(
                    "El nuevo cupo es igual al actual");
        }

        this.cupoMaximoCurso = nuevoCupo;
    }

    public void cambiarEstado(EstadoEntidad nuevoEstado) {
        this.estadoCurso = this.estadoCurso.cambiarEstadoA(nuevoEstado);
    }

    public boolean estaActivo() {
        return this.estadoCurso == EstadoEntidad.ACTIVO;
    }

    public UUID getIdCurso() {
        return idCurso;
    }

    public String getCodigoCurso() {
        return codigoCurso;
    }

    public String getNombreCurso() {
        return nombreCurso;
    }

    public Integer getCreditosCurso() {
        return creditosCurso;
    }

    public Integer getCupoMaximoCurso() {
        return cupoMaximoCurso;
    }

    public EstadoEntidad getEstadoCurso() {
        return estadoCurso;
    }
}
