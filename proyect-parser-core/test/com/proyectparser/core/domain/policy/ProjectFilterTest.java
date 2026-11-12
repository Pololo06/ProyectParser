package com.proyectparser.core.domain.policy;

import com.proyectparser.core.domain.model.ClassModel;
import com.proyectparser.core.domain.model.ProjectModel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectFilterTest {

    private static ClassModel clase(String name, String pkg, String id) {
        ClassModel model = new ClassModel(name, pkg, "CLASS", false, List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of());
        return model.withId(id);
    }

    private static ProjectModel homonimos() {
        return new ProjectModel("p", List.of(
                clase("Foo", "a", "a.Foo"),
                clase("Foo", "b", "b.Foo")), List.of());
    }

    private static List<String> ids(ProjectModel project) {
        return project.getClasses().stream().map(ClassModel::id).sorted().toList();
    }

    @Test
    void elNombreSimpleSigueAfectandoATodosLosHomonimos() {
        DiagramFilter filtro = new DiagramFilter.Builder().includeClass("Foo").build();
        assertEquals(List.of("a.Foo", "b.Foo"), ids(ProjectFilter.apply(homonimos(), filtro)));
    }

    @Test
    void elFqnSeleccionaSoloSuHomonimo() {
        DiagramFilter filtro = new DiagramFilter.Builder().includeClass("a.Foo").build();
        assertEquals(List.of("a.Foo"), ids(ProjectFilter.apply(homonimos(), filtro)));
    }

    @Test
    void laBlacklistAceptaFqnSinAfectarAlHomonimo() {
        DiagramFilter filtro = new DiagramFilter.Builder().excludeClass("b.Foo").build();
        assertEquals(List.of("a.Foo"), ids(ProjectFilter.apply(homonimos(), filtro)));
    }

    @Test
    void elFqnTambienDistingueCuandoNoHayHomonimos() {
        ProjectModel proyecto = new ProjectModel("p",
                List.of(clase("Bar", "a", "Bar"), clase("Otra", "c", "Otra")), List.of());
        DiagramFilter filtro = new DiagramFilter.Builder().includeClass("a.Bar").build();
        assertEquals(List.of("Bar"), ids(ProjectFilter.apply(proyecto, filtro)));
    }
}
