package com.universidad.vista;

import com.universidad.dto.profesor.ProfesorActualizarDto;

import com.cleandev.cli.core.SystemModule;
import com.cleandev.cli.io.Console;
import com.cleandev.cli.io.ScreenFormatter;
import com.cleandev.cli.io.TableColumn;
import com.cleandev.cli.io.TableRenderer;
import com.universidad.controlador.ProfesorControlador;
import com.universidad.dto.profesor.ProfesorCrearDto;
import com.universidad.dto.profesor.ProfesorDto;
import java.util.List;

public class ProfesorVista implements SystemModule {

    private final int ANCHO_TABLA;
    private final ProfesorControlador controlador;
    private final List<TableColumn> columnasTablaProfe;

    public ProfesorVista(ProfesorControlador controlador) {
        if (controlador == null) {
            throw new IllegalArgumentException("Error en controlador");
        }

        this.controlador = controlador;
        this.columnasTablaProfe = List.of(
                new TableColumn("Cod", "%-6s"),
                new TableColumn("Nombre", "%-30s"),
                new TableColumn("Telefono", "%-12s"),
                new TableColumn("Estado", "%-10s"));
        ANCHO_TABLA = 75;
    }

    @Override
    public String getModuleName() {
        return "Sistema de Profesores";
    }

    @Override
    public void execute(Console cnsl, ScreenFormatter sf, TableRenderer tr) {
        mostrarMenu(cnsl, sf, tr);
    }

    private void mostrarMenu(Console console, ScreenFormatter formatter, TableRenderer renderer) {
        boolean ejecutar = true;
        while (ejecutar) {
            console.showMenu("Modulo de Profesores",
                    "1. Crear Profesor",
                    "2. Listar Profesores: " + controlador.cantidadProfesores(),
                    "3. Actualizar Profesor",
                    "4. Borrar Profesor",
                    "0. Regresar al Menu Principal");
            String opcion = console.readText("Elige una opcion");
            if (opcion == null) return;
            switch (opcion.trim()) {
                case "1" -> crearProfe(console, formatter, renderer);
                case "2" -> listarProfes(console, formatter, renderer);
                case "3" -> actualizarProfesor(console, formatter, renderer);
                case "4" -> eliminarProfesor(console);
                case "0" -> ejecutar = false;
                default -> console.showError("Opcion incorrecta");
            }
        }
    }


    private String textoEstado(ProfesorDto dto, ScreenFormatter formatter) {
        String descripcion = (dto != null && dto.estado() != null) ? dto.estado().getDescription() : null;
        return formatter.optionalText(descripcion);
    }

    private Object[] extraerDatosProfesor(ProfesorDto dto, ScreenFormatter formatter) {
        return new Object[]{
            String.valueOf(dto.id()),
            dto.nombre(),
            formatter.optionalText(dto.celular()),
            textoEstado(dto, formatter)
        };
    }

    private void listarProfes(Console console, ScreenFormatter formatter, TableRenderer renderer) {
        List<ProfesorDto> Lista = controlador.listarProfesores();
        if (Lista.isEmpty()) {
            console.showMessage("No hay profes");
        }
        console.showMenu("Listado de los Profes");
        renderer.render(
                columnasTablaProfe, 
                Lista, 
                profe -> extraerDatosProfesor(profe, formatter),
                ANCHO_TABLA      
        );
        console.pause();
    }

    private void crearProfe(Console console,
            ScreenFormatter formatter, TableRenderer renderer) {
        console.showMessage("\nNuevo profesor");
        String nombre = console.readText("Dame el nombre [Enter cancelar]");
        if (nombre == null || nombre.isBlank()) {
            return;
        }
        String celular = console.readText("Dame el celular");
        ProfesorCrearDto dto = new ProfesorCrearDto(nombre, celular);
        ProfesorDto dtoRespuesta = controlador.crearProfesor(dto);

        renderer.renderSingle(
                columnasTablaProfe,
                dto,
                profe -> extraerDatosProfesor(dtoRespuesta, formatter),
                ANCHO_TABLA);
        console.pause();
    }

    private void actualizarProfesor(Console console, ScreenFormatter formatter, TableRenderer renderer) {
        console.showMessage("Actualizar profesor: Enter conserva el valor actual");
        String textoId = console.readText("ID del profesor [Enter cancela]");
        if (textoId == null || textoId.isBlank()) return;
        try {
            Long id = Long.valueOf(textoId.trim());
            if (id <= 0) throw new IllegalArgumentException("ID invalido");
            String nuevoNombre = console.readText("nuevoNombre [Enter conserva]");
            String nuevoCelular = console.readText("nuevoCelular [Enter conserva]");
            Integer estado = leerEnteroOpcional(console, "Estado: 1 Activo, 2 Inactivo [Enter conserva]");
            ProfesorActualizarDto dto = new ProfesorActualizarDto(id, nuevoNombre, nuevoCelular, estado);
            ProfesorDto respuesta = controlador.actualizarProfesor(dto);
            console.showMessage("Profesor actualizado correctamente");
            renderer.renderSingle(columnasTablaProfe, respuesta, item -> extraerDatosProfesor(item, formatter), ANCHO_TABLA);
        } catch (IllegalArgumentException | IllegalStateException e) {
            console.showError("No se pudo actualizar: " + e.getMessage());
        }
        console.pause();
    }

    private void eliminarProfesor(Console console) {
        String textoId = console.readText("ID del profesor a borrar [Enter cancela]");
        if (textoId == null || textoId.isBlank()) return;
        try {
            Long id = Long.valueOf(textoId.trim());
            if (id <= 0) throw new IllegalArgumentException("ID invalido");
            String confirmar = console.readText("Borrar definitivamente profesor con ID " + id + "? Escribe SI");
            if (confirmar == null || !confirmar.trim().equalsIgnoreCase("SI")) {
                console.showMessage("Operacion cancelada");
                return;
            }
            if (controlador.eliminarProfesor(id)) console.showMessage("Profesor eliminado correctamente");
            else console.showError("No se elimino el registro");
        } catch (IllegalArgumentException | IllegalStateException e) {
            console.showError("No se pudo borrar: " + e.getMessage());
        }
        console.pause();
    }

    private Integer leerEnteroOpcional(Console console, String mensaje) {
        String texto = console.readText(mensaje);
        if (texto == null || texto.isBlank()) return null;
        try {
            return Integer.valueOf(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Debes escribir un numero entero o dejar el campo vacio");
        }
    }

}
