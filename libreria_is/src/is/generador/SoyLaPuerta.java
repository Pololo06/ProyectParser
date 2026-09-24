package is.generador;

import is.generador.application.AnalyzeProjectUseCase;
import is.generador.application.ClassInfo;
import is.generador.application.ClassInfoService;
import is.generador.application.ExportDiagramUseCase;
import is.generador.application.FilteredProjectBuilder;
import is.generador.application.GenerateDiagramUseCase;
import is.generador.application.ProjectQueryService;
import is.generador.domain.model.PackageModel;
import is.generador.domain.model.ProjectModel;
import is.generador.domain.policy.DiagramFilter;
import is.generador.domain.policy.DiagramOptions;
import is.generador.domain.port.DiagramRendererPort;
import is.generador.domain.port.DiagramWriterPort;
import is.generador.domain.port.SourceAnalyzerPort;
import is.generador.infrastructure.javaparser.ProjectAnalyzer;
import is.generador.infrastructure.plantuml.FileSystemDiagramWriter;
import is.generador.infrastructure.plantuml.PlantUmlGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Public facade of the "libreria_is" library (API layer).
 * Thin by design: every call delegates to application services or
 * use cases; all logic lives outside this class.
 *
 * <p>Clean Architecture layout:
 * {@code domain.model} / {@code domain.policy} / {@code domain.port} (pure, no
 * external deps) &larr; {@code application} (use cases, depends only on ports)
 * &larr; {@code infrastructure.*} (JavaParser, PlantUML, files).
 * Test seam: {@link #SoyLaPuerta(SourceAnalyzerPort, DiagramRendererPort, DiagramWriterPort)}.
 */
public class SoyLaPuerta {

    private static final String[] DEFAULT_PATHS = {
            "src",
            "proyectoPaUsarLaLibreria/src",
            "../proyectoPaUsarLaLibreria/src"
    };

    private final SourceAnalyzerPort analyzer;
    private final AnalyzeProjectUseCase analyzeUseCase;
    private final GenerateDiagramUseCase generateUseCase;
    private final ExportDiagramUseCase exportUseCase;
    private final ClassInfoService classInfoService;
    private final ProjectQueryService queryService;

    public SoyLaPuerta() {
        this(new ProjectAnalyzer(), new PlantUmlGenerator(), new FileSystemDiagramWriter());
    }

    /** Composition seam: inject ports (production wires infrastructure, tests wire fakes). */
    public SoyLaPuerta(SourceAnalyzerPort analyzer, DiagramRendererPort renderer, DiagramWriterPort writer) {
        this.analyzer = analyzer;
        this.analyzeUseCase = new AnalyzeProjectUseCase(analyzer);
        this.generateUseCase = new GenerateDiagramUseCase(analyzer, renderer);
        this.exportUseCase = new ExportDiagramUseCase(generateUseCase, writer);
        this.classInfoService = new ClassInfoService(analyzer);
        this.queryService = new ProjectQueryService(analyzer);
    }

    public List<String> countClasses() throws IOException {
        return countClasses(resolveDefaultPath());
    }

    public List<String> countClasses(String folderPath) throws IOException {
        return classInfoService.countClasses(folderPath);
    }

    public Map<String, List<String>> getClassProperties() throws IOException {
        return getClassProperties(resolveDefaultPath());
    }

    public Map<String, List<String>> getClassProperties(String folderPath) throws IOException {
        return classInfoService.getClassProperties(folderPath);
    }

    public Map<String, ClassInfo> getClassDetails() throws IOException {
        return getClassDetails(resolveDefaultPath());
    }

    public Map<String, ClassInfo> getClassDetails(String folderPath) throws IOException {
        return classInfoService.getClassDetails(folderPath);
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

    /** Packages of the default project, mapped to their type names (sorted). */
    public Map<String, List<String>> getPackages() throws IOException {
        return getPackages(resolveDefaultPath());
    }

    /**
     * Packages of the analyzed project, mapped to their type names (sorted).
     * The default package is reported as "(default package)".
     */
    public Map<String, List<String>> getPackages(String folderPath) throws IOException {
        return queryService.getPackages(folderPath);
    }

    /** Sorted names of the packages in the analyzed project. */
    public List<String> getPackageNames(String folderPath) throws IOException {
        return queryService.getPackageNames(folderPath);
    }

    /** Number of distinct packages in the analyzed project. */
    public int getPackageCount(String folderPath) throws IOException {
        return queryService.getPackageCount(folderPath);
    }

    /**
     * Counts types by kind (Class, AbstractClass, Interface, Enum, Record,
     * Annotation), sorted by kind name.
     */
    public Map<String, Integer> countByKind(String folderPath) throws IOException {
        return queryService.countByKind(folderPath);
    }

    /** Counts types by kind over an already analyzed model. */
    public static Map<String, Integer> countByKind(ProjectModel project) {
        return ProjectQueryService.countByKind(project);
    }

    /** Total types in the analyzed project. */
    public int countTotalTypes(String folderPath) throws IOException {
        return queryService.countTotalTypes(folderPath);
    }

    /** Number of .java files that failed to parse in the last analysis. */
    public int getErrorCount() {
        return queryService.getErrorCount();
    }

    /** Number of .java files successfully parsed in the last analysis. */
    public int getParsedFileCount() {
        return queryService.getParsedFileCount();
    }

    /** Total .java files found in the last analysis. */
    public int getTotalFileCount() {
        return queryService.getTotalFileCount();
    }

    /** Paths of files that failed to parse in the last analysis. */
    public List<String> getFailedFiles() {
        return queryService.getFailedFiles();
    }

    /** Paths of files successfully parsed in the last analysis. */
    public List<String> getParsedFiles() {
        return queryService.getParsedFiles();
    }

    /** Human-readable "file -> reason" entries for failures in the last analysis. */
    public List<String> getFailureReasons() {
        return queryService.getFailureReasons();
    }

    /** One-line summary: "parsed X/Y, failed Z". */
    public String getLastAnalysisSummary() {
        return queryService.getLastAnalysisSummary();
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

    /** Analyzes via the canonical engine. */
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

    /** Package views as canonical {@code PackageModel}. */
    public List<PackageModel> getPackageModels(String folderPath) throws IOException {
        return queryService.getPackageModels(folderPath);
    }

    /** External classes of the default project, mapped name -&gt; package (sorted). */
    public Map<String, String> getExternalClasses() throws IOException {
        return getExternalClasses(resolveDefaultPath());
    }

    /**
     * External classes (stereotype {@code @external}) of the analyzed project,
     * mapped name -&gt; package (sorted by name).
     */
    public Map<String, String> getExternalClasses(String folderPath) throws IOException {
        return queryService.getExternalClasses(folderPath);
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
}
