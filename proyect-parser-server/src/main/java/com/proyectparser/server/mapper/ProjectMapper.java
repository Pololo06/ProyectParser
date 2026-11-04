package com.proyectparser.server.mapper;

import com.proyectparser.core.domain.model.AttributeModel;
import com.proyectparser.core.domain.model.ClassModel;
import com.proyectparser.core.domain.model.ConstructorModel;
import com.proyectparser.core.domain.model.MethodModel;
import com.proyectparser.core.domain.model.PackageModel;
import com.proyectparser.core.domain.model.ParameterModel;
import com.proyectparser.core.domain.model.ProjectModel;
import com.proyectparser.core.domain.model.RelationshipModel;
import com.proyectparser.server.dto.AnalysisResponse;
import com.proyectparser.server.dto.AttributeDto;
import com.proyectparser.server.dto.ClassDto;
import com.proyectparser.server.dto.ConstructorDto;
import com.proyectparser.server.dto.DiagramResponse;
import com.proyectparser.server.dto.MethodDto;
import com.proyectparser.server.dto.PackageDto;
import com.proyectparser.server.dto.ParameterDto;
import com.proyectparser.server.dto.RelationshipDto;
import com.proyectparser.server.dto.StatisticsDto;
import com.proyectparser.server.service.AnalysisOutcome;
import com.proyectparser.server.service.DiagramOutcome;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Maps the immutable core domain model to HTTP DTOs. Internal source types and
 * external {@code @external} boxes are kept apart, and every collection is
 * null-safe because the core is permissive with partially parsed sources.
 */
@Component
public class ProjectMapper {

    public AnalysisResponse toAnalysis(String projectName, AnalysisOutcome outcome) {
        ProjectModel project = outcome.project();
        return new AnalysisResponse(
                projectName,
                statistics(project, outcome),
                kinds(project),
                packages(project),
                classes(project),
                relationships(project),
                externalClasses(project),
                outcome.failedFiles(),
                outcome.failureReasons());
    }

    public DiagramResponse toDiagram(String projectName, DiagramOutcome outcome) {
        return new DiagramResponse(
                projectName,
                projectName + ".puml",
                outcome.plantUml(),
                statistics(outcome.analysis().project(), outcome.analysis()),
                outcome.analysis().failedFiles(),
                outcome.analysis().failureReasons());
    }

    private StatisticsDto statistics(ProjectModel project, AnalysisOutcome outcome) {
        int internal = internalClasses(project).size();
        int external = allClasses(project).size() - internal;
        int relationships = safe(project.getRelationships()).size();
        int packages = packages(project).size();
        return new StatisticsDto(outcome.totalJavaFiles(), outcome.parsedJavaFiles(),
                outcome.failedJavaFiles(), internal, external, relationships, packages);
    }

    private Map<String, Integer> kinds(ProjectModel project) {
        Map<String, Integer> counts = new TreeMap<>();
        for (ClassModel model : internalClasses(project)) {
            String kind = model.getKind() == null ? "(unknown)" : model.getKind();
            counts.merge(kind, 1, Integer::sum);
        }
        return counts;
    }

    private List<PackageDto> packages(ProjectModel project) {
        Map<String, List<String>> grouped = new TreeMap<>();
        for (ClassModel model : internalClasses(project)) {
            String packageName = model.getPackageName() == null ? "" : model.getPackageName();
            grouped.computeIfAbsent(packageName, ignored -> new ArrayList<>()).add(model.getName());
        }

        List<PackageDto> packages = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
            List<String> types = new ArrayList<>(entry.getValue());
            types.sort(String::compareTo);
            packages.add(new PackageDto(
                    entry.getKey(),
                    PackageModel.displayName(entry.getKey()),
                    types.size(),
                    types));
        }
        return packages;
    }

    private List<ClassDto> classes(ProjectModel project) {
        List<ClassDto> classes = new ArrayList<>();
        for (ClassModel model : internalClasses(project)) {
            classes.add(toClass(model));
        }
        return classes;
    }

    private ClassDto toClass(ClassModel model) {
        List<AttributeDto> attributes = new ArrayList<>();
        for (AttributeModel attribute : safe(model.getAttributes())) {
            attributes.add(new AttributeDto(attribute.getName(), attribute.getType(),
                    safe(attribute.getModifiers()), attribute.getInitialValue()));
        }

        List<MethodDto> methods = new ArrayList<>();
        for (MethodModel method : safe(model.getMethods())) {
            methods.add(new MethodDto(method.getName(), method.getReturnType(),
                    parameters(method.getParameters()), safe(method.getModifiers()),
                    safe(method.getTypeParameters())));
        }

        List<ConstructorDto> constructors = new ArrayList<>();
        for (ConstructorModel constructor : safe(model.getConstructors())) {
            constructors.add(new ConstructorDto(constructor.getName(),
                    parameters(constructor.getParameters()), safe(constructor.getModifiers()),
                    safe(constructor.getTypeParameters())));
        }

        return new ClassDto(
                model.id(),
                model.getName(),
                model.getFqn(),
                model.getPackageName(),
                model.getKind(),
                model.isAbstract(),
                model.isExternal(),
                safe(model.getStereotypes()),
                attributes,
                methods,
                constructors,
                safe(model.getExtendedTypes()),
                safe(model.getImplementedTypes()),
                safe(model.getEnumConstants()),
                safe(model.getTypeParameters()));
    }

    private List<ParameterDto> parameters(List<ParameterModel> parameters) {
        List<ParameterDto> result = new ArrayList<>();
        for (ParameterModel parameter : safe(parameters)) {
            result.add(new ParameterDto(parameter.getName(), parameter.getType()));
        }
        return result;
    }

    private List<RelationshipDto> relationships(ProjectModel project) {
        List<RelationshipDto> relationships = new ArrayList<>();
        for (RelationshipModel relationship : safe(project.getRelationships())) {
            relationships.add(new RelationshipDto(
                    relationship.getSource(), relationship.getTarget(), relationship.getType()));
        }
        return relationships;
    }

    private Map<String, String> externalClasses(ProjectModel project) {
        Map<String, String> externals = new TreeMap<>();
        for (ClassModel model : allClasses(project)) {
            if (model.isExternal()) {
                externals.put(model.id(), model.getPackageName());
            }
        }
        return new LinkedHashMap<>(externals);
    }

    private List<ClassModel> internalClasses(ProjectModel project) {
        List<ClassModel> result = new ArrayList<>();
        for (ClassModel model : allClasses(project)) {
            if (!model.isExternal()) {
                result.add(model);
            }
        }
        return result;
    }

    private List<ClassModel> allClasses(ProjectModel project) {
        return safe(project.getClasses());
    }

    private static <T> List<T> safe(List<T> values) {
        return values == null ? List.of() : values;
    }
}
