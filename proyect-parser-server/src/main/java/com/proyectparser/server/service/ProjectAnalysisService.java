package com.proyectparser.server.service;

import com.proyectparser.core.DiagramFacade;
import com.proyectparser.core.UseCases;
import com.proyectparser.core.application.DiagramService;
import com.proyectparser.core.domain.model.ProjectModel;
import com.proyectparser.core.domain.policy.DiagramFilter;
import com.proyectparser.core.domain.policy.DiagramOptions;
import com.proyectparser.core.domain.policy.ProjectFilter;
import com.proyectparser.server.dto.DiagramRequest;
import com.proyectparser.server.dto.FilterRequest;
import com.proyectparser.server.dto.OptionsRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Application service of the REST layer.
 *
 * <p>A new {@link DiagramFacade} is created for every request because the
 * core analyzer keeps the statistics of the last run as instance state.
 * Rendering is delegated to the stateless production use case.</p>
 */
@Service
public class ProjectAnalysisService {

    private final DiagramService renderer = UseCases.diagramas();

    /**
     * Analyzes {@code sourceRoot} and applies the optional blacklist/whitelist.
     * The returned model and the traceability lists are relative to the
     * workspace, so no server paths leak to clients.
     */
    public AnalysisOutcome analyze(Path sourceRoot, DiagramRequest request) throws IOException {
        DiagramFacade facade = new DiagramFacade();
        ProjectModel analyzed = facade.analyzeProject(sourceRoot.toString());
        ProjectModel filtered = ProjectFilter.apply(analyzed, filterOf(request));

        return new AnalysisOutcome(
                filtered,
                facade.getTotalFileCount(),
                facade.getParsedFileCount(),
                facade.getErrorCount(),
                relativize(facade.getFailedFiles(), sourceRoot),
                relativizeReasons(facade.getFailureReasons(), sourceRoot));
    }

    /** Renders an already analyzed model with the request's display options. */
    public DiagramOutcome render(AnalysisOutcome analysis, DiagramRequest request) {
        DiagramOptions options = optionsOf(request);
        String plantUml = renderer.render(analysis.project(), options);
        return new DiagramOutcome(analysis, plantUml);
    }

    /** Analyze + render in one call. */
    public DiagramOutcome diagram(Path sourceRoot, DiagramRequest request) throws IOException {
        return render(analyze(sourceRoot, request), request);
    }

    private DiagramFilter filterOf(DiagramRequest request) {
        DiagramFilter.Builder builder = new DiagramFilter.Builder();
        FilterRequest filter = request == null ? null : request.filter();
        if (filter != null) {
            builder.excludePackages(filter.excludePackages());
            builder.excludeClasses(filter.excludeClasses());
            builder.includePackages(filter.includePackages());
            builder.includeClasses(filter.includeClasses());
        }
        return builder.build();
    }

    private DiagramOptions optionsOf(DiagramRequest request) {
        DiagramOptions.Builder builder = new DiagramOptions.Builder();
        OptionsRequest options = request == null ? null : request.options();
        if (options == null) {
            return builder.build();
        }

        set(options.showGettersSetters(), builder::showGettersSetters);
        set(options.showAttributes(), builder::showAttributes);
        set(options.showMethods(), builder::showMethods);
        set(options.showConstructors(), builder::showConstructors);
        set(options.showExternal(), builder::showExternal);
        set(options.showJdkTypes(), builder::showJdkTypes);
        set(options.showDependencies(), builder::showDependencies);
        set(options.groupByPackage(), builder::groupByPackage);
        set(options.shortSignatures(), builder::shortSignatures);
        set(options.hideOrphans(), builder::hideOrphans);
        set(options.summary(), builder::summary);
        set(options.groupLayers(), builder::groupLayers);

        if (options.lineType() != null) {
            DiagramOptions.LineType type = DiagramOptions.LineType.fromLabel(options.lineType());
            if (type == null) {
                throw new IllegalArgumentException(
                        "Invalid lineType '" + options.lineType() + "'. Use ortho, polyline or spline");
            }
            builder.lineType(type);
        }

        List<String> layers = clean(options.layerOrder());
        if (!layers.isEmpty()) {
            builder.layerOrder(layers);
        }

        if (options.allowedDependencies() != null) {
            Set<String> allowed = new TreeSet<>();
            for (String dependency : options.allowedDependencies()) {
                if (dependency != null && !dependency.isBlank() && !dependency.contains("->")) {
                    throw new IllegalArgumentException(
                            "Invalid allowedDependencies entry '" + dependency + "'. Use \"Source->Target\"");
                }
                if (dependency != null && !dependency.isBlank()) {
                    allowed.add(dependency.trim());
                }
            }
            builder.allowedDependencies(allowed);
        }

        return builder.build();
    }

    private static void set(Boolean value, java.util.function.Consumer<Boolean> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private static List<String> clean(List<String> values) {
        if (values == null) {
            return List.of();
        }
        List<String> cleaned = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                cleaned.add(value.trim());
            }
        }
        return cleaned;
    }

    private static List<String> relativize(List<String> paths, Path root) {
        if (paths == null) {
            return List.of();
        }
        String rootText = root.toAbsolutePath().normalize().toString().replace('\\', '/');
        List<String> result = new ArrayList<>(paths.size());
        for (String path : paths) {
            result.add(relative(path, rootText));
        }
        return result;
    }

    private static List<String> relativizeReasons(List<String> reasons, Path root) {
        if (reasons == null) {
            return List.of();
        }
        String rootText = root.toAbsolutePath().normalize().toString().replace('\\', '/');
        List<String> result = new ArrayList<>(reasons.size());
        for (String reason : reasons) {
            result.add(relative(reason, rootText));
        }
        return result;
    }

    private static String relative(String value, String normalizedRoot) {
        String normalized = value.replace('\\', '/');
        if (normalized.equals(normalizedRoot)) {
            return "/";
        }
        if (normalized.startsWith(normalizedRoot + "/")) {
            return normalized.substring(normalizedRoot.length() + 1);
        }
        normalized = normalized.replace(normalizedRoot, "");
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }
}
