#!/bin/bash
# Regenera las vistas de docs/diagramas/ (.puml y .png).
# Uso: docs/diagramas/regenerar.sh   (desde cualquier carpeta)
# Renderizado: PLANTUML_JAR=/ruta/plantuml.jar, o `plantuml` en el PATH.
# Requiere PlantUML reciente (las versiones 1.2020.x no soportan `record`).
set -euo pipefail

RAIZ="$(cd "$(dirname "$0")/../.." && pwd)"
SALIDA="$RAIZ/docs/diagramas"
PROYECTO="$RAIZ/Proyecto_Poo"
CLASES="$(mktemp -d)"
trap 'rm -rf "$CLASES"' EXIT

CP="$(ls "$PROYECTO"/librerias/*.jar | tr '\n' ':')"
if command -v javac >/dev/null; then
    javac -cp "$CP" -d "$CLASES" "$PROYECTO/src/com/universidad/tools/AppGenerador.java"
else
    java -m jdk.compiler/com.sun.tools.javac.Main -cp "$CP" -d "$CLASES" \
        "$PROYECTO/src/com/universidad/tools/AppGenerador.java"
fi

generar() {
    local nombre="$1"; shift
    # </dev/null: sin respuestas a los prompts de blacklist/whitelist/relaciones.
    (cd "$PROYECTO" && java -cp "$CLASES:$CP" com.universidad.tools.AppGenerador \
        src "$SALIDA/$nombre.puml" "$@" </dev/null >/dev/null)
    echo "Generado: docs/diagramas/$nombre.puml"
}

# Vista general: es el defecto de AppGenerador sin flags
# (--sin-dependencias --capas --sin-huerfanos --resumen --excluir=com.universidad.tools).
generar vista_general

# Vista por módulo: el defecto con --modulo
# (--sin-dependencias --capas --sin-accesores --firmas-cortas).
generar vista_estudiante --modulo=Estudiante

if [ -n "${PLANTUML_JAR:-}" ]; then
    PLANTUML=(java -jar "$PLANTUML_JAR")
elif command -v plantuml >/dev/null; then
    PLANTUML=(plantuml)
else
    echo "PlantUML no encontrado (define PLANTUML_JAR): solo se generaron los .puml." >&2
    exit 0
fi
PLANTUML_LIMIT_SIZE=16384 "${PLANTUML[@]}" -tpng "$SALIDA/vista_general.puml" "$SALIDA/vista_estudiante.puml"
echo "Renderizado: docs/diagramas/vista_general.png, docs/diagramas/vista_estudiante.png"
