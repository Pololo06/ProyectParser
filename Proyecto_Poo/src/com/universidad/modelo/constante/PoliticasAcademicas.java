package com.universidad.modelo.constante;

public final class PoliticasAcademicas {

    private PoliticasAcademicas() {
        throw new UnsupportedOperationException("No se pueden crear objetos de esta clase");
    }
    
    public static final int MIN_SEMANAS_ASIGNATURA = 1;
    public static final int MAX_SEMANAS_ASIGNATURA = 16;
    public static final int MIN_CUPO_MATERIA = 10;
    public static final int MAX_CUPO_MATERIA = 30;
}
