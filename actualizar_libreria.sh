#!/bin/bash
set -e
cd proyect-parser-core && mvn clean package -DskipTests && cd ..
shopt -s nullglob
jars=(proyect-parser-core/dist/proyect-parser-core-*.jar)
if [ ${#jars[@]} -ne 1 ]; then
    echo "ERROR: se esperaba exactamente un proyect-parser-core-*.jar en proyect-parser-core/dist/ y hay ${#jars[@]}:" >&2
    for j in "${jars[@]}"; do echo "  - $j" >&2; done
    echo "Borra las versiones viejas de proyect-parser-core/dist/ y vuelve a ejecutar. No se copió nada." >&2
    exit 1
fi
cp "${jars[0]}" Proyecto_Poo/librerias/proyect-parser-core.jar
echo "Listo: proyect-parser-core.jar actualizado con éxito en Proyecto_Poo (${jars[0]})."
