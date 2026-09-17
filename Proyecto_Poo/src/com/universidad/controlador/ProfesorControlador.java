package com.universidad.controlador;

import com.universidad.dto.profesor.ProfesorActualizarDto;

import com.universidad.dto.profesor.ProfesorCrearDto;
import com.universidad.dto.profesor.ProfesorDto;
import com.universidad.servicio.ProfesorServicio;
import java.util.List;

//MESERO
public class ProfesorControlador {

    private final ProfesorServicio servi;

    public ProfesorControlador(ProfesorServicio servi) {
        if (servi == null) {
            throw new IllegalArgumentException("Pilas con el Controlador");
        }
        this.servi = servi;
    }

    public ProfesorDto crearProfesor(ProfesorCrearDto dto) {
        return servi.registrarProfesor(dto);
    }

    public int cantidadProfesores() {
        return servi.contarProfesores();
    }

    public List<ProfesorDto> listarProfesores() {
        return servi.obtenerProfesores();
    }
    public ProfesorDto actualizarProfesor(ProfesorActualizarDto dto) {
        return servi.actualizarProfesor(dto);
    }

    public boolean eliminarProfesor(Long id) {
        return servi.eliminarProfesor(id);
    }

}
