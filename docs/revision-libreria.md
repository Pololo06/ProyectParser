# Revisión de integridad de ProyectParser

Fecha: 17 de noviembre de 2026. Alcance: `proyect-parser-core` completo (escaneo,
extracción, resolución, relaciones, modelos, consultas, filtros, render y escritura),
con verificación de integración de los consumidores CLI y REST.

## Hallazgos reproducidos y corregidos

Los siguientes problemas alteraban el modelo sin indicar un fallo de análisis.
Sus reproducciones están en `TypeSemanticsTest`; se observaron los fallos antes
de corregir el código. Son errores de integridad funcional, no una afirmación
de vulnerabilidades de seguridad.

| Problema | Reproducción | Causa y corrección |
|---|---|---|
| Relaciones perdidas o destinos inventados | `class dato {}` y `class Niño {}` usados como campos | El tokenizador exigía mayúsculas ASCII. Se extraen referencias con el parser de tipos Java existente. |
| Variable genérica convertida en clase externa | `class Outer<T> { class Inner { T value; } }` | Faltaba el ámbito genérico del contenedor. Se hereda únicamente a través de tipos no estáticos. |
| Tipo calificado eliminado por una variable homónima | `class Example<T> { third.party.T value; }` | Se excluía por nombre simple. Solo un identificador sin calificar puede representar la variable `T`. |
| Herencia externa omitida | `extends java.io.IOException implements java.io.Serializable` | El registro externo ignoraba supertipos. Conserva las relaciones de herencia y el tipo interfaz cuando se conoce por la cláusula. |
| Categoría externa dependiente del orden de uso | Un campo usa `Serializable` antes de otra clase que lo implementa | Primero se registran los supertipos conocidos, después sus usos. |
| Límite genérico externo omitido | `Example<T extends java.io.Closeable>` | El registro externo no recorría los límites de clase. Ahora los incluye. |
| Homónimo externo descartado como escalar | Campo `third.party.String` | La política de escalares se aplicaba antes de resolver el paquete. Ahora distingue el tipo externo de `java.lang.String`. |
| Clases locales con identidad compartida | Dos métodos declaran cada uno `class Local` | Al no tener FQN Java, ambas recibían el mismo identificador. Se usa contenedor y coordenadas de declaración; las consultas conservan ambos modelos. |

La arquitectura por puertos y adaptadores permite corregir estas causas dentro
del adaptador JavaParser sin cambiar las firmas públicas ni mezclar las reglas
de análisis con el renderizado. No fue necesario reemplazar la arquitectura.

## Los cinco casos iniciales

Antes del cambio, el comparador original pasaba **20/25**. El verificador auditado
aceptaba seis diferencias explícitas por número de caso. No eran cinco errores
demostrados del motor: los fuentes Java sí contienen los elementos adicionales.

Se corrigieron manualmente seis omisiones de las referencias, conservando el
resto de su contenido y estilo. No se copiaron los diagramas generados sobre las
referencias ni se añadieron condiciones al motor por nombre de caso.

| Referencia | Evidencia Java | Expectativa añadida |
|---|---|---|
| caso_07 | `records/SettlementBatchRecord.java`, constructor compacto público y cinco componentes | Constructor con los cinco parámetros de la cabecera. Se conserva también la sobrecarga de tres parámetros. |
| caso_12 | `template/HighFrequencyRiskEngine.java`, parámetro `OffsetDateTime[] checkpointTimestamps` | Dependencia hacia `OffsetDateTime`. |
| caso_15 | `crtp/FinancialLedgerNode.java`, parámetros `OffsetDateTime nodeTimestamp`, `BigDecimal cumulativeWeight` | Dos dependencias hacia esos tipos. |
| caso_16 | `wildcards/TaxHoldingEntity.java`, parámetro `BigDecimal declaredWorth` | Dependencia hacia `BigDecimal`. |
| caso_25 | `domain/FiscalInvoice.java`, parámetro `OffsetDateTime createdAt` | Dependencia hacia `OffsetDateTime`. |

Pasar esos parámetros a `super(...)` no elimina su presencia en la firma del
constructor de la subclase. La reducción de dependencias redundantes de una vista
es una opción de presentación, no una razón para eliminar información del modelo.

Se retiraron las excepciones del verificador auditado: utiliza el mismo comparador
estricto que el original. Cualquier miembro o relación faltante o adicional falla.
El registro de ejecución debe contener los 25 casos una sola vez y acreditar el
análisis de todos los Java de cada fixture. Pruebas negativas comprueban que se
rechazan adiciones, omisiones y registros incompletos, repetidos o con errores.

## Validación reproducible

Desde la raíz del repositorio:

```sh
mvn -B clean verify
python -m unittest discover -s proyect-parser-core/tests -p "test_*.py"
python proyect-parser-core/tests/verificar_casos.py target/corpus
```

El último comando utiliza el JAR JavaParser de `proyect-parser-core/lib`. La
configuración de CI lo prepara desde la dependencia Maven del proyecto.

- JDK 17: compilación y empaquetado de los tres módulos; **109 pruebas Java,
  108 exitosas, 1 omitida, 0 fallos y 0 errores**.
- La omisión corresponde a `scanIgnoresSymbolicLinksOutsideSourceTree`: Windows
  deniega crear enlaces simbólicos con los permisos de esta sesión. Se declara
  como requisito de entorno, sin simular un resultado exitoso.
- Las pruebas incluyen 25 recorridos de la API pública: archivos analizados,
  identificadores únicos, extremos existentes, ausencia de relaciones duplicadas,
  generación determinista, ocultamiento de externas y exportación UTF-8.
- Comparador: **25/25** iguales en contenido normalizado, **112 Java** analizados,
  sin excepciones por caso. Los bytes y estilos no son iguales a las referencias.
- Verificador Python: **3 pruebas exitosas**, con subcasos negativos.
- PlantUML **1.2026.8**: `-checkonly` sobre los 25 archivos generados, salida 0.
- El harness del CLI fija UTF-8 en sus JVM hijas para concordar con su lector
  de salida; se corrigió una diferencia de codificación de Windows, sin cambiar
  la codificación de la consola del usuario.
- Se añadió CI para Windows y Linux con JDK 17. Su ejecución remota requiere
  subir los cambios y no se presenta como ya ejecutada.

## Límites de la revisión

Esto es una revisión de código con pruebas funcionales e integración, no una
demostración de ausencia de errores ni una certificación de producción.
El analizador trabaja sobre fuentes y usa heurísticas para resolver tipos externos
cuando no hay imports explícitos o nombres calificados; no resuelve un classpath
completo como un compilador. Los identificadores sintéticos de clases locales
cambian al mover su declaración de línea. No se hicieron pruebas de carga,
penetración ni una auditoría de dependencias transitivas.
