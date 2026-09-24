package is.generador.application;

import is.generador.domain.model.ClassModel;
import is.generador.domain.model.PackageModel;
import is.generador.domain.model.ProjectModel;
import is.generador.domain.port.RunStatsProvider;
import is.generador.domain.port.SourceAnalyzerPort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Conteos, paquetes, externas y trazabilidad (paso 8 del split).
 * Lee el puerto con {@code instanceof RunStatsProvider} para las
 * estadísticas, igual que hacía la fachada.
 */
public class ProjectQueryService {

    private final SourceAnalyzerPort analyzer;

    public ProjectQueryService(SourceAnalyzerPort analyzer) {
        this.analyzer = Objects.requireNonNull(analyzer, "analyzer is required");
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

    /** Package views as canonical {@code PackageModel}. */
    public List<PackageModel> getPackageModels(String folderPath) throws IOException {
        return analyzer.analyze(folderPath).getPackages();
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
                if (model.isExternal()) {
                    externals.put(model.getName(), model.getPackageName());
                }
            }
        }
        return externals;
    }

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
}
