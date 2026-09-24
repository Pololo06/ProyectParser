package is.generador.domain.policy;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Filtro inclusivo/exclusivo para diagramas.
 *
 * <p><b>Blacklist</b> (criterio exclusivo): veta paquetes o clases sin importar
 * el contexto; siempre gana.</p>
 *
 * <p><b>Whitelist</b> (criterio inclusivo): si contiene al menos un elemento,
 * el filtro pasa a modo restrictivo y todo lo no declarado queda descartado.</p>
 *
 * <p>Precedencia: blacklist &gt; whitelist &gt; permitir.</p>
 */
public class DiagramFilter {

    private final Set<String> blacklistedPackages = new HashSet<>();
    private final Set<String> blacklistedClasses = new HashSet<>();
    private final Set<String> whitelistedPackages = new HashSet<>();
    private final Set<String> whitelistedClasses = new HashSet<>();

    public DiagramFilter() {
    }

    // ---------- blacklist: simple ----------

    public DiagramFilter addToBlacklistPackage(String packageName) {
        if (packageName != null && !packageName.trim().isEmpty()) {
            blacklistedPackages.add(packageName.trim());
        }
        return this;
    }

    public DiagramFilter addToBlacklistClass(String className) {
        if (className != null && !className.trim().isEmpty()) {
            blacklistedClasses.add(className.trim());
        }
        return this;
    }

    // ---------- blacklist: bulk ----------

    public DiagramFilter addToBlacklistPackages(Collection<String> packageNames) {
        if (packageNames != null) {
            packageNames.forEach(this::addToBlacklistPackage);
        }
        return this;
    }

    public DiagramFilter addToBlacklistClasses(Collection<String> classNames) {
        if (classNames != null) {
            classNames.forEach(this::addToBlacklistClass);
        }
        return this;
    }

    /** Mete TODOS los paquetes dados a la blacklist (seleccionar todo). */
    public DiagramFilter blacklistAllPackages(Collection<String> allPackages) {
        return addToBlacklistPackages(allPackages);
    }

    /** Mete TODAS las clases dadas a la blacklist (seleccionar todo). */
    public DiagramFilter blacklistAllClasses(Collection<String> allClasses) {
        return addToBlacklistClasses(allClasses);
    }

    // ---------- whitelist: simple ----------

    public DiagramFilter addToWhitelistPackage(String packageName) {
        if (packageName != null && !packageName.trim().isEmpty()) {
            whitelistedPackages.add(packageName.trim());
        }
        return this;
    }

    public DiagramFilter addToWhitelistClass(String className) {
        if (className != null && !className.trim().isEmpty()) {
            whitelistedClasses.add(className.trim());
        }
        return this;
    }

    // ---------- whitelist: bulk ----------

    public DiagramFilter addToWhitelistPackages(Collection<String> packageNames) {
        if (packageNames != null) {
            packageNames.forEach(this::addToWhitelistPackage);
        }
        return this;
    }

    public DiagramFilter addToWhitelistClasses(Collection<String> classNames) {
        if (classNames != null) {
            classNames.forEach(this::addToWhitelistClass);
        }
        return this;
    }

    /** Mete TODOS los paquetes dados a la whitelist (seleccionar todo). */
    public DiagramFilter whitelistAllPackages(Collection<String> allPackages) {
        return addToWhitelistPackages(allPackages);
    }

    /** Mete TODAS las clases dadas a la whitelist (seleccionar todo). */
    public DiagramFilter whitelistAllClasses(Collection<String> allClasses) {
        return addToWhitelistClasses(allClasses);
    }

    // ---------- consulta ----------

    /** true si la whitelist tiene al menos un elemento (modo restrictivo). */
    public boolean isRestrictive() {
        return !whitelistedPackages.isEmpty() || !whitelistedClasses.isEmpty();
    }

    /**
     * Indica si el par (paquete, clase) tiene permiso de aparecer.
     * Precedencia: blacklist &gt; whitelist &gt; permitir.
     */
    public boolean isAllowed(String packageName, String className) {
        String pkg = packageName == null ? "" : packageName.trim();
        String cls = className == null ? "" : className.trim();
        if (blacklistedPackages.contains(pkg) || blacklistedClasses.contains(cls)) {
            return false;
        }
        if (isRestrictive()) {
            return whitelistedPackages.contains(pkg) || whitelistedClasses.contains(cls);
        }
        return true;
    }

    public Set<String> getBlacklistedPackages() {
        return Collections.unmodifiableSet(blacklistedPackages);
    }

    public Set<String> getBlacklistedClasses() {
        return Collections.unmodifiableSet(blacklistedClasses);
    }

    public Set<String> getWhitelistedPackages() {
        return Collections.unmodifiableSet(whitelistedPackages);
    }

    public Set<String> getWhitelistedClasses() {
        return Collections.unmodifiableSet(whitelistedClasses);
    }
}
