package com.universidad.modelo;

import com.cleandev.tpa.api.annotations.TpaConvert;
import com.cleandev.tpa.api.annotations.TpaId;
import com.universidad.modelo.convertidor.EstadoEntidadConverter;
import com.universidad.modelo.enumeracion.EstadoEntidad;

import java.util.UUID;

public class Estudiante {

    @TpaId
    private UUID idEstudiante;
    private String codigoEstudiante;
    private String nombreEstudiante;
    private String direccionEstudiante;
    private String correoEstudiante;
    private String celularEstudiante;

    @TpaConvert(converter = EstadoEntidadConverter.class)
    private EstadoEntidad estadoEstudiante;

    protected Estudiante() {
    }

    public Estudiante(
            String codigoEstudiante,
            String nombreEstudiante,
            String correoEstudiante,
            String celularEstudiante,
            String direccionEstudiante
    ) {
        this.codigoEstudiante = codigoEstudiante;
        this.nombreEstudiante = nombreEstudiante;
        this.correoEstudiante = correoEstudiante;
        this.celularEstudiante = celularEstudiante;
        this.direccionEstudiante = direccionEstudiante;
        this.estadoEstudiante = EstadoEntidad.ACTIVO;
    }

    public Estudiante(
            UUID idEstudiante,
            String codigoEstudiante,
            String nombreEstudiante,
            String correoEstudiante,
            String celularEstudiante,
            String direccionEstudiante,
            EstadoEntidad estadoEstudiante
    ) {
        this.codigoEstudiante = codigoEstudiante;
        this.nombreEstudiante = nombreEstudiante;
        this.correoEstudiante = correoEstudiante;
        this.celularEstudiante = celularEstudiante;
        this.direccionEstudiante = direccionEstudiante;
        this.estadoEstudiante = (estadoEstudiante != null) ? estadoEstudiante : EstadoEntidad.ACTIVO;

        if (idEstudiante == null) {
            throw new IllegalArgumentException("El ID del estudiante es obligatorio en hidratacion");
        }
        this.idEstudiante = idEstudiante;
    }

    public void actualizarCorreo(String nuevoCorreo) {
        if (nuevoCorreo.equalsIgnoreCase(this.correoEstudiante)) {
            throw new IllegalArgumentException("El nuevo correo es igual al actual");
        }
        this.correoEstudiante = nuevoCorreo;
    }

    public void actualizarCelular(String nuevoCelular) {
        if (nuevoCelular.equals(this.celularEstudiante)) {
            throw new IllegalArgumentException("El nuevo celular es igual al actual");
        }
        this.celularEstudiante = nuevoCelular;
    }

    public void actualizarDireccion(String nuevaDireccion) {
        if (nuevaDireccion.equalsIgnoreCase(this.direccionEstudiante)) {
            throw new IllegalArgumentException("La nueva direccion es igual a la actual");
        }
        this.direccionEstudiante = nuevaDireccion;
    }

    public void cambiarEstado(EstadoEntidad nuevoEstado) {
        this.estadoEstudiante = estadoEstudiante.cambiarEstadoA(nuevoEstado);
    }

    public boolean estaActivo() {
        return this.estadoEstudiante == EstadoEntidad.ACTIVO;
    }

    public UUID getIdEstudiante() {
        return idEstudiante;
    }

    public String getCodigoEstudiante() {
        return codigoEstudiante;
    }

    public String getNombreEstudiante() {
        return nombreEstudiante;
    }

    public String getDireccionEstudiante() {
        return direccionEstudiante;
    }

    public String getCorreoEstudiante() {
        return correoEstudiante;
    }

    public String getCelularEstudiante() {
        return celularEstudiante;
    }

    public EstadoEntidad getEstadoEstudiante() {
        return estadoEstudiante;
    }
}
