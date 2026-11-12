package com.proyectparser.core.infrastructure.javaparser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.CompactConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithTypeParameters;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.TypeParameter;
import com.proyectparser.core.domain.model.AttributeModel;
import com.proyectparser.core.domain.model.ClassModel;
import com.proyectparser.core.domain.model.ConstructorModel;
import com.proyectparser.core.domain.model.Kind;
import com.proyectparser.core.domain.model.MethodModel;
import com.proyectparser.core.domain.model.ParameterModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AST -&gt; {@link ClassModel} (paso 4 del split).
 * Parsea un {@code .java} y construye el modelo de cada tipo declarado
 * (clases, interfaces, records, enums, annotations, incluidos anidados).
 */
class TypeExtractor {

    private final JavaParser parser = new JavaParser(new ParserConfiguration()
            .setLanguageLevel(ParserConfiguration.LanguageLevel.BLEEDING_EDGE));

    List<ClassModel> analyzeFile(File javaFile, Map<String, Map<String, String>> fileImportsByClass)
            throws IOException {
        List<ClassModel> result = new ArrayList<>();
        var parsed = parser.parse(javaFile);
        if (!parsed.isSuccessful()) {
            throw new ParseProblemException(parsed.getProblems());
        }
        CompilationUnit compilationUnit = parsed.getResult().orElseThrow();
        String packageName = compilationUnit.getPackageDeclaration()
                .map(pd -> pd.getNameAsString()).orElse("");

        // findAll covers nested types at any depth (inner classes, records inside interfaces, etc.)
        List<TypeDeclaration> rawTypes = compilationUnit.findAll(TypeDeclaration.class);

        for (TypeDeclaration raw : rawTypes) {
            @SuppressWarnings("unchecked")
            TypeDeclaration<?> type = (TypeDeclaration<?>) raw;
            // fqn with the enclosing types (p.Outer.Inner); a local class has none and keeps p.Name.
            ClassModel model = buildClassModel(type, packageName);
            result.add(type.getFullyQualifiedName().map(model::withFqn).orElse(model));
        }
        Map<String, String> fileImports = ImportResolver.extractFileImports(compilationUnit);
        for (ClassModel built : result) {
            fileImportsByClass.merge(built.getFqn(), new HashMap<>(fileImports),
                    (oldMap, newMap) -> { oldMap.putAll(newMap); return oldMap; });
        }
        return result;
    }

