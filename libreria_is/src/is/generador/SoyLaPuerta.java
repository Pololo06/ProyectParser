package is.generador;

import is.generador.domain.policy.DiagramFilter;
import is.generador.domain.policy.DiagramOptions;
import is.generador.domain.BeanAccessors;
import is.generador.application.AnalyzeProjectUseCase;
import is.generador.application.ExportDiagramUseCase;
import is.generador.application.FilteredProjectBuilder;
import is.generador.application.GenerateDiagramUseCase;
import is.generador.domain.port.DiagramRendererPort;
import is.generador.domain.port.DiagramWriterPort;
import is.generador.domain.port.RunStatsProvider;
import is.generador.domain.port.SourceAnalyzerPort;
import is.generador.infrastructure.javaparser.ProjectAnalyzer;
import is.generador.domain.model.*;
import is.generador.infrastructure.plantuml.FileSystemDiagramWriter;
import is.generador.infrastructure.plantuml.PlantUmlGenerator;
import is.generador.infrastructure.plantuml.PumlFileWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Public facade of the "libreria_is" library (API layer).
 * Uses JavaParser internally (see ProjectAnalyzer) to analyze classes, records,
 * interfaces, enums, stereotypes (annotations), constructors, getters, setters
 * and properties, and to generate PlantUML diagrams.
 *
 * <p>Clean Architecture layout:
 * {@code domain.model} / {@code domain.policy} / {@code domain.port} (pure, no
 * external deps) &larr; {@code application} (use cases, depends only on ports)
 * &larr; {@code infrastructure.*} (JavaParser, PlantUML, files).
 * This facade only wires ports to use cases; all logic lives in the use cases.
 * Test seam: {@link #SoyLaPuerta(SourceAnalyzerPort, DiagramRendererPort, DiagramWriterPort)}.
 */
public class SoyLaPuerta {

    private static final String[] DEFAULT_PATHS = {
            "src",
            "proyectoPaUsarLaLibreria/src",
            "../proyectoPaUsarLaLibreria/src"
    };

    private final SourceAnalyzerPort analyzer;
    private final DiagramRendererPort renderer;
    private final AnalyzeProjectUseCase analyzeUseCase;
    private final GenerateDiagramUseCase generateUseCase;
    private final ExportDiagramUseCase exportUseCase;

    public SoyLaPuerta() {
        this(new ProjectAnalyzer(), new PlantUmlGenerator(), new FileSystemDiagramWriter());
    }

    /** Composition seam: inject ports (production wires infrastructure, tests wire fakes). */
    public SoyLaPuerta(SourceAnalyzerPort analyzer, DiagramRendererPort renderer, DiagramWriterPort writer) {
        this.analyzer = analyzer;
        this.renderer = renderer;
        this.analyzeUseCase = new AnalyzeProjectUseCase(analyzer);
        this.generateUseCase = new GenerateDiagramUseCase(analyzer, renderer);
        this.exportUseCase = new ExportDiagramUseCase(generateUseCase, writer);
    }

    public static class ClassInfo {
        private final String name;
        private final String classType;
        private final List<String> stereotypes;
        private final List<String> properties;
        private final List<String> constructors;
        private final List<String> getters;
        private final List<String> setters;

        public ClassInfo(String name, String classType, List<String> stereotypes,
                         List<String> properties, List<String> constructors,
                         List<String> getters, List<String> setters) {
            this.name = name;
            this.classType = classType;
            this.stereotypes = stereotypes;
            this.properties = properties;
            this.constructors = constructors;
            this.getters = getters;
            this.setters = setters;
        }

        public String getName() { return name; }
        public String getClassType() { return classType; }
        public List<String> getStereotypes() { return stereotypes; }
        public List<String> getProperties() { return properties; }
        public List<String> getConstructors() { return constructors; }
        public List<String> getGetters() { return getters; }
        public List<String> getSetters() { return setters; }

        // Spanish aliases (backward compatibility)
        public String getNombre() { return name; }
        public String getTipoClase() { return classType; }
        public List<String> getEstereotipos() { return stereotypes; }
        public List<String> getPropiedades() { return properties; }
        public List<String> getConstructores() { return constructors; }
    }

    /** Backward-compatible alias for {@link ClassInfo}. */
    public static class InfoClase extends ClassInfo {
        public InfoClase(String name, String classType, List<String> stereotypes,
                         List<String> properties, List<String> constructors,
                         List<String> getters, List<String> setters) {
            super(name, classType, stereotypes, properties, constructors, getters, setters);
        }
    }

    // ---------- English API ----------

    public List<String> countClasses() throws IOException {
        return countClasses(resolveDefaultPath());
    }

    public List<String> countClasses(String folderPath) throws IOException {
        ProjectModel project = analyzer.analyze(folderPath);
        List<String> names = new ArrayList<>();
        for (ClassModel model : project.getClasses()) {
            names.add(model.getName());
        }
        Collections.sort(names);
        return names;
    }

    public Map<String, List<String>> getClassProperties() throws IOException {
        return getClassProperties(resolveDefaultPath());
    }

    public Map<String, List<String>> getClassProperties(String folderPath) throws IOException {
        Map<String, ClassInfo> details = getClassDetails(folderPath);
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (Map.Entry<String, ClassInfo> entry : details.entrySet()) {
            map.put(entry.getKey(), entry.getValue().getProperties());
        }
        return map;
    }

    public Map<String, ClassInfo> getClassDetails() throws IOException {
        return getClassDetails(resolveDefaultPath());
    }

    public Map<String, ClassInfo> getClassDetails(String folderPath) throws IOException {
        ProjectModel project = analyzer.analyze(folderPath);
        Map<String, ClassInfo> details = new LinkedHashMap<>();

        for (ClassModel model : project.getClasses()) {
            List<String> properties = new ArrayList<>();
            model.getAttributes().forEach(a -> properties.add(a.getType() + " " + a.getName()));
            if ("Enum".equals(model.getKind()) && model.getEnumConstants() != null) {
                for (String constant : model.getEnumConstants()) {
                    properties.add("constant " + constant);
                }
            }

            List<String> constructorSignatures = new ArrayList<>();
            model.getConstructors().forEach(constructor -> {
                String visibility = visibilityKeyword(constructor.getModifiers());
                String prefix = visibility.isEmpty() ? "" : visibility + " ";
                StringBuilder signature = new StringBuilder(prefix + constructor.getName() + "(");
                for (int i = 0; i < constructor.getParameters().size(); i++) {
                    signature.append(constructor.getParameters().get(i).getType())
                            .append(" ")
                            .append(constructor.getParameters().get(i).getName());
                    if (i < constructor.getParameters().size() - 1) {
                        signature.append(", ");
                    }
                }
                signature.append(")");
                constructorSignatures.add(signature.toString());
            });

            List<String> getters = new ArrayList<>();
            List<String> setters = new ArrayList<>();
            model.getMethods().forEach(method -> {
                String declaration = formatMethodDeclaration(method);
                if (isGetter(method, model)) {
                    getters.add(declaration);
                } else if (isSetter(method, model)) {
                    setters.add(declaration);
                }
            });

            details.put(model.getName(), new ClassInfo(model.getName(), model.getKind(),
                    model.getStereotypes(), properties, constructorSignatures, getters, setters));
        }
        return details;
    }

    /**
     * JavaBeans getter con campo respaldo (ver {@code BeanAccessors}).
     */
    private boolean isGetter(MethodModel method, ClassModel model) {
        return BeanAccessors.isGetter(method, model);
    }

    /**
     * JavaBeans setter con campo respaldo (ver {@code BeanAccessors}).
     */
    private boolean isSetter(MethodModel method, ClassModel model) {
        return BeanAccessors.isSetter(method, model);
    }

    private String formatMethodDeclaration(MethodModel method) {
        StringBuilder sb = new StringBuilder();
        String visibility = visibilityKeyword(method.getModifiers());
        if (!visibility.isEmpty()) {
            sb.append(visibility).append(" ");
        }
        sb.append(method.getReturnType()).append(" ").append(method.getName()).append("(");
        if (method.getParameters() != null) {
            for (int i = 0; i < method.getParameters().size(); i++) {
                sb.append(method.getParameters().get(i).getType())
                        .append(" ")
                        .append(method.getParameters().get(i).getName());
                if (i < method.getParameters().size() - 1) {
                    sb.append(", ");
                }
            }
        }
        sb.append(")");
        return sb.toString();
    }

    private String visibilityKeyword(List<String> modifiers) {
        if (modifiers == null) {
            return "";
        }
        if (modifiers.contains("public")) {
            return "public";
        }
        if (modifiers.contains("protected")) {
            return "protected";
        }
        if (modifiers.contains("private")) {
            return "private";
        }
        return "";
    }

    public String generatePlantUml() throws IOException {
        return generatePlantUml(resolveDefaultPath());
    }

    public String generatePlantUml(String folderPath) throws IOException {
        return generateUseCase.execute(folderPath);
    }

    public ProjectModel analyzeProject(String folderPath) throws IOException {
        return analyzeUseCase.execute(folderPath);
    }

    // ---------- package structure ----------

    /** Packages of the default project, mapped to their type names (sorted). */
    public Map<String, List<String>> getPackages() throws IOException {
        return getPackages(resolveDefaultPath());
    }

    /**
     * Packages of the analyzed project, mapped to their type names (sorted).
     * The default package is reported as "(default package)".
     */
    public Map<String, List<String>> getPackages(String folderPath) throws IOException {
        ProjectModel project = analyzer.analyze(folderPath);
        Map<String, List<String>> packages = new TreeMap<>();
        for (ClassModel model : project.getClasses()) {
            String pkg = model.getPackageName() == null ? "" : model.getPackageName();
            if (pkg.isEmpty()) {
                pkg = "(default package)";
            }
            packages.computeIfAbsent(pkg, k -> new ArrayList<>()).add(model.getName());
        }
        for (List<String> names : packages.values()) {
            Collections.sort(names);
        }
        return packages;
    }

    /** Sorted names of the packages in the analyzed project. */
    public List<String> getPackageNames(String folderPath) throws IOException {
        return new ArrayList<>(getPackages(folderPath).keySet());
    }

    /** Number of distinct packages in the analyzed project. */
    public int getPackageCount(String folderPath) throws IOException {
        return getPackages(folderPath).size();
    }

    /**
     * Counts types by kind (Class, AbstractClass, Interface, Enum, Record,
     * Annotation), sorted by kind name.
     */
    public Map<String, Integer> countByKind(String folderPath) throws IOException {
        ProjectModel project = analyzer.analyze(folderPath);
        return countByKind(project);
    }

    /** Counts types by kind over an already analyzed model. */
    public static Map<String, Integer> countByKind(ProjectModel project) {
        Map<String, Integer> counts = new TreeMap<>();
        if (project.getClasses() != null) {
            for (ClassModel model : project.getClasses()) {
                String kind = model.getKind() == null ? "(unknown)" : model.getKind();
                counts.put(kind, counts.getOrDefault(kind, 0) + 1);
            }
        }
        return counts;
    }

    /** Total types in the analyzed project. */
    public int countTotalTypes(String folderPath) throws IOException {
        ProjectModel project = analyzer.analyze(folderPath);
        return project.getClasses() == null ? 0 : project.getClasses().size();
    }

    /** @deprecated use {@link #getPackages()} instead. */
    @Deprecated
    public Map<String, List<String>> obtenerPaquetes() throws IOException {
        return getPackages();
    }

    /** @deprecated use {@link #getPackages(String)} instead. */
    @Deprecated
    public Map<String, List<String>> obtenerPaquetes(String folderPath) throws IOException {
        return getPackages(folderPath);
    }

    // ---------- error / traceability counters (RNF-04 / RNF-06) ----------

    /** Number of .java files that failed to parse in the last analysis. */
    public int getErrorCount() {
        return analyzer instanceof RunStatsProvider stats ? stats.getFailedFileCount() : 0;
    }

    /** Number of .java files successfully parsed in the last analysis. */
    public int getParsedFileCount() {
        return analyzer instanceof RunStatsProvider stats ? stats.getParsedFileCount() : 0;
    }

    /** Total .java files found in the last analysis. */
    public int getTotalFileCount() {
        return analyzer instanceof RunStatsProvider stats ? stats.getTotalJavaFileCount() : 0;
    }

    /** Paths of files that failed to parse in the last analysis. */
    public List<String> getFailedFiles() {
        return analyzer instanceof RunStatsProvider stats ? stats.getFailedFiles() : List.of();
    }

    /** Paths of files successfully parsed in the last analysis. */
    public List<String> getParsedFiles() {
        return analyzer instanceof RunStatsProvider stats ? stats.getParsedFiles() : List.of();
    }

    /** Human-readable "file -> reason" entries for failures in the last analysis. */
    public List<String> getFailureReasons() {
        return analyzer instanceof RunStatsProvider stats ? stats.getFailureReasons() : List.of();
    }

    /** One-line summary: "parsed X/Y, failed Z". */
    public String getLastAnalysisSummary() {
        return "parsed " + getParsedFileCount() + "/" + getTotalFileCount()
                + ", failed " + getErrorCount();
    }

    private String resolveDefaultPath() {
        // Portable override: -Dis.generador.src=/path/to/src or env IS_GENERADOR_SRC.
        String override = System.getProperty("is.generador.src",
                System.getenv("IS_GENERADOR_SRC"));
        if (override != null && !override.isBlank() && Files.isDirectory(Path.of(override))) {
            return override;
        }
        for (String candidate : DEFAULT_PATHS) {
            if (Files.isDirectory(Path.of(candidate))) {
                return candidate;
            }
        }
        return DEFAULT_PATHS[0];
    }

    // ---------- Clean Architecture bridge (Fase 2/8, canonical model) ----------

    /** Analyzes via the canonical {@code is.generador.infrastructure.javaparser.ProjectAnalyzer} engine. */
    public ProjectModel analyzeWithUseCase(String folderPath) throws IOException {
        return analyzer.analyze(folderPath);
    }

    /** Generates PlantUML via the canonical engine. */
    public String generatePlantUmlViaUseCase(String folderPath) throws IOException {
        return generateUseCase.execute(folderPath);
    }

    /** Generates PlantUML and writes it to {@code outputFile}. */
    public Path exportPlantUml(String folderPath, Path outputFile) throws IOException {
        return exportUseCase.execute(folderPath, outputFile);
    }

    /** Package views as canonical {@code is.generador.domain.model.PackageModel} (Fase 10). */
    public List<PackageModel> getPackageModels(String folderPath) throws IOException {
        return analyzeWithUseCase(folderPath).getPackages();
    }

    // ---------- external classes listing + blacklist/whitelist ----------

    /** External classes of the default project, mapped name -&gt; package (sorted). */
    public Map<String, String> getExternalClasses() throws IOException {
        return getExternalClasses(resolveDefaultPath());
    }

    /**
     * External classes (stereotype {@code @external}) of the analyzed project,
     * mapped name -&gt; package (sorted by name).
     */
    public Map<String, String> getExternalClasses(String folderPath) throws IOException {
        ProjectModel project = analyzer.analyze(folderPath);
        Map<String, String> externals = new TreeMap<>();
        if (project.getClasses() != null) {
            for (ClassModel model : project.getClasses()) {
                if (model.getStereotypes() != null && model.getStereotypes().contains("@external")) {
                    externals.put(model.getName(), model.getPackageName());
                }
            }
        }
        return externals;
    }

    /** Analyzes applying a {@code DiagramFilter} (blacklist/whitelist) post-analysis. */
    public ProjectModel analyzeFiltered(String folderPath, DiagramFilter filter) throws IOException {
        ProjectModel project = analyzeUseCase.execute(folderPath);
        return new FilteredProjectBuilder(project).withFilter(filter).build();
    }

    /** Generates PlantUML applying a {@code DiagramFilter} (blacklist/whitelist). */
    public String generatePlantUml(String folderPath, DiagramFilter filter) throws IOException {
        return generatePlantUml(folderPath, filter, DiagramOptions.defaults());
    }

    /** Generates PlantUML applying a {@code DiagramFilter} and {@code DiagramOptions}. */
    public String generatePlantUml(String folderPath, DiagramFilter filter, DiagramOptions options)
            throws IOException {
        return generateUseCase.execute(folderPath, filter,
                options == null ? DiagramOptions.defaults() : options);
    }

    /** Generates PlantUML with filter and writes it to {@code outputFile}. */
    public Path exportPlantUml(String folderPath, Path outputFile, DiagramFilter filter) throws IOException {
        return exportPlantUml(folderPath, outputFile, filter, DiagramOptions.defaults());
    }

    /** Generates PlantUML with filter and options, writing it to {@code outputFile}. */
    public Path exportPlantUml(String folderPath, Path outputFile, DiagramFilter filter, DiagramOptions options)
            throws IOException {
        return exportUseCase.execute(folderPath, outputFile, filter,
                options == null ? DiagramOptions.defaults() : options);
    }

    /** @deprecated use {@link #getExternalClasses()} instead. */
    @Deprecated
    public Map<String, String> obtenerClasesExternas() throws IOException {
        return getExternalClasses();
    }

    /** @deprecated use {@link #getExternalClasses(String)} instead. */
    @Deprecated
    public Map<String, String> obtenerClasesExternas(String folderPath) throws IOException {
        return getExternalClasses(folderPath);
    }

    // ---------- Spanish aliases (backward compatibility) ----------

    /** @deprecated use {@link #countClasses()} instead. */
    @Deprecated
    public List<String> contarClases() throws IOException {
        return countClasses();
    }

    /** @deprecated use {@link #countClasses(String)} instead. */
    @Deprecated
    public List<String> contarClases(String folderPath) throws IOException {
        return countClasses(folderPath);
    }

    /** @deprecated use {@link #countByKind(String)} instead. */
    @Deprecated
    public Map<String, Integer> contarPorTipo(String folderPath) throws IOException {
        return countByKind(folderPath);
    }

    /** @deprecated use {@link #getClassProperties()} instead. */
    @Deprecated
    public Map<String, List<String>> obtenerPropiedadesClases() throws IOException {
        return getClassProperties();
    }

    /** @deprecated use {@link #getClassProperties(String)} instead. */
    @Deprecated
    public Map<String, List<String>> obtenerPropiedadesClases(String folderPath) throws IOException {
        return getClassProperties(folderPath);
    }

    /** @deprecated use {@link #getClassDetails()} instead. */
    @Deprecated
    public Map<String, InfoClase> obtenerDetalleClases() throws IOException {
        return toLegacyMap(getClassDetails());
    }

    /** @deprecated use {@link #getClassDetails(String)} instead. */
    @Deprecated
    public Map<String, InfoClase> obtenerDetalleClases(String folderPath) throws IOException {
        return toLegacyMap(getClassDetails(folderPath));
    }

    /** @deprecated use {@link #generatePlantUml()} instead. */
    @Deprecated
    public String generarPlantUml() throws IOException {
        return generatePlantUml();
    }

    /** @deprecated use {@link #generatePlantUml(String)} instead. */
    @Deprecated
    public String generarPlantUml(String folderPath) throws IOException {
        return generatePlantUml(folderPath);
    }

    private Map<String, InfoClase> toLegacyMap(Map<String, ClassInfo> details) {
        Map<String, InfoClase> legacy = new LinkedHashMap<>();
        for (Map.Entry<String, ClassInfo> entry : details.entrySet()) {
            ClassInfo info = entry.getValue();
            legacy.put(entry.getKey(), new InfoClase(info.getName(), info.getClassType(),
                    info.getStereotypes(), info.getProperties(),
                    info.getConstructors(), info.getGetters(), info.getSetters()));
        }
        return legacy;
    }
}
