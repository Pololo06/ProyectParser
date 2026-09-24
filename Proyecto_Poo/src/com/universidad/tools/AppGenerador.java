package com.universidad.tools;

import is.generador.domain.policy.DiagramFilter;
import is.generador.domain.policy.DiagramOptions;
import is.generador.domain.BeanAccessors;
import is.generador.domain.port.DiagramRendererPort;
import is.generador.domain.port.SourceAnalyzerPort;
import is.generador.infrastructure.javaparser.ProjectAnalyzer;
import is.generador.application.FilteredProjectBuilder;
import is.generador.domain.model.AttributeModel;
import is.generador.domain.model.ClassModel;
import is.generador.domain.model.ConstructorModel;
import is.generador.domain.model.MethodModel;
import is.generador.domain.model.PackageModel;
import is.generador.domain.model.ProjectModel;
import is.generador.domain.model.RelationshipModel;
import is.generador.infrastructure.plantuml.PlantUmlGenerator;
import is.generador.infrastructure.plantuml.PumlFileWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class AppGenerador {

    /**
     * Resuelve automáticamente la ruta del directorio "src".
     * Prioridad:
     * 1. Parámetro explícito en args[0] (si existe y es carpeta).
     * 2. Carpeta "src" en el directorio de trabajo actual.
     * 3. Inspección en subcarpetas del proyecto (evitando librerías o builds).
     */
    private static String resolveSourcePath(String[] args) {
        if (args.length > 0 && args[0] != null && !args[0].trim().isEmpty()) {
            File custom = new File(args[0].trim());
            if (custom.exists() && custom.isDirectory()) {
                return custom.getAbsolutePath();
            }
        }

        Path currentWorkingDir = Paths.get("").toAbsolutePath();

        // Probar ./src directo
        File directSrc = currentWorkingDir.resolve("src").toFile();
        if (directSrc.exists() && directSrc.isDirectory()) {
            return directSrc.getAbsolutePath();
        }

        // Probar si estamos parados en la raíz y buscar en subdirectorios de proyectos
        File currentDirFile = currentWorkingDir.toFile();
        File[] subFiles = currentDirFile.listFiles();
        if (subFiles != null) {
            for (File subDir : subFiles) {
                if (subDir.isDirectory() && !subDir.getName().startsWith(".")
                        && !subDir.getName().equals("out")
                        && !subDir.getName().equals("build")) {
                    File candidateSrc = new File(subDir, "src");
                    if (candidateSrc.exists() && candidateSrc.isDirectory()
                            && !subDir.getName().toLowerCase().contains("libreria")) {
                        return candidateSrc.getAbsolutePath();
                    }
                }
            }
        }

        return "src";
    }

    private static String parseVisibility(List<String> modifiers) {
        if (modifiers == null || modifiers.isEmpty()) {
            return "~"; // package-private por omisión
        }
        if (modifiers.contains("public")) return "+";
        if (modifiers.contains("private")) return "-";
        if (modifiers.contains("protected")) return "#";
        return "~";
    }

    private static void logTrace(String msg) {
        System.out.println("  [trace] " + msg);
    }

    /**
     * Lee una selección múltiple contra la lista mostrada.
     * Acepta: Enter (vacío), todo/all (todos), rangos (1-5, 3-, -4),
     * lista (1,3,5) y nombres directos. Devuelve nombres sin duplicados.
     */
    private static List<String> readSelection(Scanner scanner, String prompt, List<String> items) {
        System.out.print(prompt);
        String input = "";
        if (scanner.hasNextLine()) {
            input = scanner.nextLine().trim();
        }
        List<String> selected = new ArrayList<>();
        if (input.isEmpty()) {
            return selected;
        }
        if (input.equalsIgnoreCase("todo") || input.equalsIgnoreCase("all")) {
            selected.addAll(items);
            return selected;
        }
        for (String token : input.split(",")) {
            String clean = token.trim();
            if (clean.isEmpty()) {
                continue;
            }
            if (clean.contains("-")) {
                String[] bounds = clean.split("-", -1);
                try {
                    int from = bounds[0].trim().isEmpty() ? 1 : Integer.parseInt(bounds[0].trim());
                    int to = bounds.length < 2 || bounds[1].trim().isEmpty()
                            ? items.size() : Integer.parseInt(bounds[1].trim());
                    for (int i = from; i <= to && i <= items.size(); i++) {
                        if (i >= 1 && !selected.contains(items.get(i - 1))) {
                            selected.add(items.get(i - 1));
                        }
                    }
                } catch (NumberFormatException e) {
                    if (!selected.contains(clean)) {
                        selected.add(clean);
                    }
                }
                continue;
            }
            try {
                int idx = Integer.parseInt(clean) - 1;
                if (idx >= 0 && idx < items.size() && !selected.contains(items.get(idx))) {
                    selected.add(items.get(idx));
                } else if (idx < 0 || idx >= items.size()) {
                    System.out.println("  [!] Índice fuera de rango: " + clean);
                }
            } catch (NumberFormatException e) {
                if (!selected.contains(clean)) {
                    selected.add(clean);
                }
            }
        }
        return selected;
    }

    /**
     * Filtra una selección de whitelist contra los nombres internos conocidos.
     * El texto libre que no pertenece a la lista se ignora con aviso: un nombre
     * externo o desconocido crearía una whitelist no vacía, activaría el modo
     * restrictivo y podría excluir todas las clases internas. La blacklist
     * mantiene la selección libre, donde sí puede ser válida.
     */
    private static List<String> retainKnown(List<String> selected, List<String> known, String kind) {
        List<String> kept = new ArrayList<>();
        if (selected != null) {
            for (String name : selected) {
                if (known.contains(name)) {
                    kept.add(name);
                } else {
                    System.out.println("  [!] Ignorado en whitelist (no es " + kind + "): " + name);
                }
            }
        }
        return kept;
    }

    /**
     * Revisión interactiva de relaciones (Fase 5): lista las relaciones
     * detectadas y permite vetar las no deseadas antes de generar.
     * Devuelve el modelo reconstruido sin las vetadas.
     */
    private static ProjectModel reviewRelationships(Scanner scanner,
            FilteredProjectBuilder builder, ProjectModel filteredModel) {
        if (filteredModel == null || filteredModel.getRelationships() == null
                || filteredModel.getRelationships().isEmpty()) {
            System.out.println("\n  (sin relaciones detectadas)");
            return filteredModel;
        }
        System.out.println("\n==================================================");
        System.out.println(" RELACIONES DETECTADAS (solo lectura del AST)");
        System.out.println("==================================================");
        List<String> relKeys = new ArrayList<>();
        Map<String, String> relLabels = new java.util.LinkedHashMap<>();
        int relIdx = 0;
        for (RelationshipModel rel : filteredModel.getRelationships()) {
            String key = FilteredProjectBuilder.relationshipKey(rel);
            String label = rel.getSource() + " " + rel.getType() + " -> " + rel.getTarget();
            relKeys.add(key);
            relLabels.put(key, label);
            System.out.printf("  [%d] %s%n", (++relIdx), label);
        }
        List<String> vetoed = readSelection(scanner,
                "> Relaciones a DESCARTAR (Enter=ninguna): ", relKeys);
        for (String key : vetoed) {
            System.out.println("  [x] Relación descartada: " + relLabels.getOrDefault(key, key));
        }
        if (vetoed.isEmpty()) {
            return filteredModel;
        }
        builder.excludeRelationships(vetoed);
        logTrace("Reconstruyendo modelo sin " + vetoed.size() + " relación(es) vetada(s)...");
        return builder.build();
    }

    /**
     * Imprime el contador de tipos por kind (Class, Interface, Enum, Record,
     * AbstractClass, Annotation...) discriminando internas y externas, más el total.
     */
    private static void printTypeCounts(List<ClassModel> classes, String title) {
        System.out.println("\n==================================================");
        System.out.println(" " + title);
        System.out.println("==================================================");
        Map<String, Integer> internal = new TreeMap<>();
        Map<String, Integer> external = new TreeMap<>();
        int totalInt = 0;
        int totalExt = 0;
        if (classes != null) {
            for (ClassModel c : classes) {
                String kind = c.getKind() == null ? "(unknown)" : c.getKind();
                if (PlantUmlGenerator.isExternal(c)) {
                    external.put(kind, external.getOrDefault(kind, 0) + 1);
                    totalExt++;
                } else {
                    internal.put(kind, internal.getOrDefault(kind, 0) + 1);
                    totalInt++;
                }
            }
        }
        Set<String> kinds = new TreeSet<>(internal.keySet());
        kinds.addAll(external.keySet());
        if (kinds.isEmpty()) {
            System.out.println("  (sin tipos)");
        } else {
            for (String kind : kinds) {
                System.out.printf("  %-15s : %d (%d int + %d ext)%n",
                        kind, internal.getOrDefault(kind, 0) + external.getOrDefault(kind, 0),
                        internal.getOrDefault(kind, 0), external.getOrDefault(kind, 0));
            }
        }
        System.out.println("  --------------------------------");
        System.out.printf("  %-15s : %d (%d int + %d ext)%n", "TOTAL", totalInt + totalExt, totalInt, totalExt);
    }

    private static void printResultPackages(List<PackageModel> pkgs, Set<String> externalPkgs, boolean external) {
        boolean any = false;
        if (pkgs != null) {
            for (PackageModel p : pkgs) {
                if (externalPkgs.contains(p.getPackageName()) != external) {
                    continue;
                }
                any = true;
                int tiposCount = p.getTypeNames() != null ? p.getTypeNames().size() : 0;
                System.out.printf("  %s (%d tipos activos)%s%n",
                        p.getPackageName(), tiposCount, external ? " [external]" : "");
            }
        }
        if (!any) {
            System.out.println(external ? "  (ningún paquete externo)" : "  (ningún paquete interno)");
        }
    }

        private static void printResultClasses(List<ClassModel> classes, boolean external) {        boolean any = false;
        if (classes != null) {
            for (ClassModel c : classes) {
                if (PlantUmlGenerator.isExternal(c) != external) {
                    continue;
                }
                any = true;
                if (external) {
                    System.out.printf("  %s (paquete: %s)%n", c.getName(), c.getPackageName());
                } else {
                    System.out.printf("  %s [%s]%n", c.getName(), c.getKind());
                }
            }
        }
        if (!any) {
            System.out.println(external ? "  (ninguna clase externa)" : "  (ninguna clase interna)");
        }
    }

    /**
     * Extrae los argumentos posicionales (los que no empiezan con {@code --}).
     * Los flags de visualización no alteran las posiciones de ruta y salida.
     */
    private static String[] positionals(String[] args) {
        if (args == null) {
            return new String[0];
        }
        List<String> result = new ArrayList<>();
        for (String arg : args) {
            if (arg != null && !arg.trim().startsWith("-")) {
                result.add(arg);
            }
        }
        return result.toArray(new String[0]);
    }

    /**
     * Banderas de visualización por argumentos (Fase 2):
     * --no-getters --no-attributes --no-methods --no-constructors
     * --no-external --no-jdk --flat --help
     */
    private static DiagramOptions parseDiagramOptions(String[] args) {
        DiagramOptions.Builder options = new DiagramOptions.Builder();
        if (args == null) {
            return options.build();
        }
        for (String arg : args) {
            if (arg == null) {
                continue;
            }
            switch (arg.trim().toLowerCase()) {
                case "--no-getters":
                    options.showGettersSetters(false);
                    break;
                case "--no-attributes":
                    options.showAttributes(false);
                    break;
                case "--no-methods":
                    options.showMethods(false);
                    break;
                case "--no-constructors":
                    options.showConstructors(false);
                    break;
                case "--no-external":
                    options.showExternal(false);
                    break;
                case "--no-jdk":
                    options.showJdkTypes(false);
                    break;
                case "--flat":
                    options.groupByPackage(false);
                    break;
                case "--help":
                case "-h":
                    printUsage();
                    System.exit(0);
                    break;
                default:
                    if (arg.trim().startsWith("-")) {
                        System.err.println("Flag desconocido: " + arg);
                        printUsage();
                        System.exit(2);
                    }
                    break;
            }
        }
        return options.build();
    }

    private static void printUsage() {
        System.out.println("Uso: AppGenerador [ruta_src] [salida.puml] [flags]");
        System.out.println("Flags:");
        System.out.println("  --no-getters      Oculta getters/setters con campo respaldo");
        System.out.println("  --no-attributes   Oculta atributos");
        System.out.println("  --no-methods      Oculta métodos");
        System.out.println("  --no-constructors Oculta constructores");
        System.out.println("  --no-external     Oculta cajas @external y sus relaciones");
        System.out.println("  --no-jdk          Oculta solo externas del JDK (java.*)");
        System.out.println("  --flat            Sin bloques package (plano)");
        System.out.println("  --help, -h        Muestra esta ayuda");
    }

    private static void logOptions(DiagramOptions options) {
        logTrace(String.format(
                "Opciones: getters=%s attributes=%s methods=%s constructors=%s external=%s jdk=%s grouped=%s",
                options.isShowGettersSetters(), options.isShowAttributes(), options.isShowMethods(),
                options.isShowConstructors(), options.isShowExternal(), options.isShowJdkTypes(),
                options.isGroupByPackage()));
    }

    public static void main(String[] args) {
        int errorCount = 0;

        // 1. Detección automatizada de ruta fuente y archivo de salida
        // (los flags --* no cuentan como posicionales).
        String[] positional = positionals(args);
        String srcPath = resolveSourcePath(positional);
        String outputPathStr = (positional.length > 1 && positional[1] != null && !positional[1].trim().isEmpty())
                ? positional[1].trim()
                : "diagrama_filtrado.puml";
        DiagramOptions options = parseDiagramOptions(args);

        logTrace("Ruta fuente detectada automáticamente: " + srcPath);
        logOptions(options);

        ProjectModel originalProject;
        try {
            SourceAnalyzerPort analyzer = new ProjectAnalyzer();
            originalProject = analyzer.analyze(srcPath);
        } catch (IOException e) {
            errorCount++;
            logTrace("Error crítico al analizar código fuente: " + e.getMessage());
            System.err.println("No se pudo analizar el código fuente en: " + srcPath);
            return;
        }

        // 2. Extraer listas para menú inicial
        List<String> packageList = new ArrayList<>();
        if (originalProject.getPackages() != null) {
            for (PackageModel pkg : originalProject.getPackages()) {
                if (!packageList.contains(pkg.getPackageName())) {
                    packageList.add(pkg.getPackageName());
                }
            }
        }

        List<ClassModel> classList = new ArrayList<>();
        if (originalProject.getClasses() != null) {
            classList.addAll(originalProject.getClasses());
        }

        // Separar internas de externas (nunca juntas): internas primero, externas después.
        // El orden combinado define los índices de selección.
        List<ClassModel> internalClasses = new ArrayList<>();
        List<ClassModel> externalClasses = new ArrayList<>();
        for (ClassModel c : classList) {
            (PlantUmlGenerator.isExternal(c) ? externalClasses : internalClasses).add(c);
        }
        classList = new ArrayList<>(internalClasses);
        classList.addAll(externalClasses);

        Set<String> externalPkgNames = new HashSet<>();
        for (ClassModel c : externalClasses) {
            externalPkgNames.add(c.getPackageName());
        }
        List<String> internalPkgs = new ArrayList<>();
        List<String> externalPkgs = new ArrayList<>();
        for (String p : packageList) {
            (externalPkgNames.contains(p) ? externalPkgs : internalPkgs).add(p);
        }
        packageList = new ArrayList<>(internalPkgs);
        packageList.addAll(externalPkgs);

        // 3. Resumen global primero, luego listados (Fase 4).
        printTypeCounts(classList, "RESUMEN GLOBAL DEL PROYECTO");

        // Menú interactivo: Paquetes y Clases ordenados por tipo
        System.out.println("==================================================");
        System.out.println(" PAQUETES INTERNOS DEL PROYECTO");
        System.out.println("==================================================");
        int pkgIdx = 0;
        for (String p : internalPkgs) {
            System.out.printf("  [%d] %s%n", (++pkgIdx), p);
        }
        if (internalPkgs.isEmpty()) {
            System.out.println("  (ninguno)");
        }
        System.out.println("\n--------------------------------------------------");
        System.out.println(" PAQUETES EXTERNOS (JDK / librerías)");
        System.out.println("--------------------------------------------------");
        for (String p : externalPkgs) {
            System.out.printf("  [%d] %s [external]%n", (++pkgIdx), p);
        }
        if (externalPkgs.isEmpty()) {
            System.out.println("  (ninguno)");
        }

        // Agrupar clases internas por tipo (kind) para mostrarlas ordenadas
        Map<String, List<ClassModel>> internalByType = new TreeMap<>();
        for (ClassModel c : internalClasses) {
            String kind = c.getKind() != null ? c.getKind() : "CLASS";
            internalByType.computeIfAbsent(kind, k -> new ArrayList<>()).add(c);
        }

        System.out.println("\n==================================================");
        System.out.println(" CLASES INTERNAS DEL PROYECTO (ORDENADAS POR TIPO)");
        System.out.println("==================================================");
        int clsIdx = 0;
        // Mapeo auxiliar para mantener la correspondencia con classList si fuera necesario
        List<ClassModel> orderedInternalClasses = new ArrayList<>();
        for (Map.Entry<String, List<ClassModel>> entry : internalByType.entrySet()) {
            System.out.println("\n--- TIPO: " + entry.getKey().toUpperCase() + " ---");
            for (ClassModel c : entry.getValue()) {
                orderedInternalClasses.add(c);
                System.out.printf("  [%d] %s [%s]%n", (++clsIdx), c.getName(), c.getKind());
            }
        }
        // Actualizamos internalClasses con el nuevo orden agrupado
        internalClasses = orderedInternalClasses;
        
        // Reconstruimos classList combinado con el nuevo orden interno
        classList = new ArrayList<>(internalClasses);
        classList.addAll(externalClasses);

        if (internalClasses.isEmpty()) {
            System.out.println("  (ninguna)");
        }
        System.out.println("\n--------------------------------------------------");
        System.out.println(" CLASES EXTERNAS (JDK / librerías)");
        System.out.println("--------------------------------------------------");
        for (ClassModel c : externalClasses) {
            System.out.printf("  [%d] %s (paquete: %s)%n",
                    (++clsIdx), c.getName(), c.getPackageName());
        }
        if (externalClasses.isEmpty()) {
            System.out.println("  (ninguna)");
        }

        Scanner scanner = new Scanner(System.in);
        FilteredProjectBuilder builder = new FilteredProjectBuilder(originalProject);

        // 4. Captura de blacklist/whitelist con selección múltiple.
        // Sintaxis por prompt: Enter = ninguno | todo/all = todos |
        // rangos (1-5, 3-, -4) | lista (1,3,5) | nombres directos.
        // Whitelist vacía = permitir todo.
        System.out.println("\n--------------------------------------------------");
        System.out.println(" Sintaxis: Enter=ninguno | todo=todos | rangos 1-5 | lista 1,3 | nombres");
        System.out.println("--------------------------------------------------");

        List<String> blackPkgs = readSelection(scanner,
                "> BLACKLIST paq. a excluir (Enter=ninguno): ", packageList);
        blackPkgs.forEach(p -> System.out.println("  [-] Blacklist paquete: " + p));

        List<String> classNames = new ArrayList<>();
        for (ClassModel c : classList) {
            classNames.add(c.getName());
        }
        List<String> blackClasses = readSelection(scanner,
                "> BLACKLIST clases a excluir (Enter=ninguna): ", classNames);
        blackClasses.forEach(c -> System.out.println("  [-] Blacklist clase: " + c));

        List<String> whitePkgs = retainKnown(readSelection(scanner,
                "> WHITELIST paq. a incluir (Enter=todos, solo internas): ", internalPkgs),
                internalPkgs, "paquete interno");
        whitePkgs.forEach(p -> System.out.println("  [+] Whitelist paquete: " + p));

        List<String> internalClassNames = new ArrayList<>();
        for (ClassModel c : internalClasses) {
            internalClassNames.add(c.getName());
        }
        List<String> whiteClasses = retainKnown(readSelection(scanner,
                "> WHITELIST clases a incluir (Enter=todas, solo internas): ", internalClassNames),
                internalClassNames, "clase interna");
        whiteClasses.forEach(c -> System.out.println("  [+] Whitelist clase: " + c));
        System.out.println("  (nota: las externas solo obedecen a blacklist y a --no-external/--no-jdk)");

        DiagramFilter filter = new DiagramFilter.Builder()
                .excludePackages(blackPkgs)
                .excludeClasses(blackClasses)
                .includePackages(whitePkgs)
                .includeClasses(whiteClasses)
                .build();

        builder.setFilter(filter);

        // 5. Construcción del modelo limpio mediante el Builder
        logTrace("Construyendo modelo filtrado con Builder...");
        ProjectModel filteredModel = builder.build();

        // 5b. Revisión de relaciones: aceptar o rechazar antes de generar (Fase 5).
        // Solo se eliminan relaciones detectadas por el AST; nunca se inventan.
        // Sintaxis: Enter=ninguna (conservar todas) | todo=todas fuera |
        // rangos (1-5) | lista (1,3,5).
        filteredModel = reviewRelationships(scanner, builder, filteredModel);

        // 6. Resumen global resultante primero, luego listados (Fase 4).
        List<PackageModel> resultPkgs = filteredModel.getPackages();
        List<ClassModel> resultClasses = filteredModel.getClasses();

        printTypeCounts(resultClasses, "RESUMEN GLOBAL RESULTANTE");

        Set<String> resultExternalPkgs = new HashSet<>();
        if (resultClasses != null) {
            for (ClassModel c : resultClasses) {
                if (PlantUmlGenerator.isExternal(c)) {
                    resultExternalPkgs.add(c.getPackageName());
                }
            }
        }

        System.out.println("\n==================================================");
        System.out.println(" PAQUETES INTERNOS RESULTANTES");
        System.out.println("==================================================");
        printResultPackages(resultPkgs, resultExternalPkgs, false);

        System.out.println("\n--------------------------------------------------");
        System.out.println(" PAQUETES EXTERNOS RESULTANTES");
        System.out.println("--------------------------------------------------");
        printResultPackages(resultPkgs, resultExternalPkgs, true);

        System.out.println("\n==================================================");
        System.out.println(" CLASES INTERNAS RESULTANTES");
        System.out.println("==================================================");
        printResultClasses(resultClasses, false);

        System.out.println("\n--------------------------------------------------");
        System.out.println(" CLASES EXTERNAS RESULTANTES");
        System.out.println("--------------------------------------------------");
        printResultClasses(resultClasses, true);

        // 7. Reporte detallado: internas con todo el detalle; externas compactas
        System.out.println("\n==================================================");
        System.out.println(" DETALLE DE CLASES INTERNAS");
        System.out.println("==================================================");
        if (resultClasses != null) {
            for (ClassModel clazz : resultClasses) {
                if (PlantUmlGenerator.isExternal(clazz)) {
                    continue;
                }
                System.out.println("\n--------------------------------------------------");
                System.out.println("Class              : " + clazz.getName());
                System.out.println("Estereotipo / Kind : " + clazz.getKind());
                System.out.println("Paquete            : " + clazz.getPackageName());

                int getterCount = 0;
                int setterCount = 0;
                if (clazz.getMethods() != null) {
                    for (MethodModel m : clazz.getMethods()) {
                        if (BeanAccessors.isGetter(m, clazz)) {
                            getterCount++;
                        } else if (BeanAccessors.isSetter(m, clazz)) {
                            setterCount++;
                        }
                    }
                }
                System.out.printf("Resumen Accesores  : %d getters | %d setters%n", getterCount, setterCount);

                System.out.println("\n  [Propiedades / Atributos]:");
                if (clazz.getAttributes() != null && !clazz.getAttributes().isEmpty()) {
                    for (AttributeModel attr : clazz.getAttributes()) {
                        String vis = parseVisibility(attr.getModifiers());
                        System.out.println("    " + vis + " " + attr.getName() + " : " + attr.getType());
                    }
                } else {
                    System.out.println("    (sin atributos)");
                }

                if (clazz.getConstructors() != null && !clazz.getConstructors().isEmpty()) {
                    System.out.println("\n  [Constructores]:");
                    for (ConstructorModel ctor : clazz.getConstructors()) {
                        String vis = parseVisibility(ctor.getModifiers());
                        System.out.println("    " + vis + " " + ctor.getName() + "()");
                    }
                }

                System.out.println("\n  [Métodos]:");
                if (clazz.getMethods() != null && !clazz.getMethods().isEmpty()) {
                    for (MethodModel m : clazz.getMethods()) {
                        String vis = parseVisibility(m.getModifiers());
                        String tag = "";
                        if (BeanAccessors.isGetter(m, clazz)) {
                            tag = " [getter]";
                        } else if (BeanAccessors.isSetter(m, clazz)) {
                            tag = " [setter]";
                        }
                        System.out.println("    " + vis + " " + m.getName() + "() : " + m.getReturnType() + tag);
                    }
                } else {
                    System.out.println("    (sin métodos)");
                }
            }
        }

        System.out.println("\n==================================================");
        System.out.println(" DETALLE DE CLASES EXTERNAS (compacto)");
        System.out.println("==================================================");
        if (resultClasses != null) {
            for (ClassModel clazz : resultClasses) {
                if (!PlantUmlGenerator.isExternal(clazz)) {
                    continue;
                }
                List<String> usedBy = new ArrayList<>();
                if (filteredModel.getRelationships() != null) {
                    for (RelationshipModel rel : filteredModel.getRelationships()) {
                        if (rel.getTarget() != null && rel.getTarget().equals(clazz.getName())
                                && !usedBy.contains(rel.getSource())) {
                            usedBy.add(rel.getSource());
                        }
                    }
                }
                System.out.println("  " + clazz.getName() + " (paquete: " + clazz.getPackageName() + ")"
                        + (usedBy.isEmpty() ? "" : " usada por: " + String.join(", ", usedBy)));
            }
        }

        // 8. Generación del diagrama PlantUML con las banderas elegidas
        DiagramRendererPort renderer = new PlantUmlGenerator();
        String pumlContent = renderer.render(filteredModel, options);

        // 9. Banner gigante
        System.out.println("\n" +
                "EEEE   M   M  PPPP   IIIII  EEEEE  ZZZZZ   AAA      CCCCC  OOOO   DDDD   IIIII   GGGG   OOOO     U   U  M   M  L     \n" +
                "E      MM MM  P   P    I    E         Z   A   A    C      O    O  D   D    I    G      O    O    U   U  MM MM  L     \n" +
                "EEEE   M M M  PPPP     I    EEEE     Z    AAAAA    C      O    O  D   D    I    G  GG  O    O    U   U  M M M  L     \n" +
                "E      M   M  P        I    E       Z     A   A    C      O    O  D   D    I    G   G  O    O    U   U  M   M  L     \n" +
                "EEEEE  M   M  P      IIIII  EEEEE  ZZZZZ  A   A     CCCCC  OOOO   DDDD   IIIII   GGGG   OOOO      UUU   M   M  LLLLL "
        );

        System.out.println("\n" + pumlContent);

        // 10. Exportar a archivo
        Path outputPath = Paths.get(outputPathStr);
        try {
            PumlFileWriter.write(outputPath, pumlContent);
            logTrace("Diagrama exportado con éxito en: " + outputPath.toAbsolutePath());
            System.out.println("[✓] Diagrama guardado en: " + outputPath.toAbsolutePath());
        } catch (IOException e) {
            errorCount++;
            logTrace("Error al escribir archivo .puml: " + e.getMessage());
            System.err.println("No se pudo escribir el archivo de salida: " + e.getMessage());
        }

        logTrace("Sesión completada. Total de errores: " + errorCount);
        scanner.close();
    }
}