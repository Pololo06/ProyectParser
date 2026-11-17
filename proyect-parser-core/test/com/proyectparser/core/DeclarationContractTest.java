package com.proyectparser.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class DeclarationContractTest {
    @TempDir Path root;
    private String render(String source) throws Exception {
        Files.writeString(root.resolve("Example.java"), source);
        var facade = new DiagramFacade();
        String text = facade.generatePlantUml(root.toString());
        assertEquals(0, facade.getErrorCount());
        return text;
    }
    @Test void sealedRestrictionsSurviveRendering() throws Exception {
        String text = render("sealed interface Root permits A, B {} final class A implements Root {} non-sealed class B implements Root {}");
        assertTrue(text.contains("permits A, B"), text);
        assertTrue(text.contains("<<Sealed>>"));
        assertTrue(text.contains("<<Non-Sealed>>"));
    }
    @Test void parameterizedSupertypesKeepArgumentsOnTheirRelations() throws Exception {
        String text = render("interface Contract<T> {} class Base<T> {} class Example extends Base<java.util.List<String>> implements Contract<Integer> {}");
        assertTrue(text.contains("Base <|-- Example : Base<java.util.List<String>>"), text);
        assertTrue(text.contains("Contract <|.. Example : Contract<Integer>"), text);
    }
    @Test void externalSpecializationsKeepArgumentsToo() throws Exception {
        String text = render("class Example implements java.util.function.Supplier<java.time.Instant> { public java.time.Instant get() { return null; } }");
        assertTrue(text.contains(" : java.util.function.Supplier<java.time.Instant>"), text);
    }
    @Test void methodAndConstructorDeclarationsKeepBoundsAndThrows() throws Exception {
        String text = render("class Example { <X extends java.io.Closeable> Example(X value) throws java.io.IOException {} <Y extends Number & Comparable<Y>> Y convert(Y value) throws IllegalArgumentException { return value; } }");
        assertTrue(text.contains("<X extends java.io.Closeable> Example(value : X) : void <<Create>> throws java.io.IOException"), text);
        assertTrue(text.contains("<Y extends Number & Comparable<Y>> convert(value : Y) : Y throws IllegalArgumentException"), text);
        assertTrue(text.contains("<<external>>"));
        var model = new DiagramFacade().analyzeProject(root.toString());
        assertTrue(model.getClasses().stream().anyMatch(c -> c.getFqn().equals("java.io.Closeable")));
        assertTrue(model.getClasses().stream().anyMatch(c -> c.getFqn().equals("java.io.IOException")));
        assertTrue(model.getClasses().stream().noneMatch(c -> c.getName().equals("X") || c.getName().equals("Y")));
        assertEquals("<X extends java.io.Closeable> Example(X value) throws java.io.IOException",
                new DiagramFacade().getClassDetails(root.toString()).get("Example").getConstructors().get(0));
    }
}
