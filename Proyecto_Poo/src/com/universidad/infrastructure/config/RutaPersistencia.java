package com.universidad.infrastructure.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public enum RutaPersistencia {
    PROFESORES("profesores.txt"),
    ESTUDIANTES("estudiantes.txt"),
    CURSOS("cursos.txt"),
    ASIGNATURAS("asignaturas.txt");

    private final Path ruta;
    
    RutaPersistencia(String nombreArchivo) {
        String baseDir = System.getProperty("app.data.dir", System.getProperty("user.dir"));
        ruta = Paths.get(baseDir, "misPersistencias", nombreArchivo);
    }
    
    public Path obtenerRuta() {
        return ruta;
    }

    /** @deprecated usar {@link #obtenerRuta()}; se conserva por compatibilidad. */
    @Deprecated
    public Path obetenerRuta() {
        return obtenerRuta();
    }
}   

