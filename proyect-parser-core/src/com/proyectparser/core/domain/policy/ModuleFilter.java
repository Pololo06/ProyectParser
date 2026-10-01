package com.proyectparser.core.domain.policy;

import com.proyectparser.core.domain.model.ClassModel;
import com.proyectparser.core.domain.model.ProjectModel;
import com.proyectparser.core.domain.model.RelType;
import com.proyectparser.core.domain.model.RelationshipModel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Selecciona las clases de un módulo vertical (p. ej. {@code Estudiante}) para
 * un diagrama por módulo. Pertenece a {@code domain.policy}: no depende de infraestructura.
 *
 * <p>Incluye las clases internas cuyo nombre contiene el módulo como palabras
 * CamelCase completas (sin distinguir mayúsculas: {@code Curso} coincide con
 * {@code CursoDto} y {@code ConfiguracionModuloCurso}, no con {@code RecursoX}), sus supertipos transitivos ({@code EXTENDS}/{@code IMPLEMENTS}:
 * interfaces y clases base como {@code RepositorioBase}) y los destinos internos de
 * sus asociaciones directas (p. ej. {@code EstadoEntidad}).</p>
 *
 * <pre>
 * DiagramFilter filtro = new DiagramFilter.Builder()
 *     .includeClasses(ModuleFilter.classesOf(proyecto, "Estudiante"))
 *     .build();
 * </pre>
 */
public final class ModuleFilter {

    private ModuleFilter() {
    }

    /** Nombres de las clases del módulo, ordenados; vacío si el módulo es nulo/vacío o no coincide nada. */
    public static Set<String> classesOf(ProjectModel project, String module) {
        Set<String> names = new TreeSet<>();
        if (project == null || project.getClasses() == null || module == null || module.trim().isEmpty()) {
            return names;
        }
        List<String> needle = words(module.trim());
        Map<String, ClassModel> internalsById = new HashMap<>();
        Set<String> seeds = new HashSet<>();
        for (ClassModel model : project.getClasses()) {
            if (model.isExternal()) {
                continue;
            }
            internalsById.put(model.id(), model);
            if (model.getName() != null && containsWords(words(model.getName()), needle)) {
                seeds.add(model.id());
            }
        }

        Map<String, Set<String>> supertypes = new HashMap<>();
        Map<String, Set<String>> associations = new HashMap<>();
        if (project.getRelationships() != null) {
            for (RelationshipModel rel : project.getRelationships()) {
                RelType type = RelType.fromLabel(rel.getType());
                if (type == RelType.EXTENDS || type == RelType.IMPLEMENTS) {
                    supertypes.computeIfAbsent(rel.getSource(), k -> new HashSet<>()).add(rel.getTarget());
                } else if (type == RelType.ASSOCIATION) {
                    associations.computeIfAbsent(rel.getSource(), k -> new HashSet<>()).add(rel.getTarget());
                }
            }
        }

        Set<String> selected = new HashSet<>(seeds);
        for (String seed : seeds) {
            for (String target : associations.getOrDefault(seed, Set.of())) {
                if (internalsById.containsKey(target)) {
                    selected.add(target);
                }
            }
        }
        // Supertypes of everything selected so far, transitively.
        Deque<String> pending = new ArrayDeque<>(selected);
        while (!pending.isEmpty()) {
            for (String parent : supertypes.getOrDefault(pending.pop(), Set.of())) {
                if (internalsById.containsKey(parent) && selected.add(parent)) {
                    pending.push(parent);
                }
            }
        }

        for (String id : selected) {
            names.add(internalsById.get(id).getName());
        }
        return names;
    }

    /**
     * Solo las clases internas nombradas por el módulo (sin supertipos ni asociaciones de
     * contexto): las que {@code --modulo} considera propias al conservar sus {@code ..>}.
     */
    public static Set<String> namedClassesOf(ProjectModel project, String module) {
        Set<String> names = new TreeSet<>();
        if (project == null || project.getClasses() == null || module == null || module.trim().isEmpty()) {
            return names;
        }
        List<String> needle = words(module.trim());
        for (ClassModel model : project.getClasses()) {
            if (!model.isExternal() && model.getName() != null
                    && containsWords(words(model.getName()), needle)) {
                names.add(model.getName());
            }
        }
        return names;
    }

    /**
     * Dependencias ({@code ..>}) que la vista del módulo conserva con {@code --sin-dependencias},
     * como claves {@link DiagramOptions#dependencyKey}: ambas puntas nombradas por el módulo, salvo
     * las redundantes. {@code A ..> X} es redundante si un supertipo directo de {@code A}
     * ({@code EXTENDS}/{@code IMPLEMENTS}) ya conserva {@code ..> X}: p. ej.
     * {@code EstudianteServicio ..> EstudianteDto} cuando {@code EstudianteServicioPort ..> EstudianteDto}.
     * Las cadenas por asociaciones no cuentan ({@code EstudianteVista ..> EstudianteDto} se conserva).
     */
    public static Set<String> dependenciesOf(ProjectModel project, String module) {
        Set<String> keys = new TreeSet<>();
        Set<String> named = namedClassesOf(project, module);
        if (named.isEmpty() || project.getRelationships() == null) {
            return keys;
        }
        Map<String, ClassModel> byId = new HashMap<>();
        for (ClassModel model : project.getClasses()) {
            byId.put(model.id(), model);
        }
        Map<String, Set<String>> supertypes = new HashMap<>();
        Map<String, Set<String>> dependencies = new HashMap<>();
        for (RelationshipModel rel : project.getRelationships()) {
            RelType type = RelType.fromLabel(rel.getType());
            ClassModel source = byId.get(rel.getSource());
            ClassModel target = byId.get(rel.getTarget());
            if (source == null || target == null) {
                continue;
            }
            if (type == RelType.EXTENDS || type == RelType.IMPLEMENTS) {
                supertypes.computeIfAbsent(source.getName(), k -> new HashSet<>()).add(target.getName());
            } else if (type == RelType.DEPENDENCY
                    && named.contains(source.getName()) && named.contains(target.getName())) {
                dependencies.computeIfAbsent(source.getName(), k -> new HashSet<>()).add(target.getName());
            }
        }
        for (Map.Entry<String, Set<String>> entry : dependencies.entrySet()) {
            for (String target : entry.getValue()) {
                boolean redundant = false;
                for (String parent : supertypes.getOrDefault(entry.getKey(), Set.of())) {
                    redundant |= dependencies.getOrDefault(parent, Set.of()).contains(target);
                }
                if (!redundant) {
                    keys.add(DiagramOptions.dependencyKey(entry.getKey(), target));
                }
            }
        }
        return keys;
    }

    /** CamelCase words in lower case: {@code "UUIDCursoDto"} gives {@code [uuid, curso, dto]}. */
    static List<String> words(String name) {
        List<String> words = new ArrayList<>();
        for (String word : name.split("(?<=[\\p{Ll}\\p{N}])(?=\\p{Lu})|(?<=\\p{Lu})(?=\\p{Lu}\\p{Ll})|[^\\p{L}\\p{N}]+")) {
            if (!word.isEmpty()) {
                words.add(word.toLowerCase(Locale.ROOT));
            }
        }
        return words;
    }

    /** true if {@code needle} appears as a consecutive run of whole words in {@code words}. */
    private static boolean containsWords(List<String> words, List<String> needle) {
        return !needle.isEmpty() && Collections.indexOfSubList(words, needle) >= 0;
    }
}
