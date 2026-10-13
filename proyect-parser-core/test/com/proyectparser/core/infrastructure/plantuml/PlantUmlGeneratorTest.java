package com.proyectparser.core.infrastructure.plantuml;

import com.proyectparser.core.domain.model.AttributeModel;
import com.proyectparser.core.domain.model.ClassModel;
import com.proyectparser.core.domain.model.ConstructorModel;
import com.proyectparser.core.domain.model.MethodModel;
import com.proyectparser.core.domain.model.ParameterModel;
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

    @Test
    void anidadosSeEnlazanConMasYSinRepetirRelacionesInternas() {
        ClassModel outer = clase("Outer").withFqn("x.app.Outer");
        ClassModel inner = clase("Inner").withFqn("x.app.Outer.Inner");
        ClassModel deep = clase("Deep").withFqn("x.app.Outer.Inner.Deep");
        ClassModel other = clase("Other");
        ProjectModel proyecto = new ProjectModel("p", List.of(outer, inner, deep, other),
                List.of(new RelationshipModel("Inner", "Outer", RelType.NESTING.label()),
                        new RelationshipModel("Deep", "Inner", RelType.NESTING.label()),
                        new RelationshipModel("Outer", "Inner", RelType.ASSOCIATION.label()),
                        new RelationshipModel("Deep", "Outer", RelType.EXTENDS.label()),
                        new RelationshipModel("Deep", "Other", RelType.IMPLEMENTS.label())));
        String puml = new PlantUmlGenerator().render(proyecto, DiagramOptions.defaults());
        assertTrue(puml.contains("\nx.app.Inner +-- x.app.Outer\n"), puml);
        assertTrue(puml.contains("\nx.app.Deep +-- x.app.Inner\n"), puml);
        assertTrue(puml.contains("\nx.app.Other <|.. x.app.Deep\n"), puml);
        assertFalse(puml.contains("x.app.Outer --> x.app.Inner"), puml);
        assertFalse(puml.contains("x.app.Outer <|-- x.app.Deep"), puml);
    }

    @Test
    void miembrosUsanLaNotacionDeReferencia() {
        ClassModel servicio = new ClassModel("Servicio", "x.app", "AbstractClass", true, List.of(),
                List.of(new AttributeModel("timeout", "long", List.of("protected"), "5000"),
                        new AttributeModel("nombre", "String", List.of("private", "final")),
                        new AttributeModel("MAX", "int", List.of("public", "static", "final"), "3")),
                List.of(new MethodModel("ejecutar", "void", List.of(), List.of("public", "abstract"), List.of())),
                List.of(new ConstructorModel("Servicio", List.of(new ParameterModel("nombre", "String")),
                        List.of("public"), List.of())),
                List.of(), List.of(), List.of(), List.of());
        ClassModel puerto = new ClassModel("Puerto", "x.app", "Interface", false, List.of(), List.of(),
                List.of(new MethodModel("abrir", "void", List.of(), List.of(), List.of()),
                        new MethodModel("cerrar", "void", List.of(), List.of("default"), List.of())),
                List.of(), List.of(), List.of(), List.of(), List.of());
        ClassModel estado = new ClassModel("Estado", "x.app", "Enum", false, List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of("ON", "OFF"), List.of());
        String puml = new PlantUmlGenerator().render(new ProjectModel("p", List.of(servicio, puerto, estado),
                List.of()), DiagramOptions.defaults());
        assertTrue(puml.contains("package x.app {\n"), puml);
        assertTrue(puml.contains("abstract class Servicio <<Abstract>> {"), puml);
        assertTrue(puml.contains("#  timeout : long = 5000\n"), puml);
        assertTrue(puml.contains("-  {readOnly} nombre : String\n"), puml);
        assertTrue(puml.contains("+  {static, readOnly} MAX : int = 3\n"), puml);
        assertTrue(puml.contains("+  Servicio(nombre : String) : void <<Create>>\n"), puml);
        assertTrue(puml.contains("+  {abstract} ejecutar() : void\n"), puml);
        assertTrue(puml.contains("interface Puerto <<Interface>> {"), puml);
        assertTrue(puml.contains("+  {abstract} abrir() : void\n"), puml);
        assertTrue(puml.contains("+  cerrar() : void\n"), puml);
        assertTrue(puml.contains("+  {static, readOnly} ON : Estado\n"), puml);
        assertTrue(puml.contains("skinparam defaultFontName Helvetica\n"), puml);
        assertTrue(puml.contains("skinparam linetype ortho\n"), puml);
        assertFalse(puml.contains("set separator none"), puml);
    }
}