    private ClassModel buildClassModel(TypeDeclaration<?> type, String packageName) {
        String name = type.getNameAsString();
        String kind = Kind.CLASS.label();
        boolean isAbstract = false;
        List<String> stereotypes = new ArrayList<>();
        List<AttributeModel> attributes = new ArrayList<>();
        List<MethodModel> methods = new ArrayList<>();
        List<ConstructorModel> constructors = new ArrayList<>();
        List<String> extendedTypes = new ArrayList<>();
        List<String> implementedTypes = new ArrayList<>();
        List<String> enumConstants = new ArrayList<>();
        List<String> typeParameters = new ArrayList<>();
        List<TypeParameter> generics = type instanceof NodeWithTypeParameters<?> generic
                ? generic.getTypeParameters() : List.of();
        List<String> bounds = generics.stream().flatMap(parameter -> parameter.getTypeBound().stream())
                .map(ClassOrInterfaceType::asString).toList();
        List<String> declarations = generics.stream().map(TypeParameter::toString).toList();

        for (AnnotationExpr annotation : type.getAnnotations()) {
            stereotypes.add("@" + annotation.getNameAsString());
        }

        if (type instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration declaration = (ClassOrInterfaceDeclaration) type;
            isAbstract = declaration.isAbstract();
            for (String modifier : modifierNames(declaration.getModifiers())) {
                switch (modifier) {
                    case "final" -> stereotypes.add("Final");
                    case "sealed" -> stereotypes.add("Sealed");
                    case "non-sealed" -> stereotypes.add("Non-Sealed");
                    default -> { }
                }
            }
            kind = declaration.isInterface() ? Kind.INTERFACE.label() : (isAbstract ? Kind.ABSTRACT_CLASS.label() : Kind.CLASS.label());
            typeParameters.addAll(typeParameterNames(declaration.getTypeParameters()));

            for (ClassOrInterfaceType extended : declaration.getExtendedTypes()) {
                extendedTypes.add(extended.asString());
            }
            for (ClassOrInterfaceType implemented : declaration.getImplementedTypes()) {
                implementedTypes.add(implemented.asString());
            }

            extractMembers(declaration, attributes, constructors, methods);
        } else if (type instanceof RecordDeclaration) {
            kind = Kind.RECORD.label();
            RecordDeclaration record = (RecordDeclaration) type;
            typeParameters.addAll(typeParameterNames(record.getTypeParameters()));
            // records can also implement interfaces (RecordDeclaration is NOT a ClassOrInterfaceDeclaration)
            for (ClassOrInterfaceType implemented : record.getImplementedTypes()) {
                implementedTypes.add(implemented.asString());
            }
            // record header components are persistent state -> attributes
            for (Parameter component : record.getParameters()) {
                attributes.add(new AttributeModel(
                        component.getNameAsString(),
                        parameterType(component),
                        List.of("private", "final")));
            }
            extractMembers(record, attributes, constructors, methods);
        } else if (type instanceof EnumDeclaration) {
            kind = Kind.ENUM.label();
            EnumDeclaration enumDeclaration = (EnumDeclaration) type;
            // enum literals / constants
            for (EnumConstantDeclaration entry : enumDeclaration.getEntries()) {
                enumConstants.add(entry.getNameAsString());
            }
            for (ClassOrInterfaceType implemented : enumDeclaration.getImplementedTypes()) {
                implementedTypes.add(implemented.asString());
            }
            extractMembers(enumDeclaration, attributes, constructors, methods);
        } else if (type instanceof AnnotationDeclaration) {
            kind = Kind.ANNOTATION.label();
            extractMembers(type, attributes, constructors, methods);
        }

        return new ClassModel(name, packageName, kind, isAbstract, stereotypes,
                attributes, methods, constructors, extendedTypes, implementedTypes, enumConstants,
                typeParameters).withTypeParameterDetails(bounds, declarations);
    }

    /**
     * Fields, constructors and methods declared in the type body, in declaration order.
     * Compact record constructors use the parameters declared in the record header.
     */
    private void extractMembers(TypeDeclaration<?> type, List<AttributeModel> attributes,
                                List<ConstructorModel> constructors, List<MethodModel> methods) {
        for (BodyDeclaration<?> member : type.getMembers()) {
            if (member instanceof FieldDeclaration) {
                FieldDeclaration field = (FieldDeclaration) member;
                List<String> modifiers = modifierNames(field.getModifiers());
                if (type instanceof ClassOrInterfaceDeclaration declaration && declaration.isInterface()) {
                    addImplicit(modifiers, "public", "static", "final");
                }
                for (VariableDeclarator variable : field.getVariables()) {
                    attributes.add(new AttributeModel(
                            variable.getNameAsString(),
                            variable.getType().asString(),
                            modifiers,
                            initialValue(variable), initializedWithNew(type, variable)));
                }
            } else if (member instanceof ConstructorDeclaration) {
                ConstructorDeclaration constructor = (ConstructorDeclaration) member;
                constructors.add(new ConstructorModel(
                        constructor.getNameAsString(),
                        toParameters(constructor.getParameters()),
                        modifierNames(constructor.getModifiers()),
                        typeParameterNames(constructor.getTypeParameters())));
            } else if (member instanceof CompactConstructorDeclaration constructor
                    && type instanceof RecordDeclaration record) {
                constructors.add(new ConstructorModel(constructor.getNameAsString(),
                        toParameters(record.getParameters()), modifierNames(constructor.getModifiers()), List.of()));
            } else if (member instanceof AnnotationMemberDeclaration annotationMember) {
                methods.add(new MethodModel(annotationMember.getNameAsString(),
                        annotationMember.getType().asString(), List.of(), List.of("public", "abstract"), List.of()));
            } else if (member instanceof MethodDeclaration) {
                MethodDeclaration method = (MethodDeclaration) member;
                List<String> modifiers = modifierNames(method.getModifiers());
                if (type instanceof ClassOrInterfaceDeclaration declaration
                        && declaration.isInterface() && !method.isPrivate()) {
                    addImplicit(modifiers, "public");
                    if (method.getBody().isEmpty()) {
                        addImplicit(modifiers, "abstract");
                    }
                }
                methods.add(new MethodModel(
                        method.getNameAsString(),
                        method.getType().asString(),
                        toParameters(method.getParameters()),
                        modifiers,
                        typeParameterNames(method.getTypeParameters())));
            }
        }
    }

