package com.proyectparser.core.domain.policy;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Filtro inclusivo/exclusivo para diagramas. Inmutable: se construye con
 * {@link Builder} y no se puede modificar después de {@code build()}.
 *
 * <p><b>Blacklist</b> (criterio exclusivo, métodos {@code exclude*}): veta
 * paquetes o clases sin importar el contexto; siempre gana.</p>
 *
 * <p><b>Whitelist</b> (criterio inclusivo, métodos {@code include*}): si
 * contiene al menos un elemento, el filtro pasa a modo restrictivo y todo
 * lo no declarado queda descartado.</p>
 *
 * <p>Precedencia: blacklist &gt; whitelist &gt; permitir.</p>
 *
 * <pre>
 * DiagramFilter filtro = new DiagramFilter.Builder()
 *     .excludePackage("com.universidad.test")
 *     .excludeClass("Utilidades")
 *     .includePackage("com.universidad.modelo")
 *     .build();
 * </pre>
 */
public class DiagramFilter {

    private final Set<String> blacklistedPackages;
    private final Set<String> blacklistedClasses;
    private final Set<String> whitelistedPackages;
    private final Set<String> whitelistedClasses;

    /** Filtro permisivo vacío (todo permitido). */
    public DiagramFilter() {
        this(new Builder());
    }

    private DiagramFilter(Builder builder) {
        this.blacklistedPackages = Collections.unmodifiableSet(new HashSet<>(builder.blacklistedPackages));
        this.blacklistedClasses = Collections.unmodifiableSet(new HashSet<>(builder.blacklistedClasses));
        this.whitelistedPackages = Collections.unmodifiableSet(new HashSet<>(builder.whitelistedPackages));
        this.whitelistedClasses = Collections.unmodifiableSet(new HashSet<>(builder.whitelistedClasses));
    }

    /** Constructor paso a paso del filtro. Reutilizable: cada {@code build()} es independiente. */
    public static class Builder {
        private final Set<String> blacklistedPackages = new HashSet<>();
        private final Set<String> blacklistedClasses = new HashSet<>();
        private final Set<String> whitelistedPackages = new HashSet<>();
        private final Set<String> whitelistedClasses = new HashSet<>();

        /** Veta un paquete (blacklist). */
        public Builder excludePackage(String packageName) {
            addClean(this.blacklistedPackages, packageName);
            return this;
        }

        /** Veta una clase (blacklist). */
        public Builder excludeClass(String className) {
            addClean(this.blacklistedClasses, className);
            return this;
        }

        /** Veta varios paquetes (blacklist). */
        public Builder excludePackages(Collection<String> packageNames) {
            addAllClean(this.blacklistedPackages, packageNames);
            return this;
        }

        /** Veta varias clases (blacklist). */
        public Builder excludeClasses(Collection<String> classNames) {
            addAllClean(this.blacklistedClasses, classNames);
            return this;
        }

        /** Incluye un paquete (whitelist). */
        public Builder includePackage(String packageName) {
            addClean(this.whitelistedPackages, packageName);
            return this;
        }

        /** Incluye una clase (whitelist). */
        public Builder includeClass(String className) {
            addClean(this.whitelistedClasses, className);
            return this;
        }

        /** Incluye varios paquetes (whitelist). */
        public Builder includePackages(Collection<String> packageNames) {
            addAllClean(this.whitelistedPackages, packageNames);
            return this;
        }

        /** Incluye varias clases (whitelist). */
        public Builder includeClasses(Collection<String> classNames) {
            addAllClean(this.whitelistedClasses, classNames);
            return this;
        }

        /** Construye el filtro inmutable. */
        public DiagramFilter build() {
            return new DiagramFilter(this);
        }

        private static void addClean(Set<String> target, String value) {
            if (value != null && !value.trim().isEmpty()) {
                target.add(value.trim());
            }
        }

        private static void addAllClean(Set<String> target, Collection<String> values) {
            if (values != null) {
                for (String value : values) {
                    addClean(target, value);
                }
            }
        }
    }

    /** true si la whitelist tiene al menos un elemento (modo restrictivo). */
    public boolean isRestrictive() {
        return !whitelistedPackages.isEmpty() || !whitelistedClasses.isEmpty();
    }

    /**
     * true si el par (paquete, clase) está vetado por la blacklist,
     * sin evaluar la whitelist. Las clases externas solo obedecen
     * a este criterio (opción B): la whitelist es solo para internas.
     */
    public boolean isBlacklisted(String packageName, String className) {
        return isBlacklisted(packageName, className, null, null);
    }

    /**
     * Igual que {@link #isBlacklisted(String, String)}, aceptando además el id y el fqn
     * de la clase: un nombre simple afecta a todos sus homónimos; un id o fqn, solo a esa.
     */
    public boolean isBlacklisted(String packageName, String className, String id, String fqn) {
        String pkg = packageName == null ? "" : packageName.trim();
        return blacklistedPackages.contains(pkg) || matchesClass(blacklistedClasses, className, id, fqn);
    }

    /**
     * Indica si el par (paquete, clase) tiene permiso de aparecer.
     * Precedencia: blacklist &gt; whitelist &gt; permitir.
     */
    public boolean isAllowed(String packageName, String className) {
        return isAllowed(packageName, className, null, null);
    }

    /**
     * Igual que {@link #isAllowed(String, String)}, aceptando además el id y el fqn
     * de la clase: el nombre simple conserva su semántica sobre todos los homónimos.
     */
    public boolean isAllowed(String packageName, String className, String id, String fqn) {
        String pkg = packageName == null ? "" : packageName.trim();
        if (blacklistedPackages.contains(pkg) || matchesClass(blacklistedClasses, className, id, fqn)) {
            return false;
        }
        if (isRestrictive()) {
            return whitelistedPackages.contains(pkg) || matchesClass(whitelistedClasses, className, id, fqn);
        }
        return true;
    }

    private static boolean matchesClass(Set<String> names, String className, String id, String fqn) {
        String cls = className == null ? "" : className.trim();
        return names.contains(cls)
                || (id != null && names.contains(id))
                || (fqn != null && names.contains(fqn));
    }

    public Set<String> getBlacklistedPackages() {
        return blacklistedPackages;
    }

    public Set<String> getBlacklistedClasses() {
        return blacklistedClasses;
    }

    public Set<String> getWhitelistedPackages() {
        return whitelistedPackages;
    }

    public Set<String> getWhitelistedClasses() {
        return whitelistedClasses;
    }
}
