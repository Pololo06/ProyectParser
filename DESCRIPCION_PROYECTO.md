# ProyectParser — Explicación del Proyecto

## 1. ¿Qué es?

**ProyectParser** es una herramienta de **análisis estático de código Java** que inspecciona código fuente mediante un **AST (Abstract Syntax Tree)** y genera automáticamente **diagramas de clases PlantUML (`.puml`)** estructurados.

Flujo general:

```
Código fuente Java (.java)
  → ProjectAnalyzer (JavaParser)
  → Modelo interno (ProjectModel / ClassModel / RelationshipModel)
  → Filtro (DiagramFilter + FilteredProjectBuilder)
  → PlantUmlGenerator + PumlFileWriter
  → diagrama.puml
```

Repositorio con dos submódulos:

| Módulo | Rol |
|---|---|
| `libreria_is/` | Librería reutilizable: parsing AST, modelo de dominio, filtrado y exportación PlantUML. Se compila con Maven. |
| `Proyecto_Poo/` | Aplicación de ejemplo/demostración: sistema de gestión universitaria (CLI) + `AppGenerador` interactivo que usa la librería para auto-generar su propio diagrama. Se compila con Ant/NetBeans. |
| `actualizar_libreria.sh` | Script puente: compila `libreria_is` y copia el JAR a `Proyecto_Poo/librerias/`. |
| `Proyecto_Poo/diagrama_filtrado.puml` | Ejemplo de salida real generada. |

## 2. Stack tecnológico

* **Lenguaje:** Java 17
* **Parser:** `com.github.javaparser:javaparser-core:3.28.2` (ver `libreria_is/pom.xml`)
* **Notación de diagramas:** PlantUML (`@startuml` / `@enduml`)
* **Build:** Maven 3.8+ (librería), Apache Ant + NetBeans (`Proyecto_Poo/build.xml`, `nbproject/`)
* **Dependencias de la demo:** `cleandev-scanner-cli.jar` (CLI), `tpa-library-blaster.jar` (persistencia), `javaparser-core.jar`, `libreria_is.jar` (en `Proyecto_Poo/librerias/`)

Requisitos: JDK 17+ (o 21), Maven 3.8+.

## 3. `libreria_is` — La librería (núcleo)

Punto de entrada único: **`is.generador.SoyLaPuerta`** (facade). Todo lo demás es interno.

### 3.1. Estructura de paquetes

```
libreria_is/src/is/generador/
├── SoyLaPuerta.java              # Fachada delgada: solo delega
├── domain/                       # Puro, sin dependencias externas
│   ├── model/                    # ProjectModel, ClassModel, Kind, RelType, ...
│   ├── policy/                   # DiagramFilter (Builder), DiagramOptions
│   ├── port/                     # SourceAnalyzerPort, DiagramRendererPort, ...
│   └── BeanAccessors.java        # Regla estricta getter/setter
├── application/                  # Casos de uso + ClassInfo, ClassInfoService, ProjectQueryService
└── infrastructure/
    ├── javaparser/               # ProjectAnalyzer (orquesta) + SourceScanner, TypeExtractor,
    │                             # GenericTypeParser, ImportResolver, RelationshipDetector,
    │                             # ExternalTypeRegistrar, TypeClassifier
    └── plantuml/                 # PlantUmlGenerator, PumlFileWriter, FileSystemDiagramWriter
```

### 3.2. `ProjectAnalyzer` — cómo analiza

1. `analyze(folderPath)` valida la carpeta y recorre recursivamente todos los `*.java`.
2. Por archivo: `StaticJavaParser.parse(file)` → `CompilationUnit` → `findAll(TypeDeclaration.class)` (cubre **tipos anidados a cualquier profundidad**: inner classes, records en interfaces, etc.).
3. Por tipo construye un `ClassModel`:
   * `ClassOrInterfaceDeclaration`: kind `Class` / `AbstractClass` / `Interface`, `extends` / `implements`, fields, constructores, métodos.
   * `RecordDeclaration`: kind `Record`, componentes del header como atributos `private final`, más miembros.
   * `EnumDeclaration`: kind `Enum`, constantes vía `getEntries()`, más miembros.
   * `AnnotationDeclaration`: kind `Annotation`.
   * Anotaciones Java → estereotipos (`@Entity` → `<<Entity>>` en PlantUML).
4. Resuelve imports explícitos por archivo (`simpleName → paquete`) para ubicar tipos externos reales (ej. `Path`, `SystemModule`).
5. **Tolerancia a fallos (RNF-04/RNF-06):** no aborta ni imprime a `stderr`; acumula `parsedFiles`, `failedFiles`, `failureReasons`, consultables vía `getParsedFileCount()`, `getFailedFiles()`, `getLastAnalysisSummary()`, etc.

### 3.3. Detección de relaciones

`detectRelationships()` genera 4 tipos:

