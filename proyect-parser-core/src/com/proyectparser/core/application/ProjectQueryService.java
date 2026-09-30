package com.proyectparser.core.application;

import com.proyectparser.core.domain.model.ClassModel;
import com.proyectparser.core.domain.model.PackageModel;
import com.proyectparser.core.domain.model.ProjectModel;
import com.proyectparser.core.domain.port.RunStatsProvider;
import com.proyectparser.core.domain.port.SourceAnalyzerPort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Conteos, paquetes, externas y trazabilidad (paso 8 del split).
 * Las estadísticas de la última ejecución llegan como dependencia explícita
 * ({@link RunStatsProvider}); quien construye el servicio decide de dónde salen.
 */
public class ProjectQueryService {

    private final SourceAnalyzerPort analyzer;
    private final RunStatsProvider stats;

    public ProjectQueryService(SourceAnalyzerPort analyzer, RunStatsProvider stats) {
        this.analyzer = Objects.requireNonNull(analyzer, "analyzer is required");
        this.stats = Objects.requireNonNull(stats, "stats is required");
    }

    /**
     * Packages of the analyzed project, mapped to their type names (sorted).
     * The default package is reported as "(default package)".
     */
    public Map<String, List<String>> getPackages(String folderPath) throws IOException {
        ProjectModel project = analyzer.analyze(folderPath);
        Map<String, List<String>> packages = new TreeMap<>();
        for (ClassModel model : project.getClasses()) {
            String pkg = PackageModel.displayName(model.getPackageName());
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
     * mapped id -&gt; package, sorted by name and, for homonyms, by fqn.
     * The id is the simple name, or the fqn when another class shares it.
     */
    public Map<String, String> getExternalClasses(String folderPath) throws IOException {
        ProjectModel project = analyzer.analyze(folderPath);
        List<ClassModel> sorted = new ArrayList<>();
        if (project.getClasses() != null) {
            for (ClassModel model : project.getClasses()) {
                if (model.isExternal()) {
                    sorted.add(model);
                }
            }
        }
        sorted.sort(Comparator.comparing(ClassModel::getName).thenComparing(ClassModel::getFqn));
        Map<String, String> externals = new LinkedHashMap<>();
        for (ClassModel model : sorted) {
            externals.put(model.id(), model.getPackageName());
        }
        return externals;
    }

    /** Number of .java files that failed to parse in the last analysis. */
    public int getErrorCount() {
        return stats.getFailedFileCount();
    }

    /** Number of .java files successfully parsed in the last analysis. */
    public int getParsedFileCount() {
        return stats.getParsedFileCount();
    }

    /** Total .java files found in the last analysis. */
    public int getTotalFileCount() {
        return stats.getTotalJavaFileCount();
    }

    /** Paths of files that failed to parse in the last analysis. */
    public List<String> getFailedFiles() {
        return stats.getFailedFiles();
    }

    /** Paths of files successfully parsed in the last analysis. */
    public List<String> getParsedFiles() {
        return stats.getParsedFiles();
    }

    /** Human-readable "file -> reason" entries for failures in the last analysis. */
    public List<String> getFailureReasons() {
        return stats.getFailureReasons();
    }

    /** One-line summary: "parsed X/Y, failed Z". */
    public String getLastAnalysisSummary() {
        return "parsed " + getParsedFileCount() + "/" + getTotalFileCount()
                + ", failed " + getErrorCount();
    }
}
