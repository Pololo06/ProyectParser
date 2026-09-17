package com.universidad.vista;

import com.cleandev.cli.core.SystemModule;
import com.cleandev.cli.io.Console;
import com.cleandev.cli.io.ScreenFormatter;
import com.cleandev.cli.io.TableColumn;
import com.cleandev.cli.io.TableRenderer;
import com.universidad.controlador.AsignaturaControlador;
import com.universidad.dto.asignaturacosto.AsignaturaCostoCrearDto;
import com.universidad.dto.asignaturacosto.AsignaturaCostoDto;
import java.math.BigDecimal;
import java.util.List;

public class AsignaturaVista implements SystemModule {

    private final int ANCHO_TABLA;
    private final AsignaturaControlador controlador;
    private final List<TableColumn> columnasTablaMateria;

    public AsignaturaVista(AsignaturaControlador controlador) {
        if (controlador == null) {
            throw new IllegalArgumentException("Error en controlador");
        }

        this.controlador = controlador;
        this.columnasTablaMateria = List.of(
                new TableColumn("Cod", "%-6s"),
                new TableColumn("Nombre", "%-30s"),
                new TableColumn("semanas", "%-12s"),
                new TableColumn("costo", "%-20s"),
                new TableColumn("Estado", "%-10s"));
        ANCHO_TABLA = 75;
    }

    @Override
    public String getModuleName() {
        return "Modulo de asignaturas";
    }

    @Override
    public void execute(Console console, ScreenFormatter formatter, TableRenderer renderer) {
        mostrarMenu(console, formatter, renderer);
    }

    private void mostrarMenu(Console console, ScreenFormatter formatter, TableRenderer renderer) {
        boolean ejecutar = true;
        while (ejecutar) {
            console.showMenu("Modulo de Asignaturas",
                    "1. Crear Asignaturas",
                    "2. Listar Asignaturas: " + controlador.cantidadAsignatura(),
                    "0. Regresar al Menu Principal");
            String opcion = console.readText("Elige una opcion");
            if (opcion == null) {
                return;
            }
            switch (opcion.trim()) {
                case "1" ->
                    crearAsignatura(console, formatter, renderer);
                case "2" ->
                    listarAsignatura(console, formatter, renderer);
                case "0" ->
                    ejecutar = false;
                default ->
                    console.showError("Opcion incorrecta");
            }
        }
    }
    
    private void listarAsignatura(Console console, ScreenFormatter formatter, TableRenderer renderer) {
        List<AsignaturaCostoDto> Lista = controlador.listarAsignatura();
        if (Lista.isEmpty()) {
            console.showMessage("No hay Materias");
        }
        console.showMenu("Listado de las Materias");
        renderer.render(
                columnasTablaMateria, 
                Lista, 
                materia -> extraerDatosAsignatura(materia, formatter),
                ANCHO_TABLA      
        );
        console.pause();
    }
    
    private String textoEstado(AsignaturaCostoDto dto, ScreenFormatter formatter) {
        String descripcion = (dto != null && dto.estado() != null) ? dto.estado().getDescription() : null;
        return formatter.optionalText(descripcion);
    }
    
    private Object[] extraerDatosAsignatura(AsignaturaCostoDto dto, ScreenFormatter formatter) {
        return new Object[]{
            String.valueOf(dto.id()),
            dto.nombre(),
            dto.semanas(),
            dto.costoBase(),
            textoEstado(dto, formatter)
        };
    }
    
    private void crearAsignatura(Console console, ScreenFormatter formatter, TableRenderer renderer) {
        console.showMessage("\nNueva Asignatura");
        String nombre = console.readText("Dame el nombre [Enter cancelar]");
        if (nombre == null || nombre.isBlank()) {
            return;
        }
        Integer semanas = console.readInteger("Dame el Numero de semanas:");
        BigDecimal costoMateria = console.readBigDecimal("Dime el costo: ");
        AsignaturaCostoCrearDto dto = new AsignaturaCostoCrearDto(nombre, semanas.shortValue(), costoMateria);
        AsignaturaCostoDto dtoRespuesta = controlador.crearAsignatura(dto);

        renderer.renderSingle(
                columnasTablaMateria,
                dto,
                materia -> extraerDatosAsignatura(dtoRespuesta, formatter),
                ANCHO_TABLA);
        console.pause();
    }
}
