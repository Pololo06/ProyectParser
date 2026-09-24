package is.generador.domain.model;

import java.util.List;

/** Typed view of {@link ClassModel} with kind fixed to Enum (spec §8 / Fase 1). */
public class EnumModel extends ClassModel {
    public EnumModel(String name, String packageName,
                      List<String> stereotypes, List<AttributeModel> attributes,
                      List<MethodModel> methods, List<ConstructorModel> constructors,
                      List<String> enumConstants, List<String> typeParameters) {
        super(name, packageName, "Enum", false, stereotypes, attributes,
                methods, constructors, List.of(), List.of(), enumConstants, typeParameters);
    }
}
