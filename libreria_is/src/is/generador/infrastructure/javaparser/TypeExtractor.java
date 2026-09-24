package is.generador.infrastructure.javaparser;

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
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.TypeParameter;
import is.generador.domain.model.AttributeModel;
import is.generador.domain.model.ClassModel;
import is.generador.domain.model.ConstructorModel;
import is.generador.domain.model.MethodModel;
import is.generador.domain.model.ParameterModel;

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

    List<ClassModel> analyzeFile(File javaFile, Map<String, Map<String, String>> fileImportsByClass)
            throws IOException {
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
        Map<String, String> fileImports = ImportResolver.extractFileImports(compilationUnit);
        for (ClassModel built : result) {
            fileImportsByClass.merge(built.getName(), new HashMap<>(fileImports),
                    (oldMap, newMap) -> { oldMap.putAll(newMap); return oldMap; });
        }
        return result;
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
        List<String> typeParameters = new ArrayList<>();

        for (AnnotationExpr annotation : type.getAnnotations()) {
            stereotypes.add("@" + annotation.getNameAsString());
        }

        if (type instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration declaration = (ClassOrInterfaceDeclaration) type;
            isAbstract = declaration.isAbstract();
            kind = declaration.isInterface() ? "Interface" : (isAbstract ? "AbstractClass" : "Class");
            typeParameters.addAll(typeParameterNames(declaration.getTypeParameters()));

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
                        modifierNames(constructor.getModifiers()),
                        typeParameterNames(constructor.getTypeParameters())));
            }
            for (MethodDeclaration method : declaration.getMethods()) {
                methods.add(new MethodModel(
                        method.getNameAsString(),
                        method.getType().asString(),
                        toParameters(method.getParameters()),
                        modifierNames(method.getModifiers()),
                        typeParameterNames(method.getTypeParameters())));
            }
        } else if (type instanceof RecordDeclaration) {
            kind = "Record";
            RecordDeclaration record = (RecordDeclaration) type;
            typeParameters.addAll(typeParameterNames(record.getTypeParameters()));
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
                            modifierNames(constructor.getModifiers()),
                            typeParameterNames(constructor.getTypeParameters())));
                } else if (member instanceof MethodDeclaration) {
                    MethodDeclaration method = (MethodDeclaration) member;
                    methods.add(new MethodModel(
                            method.getNameAsString(),
                            method.getType().asString(),
                            toParameters(method.getParameters()),
                            modifierNames(method.getModifiers()),
                            typeParameterNames(method.getTypeParameters())));
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
                            modifierNames(constructor.getModifiers()),
                            typeParameterNames(constructor.getTypeParameters())));
                } else if (member instanceof MethodDeclaration) {
                    MethodDeclaration method = (MethodDeclaration) member;
                    methods.add(new MethodModel(
                            method.getNameAsString(),
                            method.getType().asString(),
                            toParameters(method.getParameters()),
                            modifierNames(method.getModifiers()),
                            typeParameterNames(method.getTypeParameters())));
                }
            }
        } else if (type instanceof AnnotationDeclaration) {
            kind = "Annotation";
        }

        return new ClassModel(name, packageName, kind, isAbstract, stereotypes,
                attributes, methods, constructors, extendedTypes, implementedTypes, enumConstants,
                typeParameters);
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
}
