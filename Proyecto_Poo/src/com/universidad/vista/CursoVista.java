package com.universidad.vista;

import java.util.UUID;

import com.universidad.dto.curso.CursoActualizarDto;

import com.cleandev.cli.core.SystemModule;
import com.cleandev.cli.io.Console;
import com.cleandev.cli.io.ScreenFormatter;
import com.cleandev.cli.io.TableColumn;
import com.cleandev.cli.io.TableRenderer;
import com.universidad.controlador.CursoControlador;
import com.universidad.dto.curso.CursoCrearDto;
import com.universidad.dto.curso.CursoDto;
import java.util.List;

public class CursoVista implements SystemModule{
    private static int ANCHO_TABLA;
    
    private final CursoControlador controlador;
    private final List<TableColumn> columnas;

    public CursoVista(CursoControlador controlador) {
        if (controlador == null) {
            throw new IllegalArgumentException("El controlador es obligatorio");
        }
        
        this.controlador = controlador;
        
        this.columnas = List.of(
                new TableColumn("ID", "%-36s"),
                new TableColumn("Codigo Curso", "%-12s"),
                new TableColumn("Nombre Curso", "%-25s"),
                new TableColumn("Cdts", "%-4s"),
                new TableColumn("Cupo", "%-4s"),
                new TableColumn("Estado", "%-10s")
                
        );
        ANCHO_TABLA = 103;
    }

    @Override
    public String getModuleName() {
        return "Sistema de Cursos";
    }

    @Override
    public void execute(Console console, ScreenFormatter formatter, TableRenderer renderer) {
        mostrarMenu(console, formatter, renderer);
    }
    
    private void mostrarMenu(Console console, ScreenFormatter formatter, TableRenderer renderer) {
        boolean ejecutar = true;
        while (ejecutar) {
            console.showMenu("Modulo de Cursos",
                    "1. Crear Curso",
                    "2. Listar Cursos: " + controlador.cantidadCursos(),
                    "3. Actualizar Curso",
                    "4. Borrar Curso",
                    "0. Regresar al Menu Principal");
            String opcion = console.readText("Elige una opcion");
            if (opcion == null) return;
            switch (opcion.trim()) {
                case "1" -> crearCurso(console, formatter, renderer);
                case "2" -> listarCursos(console, formatter, renderer);
                case "3" -> actualizarCurso(console, formatter, renderer);
                case "4" -> eliminarCurso(console);
                case "0" -> ejecutar = false;
                default -> console.showError("Opcion incorrecta");
            }
        }
    }

    
    private Object[] extraerDatos(CursoDto dto, ScreenFormatter formatter) {
        String estado = dto.estado() == null? null: dto.estado().getDescription();
        
        return new Object[] {
            String.valueOf(dto.id()),
            dto.codigoCurso(),
            dto.nombreCurso(),
            dto.creditosCurso(),
            dto.cupoMaximoCurso(),
            formatter.optionalText(estado)  
        };
    }
    
    private void crearCurso(Console console, ScreenFormatter formatter, TableRenderer renderer) {
        console.showMessage("\nNuevo Curso");
        
        String codigoCurso = console.readText("Codigo [Enter para cancelar]");
        
        if (codigoCurso == null || codigoCurso.isBlank()) {
            return;
        }
        
        String nombreCurso = console.readText("Nombre del Curso");
        Integer creditosCurso = console.readInteger("Creditos del curso");
        Integer cupoMaximoCurso = console.readInteger("Cupo maximo del Curso");
        try {
            CursoCrearDto dto = new CursoCrearDto(
                    codigoCurso, 
                    nombreCurso, 
                    creditosCurso, 
                    cupoMaximoCurso
            );
            
            CursoDto respuesta = controlador.CrearCurso(dto);
            
            console.showMessage("Curso registrado Correctamente");
            
            renderer.renderSingle(
                    columnas, 
                    respuesta, 
                    Curso -> extraerDatos(Curso, formatter), 
                    ANCHO_TABLA
            );
        } catch (IllegalArgumentException e) {
            console.showMessage(e.getMessage());
        }
        console.pause();
    }
    
    private void listarCursos(Console console, ScreenFormatter formatter, TableRenderer renderer) {
        List<CursoDto> cursos = controlador.listarCursos();
        
        if (cursos.isEmpty()) {
            console.showMessage("No hay cursos registrados");
            console.pause();
            return;
        }
        
        console.showMessage("\nListado de Cursos");
        
        renderer.render(
                columnas, 
                cursos, 
                Curso -> extraerDatos(Curso, formatter),
                ANCHO_TABLA
        );
        console.pause();
    }
    private void actualizarCurso(Console console, ScreenFormatter formatter, TableRenderer renderer) {
        console.showMessage("Actualizar curso: Enter conserva el valor actual");
        String textoId = console.readText("ID del curso [Enter cancela]");
        if (textoId == null || textoId.isBlank()) return;
        try {
            UUID id = UUID.fromString(textoId.trim());
            
            String nuevoNombre = console.readText("nuevoNombre [Enter conserva]");
            Integer nuevoCupo = leerEnteroOpcional(console, "Nuevo cupo [Enter conserva]");
            Integer estado = leerEnteroOpcional(console, "Estado: 1 Activo, 2 Inactivo [Enter conserva]");
            CursoActualizarDto dto = new CursoActualizarDto(id, nuevoNombre, nuevoCupo, estado);
            CursoDto respuesta = controlador.actualizarCurso(dto);
            console.showMessage("Curso actualizado correctamente");
            renderer.renderSingle(columnas, respuesta, item -> extraerDatos(item, formatter), ANCHO_TABLA);
        } catch (IllegalArgumentException | IllegalStateException e) {
            console.showError("No se pudo actualizar: " + e.getMessage());
        }
        console.pause();
    }

    private void eliminarCurso(Console console) {
        String textoId = console.readText("ID del curso a borrar [Enter cancela]");
        if (textoId == null || textoId.isBlank()) return;
        try {
            UUID id = UUID.fromString(textoId.trim());
            
            String confirmar = console.readText("Borrar definitivamente curso con ID " + id + "? Escribe SI");
            if (confirmar == null || !confirmar.trim().equalsIgnoreCase("SI")) {
                console.showMessage("Operacion cancelada");
                return;
            }
            if (controlador.eliminarCurso(id)) console.showMessage("Curso eliminado correctamente");
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
