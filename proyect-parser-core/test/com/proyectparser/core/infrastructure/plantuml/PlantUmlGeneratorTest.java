package com.proyectparser.core.infrastructure.plantuml;

import com.proyectparser.core.domain.model.ClassModel;
import com.proyectparser.core.domain.model.ProjectModel;
import com.proyectparser.core.domain.model.RelType;
import com.proyectparser.core.domain.model.RelationshipModel;
import com.proyectparser.core.domain.policy.DiagramOptions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlantUmlGeneratorTest {

    private static ClassModel clase(String name) {
        return new ClassModel(name, "x.app", "CLASS", false, List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of());
    }

    /** A --> B, C ..> D (only a dependency), E alone. */
    private static ProjectModel proyecto() {
        return new ProjectModel("p",
                List.of(clase("A"), clase("B"), clase("C"), clase("D"), clase("E")),
                List.of(new RelationshipModel("A", "B", RelType.ASSOCIATION.label()),
                        new RelationshipModel("C", "D", RelType.DEPENDENCY.label())));
    }

    private static String render(DiagramOptions.Builder options) {
        return new PlantUmlGenerator().render(proyecto(), options.build());
    }

    private static boolean declara(String puml, String name) {
        return puml.contains("class " + name + " {");
    }

    @Test
    void porDefectoSeDeclaranTodasLasClases() {
        String puml = render(new DiagramOptions.Builder());
        for (String name : List.of("A", "B", "C", "D", "E")) {
            assertTrue(declara(puml, name), name);
        }
    }

    @Test
    void sinHuerfanosOmiteLasClasesSinRelaciones() {
        String puml = render(new DiagramOptions.Builder().hideOrphans(true));
        assertTrue(declara(puml, "A"));
        assertTrue(declara(puml, "B"));
        assertTrue(declara(puml, "C"));
        assertTrue(declara(puml, "D"));
        assertFalse(declara(puml, "E"));
    }

    @Test
    void sinHuerfanosSeAplicaTrasOcultarDependencias() {
        String puml = render(new DiagramOptions.Builder().hideOrphans(true).showDependencies(false));
        assertTrue(declara(puml, "A"));
        assertTrue(declara(puml, "B"));
        assertFalse(declara(puml, "C"));
        assertFalse(declara(puml, "D"));
        assertFalse(declara(puml, "E"));
    }

    @Test
    void resumenEmiteHideMembersSoloSiSePide() {
        assertTrue(render(new DiagramOptions.Builder().summary(true)).contains("\nhide members\n"));
        assertFalse(render(new DiagramOptions.Builder()).contains("hide members"));
    }

    @Test
    void agruparCapasEnvuelveLosPaquetesYEnlazaContenedores() {
        ClassModel ui = new ClassModel("Vista", "x.cli", "CLASS", false, List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of());
        ClassModel dom = new ClassModel("Entidad", "x.domain", "CLASS", false, List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of());
        ClassModel suelta = new ClassModel("Util", "x.util", "CLASS", false, List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of());
        ProjectModel proyecto = new ProjectModel("p", List.of(ui, dom, suelta), List.of());
        String puml = new PlantUmlGenerator().render(proyecto, new DiagramOptions.Builder()
                .layerOrder(List.of("cli", "domain")).groupLayers(true).build());
        assertTrue(puml.contains("package \"cli\" as layer_0 {\n  package \"x.cli\" as pkg_x_cli {"), puml);
        assertTrue(puml.contains("package \"domain\" as layer_1 {"), puml);
        assertTrue(puml.contains("\npackage \"x.util\" as pkg_x_util {"), puml);
        assertTrue(puml.contains("layer_0 -[hidden]down- layer_1"), puml);
        assertFalse(puml.contains("pkg_x_cli -[hidden]"), puml);
    }
}
