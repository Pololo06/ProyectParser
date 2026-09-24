package is.generador.application;

import is.generador.domain.BeanAccessors;
import is.generador.domain.model.ClassModel;
import is.generador.domain.model.MethodModel;
import is.generador.domain.model.ProjectModel;
import is.generador.domain.port.SourceAnalyzerPort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Detalle por clase: nombres, propiedades, constructores, getters y setters
 * (paso 8 del split). Usa {@link BeanAccessors} para la regla estricta.
 */
public class ClassInfoService {

    private final SourceAnalyzerPort analyzer;

    public ClassInfoService(SourceAnalyzerPort analyzer) {
        this.analyzer = Objects.requireNonNull(analyzer, "analyzer is required");
    }

    public List<String> countClasses(String folderPath) throws IOException {
        ProjectModel project = analyzer.analyze(folderPath);
        List<String> names = new ArrayList<>();
        for (ClassModel model : project.getClasses()) {
            names.add(model.getName());
        }
        Collections.sort(names);
        return names;
    }

    public Map<String, List<String>> getClassProperties(String folderPath) throws IOException {
        Map<String, ClassInfo> details = getClassDetails(folderPath);
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (Map.Entry<String, ClassInfo> entry : details.entrySet()) {
            map.put(entry.getKey(), entry.getValue().getProperties());
        }
        return map;
    }

    public Map<String, ClassInfo> getClassDetails(String folderPath) throws IOException {
        ProjectModel project = analyzer.analyze(folderPath);
        Map<String, ClassInfo> details = new LinkedHashMap<>();

        for (ClassModel model : project.getClasses()) {
            List<String> properties = new ArrayList<>();
            model.getAttributes().forEach(a -> properties.add(a.getType() + " " + a.getName()));
            if ("Enum".equals(model.getKind()) && model.getEnumConstants() != null) {
                for (String constant : model.getEnumConstants()) {
                    properties.add("constant " + constant);
                }
            }

            List<String> constructorSignatures = new ArrayList<>();
            model.getConstructors().forEach(constructor -> {
                String visibility = visibilityKeyword(constructor.getModifiers());
                String prefix = visibility.isEmpty() ? "" : visibility + " ";
                StringBuilder signature = new StringBuilder(prefix + constructor.getName() + "(");
                for (int i = 0; i < constructor.getParameters().size(); i++) {
                    signature.append(constructor.getParameters().get(i).getType())
                            .append(" ")
                            .append(constructor.getParameters().get(i).getName());
                    if (i < constructor.getParameters().size() - 1) {
                        signature.append(", ");
                    }
                }
                signature.append(")");
                constructorSignatures.add(signature.toString());
            });

            List<String> getters = new ArrayList<>();
            List<String> setters = new ArrayList<>();
            model.getMethods().forEach(method -> {
                String declaration = formatMethodDeclaration(method);
                if (BeanAccessors.isGetter(method, model)) {
                    getters.add(declaration);
                } else if (BeanAccessors.isSetter(method, model)) {
                    setters.add(declaration);
                }
            });

            details.put(model.getName(), new ClassInfo(model.getName(), model.getKind(),
                    model.getStereotypes(), properties, constructorSignatures, getters, setters));
        }
        return details;
    }

    private String formatMethodDeclaration(MethodModel method) {
        StringBuilder sb = new StringBuilder();
        String visibility = visibilityKeyword(method.getModifiers());
        if (!visibility.isEmpty()) {
            sb.append(visibility).append(" ");
        }
        sb.append(method.getReturnType()).append(" ").append(method.getName()).append("(");
        if (method.getParameters() != null) {
            for (int i = 0; i < method.getParameters().size(); i++) {
                sb.append(method.getParameters().get(i).getType())
                        .append(" ")
                        .append(method.getParameters().get(i).getName());
                if (i < method.getParameters().size() - 1) {
                    sb.append(", ");
                }
            }
        }
        sb.append(")");
        return sb.toString();
    }

    private String visibilityKeyword(List<String> modifiers) {
        if (modifiers == null) {
            return "";
        }
        if (modifiers.contains("public")) {
            return "public";
        }
        if (modifiers.contains("protected")) {
            return "protected";
        }
        if (modifiers.contains("private")) {
            return "private";
        }
        return "";
    }
}
