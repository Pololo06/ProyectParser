package is.generador.domain;

import is.generador.domain.model.AttributeModel;
import is.generador.domain.model.ClassModel;
import is.generador.domain.model.MethodModel;

/**
 * Regla estricta JavaBeans para detectar getters/setters (Fase 2).
 * Solo cuenta el método con campo respaldo real; evita contar métodos
 * de negocio como {@code getCode()} o {@code isFinal()} sin atributo.
 * Vive en {@code domain}: solo depende del modelo canónico.
 */
public final class BeanAccessors {

    private BeanAccessors() {
    }

    /**
     * JavaBeans getter: getXxx() / isXxx() with no params, non-void return
     * (boolean for isXxx), and a matching attribute (decapitalized suffix).
     */
    public static boolean isGetter(MethodModel method, ClassModel model) {
        if (method == null || model == null) {
            return false;
        }
        String name = method.getName();
        if (name == null) {
            return false;
        }
        if (method.getParameters() == null || !method.getParameters().isEmpty()) {
            return false;
        }
        String returnType = method.getReturnType() == null ? "" : method.getReturnType();
        if (returnType.isEmpty() || "void".equals(returnType)) {
            return false;
        }
        String suffix = null;
        if (name.startsWith("get") && name.length() > 3 && Character.isUpperCase(name.charAt(3))) {
            suffix = name.substring(3);
        } else if (name.startsWith("is") && name.length() > 2 && Character.isUpperCase(name.charAt(2))
                && ("boolean".equals(returnType) || "Boolean".equals(returnType))) {
            suffix = name.substring(2);
        } else {
            return false;
        }
        return hasMatchingAttribute(model, suffix);
    }

    /**
     * JavaBeans setter: setXxx(singleParam), void return, matching attribute.
     */
    public static boolean isSetter(MethodModel method, ClassModel model) {
        if (method == null || model == null) {
            return false;
        }
        String name = method.getName();
        if (name == null || !name.startsWith("set") || name.length() <= 3
                || !Character.isUpperCase(name.charAt(3))) {
            return false;
        }
        if (method.getParameters() == null || method.getParameters().size() != 1) {
            return false;
        }
        String returnType = method.getReturnType() == null ? "" : method.getReturnType();
        if (!"void".equals(returnType)) {
            return false;
        }
        return hasMatchingAttribute(model, name.substring(3));
    }

    private static boolean hasMatchingAttribute(ClassModel model, String suffix) {
        if (suffix == null || suffix.isEmpty() || model.getAttributes() == null) {
            return false;
        }
        String field = Character.toLowerCase(suffix.charAt(0)) + suffix.substring(1);
        for (AttributeModel attr : model.getAttributes()) {
            if (attr.getName().equals(field) || attr.getName().equalsIgnoreCase(suffix)) {
                return true;
            }
        }
        return false;
    }
}
