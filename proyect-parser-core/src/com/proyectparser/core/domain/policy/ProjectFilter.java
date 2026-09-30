package com.proyectparser.core.domain.policy;

import com.proyectparser.core.domain.model.ClassModel;
import com.proyectparser.core.domain.model.ProjectModel;
import com.proyectparser.core.domain.model.RelationshipModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Política de dominio: qué contiene un diagrama. Aplica un {@link DiagramFilter}
 * (lista negra/blanca) y los vetos de relaciones sobre un {@link ProjectModel}
 * y devuelve un modelo nuevo; no modifica el original.
 * El filtro decide por paquete y nombre simple; las clases vetadas y los extremos
 * de las relaciones se cruzan por id ({@link ClassModel#id()}), así que excluir
 * {@code h.b} no se lleva a la homónima {@code h.a.Foo}.
 */
public final class ProjectFilter {

    private ProjectFilter() {
    }

    /** Igual que {@link #apply(ProjectModel, DiagramFilter, Collection)} sin relaciones vetadas. */
    public static ProjectModel apply(ProjectModel project, DiagramFilter filter) {
        return apply(project, filter, List.of());
    }

    /**
     * @param filter                 {@code null} equivale a no filtrar
     * @param vetoedRelationshipKeys claves {@code "origen|TIPO|destino"} a descartar;
     *                               se recortan y se ignoran las nulas o vacías
     * @throws IllegalStateException si {@code project} es {@code null}
     */
    public static ProjectModel apply(ProjectModel project, DiagramFilter filter,
            Collection<String> vetoedRelationshipKeys) {
        if (project == null) {
            throw new IllegalStateException("Se requiere un ProjectModel original para construir el modelo filtrado.");
        }

        Set<String> vetoedRelationships = new HashSet<>();
        if (vetoedRelationshipKeys != null) {
            for (String key : vetoedRelationshipKeys) {
                if (key != null && !key.trim().isEmpty()) {
                    vetoedRelationships.add(key.trim());
                }
            }
        }

        Set<String> blacklistedClasses = new HashSet<>();

        // 1. Fusionar DiagramFilter. Opción B: las externas solo obedecen
        // a la blacklist; la whitelist es solo para clases internas.
        if (project.getClasses() != null && filter != null) {
            for (ClassModel clazz : project.getClasses()) {
                boolean vetoed = clazz.isExternal()
                        ? filter.isBlacklisted(clazz.getPackageName(), clazz.getName())
                        : !filter.isAllowed(clazz.getPackageName(), clazz.getName());
                if (vetoed) {
                    blacklistedClasses.add(clazz.id());
                }
            }
        }

        // 2. Filtrar clases sobrevivientes
        List<ClassModel> filteredClasses = new ArrayList<>();
        if (project.getClasses() != null) {
            for (ClassModel clazz : project.getClasses()) {
                if (!blacklistedClasses.contains(clazz.id())) {
                    filteredClasses.add(clazz);
                }
            }
        }

        // 3. Filtrar relaciones comprobando que ambos extremos sobrevivan
        // y que la relación no haya sido vetada (Fase 5).
        List<RelationshipModel> filteredRelationships = new ArrayList<>();
        if (project.getRelationships() != null) {
            for (RelationshipModel rel : project.getRelationships()) {
                if (!blacklistedClasses.contains(rel.getSource()) && !blacklistedClasses.contains(rel.getTarget())
                        && !vetoedRelationships.contains(rel.key())) {
                    filteredRelationships.add(rel);
                }
            }
        }

        // 3b. Regla de huérfanas (opción B): tras filtrar y vetar,
        // elimina las externas que ya no tengan ninguna relación.
        // Solo externas: las internas se conservan aunque queden aisladas.
        Set<String> linkedNames = new HashSet<>();
        for (RelationshipModel rel : filteredRelationships) {
            linkedNames.add(rel.getSource());
            linkedNames.add(rel.getTarget());
        }
        List<ClassModel> survivors = new ArrayList<>();
        for (ClassModel clazz : filteredClasses) {
            if (!clazz.isExternal() || linkedNames.contains(clazz.id())) {
                survivors.add(clazz);
            }
        }
        filteredClasses = survivors;

        // 4. Instanciar el ProjectModel canónico con getProjectName()
        return new ProjectModel(project.getProjectName(), filteredClasses, filteredRelationships);
    }
}
