package com.proyectparser.core.infrastructure.javaparser;

import com.proyectparser.core.domain.model.ProjectModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class TypeSemanticsTest {
    @TempDir Path root;

    private ProjectModel analyze(String source) throws Exception {
        Files.writeString(root.resolve("Example.java"), source);
        var analyzer = new ProjectAnalyzer();
        var model = analyzer.analyze(root.toString());
        assertEquals(0, analyzer.getFailedFileCount(), analyzer.getFailureReasons().toString());
        return model;
    }

    private Set<String> edges(ProjectModel model) {
        return model.getRelationships().stream().map(r -> r.key()).collect(Collectors.toSet());
    }

    @Test void identifiersAreJavaIdentifiersRatherThanNamingConventions() throws Exception {
        var model = analyze("class dato {} class Niño {} class Example { dato a; Niño b; }");
        assertEquals(Set.of("Example|ASSOCIATION|dato", "Example|ASSOCIATION|Niño"), edges(model));
    }

    @Test void typeUseAnnotationsAreNotDependencies() throws Exception {
        var model = analyze("""
                import java.lang.annotation.*;
                import java.time.Instant;
                @Target(ElementType.TYPE_USE) @interface Mark {}
                class Example { java.util.List<@Mark Instant> instants; }
                """);
        assertEquals(Set.of("Example|DEPENDENCY|Instant"), edges(model));
    }

    @Test void enclosingTypeVariablesAreNotExternalClasses() throws Exception {
        var model = analyze("class Outer<T> { class Inner { T value; } }");
        assertTrue(model.getClasses().stream().noneMatch(c -> c.isExternal()));
        assertEquals(Set.of("Inner|NESTING|Outer"), edges(model));
    }

    @Test void externalSupertypesKeepInheritanceAndInterfaceKind() throws Exception {
        var model = analyze("class Example extends java.io.IOException implements java.io.Serializable {}");
        assertEquals(Set.of("Example|EXTENDS|IOException", "Example|IMPLEMENTS|Serializable"), edges(model));
        assertEquals("Interface", model.getClasses().stream().filter(c -> c.getName().equals("Serializable"))
                .findFirst().orElseThrow().getKind());
    }

    @Test void externalBoundsAndNamesakesOfScalarsArePreserved() throws Exception {
        var model = analyze("class Example<T extends java.io.Closeable> { third.party.String value; }");
        assertEquals(Set.of("Example|DEPENDENCY|Closeable", "Example|DEPENDENCY|String"), edges(model));
        assertTrue(model.getClasses().stream().anyMatch(c -> c.getFqn().equals("third.party.String")));
    }

    @Test void staticNestedTypesDoNotInheritOuterTypeVariables() throws Exception {
        var model = analyze("class T {} class Outer<T> { static class Inner { T value; } }");
        assertTrue(edges(model).contains("Inner|ASSOCIATION|T"));
    }

    @Test void externalHomonymsRemainSeparateAcrossSignatures() throws Exception {
        var model = analyze("class Example { Example(java.sql.Date sql, java.util.Date util) {} }");
        assertEquals(Set.of("Example|DEPENDENCY|java.sql.Date", "Example|DEPENDENCY|java.util.Date"), edges(model));
    }

    @Test void qualifiedTypesAreNotShadowedByTypeVariables() throws Exception {
        var model = analyze("class Example<T> { third.party.T value; }");
        assertEquals(Set.of("Example|DEPENDENCY|T"), edges(model));
        assertTrue(model.getClasses().stream().anyMatch(c -> c.getFqn().equals("third.party.T")));
    }

    @Test void externalInterfaceKindDoesNotDependOnFirstUse() throws Exception {
        var model = analyze("class A { java.io.Serializable value; } class Z implements java.io.Serializable {}");
        assertEquals("Interface", model.getClasses().stream().filter(c -> c.getName().equals("Serializable"))
                .findFirst().orElseThrow().getKind());
    }

    @Test void localClassesWithTheSameNameDoNotOverwriteEachOther() throws Exception {
        var model = analyze("""
                class Example {
                    void first() { class Local { int first; Local next; } }
                    void second() { class Local { String second; Local next; } }
                }
                """);
        var locals = model.getClasses().stream().filter(c -> c.getName().equals("Local")).toList();
        assertEquals(2, locals.size());
        assertNotEquals(locals.get(0).id(), locals.get(1).id());
        for (var local : locals) {
            assertTrue(edges(model).contains(local.id() + "|ASSOCIATION|" + local.id()));
        }
        assertEquals(3, new com.proyectparser.core.DiagramFacade().getClassDetails(root.toString()).size());
    }

    @Test void compactConstructorAndExternalConstructorSignaturesFollowSource() throws Exception {
        var model = analyze("""
                import java.time.Instant;
                record Event(Instant time, String label) { public Event {} public Event(Instant t) { this(t, ""); } }
                class Parent { Parent(Instant time) {} }
                class Child extends Parent { Child(Instant... times) { super(times[0]); } }
                """);
        var event = model.getClasses().stream().filter(c -> c.getName().equals("Event")).findFirst().orElseThrow();
        assertEquals(2, event.getConstructors().size());
        assertEquals(2, event.getConstructors().get(0).getParameters().size());
        assertEquals(Set.of("Child|EXTENDS|Parent", "Event|DEPENDENCY|Instant",
                "Parent|DEPENDENCY|Instant", "Child|DEPENDENCY|Instant"), edges(model));
    }
}
