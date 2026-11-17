# Referencia de validación

Un resultado de comparación con diagramas históricos no acredita todos los
requisitos de la librería. La afirmación anterior de completitud basada solo
en esa comparación queda sustituida por el proceso descrito aquí.

La referencia verificable consta de:

1. `BorderRequirementsTest`: transcripción ejecutable de los criterios de las
   páginas 5–17 del informe actualizado del Grupo 2. Comprueba los 25 casos
   a través del modelo y del renderizado; no utiliza los diagramas esperados.
2. `DeclarationContractTest`: ejemplos distintos del corpus para restricciones
   selladas, especializaciones internas y externas y firmas de métodos y
   constructores con cotas y excepciones declaradas.
3. Las suites de regresión, integridad, filtros, CLI y REST. La prueba HTTP
   comprueba que los metadatos nuevos llegan al consumidor, no solo al dibujo.
4. El comparador estricto de tipos, miembros, restricciones y relaciones contra
   las referencias revisadas. Sus pruebas negativas detectan pérdida de
   `permits`, declaraciones genéricas y `throws`, además de omisiones/adiciones.
5. La validación de sintaxis de los 25 diagramas con PlantUML.

## Ejecutar y generar el informe

Con Maven, Python y JDK 17 en PATH y un JAR de PlantUML disponible:

```sh
python proyect-parser-core/tests/verificar_entrega.py out/verificacion --plantuml /ruta/plantuml.jar
```

Opciones: `--maven` permite indicar el ejecutable de Maven y `--pdf-origen` añade
la huella del PDF de criterios. JavaParser debe estar en `proyect-parser-core/lib`
como en la ejecución del comparador y la preparación de CI.

La salida incluye `informe.md`, `evidencia.json`, los diagramas y los registros
de cada comando. Se ejecuta `mvn clean verify`, no se reutilizan XML anteriores.
El JSON identifica fuentes, pruebas, diagramas y JAR mediante SHA-256.
Si una comprobación falla o el código cambia durante la ejecución, el estado
es fallido. Una prueba omitida se declara pendiente y nunca se cuenta como aprobada.
La carpeta de salida debe estar fuera de `target`, que Maven elimina.

## Cambio del modelo

Las restricciones `permittedTypes` se conservan en el tipo. Cada relación de
herencia distingue la identidad del destino de su expresión declarada `targetType`.
Métodos y constructores comparten `SignatureDetails`, con declaraciones genéricas,
cotas y excepciones. Esa información se extrae una vez del AST y se conserva al
asignar identidades, filtrar, renderizar y responder por REST.
Los constructores públicos anteriores del modelo core siguen disponibles.
El JSON incorpora campos nuevos documentados en OpenAPI.

Las referencias 10, 13 y 15 recibieron explícitamente la lista `permits` y las
etiquetas de especialización presentes en sus fuentes. Las referencias son
evidencia auxiliar; no sustituyen al contrato ni deciden qué debe eliminar el analizador.

## Límites

Los criterios del PDF no evalúan lógica de negocio, accesores implícitos,
multiplicidades o propiedad del ciclo de vida. Este proceso tampoco certifica
ausencia universal de defectos, carga o seguridad. El analizador sigue siendo
sintáctico con resolución heurística cuando faltan datos de classpath.
La prueba de enlaces simbólicos requiere un entorno que permita crearlos;
si se omite, ese comportamiento queda sin acreditar en esa ejecución.
El informe distingue la ejecución local de una futura ejecución de CI remota.

## Integración Ant reconstruida y enlaces de Windows

Si no están disponibles los proyectos externos, la opción `--ant /ruta/ant.bat`
prepara copias aisladas de los 25 casos, respalda sus lanzadores y crea un
lanzador con `DiagramFacade`. Ejecuta `ant clean jar`, el JAR resultante y la
comparación de su salida; valida también esa salida con PlantUML. Los fuentes
originales no se modifican. El manifiesto identifica cada copia y sus registros.
Esto comprueba el empaquetado y la integración reales, pero no se presenta como
ejecución de los proyectos originales de TesterGrupos.

La prueba adicional `scanIgnoresWindowsJunctionsOutsideSourceTree` reprodujo una
lectura fuera del árbol mediante una unión de directorio Windows: el escáner
anterior encontraba tanto A.java como Secret.java. Se corrigió la visita de
directorios para omitir reparse points anidados antes de entrar en ellos. La
prueba verifica que solo se conserve A.java, sin requerir permisos de enlaces
simbólicos. Es independiente de la prueba de enlaces simbólicos, que sigue
pendiente cuando Windows no permite crearlos. No se cambió la configuración
de seguridad del equipo para ejecutar estas comprobaciones.