    /**
     * Initializer of a field as one line ({@code 5000}, {@code "unimag"}), or {@code null}
     * when it has none or spans several lines (lambdas, anonymous classes).
     */
    private static String initialValue(VariableDeclarator variable) {
        return variable.getInitializer()
                .map(Object::toString)
                .filter(text -> !text.contains("\n"))
                .orElse(null);
    }

    private static void addImplicit(List<String> modifiers, String... implicit) {
        for (String modifier : implicit) {
            if (!modifiers.contains(modifier)) {
                modifiers.add(modifier);
            }
        }
    }

    private static boolean initializedWithNew(TypeDeclaration<?> owner, VariableDeclarator field) {
        if (field.getInitializer().filter(expression -> expression.isObjectCreationExpr()).isPresent()) {
            return true;
        }
        if (!field.getParentNode().filter(FieldDeclaration.class::isInstance)
                .map(FieldDeclaration.class::cast).map(FieldDeclaration::isFinal).orElse(false)) {
            return false;
        }
        return owner.getConstructors().stream()
                .flatMap(constructor -> constructor.getBody().getStatements().stream())
                .filter(statement -> statement.isExpressionStmt())
                .map(statement -> statement.asExpressionStmt().getExpression())
                .filter(expression -> expression.isAssignExpr())
                .map(expression -> expression.asAssignExpr())
                .anyMatch(assignment -> assignment.getOperator() == AssignExpr.Operator.ASSIGN
                        && assignment.getValue().isObjectCreationExpr()
                        && assignment.getTarget().isFieldAccessExpr()
                        && assignment.getTarget().asFieldAccessExpr().getScope().isThisExpr()
                        && assignment.getTarget().asFieldAccessExpr().getNameAsString()
                                .equals(field.getNameAsString()));
    }

    /** Names of declared type variables (e.g. {@code <T, ID>} -> [T, ID]). */
    private static List<String> typeParameterNames(List<TypeParameter> params) {
        List<String> names = new ArrayList<>();
        if (params != null) {
            for (TypeParameter param : params) {
                names.add(param.getNameAsString());
            }
        }
        return names;
    }

    private List<ParameterModel> toParameters(List<Parameter> parameters) {
        List<ParameterModel> result = new ArrayList<>();
        for (Parameter parameter : parameters) {
            result.add(new ParameterModel(parameter.getNameAsString(), parameterType(parameter)));
        }
        return result;
    }

    private static String parameterType(Parameter parameter) {
        return parameter.getType().asString() + (parameter.isVarArgs() ? "[]" : "");
    }

    private List<String> modifierNames(List<com.github.javaparser.ast.Modifier> modifiers) {
        List<String> names = new ArrayList<>();
        for (com.github.javaparser.ast.Modifier modifier : modifiers) {
            names.add(modifier.getKeyword().asString());
        }
        return names;
    }
}
