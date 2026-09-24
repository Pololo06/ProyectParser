package is.generador.domain.model;

import java.util.List;

/** Typed view of {@link ClassModel} with kind fixed to Record (spec §8 / Fase 1). */
public class RecordModel extends ClassModel {
    public RecordModel(String name, String packageName,
                       List<String> stereotypes, List<AttributeModel> attributes,
                       List<MethodModel> methods, List<ConstructorModel> constructors,
                       List<String> implementedTypes) {
        super(name, packageName, "Record", false, stereotypes, attributes,
                methods, constructors, List.of(), implementedTypes, List.of());
    }
}