| Tipo | Origen | Flecha PlantUML |
|---|---|---|
| `EXTENDS` | `extends` entre clases internas | `<\|--` (`Padre <\|-- Hijo`) |
| `IMPLEMENTS` | `implements` / interfaces | `<\|..` |
| `ASSOCIATION` | Atributos (incluye genéricos: `List<Paquete>`, `Map<K,V>`, wildcards, anidados) | `-->` |
| `DEPENDENCY` | Solo en firmas de métodos/constructores (params, retornos), no como atributo | `..>` |

Detalles: parseo de genéricos con conteo balanceado de `< >` (soporta `Map<String, List<SubZone>>`), deduplicación con clave `source|tipo|target`.

**Tipos externos:** `registerExternalTypes()` crea cajas `ClassModel` con estereotipo `@external` para atributos no primitivos/no internos (ignora `String`, wrappers, primitivos vía `TypeClassifier.shouldIgnoreBox`). Resuelve paquete por imports → mapa JDK común → fallback `java.lang`.

### 3.4. Filtrado (Fase 1: Builder)

* **`domain.policy.DiagramFilter`** (inmutable, se construye con `Builder`): blacklist `exclude*` (veta siempre, gana a todo) + whitelist `include*` (si no está vacía → modo restrictivo, todo lo no declarado se descarta).
```java
DiagramFilter filtro = new DiagramFilter.Builder()
    .excludePackage("com.universidad.test")
    .excludeClass("Utilidades")
    .includePackage("com.universidad.modelo")
    .build();
```
* **`application.FilteredProjectBuilder`**: aplica el filtro, elimina clases vetadas y relaciones con extremos caídos. Fase 5: además acepta vetos de relaciones (`excludeRelationship(s)`, clave `"origen|TIPO|destino"`) — solo elimina, nunca inventa.
* Fachada: `analyzeFiltered(path, filter)`, `generatePlantUml(path, filter[, options])`, `exportPlantUml(path, output, filter[, options])`.

### 3.5. Banderas de visualización (Fase 2: `domain.policy.DiagramOptions`)

```java
DiagramOptions opciones = new DiagramOptions.Builder()
    .showGettersSetters(false) // oculta getters/setters JavaBeans con campo respaldo
    .showAttributes(true).showMethods(true).showConstructors(true)
    .showExternal(true)        // cajas @external y sus relaciones
    .showJdkTypes(true)        // solo java.* (UUID, BigDecimal, OffsetDateTime...)
    .groupByPackage(true)
    .build();
```
`infrastructure.plantuml.PlantUmlGenerator.generate(project, options)` (el `generate(p, boolean)` delega; salida idéntica con `defaults()`). `domain.BeanAccessors` centraliza la regla estricta getter/setter. Puerto `DiagramRendererPort.render(project, options)` + sobrecargas `execute(...)` en los casos de uso.

### 3.6. Generación PlantUML

`PlantUmlGenerator.generate(project, groupByPackage)`:

* Agrupa clases internas por paquete en bloques `package "..." { }` ordenados alfabéticamente; externas en bloque `package "EXTERNAL"` separado al final.
* Por clase: keyword según `kind` (`class`, `abstract class`, `interface`, `enum`, `record`, `annotation`), estereotipos `<<...>>`, constantes enum, atributos `visibilidad tipo nombre`, constructores y métodos con parámetros.
* Visibilidad: `+` public, `-` private, `#` protected, `~` package-private. Sufijos `{static}`, `{abstract}` (`final` se omite a propósito).
* Relaciones internas primero, luego sección `' ---- EXTERNAL RELATIONSHIPS ----`.

`PumlFileWriter.write(path, content)`: crea directorios padre, fuerza extensión `.puml`, escribe UTF-8.

### 3.7. API de `SoyLaPuerta` (resumen)

* Análisis/conteo: `countClasses()`, `countByKind()`, `countTotalTypes()`, `getPackages()`, `getPackageNames()`, `getPackageModels()`, `getClassDetails()` / `ClassInfo` (propiedades, constructores, getters/setters JavaBeans reales con campo respaldo), `getExternalClasses()`.
* Generación: `generatePlantUml()`, `generatePlantUmlViaUseCase()`, `exportPlantUml()`, variantes con `DiagramFilter`.
* Trazabilidad: `getParsedFileCount()`, `getErrorCount()`, `getFailedFiles()`, `getFailureReasons()`, `getLastAnalysisSummary()`.
* Compatibilidad: aliases en español `@Deprecated` (`contarClases`, `generarPlantUml`, `obtenerPaquetes`, etc.).

## 4. `Proyecto_Poo` — Aplicación de demostración

Sistema CLI de **gestión universitaria** (estudiantes, profesores, cursos, asignaturas/costos) con arquitectura por capas. Además de ser funcional, es el **caso de prueba** sobre el que la librería genera diagramas.

### 4.1. Capas (`src/com/universidad/`)

```
App.java / AppScannerCli.java
├── domain/               Entidades (Estudiante, Profesor, ...), repositorios, enumeracion/
├── application/          DTOs, mappers, servicios, puertos
├── infrastructure/       cli/ (vistas), config/, persistence/
├── interfaceadapters/    controller/
└── tools/                AppGenerador.java + MuestraTiposExternos.java (fixture de externas)
```

