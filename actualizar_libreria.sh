#!/bin/bash
set -e
cd libreria_is && mvn clean package -DskipTests && cd ..
shopt -s nullglob
jars=(libreria_is/dist/libreria_is-*.jar)
if [ ${#jars[@]} -ne 1 ]; then
    echo "ERROR: se esperaba exactamente un libreria_is-*.jar en libreria_is/dist/ y hay ${#jars[@]}:" >&2
    for j in "${jars[@]}"; do echo "  - $j" >&2; done
    echo "Borra las versiones viejas de libreria_is/dist/ y vuelve a ejecutar. No se copió nada." >&2
    exit 1
fi
cp "${jars[0]}" Proyecto_Poo/librerias/libreria_is.jar
echo "Listo: libreria_is.jar actualizado con éxito en Proyecto_Poo (${jars[0]})."
