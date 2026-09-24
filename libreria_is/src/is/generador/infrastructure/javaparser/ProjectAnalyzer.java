package is.generador.infrastructure.javaparser;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.WildcardType;
import is.generador.domain.model.AttributeModel;
import is.generador.domain.model.ClassModel;
import is.generador.domain.model.ConstructorModel;
import is.generador.domain.model.MethodModel;
import is.generador.domain.model.ParameterModel;
import is.generador.domain.model.ProjectModel;
import is.generador.domain.model.RelationshipModel;

import java.io.File;
import java.io.IOException;
import is.generador.domain.policy.DiagramFilter;
import is.generador.domain.port.RunStatsProvider;
import is.generador.domain.port.SourceAnalyzerPort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Analyzes a Java source tree with JavaParser and builds the internal domain model.
 *
 * Covers:
 * - nested types at any depth (classes, interfaces, enums, records, annotations)
 * - generic type arguments (List&lt;T&gt;, Map&lt;K,V&gt;, wildcards, nested generics)
 * - enum constants via EnumDeclaration.getEntries()
 */
public class ProjectAnalyzer implements SourceAnalyzerPort, RunStatsProvider {

    static {
        StaticJavaParser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.BLEEDING_EDGE);
    }

    private final List<String> parsedFiles = new ArrayList<>();
    private final List<String> failedFiles = new ArrayList<>();
    private final List<String> failureReasons = new ArrayList<>();
    // Imports por clase (simpleName -> package), según el archivo donde se declaró.
    private final Map<String, Map<String, String>> fileImportsByClass = new HashMap<>();
    // Filtro blacklist/whitelist (nunca null: vacío = permisivo).
    private DiagramFilter filter = new DiagramFilter();

    public ProjectAnalyzer() {
    }

    public ProjectAnalyzer(DiagramFilter filter) {
        setFilter(filter);
    }

    public ProjectAnalyzer setFilter(DiagramFilter filter) {
        this.filter = (filter != null) ? filter : new DiagramFilter();
        return this;
    }

    public DiagramFilter getFilter() {
        return filter;
    }

    @Override
    public ProjectModel analyze(String folderPath) throws IOException {
        File folder = new File(folderPath);
        if (!folder.isDirectory()) {
            throw new IOException("Folder not found: " + folderPath);
        }
        parsedFiles.clear();
        failedFiles.clear();
        failureReasons.clear();
        fileImportsByClass.clear();
        List<ClassModel> classes = new ArrayList<>();
        List<RelationshipModel> relationships = new ArrayList<>();
        traverseFolder(folder, classes);
        detectRelationships(classes, relationships);
        registerExternalTypes(classes, relationships);
        return new ProjectModel(folder.getName(), classes, relationships);
    }

    /** Files successfully parsed in the last {@link #analyze(String)} run. */
    public List<String> getParsedFiles() {
        return new ArrayList<>(parsedFiles);
    }

    /** Files that failed to parse in the last {@link #analyze(String)} run. */
    public List<String> getFailedFiles() {
        return new ArrayList<>(failedFiles);
    }

    /** Human-readable "file -> reason" entries for the last run. */
    public List<String> getFailureReasons() {
        return new ArrayList<>(failureReasons);
    }

    public int getParsedFileCount() {
        return parsedFiles.size();
    }

    public int getFailedFileCount() {
        return failedFiles.size();
    }

    public int getTotalJavaFileCount() {
        return parsedFiles.size() + failedFiles.size();
    }

    private void traverseFolder(File folder, List<ClassModel> classes) throws IOException {
        File[] contents = folder.listFiles();
        if (contents == null) {
            return;
        }
        for (File file : contents) {
            if (file.isDirectory()) {
                traverseFolder(file, classes);
            } else if (file.getName().endsWith(".java")) {
                try {
                    classes.addAll(analyzeFile(file));
                    parsedFiles.add(file.getPath());
                } catch (Exception e) {
                    String reason = e.getMessage() == null ? e.toString() : e.getMessage();
                    failedFiles.add(file.getPath());
                    failureReasons.add(file.getPath() + " -> " + reason);
                    // RNF-04/RNF-06: no System.err here; failures are exposed via
                    // getFailedFiles()/getFailureReasons()/getLastAnalysisSummary().
                }
            }
        }
    }

    private List<ClassModel> analyzeFile(File javaFile) throws IOException {
        List<ClassModel> result = new ArrayList<>();
        CompilationUnit compilationUnit = StaticJavaParser.parse(javaFile);
        String packageName = compilationUnit.getPackageDeclaration()
                .map(pd -> pd.getNameAsString()).orElse("");

        // findAll covers nested types at any depth (inner classes, records inside interfaces, etc.)
        List<TypeDeclaration> rawTypes = compilationUnit.findAll(TypeDeclaration.class);

        for (TypeDeclaration raw : rawTypes) {
            @SuppressWarnings("unchecked")
            TypeDeclaration<?> type = (TypeDeclaration<?>) raw;
            result.add(buildClassModel(type, packageName));
        }
        Map<String, String> fileImports = extractFileImports(compilationUnit);
        for (ClassModel built : result) {
            fileImportsByClass.merge(built.getName(), new HashMap<>(fileImports),
                    (oldMap, newMap) -> { oldMap.putAll(newMap); return oldMap; });
        }
        return result;
    }

    /**
     * Recolecta los imports explícitos de cada archivo (simpleName -&gt; package),
     * para resolver el paquete real de tipos externos como Path o SystemModule.
     */
    private Map<String, String> extractFileImports(CompilationUnit cu) {
        Map<String, String> fileImports = new HashMap<>();
        for (ImportDeclaration id : cu.getImports()) {
            if (!id.isAsterisk() && !id.isStatic()) {
                String fqcn = id.getNameAsString();
                int lastDot = fqcn.lastIndexOf('.');
                if (lastDot > 0) {
                    String simpleName = fqcn.substring(lastDot + 1);
                    String packageName = fqcn.substring(0, lastDot);
                    fileImports.put(simpleName, packageName);
                }
            }
        }
        return fileImports;
    }

    private ClassModel buildClassModel(TypeDeclaration<?> type, String packageName) {
        String name = type.getNameAsString();
        String kind = "Class";
        boolean isAbstract = false;
        List<String> stereotypes = new ArrayList<>();
        List<AttributeModel> attributes = new ArrayList<>();
        List<MethodModel> methods = new ArrayList<>();
        List<ConstructorModel> constructors = new ArrayList<>();
        List<String> extendedTypes = new ArrayList<>();
        List<String> implementedTypes = new ArrayList<>();
        List<String> enumConstants = new ArrayList<>();

        for (AnnotationExpr annotation : type.getAnnotations()) {
            stereotypes.add("@" + annotation.getNameAsString());
        }

        if (type instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration declaration = (ClassOrInterfaceDeclaration) type;
            isAbstract = declaration.isAbstract();
            kind = declaration.isInterface() ? "Interface" : (isAbstract ? "AbstractClass" : "Class");

            for (ClassOrInterfaceType extended : declaration.getExtendedTypes()) {
                extendedTypes.add(extended.getNameAsString());
            }
            for (ClassOrInterfaceType implemented : declaration.getImplementedTypes()) {
                implementedTypes.add(implemented.getNameAsString());
            }

            for (FieldDeclaration field : declaration.getFields()) {
                List<String> modifiers = modifierNames(field.getModifiers());
                for (VariableDeclarator variable : field.getVariables()) {
                    attributes.add(new AttributeModel(
                            variable.getNameAsString(),
                            variable.getType().asString(),
                            modifiers));
                }
            }
            for (ConstructorDeclaration constructor : declaration.getConstructors()) {
                constructors.add(new ConstructorModel(
                        constructor.getNameAsString(),
                        toParameters(constructor.getParameters()),
                        modifierNames(constructor.getModifiers())));
            }
            for (MethodDeclaration method : declaration.getMethods()) {
                methods.add(new MethodModel(
                        method.getNameAsString(),
                        method.getType().asString(),
                        toParameters(method.getParameters()),
                        modifierNames(method.getModifiers())));
            }
        } else if (type instanceof RecordDeclaration) {
            kind = "Record";
            RecordDeclaration record = (RecordDeclaration) type;
            // records can also implement interfaces (RecordDeclaration is NOT a ClassOrInterfaceDeclaration)
            for (ClassOrInterfaceType implemented : record.getImplementedTypes()) {
                implementedTypes.add(implemented.getNameAsString());
            }
            // record header components are persistent state -> attributes
            for (Parameter component : record.getParameters()) {
                attributes.add(new AttributeModel(
                        component.getNameAsString(),
                        component.getType().asString(),
                        List.of("private", "final")));
            }
            for (BodyDeclaration<?> member : record.getMembers()) {
                if (member instanceof FieldDeclaration) {
                    FieldDeclaration field = (FieldDeclaration) member;
                    List<String> modifiers = modifierNames(field.getModifiers());
                    for (VariableDeclarator variable : field.getVariables()) {
                        attributes.add(new AttributeModel(
                                variable.getNameAsString(),
                                variable.getType().asString(),
                                modifiers));
                    }
                } else if (member instanceof ConstructorDeclaration) {
                    ConstructorDeclaration constructor = (ConstructorDeclaration) member;
                    constructors.add(new ConstructorModel(
                            constructor.getNameAsString(),
                            toParameters(constructor.getParameters()),
                            modifierNames(constructor.getModifiers())));
                } else if (member instanceof MethodDeclaration) {
                    MethodDeclaration method = (MethodDeclaration) member;
                    methods.add(new MethodModel(
                            method.getNameAsString(),
                            method.getType().asString(),
                            toParameters(method.getParameters()),
                            modifierNames(method.getModifiers())));
                }
            }
        } else if (type instanceof EnumDeclaration) {
            kind = "Enum";
            EnumDeclaration enumDeclaration = (EnumDeclaration) type;
            // enum literals / constants
            for (EnumConstantDeclaration entry : enumDeclaration.getEntries()) {
                enumConstants.add(entry.getNameAsString());
            }
            for (BodyDeclaration<?> member : enumDeclaration.getMembers()) {
                if (member instanceof FieldDeclaration) {
                    FieldDeclaration field = (FieldDeclaration) member;
                    List<String> modifiers = modifierNames(field.getModifiers());
                    for (VariableDeclarator variable : field.getVariables()) {
                        attributes.add(new AttributeModel(
                                variable.getNameAsString(),
                                variable.getType().asString(),
                                modifiers));
                    }
                } else if (member instanceof ConstructorDeclaration) {
                    ConstructorDeclaration constructor = (ConstructorDeclaration) member;
                    constructors.add(new ConstructorModel(
                            constructor.getNameAsString(),
                            toParameters(constructor.getParameters()),
                            modifierNames(constructor.getModifiers())));
                } else if (member instanceof MethodDeclaration) {
                    MethodDeclaration method = (MethodDeclaration) member;
                    methods.add(new MethodModel(
                            method.getNameAsString(),
                            method.getType().asString(),
                            toParameters(method.getParameters()),
                            modifierNames(method.getModifiers())));
                }
            }
        } else if (type instanceof AnnotationDeclaration) {
            kind = "Annotation";
        }

        return new ClassModel(name, packageName, kind, isAbstract, stereotypes,
                attributes, methods, constructors, extendedTypes, implementedTypes, enumConstants);
    }

    private List<ParameterModel> toParameters(List<Parameter> parameters) {
        List<ParameterModel> result = new ArrayList<>();
        for (Parameter parameter : parameters) {
            result.add(new ParameterModel(parameter.getNameAsString(), parameter.getType().asString()));
        }
        return result;
    }

    private List<String> modifierNames(List<com.github.javaparser.ast.Modifier> modifiers) {
        List<String> names = new ArrayList<>();
        for (com.github.javaparser.ast.Modifier modifier : modifiers) {
            names.add(modifier.getKeyword().asString());
        }
        return names;
    }

    // ---------- relationship detection ----------

    private void detectRelationships(List<ClassModel> classes, List<RelationshipModel> relationships) {
        Set<String> classNames = new HashSet<>();
        for (ClassModel model : classes) {
            classNames.add(model.getName());
        }
        Set<String> seen = new HashSet<>();

        for (ClassModel model : classes) {
            for (String parent : model.getExtendedTypes()) {
                String simple = simpleName(parent);
                if (classNames.contains(simple)) {
                    addOnce(relationships, seen, model.getName(), simple, "EXTENDS");
                }
                // generics inside extends clause, e.g. extends Base<Package>
                for (String inner : splitTypeNames(parent)) {
                    if (classNames.contains(inner) && !inner.equals(model.getName()) && !inner.equals(simple)) {
                        addOnce(relationships, seen, model.getName(), inner, "ASSOCIATION");
                    }
                }
            }
            for (String parent : model.getImplementedTypes()) {
                String simple = simpleName(parent);
                if (classNames.contains(simple)) {
                    addOnce(relationships, seen, model.getName(), simple, "IMPLEMENTS");
                }
                for (String inner : splitTypeNames(parent)) {
                    if (classNames.contains(inner) && !inner.equals(model.getName()) && !inner.equals(simple)) {
                        addOnce(relationships, seen, model.getName(), inner, "ASSOCIATION");
                    }
                }
            }

            // ASSOCIATION: attribute types (including generic arguments like List<Package>)
            Set<String> associated = new HashSet<>();
            for (AttributeModel attribute : model.getAttributes()) {
                for (String target : extractReferencedNames(attribute.getType())) {
                    if (classNames.contains(target) && !target.equals(model.getName())) {
                        addOnce(relationships, seen, model.getName(), target, "ASSOCIATION");
                        associated.add(target);
                    }
                }
            }

            // DEPENDENCY: types used only in method/constructor signatures (params, returns)
            // or record components already covered as attributes are skipped.
            for (MethodModel method : model.getMethods()) {
                for (String target : extractReferencedNames(method.getReturnType())) {
                    if (classNames.contains(target) && !target.equals(model.getName()) && !associated.contains(target)) {
                        addOnce(relationships, seen, model.getName(), target, "DEPENDENCY");
                    }
                }
                for (ParameterModel parameter : method.getParameters()) {
                    for (String target : extractReferencedNames(parameter.getType())) {
                        if (classNames.contains(target) && !target.equals(model.getName()) && !associated.contains(target)) {
                            addOnce(relationships, seen, model.getName(), target, "DEPENDENCY");
                        }
                    }
                }
            }
            for (ConstructorModel constructor : model.getConstructors()) {
                for (ParameterModel parameter : constructor.getParameters()) {
                    for (String target : extractReferencedNames(parameter.getType())) {
                        if (classNames.contains(target) && !target.equals(model.getName()) && !associated.contains(target)) {
                            addOnce(relationships, seen, model.getName(), target, "DEPENDENCY");
                        }
                    }
                }
            }
        }
    }

    /**
     * Registers external (non-project, non-primitive) attribute types as
     * stereotyped {@code @external} boxes with their real JDK package and an
     * ASSOCIATION from the using class. Internal types and primitives are ignored.
     */
    private void registerExternalTypes(List<ClassModel> classes, List<RelationshipModel> relationships) {
        Set<String> internal = new HashSet<>();
        for (ClassModel model : classes) {
            internal.add(model.getName());
        }
        Set<String> seen = new HashSet<>();
        for (RelationshipModel rel : relationships) {
            seen.add(rel.getSource() + "|" + rel.getType() + "|" + rel.getTarget());
        }
        Map<String, ClassModel> externals = new LinkedHashMap<>();
        for (ClassModel model : classes) {
            if (externals.containsKey(model.getName())) {
                continue; // skip boxes created in this same pass
            }
            if (model.getAttributes() == null) {
                continue;
            }
            for (AttributeModel attribute : model.getAttributes()) {
                // 1. Omitir primitivos y escalares básicos (String, wrappers, etc.)
                if (TypeClassifier.shouldIgnoreBox(attribute.getType())) {
                    continue;
                }
                // 2. Registrar TODOS los tipos referenciados (incluye genéricos
                //    anidados: Map<String,List<UUID>> -> UUID, no solo el último).
                for (String target : TypeClassifier.referencedTypeNames(attribute.getType())) {
                    if (TypeClassifier.shouldIgnoreBox(target)) {
                        continue;
                    }
                    if (internal.contains(target)) {
                        continue;
                    }
                if (!externals.containsKey(target)) {
                    // 2. Prioridad: imports del archivo > mapa común > fallback
                    String resolvedPkg = fileImportsByClass
                            .getOrDefault(model.getName(), Map.of())
                            .getOrDefault(target, TypeClassifier.resolvePackage(target));
                    // 3. La caja externa solo se crea si no está en blacklist
                    // (opción B: la whitelist es solo para clases internas).
                    if (filter.isBlacklisted(resolvedPkg, target)) {
                        continue;
                    }
                    List<String> stereotypes = new ArrayList<>();
                    stereotypes.add("@external");
                    externals.put(target, new ClassModel(target,
                            resolvedPkg, "Class", false,
                            stereotypes, new ArrayList<>(), new ArrayList<>(),
                            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                            new ArrayList<>()));
                    }
                    addOnce(relationships, seen, model.getName(), target, "ASSOCIATION");
                }
            }
        }
        classes.addAll(externals.values());
    }

    private void addOnce(List<RelationshipModel> relationships, Set<String> seen,
                         String source, String target, String type) {
        String key = source + "|" + type + "|" + target;
        if (seen.add(key)) {
            relationships.add(new RelationshipModel(source, target, type));
        }
    }

    /**
     * Extracts every referenced type name from a type string, including generic arguments.
     * Examples: "List&lt;Package&gt;" -&gt; {List, Package};
     * "Map&lt;String, List&lt;SubZone&gt;&gt;" -&gt; {Map, String, List, SubZone}.
     */
    Set<String> extractReferencedNames(String typeString) {
        Set<String> names = new LinkedHashSet<>();
        if (typeString == null || typeString.isBlank()) {
            return names;
        }
        // split on anything that is not a Java identifier part
        for (String token : typeString.split("[^A-Za-z0-9_.]+")) {
            if (token.isBlank()) {
                continue;
            }
            String simple = simpleName(token);
            if (!simple.isEmpty() && Character.isUpperCase(simple.charAt(0))) {
                names.add(simple);
            }
        }
        return names;
    }

    private Set<String> splitTypeNames(String typeString) {
        return extractReferencedNames(typeString);
    }

    private String simpleName(String typeName) {
        if (typeName == null) {
            return "";
        }
        // Strip generic arguments with balanced depth counting, so nested
        // generics like Map<String, List<SubZone>> resolve to "Map" without
        // the greedy-regex pitfall of "<.*>" eating too much or too little.
        StringBuilder outer = new StringBuilder();
        int depth = 0;
        for (int i = 0; i < typeName.length(); i++) {
            char c = typeName.charAt(i);
            if (c == '<') {
                depth++;
            } else if (c == '>') {
                if (depth > 0) {
                    depth--;
                }
            } else if (depth == 0) {
                outer.append(c);
            }
        }
        String cleaned = outer.toString().trim();
        int dot = cleaned.lastIndexOf('.');
        if (dot >= 0) {
            cleaned = cleaned.substring(dot + 1);
        }
        // strip array brackets and varargs
        cleaned = cleaned.replace("[]", "").replace("...", "").trim();
        return cleaned;
    }

    /**
     * Type-object based extraction (used internally for future extensions).
     * Collects the raw name plus every generic argument recursively.
     */
    Set<String> collectTypeNames(Type type) {
        Set<String> names = new LinkedHashSet<>();
        collectTypeNamesRecursive(type, names);
        return names;
    }

    private void collectTypeNamesRecursive(Type type, Set<String> names) {
        if (type == null) {
            return;
        }
        if (type instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType classOrInterfaceType = (ClassOrInterfaceType) type;
            names.add(classOrInterfaceType.getNameAsString());
            if (classOrInterfaceType.getTypeArguments().isPresent()) {
                for (Type argument : classOrInterfaceType.getTypeArguments().get()) {
                    collectTypeNamesRecursive(argument, names);
                }
            }
        } else if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            wildcardType.getExtendedType().ifPresent(t -> collectTypeNamesRecursive(t, names));
            wildcardType.getSuperType().ifPresent(t -> collectTypeNamesRecursive(t, names));
        } else {
            for (String token : type.asString().split("[^A-Za-z0-9_.]+")) {
                if (!token.isBlank()) {
                    names.add(simpleName(token));
                }
            }
        }
    }
}
