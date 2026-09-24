package is.generador.domain.model;

import java.util.List;

/** Typed view of {@link ClassModel} with kind fixed to Interface (spec §8 / Fase 1). */
public class InterfaceModel extends ClassModel {
    public InterfaceModel(String name, String packageName,
                          List<String> stereotypes, List<AttributeModel> attributes,
                          List<MethodModel> methods, List<ConstructorModel> constructors,
                          List<String> extendedTypes, List<String> implementedTypes) {
        super(name, packageName, "Interface", false, stereotypes, attributes,
                methods, constructors, extendedTypes, implementedTypes, List.of());
    }
}
