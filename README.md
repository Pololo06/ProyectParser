# ProyectParser

A static code analysis tool and Java AST parser that inspects Java source code to automatically generate structured PlantUML class diagrams.

---

## Overview

**ProyectParser** processes Java codebases using an Abstract Syntax Tree (AST) approach to inspect classes, interfaces, records, and relationships. It translates structural source metadata into clean PlantUML (`.puml`) diagrams for architecture visualization.

## Key Features

* **AST Parsing:** Recursively scans Java source files using JavaParser to extract models, attributes, methods, constructors, and parameter data.
* **Relationship Mapping:** Detects associations, dependencies, generalizations, and implementations across packages.
* **Filter & Classification Engine:** Includes configurable pipeline components (`DiagramFilter`, `TypeClassifier`) to isolate specific types or packages before export.
* **PlantUML Export:** Automatically generates formatted `.puml` files ready for rendering with PlantUML or Graphviz.

## Architecture

The project is divided into two primary submodules:

* `libreria_is/`: The core parsing and export library containing AST traversal logic, internal domain models, and the diagram writer pipeline.
* `Proyecto_Poo/`: The reference application and domain model implementation demonstrating CLI integration, persistence, and automated diagram output.

## Tech Stack

* **Language:** Java
* **Parser Engine:** [JavaParser](https://javaparser.org/) (`javaparser-core`)
* **Diagram Notation:** [PlantUML](https://plantuml.com/)
* **Build Tools:** Maven / Apache Ant

## Quick Start

### Prerequisites
* JDK 17+ (or JDK 21)
* Maven 3.8+ (for `libreria_is`)
