package com.universidad.vista;

import java.util.UUID;

import com.universidad.dto.estudiante.EstudianteActualizarDto;

import com.cleandev.cli.core.SystemModule;
import com.cleandev.cli.io.Console;
import com.cleandev.cli.io.ScreenFormatter;
import com.cleandev.cli.io.TableColumn;
import com.cleandev.cli.io.TableRenderer;
import com.universidad.controlador.EstudianteControlador;
import com.universidad.dto.estudiante.EstudianteCrearDto;
import com.universidad.dto.estudiante.EstudianteDto;
import java.util.List;

public class EstudianteVista implements SystemModule{
    private static final int ANCHO_TABLA = 175;
    
    private final EstudianteControlador controlador;
    private final List<TableColumn> columnas;

    public EstudianteVista(EstudianteControlador controlador) {
        if (controlador == null) {
            throw new IllegalArgumentException("El controlador es obligatorio");
        }
        this.controlador = controlador;
        
        this.columnas = List.of(
                new TableColumn("ID", "%-36s"),
                new TableColumn("Codigo", "%-12s"),
                new TableColumn("Nombre", "%-25s"),
                new TableColumn("Correo", "%-30s"),
                new TableColumn("Celular", "%-15s"),
                new TableColumn("Direccion", "%-25s"),
                new TableColumn("Estado", "%-10s")
        );
    }

    @Override
    public String getModuleName() {
        return "Sistema de Estudiantes";
    }

    @Override
    public void execute(Console console, ScreenFormatter formatter, TableRenderer renderer) {
        mostrarMenu(console, formatter, renderer);
    }
    
    private void mostrarMenu(Console console, ScreenFormatter formatter, TableRenderer renderer) {
        boolean ejecutar = true;
        while (ejecutar) {
            console.showMenu("Modulo de Estudiantes",
                    "1. Crear Estudiante",
                    "2. Listar Estudiantes: " + controlador.cantidadEstudiantes(),
                    "3. Actualizar Estudiante",
                    "4. Borrar Estudiante",
                    "0. Regresar al Menu Principal");
            String opcion = console.readText("Elige una opcion");
            if (opcion == null) return;
            switch (opcion.trim()) {
                case "1" -> crearEstudiante(console, formatter, renderer);
                case "2" -> listarEstudiantes(console, formatter, renderer);
                case "3" -> actualizarEstudiante(console, formatter, renderer);
                case "4" -> eliminarEstudiante(console);
                case "0" -> ejecutar = false;
                default -> console.showError("Opcion incorrecta");
            }
        }
    }

    
    private Object[] extraerDatos(EstudianteDto dto, ScreenFormatter formatter) {
        String estado = dto.estado() == null? null: dto.estado().getDescription();
        
        return new Object[] {
            String.valueOf(dto.id()),
            dto.codigo(),
            dto.nombre(),
            formatter.optionalText(dto.correo()),
            formatter.optionalText(dto.celular()),
            formatter.optionalText(dto.direccion()),
            formatter.optionalText(estado)
        };
    }
    
    private void crearEstudiante(Console console, ScreenFormatter formatter, TableRenderer renderer) {
        console.showMessage("\nNuevo estudiante");
        
        String codigo = console.readText("Codigo [Enter para cancelar]");
        
        if (codigo == null || codigo.isBlank()) {
            return;
        }
        
        String nombre = console.readText("Nombre");
        String correo = console.readText("Correo");
        String celular = console.readText("Celular");
        String direccion = console.readText("Direccion");
        try {
            EstudianteCrearDto dto = new EstudianteCrearDto(
                    codigo,
                    nombre,
                    correo,
                    celular, 
                    direccion
            );
            
            EstudianteDto respuesta = controlador.crearEstudiante(dto);
            
            console.showMessage("Estudiante registrado Correctamente");
            
            renderer.renderSingle(
                    columnas,
                    respuesta, 
                    Estudiante -> extraerDatos(Estudiante, formatter),
                    ANCHO_TABLA
            );
        } catch (IllegalArgumentException e) {
            console.showMessage(e.getMessage());
        }
        console.pause();
    }   
    
    private void listarEstudiantes(Console console, ScreenFormatter formatter, TableRenderer renderer) {
        List<EstudianteDto> estudiantes = controlador.listarEstudiantes();
        
        if (estudiantes.isEmpty()) {
            console.showMessage("No hay estudaintes registrados");
            console.pause();
            return;
        }
        
        console.showMessage("\nListado de Estudiantes");
        
        renderer.render(
                columnas, 
                estudiantes, 
                Estudiante -> extraerDatos(Estudiante, formatter),
                ANCHO_TABLA
        );
        
        console.pause();
    }
    
    private void actualizarEstudiante(Console console, ScreenFormatter formatter, TableRenderer renderer) {
        console.showMessage("Actualizar estudiante: Enter conserva el valor actual");
        String textoId = console.readText("ID del estudiante [Enter cancela]");
        if (textoId == null || textoId.isBlank()) return;
        try {
            UUID id = UUID.fromString(textoId.trim());
            
            String nuevoCorreo = console.readText("nuevoCorreo [Enter conserva]");
            String nuevoCelular = console.readText("nuevoCelular [Enter conserva]");
            String nuevaDireccion = console.readText("nuevaDireccion [Enter conserva]");
            Integer estado = leerEnteroOpcional(console, "Estado: 1 Activo, 2 Inactivo [Enter conserva]");
            EstudianteActualizarDto dto = new EstudianteActualizarDto(id, nuevoCorreo, nuevoCelular, nuevaDireccion, estado);
            EstudianteDto respuesta = controlador.actualizarEstudiante(dto);
            console.showMessage("Estudiante actualizado correctamente");
            renderer.renderSingle(columnas, respuesta, item -> extraerDatos(item, formatter), ANCHO_TABLA);
        } catch (IllegalArgumentException | IllegalStateException e) {
            console.showError("No se pudo actualizar: " + e.getMessage());
        }
        console.pause();
    }

    private void eliminarEstudiante(Console console) {
        String textoId = console.readText("ID del estudiante a borrar [Enter cancela]");
        if (textoId == null || textoId.isBlank()) return;
        try {
            UUID id = UUID.fromString(textoId.trim());
            
            String confirmar = console.readText("Borrar definitivamente estudiante con ID " + id + "? Escribe SI");
            if (confirmar == null || !confirmar.trim().equalsIgnoreCase("SI")) {
                console.showMessage("Operacion cancelada");
                return;
            }
            if (controlador.eliminarEstudiante(id)) console.showMessage("Estudiante eliminado correctamente");
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
