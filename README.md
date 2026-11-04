# ProyectParser

A static code analysis tool and Java AST parser that inspects Java source code to automatically generate structured PlantUML class diagrams.

---

## Overview

**ProyectParser** processes Java codebases using an Abstract Syntax Tree (AST) approach to inspect classes, interfaces, records, and relationships. It translates structural source metadata into clean PlantUML (`.puml`) diagrams for architecture visualization.

## Key Features

* **AST Parsing:** Recursively scans Java source files using JavaParser to extract models, attributes, methods, constructors, and parameter data.
* **Relationship Mapping:** Detects associations, dependencies, generalizations, and implementations across packages.
* **Filter & Classification Engine:** Includes configurable pipeline components (`DiagramFilter`, `DiagramOptions`, `ProjectFilter`) to isolate specific types or packages before export.
* **PlantUML Export:** Automatically generates formatted `.puml` files ready for rendering with PlantUML or Graphviz.

## Architecture

The project is divided into three primary submodules:

* `proyect-parser-core/`: The core parsing and export library containing AST traversal logic, internal domain models, and the diagram writer pipeline.
* `proyect-parser-server/`: The REST web service that exposes the library over HTTP (JSON and multipart input, PlantUML output) and is ready to deploy on Railway.
* `Proyecto_Poo/`: The reference application and domain model implementation demonstrating CLI integration, persistence, and automated diagram output.

## Docs

* [Flujo de la librería en 3 fases](docs/flujo-3-fases.md) (ES): obtención de información → inferencia de relaciones → generación del `.puml`.
* [API REST](docs/api.md) (ES): endpoints, formatos de entrada, filtros, opciones, errores, límites e integración desde otra aplicación.
* OpenAPI 3.0: [`proyect-parser-server/src/main/resources/static/openapi.yaml`](proyect-parser-server/src/main/resources/static/openapi.yaml) (served at `/openapi.yaml`).

## Tech Stack

* **Language:** Java 17
* **Parser Engine:** [JavaParser](https://javaparser.org/) (`javaparser-core`)
* **Diagram Notation:** [PlantUML](https://plantuml.com/)
* **Web Service:** Spring Boot 3 (REST API, JSON/multipart)
* **Build Tools:** Maven / Apache Ant

## Quick Start

### Prerequisites
* JDK 17+ (or JDK 21)
* Maven 3.8+ (multi-module build)

### Library usage (filters + display options)

```java
DiagramFacade fachada = new DiagramFacade();

// Blacklist/whitelist filter (immutable, built step by step)
DiagramFilter filtro = new DiagramFilter.Builder()
    .excludePackage("com.universidad.test")
    .excludeClass("Utilidades")
    .includePackage("com.universidad.modelo")
    .build();

// Display flags (all visible by default)
DiagramOptions opciones = new DiagramOptions.Builder()
    .showGettersSetters(false)
    .showExternal(true)
    .showJdkTypes(true)
    .build();

Path salida = fachada.exportPlantUml("ruta/a/src", Path.of("diagrama.puml"), filtro, opciones);
```

### Web service (REST API)

The `proyect-parser-server` module wraps the library in a Spring Boot service.
It accepts a ZIP of `.java` sources, loose `.java` files, or a JSON array with
`path`/`content` pairs, and returns the full model or the PlantUML diagram.

Build and run from the repository root:

```bash
mvn -pl proyect-parser-server -am package
java -jar proyect-parser-server/target/proyect-parser-server-1.0.0.jar
```

```bash
curl http://localhost:8080/api/v1/health

curl -s -X POST http://localhost:8080/api/v1/diagram/json \
  -H "Content-Type: application/json" \
  -d '{
    "projectName": "demo",
    "files": [
      {"path": "Curso.java", "content": "public class Curso { private Profesor profesor; }"},
      {"path": "Profesor.java", "content": "public class Profesor {}"}
    ],
    "options": {"showExternal": false}
  }'
```

| Endpoint | Purpose |
|---|---|
| `GET /api/v1/health` | Healthcheck used by Railway |
| `GET /api/v1` | Endpoint index and active limits |
| `POST /api/v1/analysis[/json]` | Full project model |
| `POST /api/v1/diagram[/json]` | PlantUML as JSON |
| `POST /api/v1/diagram/file[/json]` | Downloadable `.puml` file |

Railway deployment is preconfigured with `Dockerfile`, `railway.json` and
`/api/v1/health`. See the [REST API documentation](docs/api.md) for the complete
contract, filters, options, error codes, limits and integration examples.

### AppGenerador flags

```
--no-getters, --sin-accesores
                  Hide backing-field getters/setters
--no-attributes   Hide attributes
--no-methods      Hide methods
--no-constructors Hide constructors
--external        Show @external boxes and their relationships (hidden by default)
--no-external     Hide @external boxes (default)
--no-jdk          Hide only JDK externals (java.*)
--flat            Flat output, no package blocks
--resumen         Overview: class names only (hide members)
--sin-huerfanos   Omit classes with no visible relationship after filters
--firmas-cortas   Show (…n) for methods/constructors with more than 3 parameters
--sin-dependencias Omit ..> arrows (with --modulo, keeps the module's own, minus those a direct supertype already has)
--lineas=TYPE     Line style: ortho, polyline or spline (default: ortho)
--capas[=a,b,...] Order layers top to bottom and direct arrows down/up
--agrupar-capas   (Experimental) With --capas: wrap each layer's packages in a container
--excluir a,b     Exclude packages (blacklist), e.g. --excluir=com.universidad.tools
--modulo X        Only module X: its classes, their supertypes and association targets
--help, -h        Show help
```

Views in `docs/diagramas/` are regenerated with `docs/diagramas/regenerar.sh`.
