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
                clase("CursoMapeador", "x.application.mapper", "CLASS"),
                clase("CursoControlador", "x.interfaceadapters.controller", "CLASS"),
                clase("ConfiguracionModuloCurso", "x.infrastructure.config", "CLASS"),
                clase("ModuloConfigurable", "x.infrastructure.config", "INTERFACE"),
                clase("EstadoEntidad", "x.domain.model", "ENUM"),
                clase("RecursoX", "x.domain.model", "CLASS"),
                clase("Estudiante", "x.domain.model", "CLASS"),
                clase("EstudianteDto", "x.application.dto", "RECORD"),
                clase("EstudianteServicio", "x.application.service", "CLASS"),
                clase("EstudianteServicioPort", "x.application.port", "INTERFACE"),
                clase("EstudianteMapeador", "x.application.mapper", "CLASS"),
                clase("EstudianteVista", "x.interfaceadapters.view", "CLASS"),
                clase("EstudianteControlador", "x.interfaceadapters.controller", "CLASS"),
                externa("UUID")
        ), List.of(
                rel("CursoServicio", "CursoServicioPort", RelType.IMPLEMENTS),
                rel("ConfiguracionModuloCurso", "ModuloConfigurable", RelType.IMPLEMENTS),
                rel("Curso", "EstadoEntidad", RelType.ASSOCIATION),
                rel("Curso", "UUID", RelType.ASSOCIATION),
                rel("CursoServicio", "RecursoX", RelType.DEPENDENCY),
                rel("CursoServicio", "CursoDto", RelType.DEPENDENCY),
                rel("CursoControlador", "CursoDto", RelType.DEPENDENCY),
                rel("CursoServicioPort", "CursoDto", RelType.DEPENDENCY),
                rel("CursoMapeador", "CursoDto", RelType.DEPENDENCY),
                rel("CursoMapeador", "Curso", RelType.DEPENDENCY),
                rel("EstudianteServicio", "EstudianteServicioPort", RelType.IMPLEMENTS),
                rel("EstudianteServicioPort", "EstudianteDto", RelType.DEPENDENCY),
                rel("EstudianteServicio", "EstudianteDto", RelType.DEPENDENCY),
                rel("EstudianteMapeador", "EstudianteDto", RelType.DEPENDENCY),
                rel("EstudianteMapeador", "Estudiante", RelType.DEPENDENCY),
                rel("EstudianteVista", "EstudianteControlador", RelType.ASSOCIATION),
                rel("EstudianteControlador", "EstudianteServicioPort", RelType.ASSOCIATION),
                rel("EstudianteVista", "EstudianteDto", RelType.DEPENDENCY)
        ));
    }

    @Test
    void cursoNoIncluyeClasesQueSoloContienenElTextoComoRecursoX() {
        Set<String> clases = ModuleFilter.classesOf(proyecto(), "Curso");
        assertFalse(clases.contains("RecursoX"), clases.toString());
    }

    @Test
    void cursoIncluyeSusClasesSupertiposYAsociacionesInternas() {
        assertEquals(Set.of("Curso", "CursoDto", "CursoServicio", "CursoServicioPort", "CursoMapeador",
                        "CursoControlador", "ConfiguracionModuloCurso", "ModuloConfigurable", "EstadoEntidad"),
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

    @Test
    void namedClassesOfSoloIncluyeLasClasesNombradasPorElModulo() {
        assertEquals(Set.of("Curso", "CursoDto", "CursoServicio", "CursoServicioPort", "CursoMapeador",
                        "CursoControlador", "ConfiguracionModuloCurso"),
                ModuleFilter.namedClassesOf(proyecto(), "Curso"));
    }

    @Test
    void omiteLaDependenciaQueUnSupertipoDirectoYaTiene() {
        Set<String> deps = ModuleFilter.dependenciesOf(proyecto(), "Estudiante");
        assertTrue(deps.contains("EstudianteServicioPort->EstudianteDto"), deps.toString());
        assertFalse(deps.contains("EstudianteServicio->EstudianteDto"), deps.toString());
    }

    @Test
    void conservaLasDependenciasNoRedundantes() {
        Set<String> deps = ModuleFilter.dependenciesOf(proyecto(), "Estudiante");
        assertTrue(deps.contains("EstudianteMapeador->Estudiante"), deps.toString());
        // Vista -> Controlador -> Port es una cadena de asociaciones: no la vuelve redundante.
        assertTrue(deps.contains("EstudianteVista->EstudianteDto"), deps.toString());
    }

    @Test
    void conservaLasDependenciasEntreClasesDelModuloSalvoLasRedundantes() {
        assertEquals(Set.of("CursoServicioPort->CursoDto", "CursoControlador->CursoDto",
                        "CursoMapeador->CursoDto", "CursoMapeador->Curso"),
                ModuleFilter.dependenciesOf(proyecto(), "Curso"));
    }

    @Test
    void sinDependenciasConservaSoloLasPermitidas() {
        DiagramOptions opciones = new DiagramOptions.Builder()
                .showDependencies(false)
                .allowedDependencies(ModuleFilter.dependenciesOf(proyecto(), "Curso"))
                .build();
        assertTrue(opciones.showsDependency("CursoServicioPort", "CursoDto"));
        assertFalse(opciones.showsDependency("CursoServicio", "CursoDto"));
        assertTrue(opciones.showsDependency("CursoControlador", "CursoDto"));
        assertFalse(opciones.showsDependency("CursoServicio", "RecursoX"));
        assertTrue(new DiagramOptions.Builder().build().showsDependency("CursoServicio", "RecursoX"));
    }
}
