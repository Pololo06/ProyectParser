package com.proyectparser.core.domain.policy;

import com.proyectparser.core.domain.model.ClassModel;
import com.proyectparser.core.domain.model.ProjectModel;
import com.proyectparser.core.domain.model.RelType;
import com.proyectparser.core.domain.model.RelationshipModel;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleFilterTest {

    private static ClassModel clase(String name, String pkg, String kind) {
        return new ClassModel(name, pkg, kind, false, List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of());
    }

    private static ClassModel externa(String name) {
        return new ClassModel(name, "java.util", "CLASS", false, List.of("@external"), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of());
    }

    private static RelationshipModel rel(String source, String target, RelType type) {
        return new RelationshipModel(source, target, type.label());
    }

    private static ProjectModel proyecto() {
        return new ProjectModel("p", List.of(
                clase("Curso", "x.domain.model", "CLASS"),
                clase("CursoDto", "x.application.dto", "RECORD"),
                clase("CursoServicio", "x.application.service", "CLASS"),
                clase("CursoServicioPort", "x.application.port", "INTERFACE"),
                clase("ConfiguracionModuloCurso", "x.infrastructure.config", "CLASS"),
                clase("ModuloConfigurable", "x.infrastructure.config", "INTERFACE"),
                clase("EstadoEntidad", "x.domain.model", "ENUM"),
                clase("RecursoX", "x.domain.model", "CLASS"),
                clase("Estudiante", "x.domain.model", "CLASS"),
                externa("UUID")
        ), List.of(
                rel("CursoServicio", "CursoServicioPort", RelType.IMPLEMENTS),
                rel("ConfiguracionModuloCurso", "ModuloConfigurable", RelType.IMPLEMENTS),
                rel("Curso", "EstadoEntidad", RelType.ASSOCIATION),
                rel("Curso", "UUID", RelType.ASSOCIATION),
                rel("CursoServicio", "RecursoX", RelType.DEPENDENCY)
        ));
    }

    @Test
    void cursoNoIncluyeClasesQueSoloContienenElTextoComoRecursoX() {
        Set<String> clases = ModuleFilter.classesOf(proyecto(), "Curso");
        assertFalse(clases.contains("RecursoX"), clases.toString());
    }

    @Test
    void cursoIncluyeSusClasesSupertiposYAsociacionesInternas() {
        assertEquals(Set.of("Curso", "CursoDto", "CursoServicio", "CursoServicioPort",
                        "ConfiguracionModuloCurso", "ModuloConfigurable", "EstadoEntidad"),
                ModuleFilter.classesOf(proyecto(), "Curso"));
    }

    @Test
    void ignoraMayusculasDelModulo() {
        assertEquals(ModuleFilter.classesOf(proyecto(), "Curso"), ModuleFilter.classesOf(proyecto(), "curso"));
    }

    @Test
    void moduloDeVariasPalabrasCoincideConPalabrasConsecutivas() {
        assertTrue(ModuleFilter.classesOf(proyecto(), "ServicioPort").contains("CursoServicioPort"));
        assertFalse(ModuleFilter.classesOf(proyecto(), "ServicioPort").contains("CursoServicio"));
    }

    @Test
    void separaPalabrasCamelCaseConSiglas() {
        assertEquals(List.of("uuid", "curso", "dto"), ModuleFilter.words("UUIDCursoDto"));
        assertEquals(List.of("recurso", "x"), ModuleFilter.words("RecursoX"));
    }

    @Test
    void moduloVacioNoSeleccionaNada() {
        assertTrue(ModuleFilter.classesOf(proyecto(), " ").isEmpty());
    }
}
