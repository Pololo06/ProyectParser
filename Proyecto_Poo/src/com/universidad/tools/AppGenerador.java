package com.universidad.tools;

import is.generador.domain.policy.DiagramFilter;
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

    public static void main(String[] args) {
        int errorCount = 0;

        // 1. Detección automatizada de ruta fuente y archivo de salida
        String srcPath = resolveSourcePath(args);
        String outputPathStr = (args.length > 1 && args[1] != null && !args[1].trim().isEmpty())
                ? args[1].trim()
                : "diagrama_filtrado.puml";

        logTrace("Ruta fuente detectada automáticamente: " + srcPath);

        ProjectModel originalProject;
        try {
            ProjectAnalyzer analyzer = new ProjectAnalyzer();
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

        // 3. Menú interactivo: Paquetes y Clases ordenados por tipo
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

        printTypeCounts(classList, "CONTADOR DE TIPOS DETECTADOS");

        Scanner scanner = new Scanner(System.in);
        DiagramFilter filter = new DiagramFilter();
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
        filter.addToBlacklistPackages(blackPkgs);
        blackPkgs.forEach(p -> System.out.println("  [-] Blacklist paquete: " + p));

        List<String> classNames = new ArrayList<>();
        for (ClassModel c : classList) {
            classNames.add(c.getName());
        }
        List<String> blackClasses = readSelection(scanner,
                "> BLACKLIST clases a excluir (Enter=ninguna): ", classNames);
        filter.addToBlacklistClasses(blackClasses);
        blackClasses.forEach(c -> System.out.println("  [-] Blacklist clase: " + c));

        List<String> whitePkgs = readSelection(scanner,
                "> WHITELIST paq. a incluir (Enter=todos): ", packageList);
        filter.addToWhitelistPackages(whitePkgs);
        whitePkgs.forEach(p -> System.out.println("  [+] Whitelist paquete: " + p));

        List<String> whiteClasses = readSelection(scanner,
                "> WHITELIST clases a incluir (Enter=todas): ", classNames);
        filter.addToWhitelistClasses(whiteClasses);
        whiteClasses.forEach(c -> System.out.println("  [+] Whitelist clase: " + c));

        builder.setFilter(filter);

        // 5. Construcción del modelo limpio mediante el Builder
        logTrace("Construyendo modelo filtrado con Builder...");
        ProjectModel filteredModel = builder.build();

        // 6. Resumen de paquetes y clases sobrevivientes (separados)
        List<PackageModel> resultPkgs = filteredModel.getPackages();
        List<ClassModel> resultClasses = filteredModel.getClasses();

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

        printTypeCounts(resultClasses, "CONTADOR DE TIPOS RESULTANTES");

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
                        String name = m.getName();
                        if (name.startsWith("get") || name.startsWith("is")) {
                            getterCount++;
                        } else if (name.startsWith("set")) {
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
                        if (m.getName().startsWith("get") || m.getName().startsWith("is")) {
                            tag = " [getter]";
                        } else if (m.getName().startsWith("set")) {
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

        // 8. Generación del diagrama PlantUML sin condicionales repetitivas
        PlantUmlGenerator generator = new PlantUmlGenerator();
        String pumlContent = generator.generate(filteredModel);

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