* **Persistencia:** archivos planos en `misPersistencias/` (`estudiantes.txt`, `profesore.txt`, `cursos.txt`, `asignaturas.txt`, `materias.txt`).
* **Entrada:** `App.java` → `AppScannerCli.configuracionCompleta()` → `CliEngine` + módulos registrados desde `ConfiguracionDeDependencias`.

### 4.2. `AppGenerador` — generador interactivo de diagramas

`com.universidad.tools.AppGenerador` es el cliente de referencia de la librería:

1. Resuelve `srcPath` (posicional 1 → `./src` → subcarpetas) y salida (posicional 2 o `diagrama_filtrado.puml`); flags `--*` no cuentan como posicionales.
2. **Resumen global primero** (Fase 4: conteo por `kind`), luego menús numerados de paquetes/clases.
3. Pide **blacklist/whitelist** con sintaxis: `Enter`=ninguno/todos, `todo/all`=todos, rangos `1-5, 3-, -4`, listas `1,3,5`, nombres directos; construye el filtro con `DiagramFilter.Builder`.
4. **Revisión de relaciones** (Fase 5): lista `ORIGEN TIPO -> DESTINO` y permite descartar (`Enter`=ninguna); reconstruye sin las vetadas.
5. Imprime resumen resultante + detalle y genera con las **banderas** (`--no-getters --no-attributes --no-methods --no-constructors --no-external --no-jdk --flat --help`).

## 5. Uso rápido

```bash
# 1. Compilar librería y actualizar el JAR usado por la demo
./actualizar_libreria.sh
# (equivale a: cd libreria_is && mvn clean package -DskipTests
#  + cp libreria_is/dist/libreria_is-*.jar Proyecto_Poo/librerias/libreria_is.jar)

# 2. Generar diagrama de la demo (interactivo, pregunta filtros)
cd Proyecto_Poo
java -cp "librerias/*:build/classes" com.universidad.tools.AppGenerador src diagrama_filtrado.puml

# 2b. Con banderas (no interactivas para visualización)
java -cp "librerias/*:build/classes" com.universidad.tools.AppGenerador src diagrama.puml --no-getters --no-jdk

# 3. Renderizar el .puml con PlantUML / VS Code / IntelliJ / plantuml.com
```

Uso como librería en otro proyecto:

```java
SoyLaPuerta puerta = new SoyLaPuerta();
DiagramFilter filtro = new DiagramFilter.Builder()
    .excludePackage("com.ejemplo.test")
    .includePackage("com.ejemplo.modelo")
    .build();
DiagramOptions opciones = new DiagramOptions.Builder()
    .showGettersSetters(false).showJdkTypes(true).build();
Path salida = puerta.exportPlantUml("ruta/a/src", Path.of("diagrama.puml"), filtro, opciones);
```

Nota multilenguaje (Fase 6): el contrato ya existe como `domain.port.SourceAnalyzerPort.analyze(path) → ProjectModel`, implementado por `infrastructure.javaparser.ProjectAnalyzer`. Un futuro adaptador (ej. Python) solo implementa el puerto; modelo, filtros, banderas y PlantUML se reutilizan.

## 6. Ejemplo de salida

`diagrama_filtrado.puml` agrupa por paquete (`com.universidad.modelo`, `.dto.curso`, `.servicio`, `.vista`, etc.) y al final lista relaciones:

```plantuml
@startuml
skinparam classAttributeIconSize 0
package "com.universidad.modelo" {
  class Curso {
    -UUID idCurso
    +Curso(String codigoCurso, String nombreCurso, ...)
    +boolean estaActivo()
  }
}
...
CursoServicio --> CursoRepositorio
CursoServicio ..> CursoDto
RepositorioBaseAbstracto <|-- CursoRepositorioImpl
CursoRepositorio <|.. CursoRepositorioImpl
@enduml
```

## 7. Ideas clave para explicar/defender el proyecto

* **AST vs. regex:** se usa JavaParser, no expresiones regulares → soporta genéricos anidados, records, enums, tipos internos y anotaciones con precisión.
* **Separación Clean Architecture:** `domain` (modelo + policies, puro) ← `application` (casos de uso, solo puertos) ← `infrastructure` (JavaParser, PlantUML, archivos). La fachada solo conecta puertos. El `ProjectModel` es canónico y testeable sin PlantUML.
* **Whitelist solo para internas (opción B):** las clases externas (`@external`: JDK y librerías) solo obedecen a la blacklist y a `--no-external`/`--no-jdk`. Tras filtrar y vetar, las externas sin relaciones se eliminan (regla de huérfanas): al whitelistear un paquete solo ves las externas que ese paquete usa.
* **Diagramas útiles, no ruidosos:** primitivos/escalares no crean cajas, externas van a bloque `EXTERNAL` separado, filtros blacklist>whitelist permiten aislar un subsistema.
* **Trazabilidad:** cada análisis reporta qué archivos se parsearon y cuáles fallaron con motivo, sin tumbar la generación.
