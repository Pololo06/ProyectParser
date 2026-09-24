package is.generador.infrastructure.javaparser;

import is.generador.domain.model.AttributeModel;
import is.generador.domain.model.ClassModel;
import is.generador.domain.model.ConstructorModel;
import is.generador.domain.model.MethodModel;
import is.generador.domain.model.ParameterModel;
import is.generador.domain.model.RelationshipModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Deduce relaciones del modelo (paso 5 del split).
 * Solo lectura del AST ya convertido: EXTENDS, IMPLEMENTS,
 * ASSOCIATION (atributos, con genéricos) y DEPENDENCY (firmas).
 * Las variables de tipo declaradas nunca generan relaciones.
 */
class RelationshipDetector {

    void detectRelationships(List<ClassModel> classes, List<RelationshipModel> relationships) {
        Set<String> classNames = new HashSet<>();
        for (ClassModel model : classes) {
            classNames.add(model.getName());
        }
        Set<String> seen = new HashSet<>();

        for (ClassModel model : classes) {
            for (String parent : model.getExtendedTypes()) {
                String simple = GenericTypeParser.simpleName(parent);
                if (classNames.contains(simple)) {
                    addOnce(relationships, seen, model.getName(), simple, "EXTENDS");
                }
                // generics inside extends clause, e.g. extends Base<Package>
                for (String inner : GenericTypeParser.visibleTypeNames(GenericTypeParser.splitTypeNames(parent), model.getTypeParameters())) {
                    if (classNames.contains(inner) && !inner.equals(model.getName()) && !inner.equals(simple)) {
                        addOnce(relationships, seen, model.getName(), inner, "ASSOCIATION");
                    }
                }
            }
            for (String parent : model.getImplementedTypes()) {
                String simple = GenericTypeParser.simpleName(parent);
                if (classNames.contains(simple)) {
                    addOnce(relationships, seen, model.getName(), simple, "IMPLEMENTS");
                }
                for (String inner : GenericTypeParser.visibleTypeNames(GenericTypeParser.splitTypeNames(parent), model.getTypeParameters())) {
                    if (classNames.contains(inner) && !inner.equals(model.getName()) && !inner.equals(simple)) {
                        addOnce(relationships, seen, model.getName(), inner, "ASSOCIATION");
                    }
                }
            }

            // ASSOCIATION: attribute types (including generic arguments like List<Package>)
            Set<String> associated = new HashSet<>();
            for (AttributeModel attribute : model.getAttributes()) {
                for (String target : GenericTypeParser.visibleTypeNames(GenericTypeParser.extractReferencedNames(attribute.getType()),
                        model.getTypeParameters())) {
                    if (classNames.contains(target) && !target.equals(model.getName())) {
                        addOnce(relationships, seen, model.getName(), target, "ASSOCIATION");
                        associated.add(target);
                    }
                }
            }

            // DEPENDENCY: types used only in method/constructor signatures (params, returns)
            // or record components already covered as attributes are skipped.
            for (MethodModel method : model.getMethods()) {
                for (String target : GenericTypeParser.visibleTypeNames(GenericTypeParser.extractReferencedNames(method.getReturnType()),
                        model.getTypeParameters(), method.getTypeParameters())) {
                    if (classNames.contains(target) && !target.equals(model.getName()) && !associated.contains(target)) {
                        addOnce(relationships, seen, model.getName(), target, "DEPENDENCY");
                    }
                }
                for (ParameterModel parameter : method.getParameters()) {
                    for (String target : GenericTypeParser.visibleTypeNames(GenericTypeParser.extractReferencedNames(parameter.getType()),
                            model.getTypeParameters(), method.getTypeParameters())) {
                        if (classNames.contains(target) && !target.equals(model.getName()) && !associated.contains(target)) {
                            addOnce(relationships, seen, model.getName(), target, "DEPENDENCY");
                        }
                    }
                }
            }
            for (ConstructorModel constructor : model.getConstructors()) {
                for (ParameterModel parameter : constructor.getParameters()) {
                    for (String target : GenericTypeParser.visibleTypeNames(GenericTypeParser.extractReferencedNames(parameter.getType()),
                            model.getTypeParameters(), constructor.getTypeParameters())) {
                        if (classNames.contains(target) && !target.equals(model.getName()) && !associated.contains(target)) {
                            addOnce(relationships, seen, model.getName(), target, "DEPENDENCY");
                        }
                    }
                }
            }
        }
    }

    private void addOnce(List<RelationshipModel> relationships, Set<String> seen,
                         String source, String target, String type) {
        String key = source + "|" + type + "|" + target;
        if (seen.add(key)) {
            relationships.add(new RelationshipModel(source, target, type));
        }
    }
}
