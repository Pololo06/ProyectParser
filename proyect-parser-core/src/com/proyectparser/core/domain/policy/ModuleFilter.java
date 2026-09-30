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
