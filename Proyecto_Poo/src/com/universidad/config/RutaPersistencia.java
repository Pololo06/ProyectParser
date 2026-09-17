package com.universidad.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public enum RutaPersistencia {
    PROFESORES("profesore.txt"),
    ESTUDIANTES("estudiantes.txt"),
    CURSOS("cursos.txt"),
    ASIGNATURAS("asignaturas.txt");

    private final Path ruta;
    
    RutaPersistencia(String nombreArchivo) {
        ruta = Paths.get(
                System.getProperty("user.dir"), 
                "misPersistencias", 
                nombreArchivo);
    }
    
    public Path obetenerRuta() {
        return ruta;
    }
}   

