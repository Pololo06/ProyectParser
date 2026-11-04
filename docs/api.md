# API REST de ProyectParser

Servicio HTTP para analizar código fuente Java y generar diagramas PlantUML.
Es un adaptador fino sobre la librería `proyect-parser-core`: el análisis, la
inferencia de relaciones y el renderizado siguen viviendo en la librería.

- **URL local:** `http://localhost:8080`
- **URL en Railway:** `https://<servicio>.up.railway.app`
- **Prefijo versionado:** `/api/v1`
- **Formato de error:** `application/problem+json` (RFC 7807)
- **Autenticación:** ninguna por defecto (ver [Seguridad](#12-seguridad))
- **Especificación OpenAPI:** [`/openapi.yaml`](/openapi.yaml) o
  [`/api/v1/openapi.yaml`](/api/v1/openapi.yaml) (también en
  `proyect-parser-server/src/main/resources/static/openapi.yaml`)

---

## 1. Qué hace y qué no hace

El servicio recibe archivos `.java`, los escribe en un directorio temporal
aislado y ejecuta el mismo flujo de 3 fases de la librería:

```text
.java subidos
  → Fase 1: ProjectModel (paquetes + clases)
  → Fase 2: relaciones (EXTENDS, IMPLEMENTS, ASSOCIATION, DEPENDENCY, NESTING)
  → Fase 3: texto PlantUML (@startuml ... @enduml)
```

- **No compila** el código ni ejecuta el proyecto subido.
- Solo se leen archivos con extensión exacta `.java`.
- Un archivo que no parsea **no aborta** la petición: se devuelve en
  `failedFiles` / `failureReasons` y el resto del proyecto se analiza igual.
- El directorio temporal se elimina al terminar cada petición.
- El servicio es *stateless*: cada petición crea su propio analizador.

---

## 2. Inicio rápido

### 2.1 Diagrama desde un ZIP (multipart)

```bash
BASE="http://localhost:8080"

curl -X POST "$BASE/api/v1/diagram/file" \
  -H "Accept: text/plain" \
  -F "file=@proyecto.zip" \
  -F 'request={"projectName":"mi-proyecto","options":{"showExternal":false,"summary":true}};type=application/json' \
  -o mi-proyecto.puml
```

`proyecto.zip` puede contener cualquier estructura de carpetas; solo se
extraen las entradas `.java`.

### 2.2 Diagrama desde JSON (recomendado para otra aplicación)

`request.json`:

```json
{
  "projectName": "mi-proyecto",
  "files": [
    {
      "path": "src/main/java/com/example/model/Curso.java",
      "content": "package com.example.model;\npublic class Curso {\n  private Profesor profesor;\n}\n"
    },
    {
      "path": "src/main/java/com/example/model/Profesor.java",
      "content": "package com.example.model;\npublic class Profesor {\n}\n"
    }
  ],
  "options": {
    "showGettersSetters": false,
    "showExternal": false
  }
}
```

```bash
curl -s -X POST "$BASE/api/v1/diagram/json" \
  -H "Content-Type: application/json" \
  --data-binary @request.json
```

Respuesta (resumida):

```json
{
  "projectName": "mi-proyecto",
  "fileName": "mi-proyecto.puml",
  "plantUml": "@startuml\n...\n@enduml\n",
  "statistics": {
    "totalJavaFiles": 2,
    "parsedJavaFiles": 2,
    "failedJavaFiles": 0,
    "classes": 2,
    "externalClasses": 0,
    "relationships": 1,
    "packages": 1
  },
  "failedFiles": [],
  "failureReasons": []
}
```

---

## 3. Formas de entrada

### 3.1 JSON (`application/json`)

Ideal cuando la aplicación consumidora ya tiene el código en memoria.

```json
{
  "projectName": "opcional",
  "files": [
    { "path": "ruta/relativa/Algo.java", "content": "código fuente completo" }
  ],
  "filter": { },
  "options": { }
}
```

Reglas de `files`:

- `path` debe ser **relativo**, terminar en `.java` y no contener `..`.
- `content` es el texto completo del archivo (puede ir vacío).
- Los `path` duplicados devuelven `400 INVALID_SOURCE`.
- Si no se envía `projectName`, se usa `project`.
- El nombre se sanea a `[A-Za-z0-9._-]` (máximo 80 caracteres).

### 3.2 Multipart (`multipart/form-data`)

| Parte | Tipo | Obligatoria | Descripción |
|---|---|---|---|
| `file` | binario | No | ZIP con archivos `.java`. Las entradas que no son `.java` se ignoran. |
| `files` | binario (repetible) | No | Archivos `.java` sueltos. Se puede combinar con `file`. |
| `request` | JSON (`application/json`) | No | `projectName`, `filter` y `options`. |

Debe existir al menos un `.java` entre `file` y `files`. Si no se envía la parte
`request`, se usan los valores por defecto.

> Si el nombre del ZIP es `mi-proyecto.zip` y no se envía `projectName`, el
> proyecto se llama `mi-proyecto`. Un archivo suelto `Curso.java` produce el
> proyecto `Curso`.

---

## 4. Endpoints

| Método | Ruta | Entrada | Salida |
|---|---|---|---|
| `GET` | `/api/v1/health` | — | JSON con estado, versión y timestamp |
| `GET` | `/api/v1` | — | Índice de endpoints y límites activos |
| `POST` | `/api/v1/analysis` | multipart | Modelo completo (`AnalysisResponse`) |
| `POST` | `/api/v1/analysis/json` | JSON | Modelo completo (`AnalysisResponse`) |
| `POST` | `/api/v1/diagram` | multipart | JSON con `plantUml` (`DiagramResponse`) |
| `POST` | `/api/v1/diagram/json` | JSON | JSON con `plantUml` (`DiagramResponse`) |
| `POST` | `/api/v1/diagram/file` | multipart | Archivo `.puml` (`text/plain`) |
| `POST` | `/api/v1/diagram/file/json` | JSON | Archivo `.puml` (`text/plain`) |
| `GET` | `/openapi.yaml` | — | Contrato OpenAPI 3.0 (`application/yaml`) |

### 4.1 Healthcheck

```http
GET /api/v1/health
```

```json
{
  "status": "UP",
  "service": "proyect-parser-api",
  "version": "1.0.0",
  "timestamp": "2026-11-04T04:52:40.049Z"
}
```

También existe el healthcheck estándar de Spring Boot:
`GET /actuator/health`.

### 4.2 Análisis

Devuelve el modelo completo, sin generar PlantUML. Útil para inspeccionar
paquetes, clases, miembros y relaciones, o para construir una vista propia.

```bash
curl -s -X POST "$BASE/api/v1/analysis/json" \
  -H "Content-Type: application/json" \
  --data-binary @request.json
```

### 4.3 Diagrama como archivo

```bash
curl -X POST "$BASE/api/v1/diagram/file/json" \
  -H "Content-Type: application/json" \
  --data-binary @request.json \
  -o mi-proyecto.puml
```

La respuesta incluye:

```http
Content-Type: text/plain;charset=UTF-8
Content-Disposition: attachment; filename="mi-proyecto.puml"
```

### 4.4 Contrato OpenAPI

La especificación completa está disponible en:

```http
GET /openapi.yaml
GET /api/v1/openapi.yaml
Content-Type: application/yaml
```

Se puede importar directamente en Postman, Insomnia, Swagger UI o generar un
cliente con `openapi-generator`. El archivo fuente es
`proyect-parser-server/src/main/resources/static/openapi.yaml`.

---

## 5. Filtros (`filter`)

Se aplican después del análisis, igual que `DiagramFilter` en la librería.
La precedencia es **blacklist > whitelist > permitir**.

```json
{
  "filter": {
    "excludePackages": ["com.example.test"],
    "excludeClasses": ["Utilidades"],
    "includePackages": ["com.example.model"],
    "includeClasses": []
  }
}
```

| Campo | Efecto |
|---|---|
| `excludePackages` | Veta siempre esos paquetes. |
| `excludeClasses` | Veta siempre esos nombres simples de clase. |
| `includePackages` | Si hay al menos un `include*`, solo pasa lo listado. |
| `includeClasses` | Igual que el anterior, por nombre simple. |

Reglas:

- Los nombres de clase son **simples** (`Curso`), no FQN.
- Las cajas externas `@external` solo obedecen a `exclude*`.
- El filtro afecta a `classes`, `packages`, `relationships` y a los contadores
  del modelo devuelto. Los contadores de archivos (`totalJavaFiles`,
  `parsedJavaFiles`, `failedJavaFiles`) siempre reflejan todo lo subido.
- Si un paquete/archivo se excluye, sus relaciones también se eliminan.

---

## 6. Opciones de visualización (`options`)

Todas son opcionales. Si se omite una, se usa el valor por defecto de la
librería.

| Campo | Defecto | Efecto |
|---|---|---|
| `showGettersSetters` | `true` | Muestra getters/setters detectados por campo respaldo. |
| `showAttributes` | `true` | Muestra atributos. |
| `showMethods` | `true` | Muestra métodos. |
| `showConstructors` | `true` | Muestra constructores. |
| `showExternal` | `true` | Muestra cajas `@external` y sus relaciones. |
| `showJdkTypes` | `true` | Muestra solo externas `java.*` (requiere `showExternal`). |
| `showDependencies` | `true` | Muestra flechas `..>` (`DEPENDENCY`). |
| `groupByPackage` | `true` | Agrupa en bloques `package`. `false` = salida plana. |
| `shortSignatures` | `false` | Métodos/constructores con >3 parámetros se pintan `(…n)`. |
| `hideOrphans` | `false` | Omite clases sin relaciones visibles tras filtros/opciones. |
| `summary` | `false` | Emite `hide members`: solo nombres de clase. |
| `groupLayers` | `false` | Experimental: agrupa paquetes de cada capa en un contenedor. |
| `lineType` | `ortho` | `ortho`, `polyline` o `spline` (`skinparam linetype`). |
| `layerOrder` | vacío | Capas de arriba a abajo; habilita flechas `down/up` y enlaces ocultos. |
| `allowedDependencies` | vacío | Pares `"Origen->Destino"` que sobreviven a `showDependencies=false`. |

Ejemplo con capas y estilo:

```json
{
  "options": {
    "lineType": "spline",
    "layerOrder": ["domain", "application", "infrastructure"],
    "shortSignatures": true,
    "hideOrphans": true
  }
}
```

`lineType` inválido devuelve `400 INVALID_ARGUMENT`.

---

## 7. Modelo de respuesta

### 7.1 `AnalysisResponse`

```json
{
  "projectName": "mi-proyecto",
  "statistics": {
    "totalJavaFiles": 12,
    "parsedJavaFiles": 11,
    "failedJavaFiles": 1,
    "classes": 10,
    "externalClasses": 4,
    "relationships": 18,
    "packages": 3
  },
  "kinds": { "Class": 8, "Interface": 2 },
  "packages": [
    {
      "name": "com.example.model",
      "displayName": "com.example.model",
      "typeCount": 2,
      "types": ["Curso", "Profesor"]
    }
  ],
  "classes": [ ],
  "relationships": [ ],
  "externalClasses": { "UUID": "java.util" },
  "failedFiles": ["zip/Broken.java"],
  "failureReasons": ["zip/Broken.java -> ..."]
}
```

Notas:

- `classes` contiene solo tipos internos. Las cajas externas van en
  `externalClasses` (`id → paquete real`).
- `packages` contiene solo paquetes internos; el paquete por defecto se
  devuelve como `(default package)` en `displayName` y `""` en `name`.
- `kinds` cuenta tipos internos por categoría (`Class`, `AbstractClass`,
  `Interface`, `Enum`, `Record`, `Annotation`).
- `failedFiles` y `failureReasons` usan rutas relativas al árbol subido, nunca
  rutas del servidor.

### 7.2 `Class`

```json
{
  "id": "Curso",
  "name": "Curso",
  "fqn": "com.example.model.Curso",
  "packageName": "com.example.model",
  "kind": "Class",
  "abstractType": false,
  "external": false,
  "stereotypes": [],
  "attributes": [
    { "name": "profesor", "type": "Profesor", "modifiers": ["private"], "initialValue": null }
  ],
  "methods": [
    {
      "name": "getNombre",
      "returnType": "String",
      "parameters": [],
      "modifiers": ["public"],
      "typeParameters": []
    }
  ],
  "constructors": [
    {
      "name": "Curso",
      "parameters": [{ "name": "nombre", "type": "String" }],
      "modifiers": ["public"],
      "typeParameters": []
    }
  ],
  "extendedTypes": [],
  "implementedTypes": [],
  "enumConstants": [],
  "typeParameters": []
}
```

`id` es el nombre simple, salvo que dos clases del proyecto compartan nombre:
en ese caso es el FQN. Los extremos de `relationships` usan ese mismo `id`.

### 7.3 `Relationship`

```json
{ "source": "Curso", "target": "Profesor", "type": "ASSOCIATION" }
```

| Tipo | Significado | Flecha PlantUML |
|---|---|---|
| `EXTENDS` | Herencia de clase o interfaz | `Padre <\|-- Hija` |
| `IMPLEMENTS` | Implementación de interfaz | `Interfaz <\|.. Impl` |
| `ASSOCIATION` | Tipo de atributo/campo | `Origen --> Destino` |
| `DEPENDENCY` | Tipo usado solo en firmas de métodos/constructores | `Origen ..> Destino` |
| `NESTING` | Tipo anidado hacia su contenedor directo | `Contenedor +-- Anidado` |

---

## 8. Errores

Formato (`application/problem+json`):

```json
{
  "type": "about:blank",
  "title": "Invalid source input",
  "status": 400,
  "detail": "At least one .java source file is required (ZIP archive, 'files' parts or JSON 'files')",
  "instance": "/api/v1/analysis/json",
  "code": "INVALID_SOURCE"
}
```

| HTTP | `code` | Causa típica |
|---|---|---|
| `400` | `INVALID_SOURCE` | Sin archivos `.java`, ZIP inválido, ruta con `..`, ruta absoluta, archivo no `.java`, duplicado o límites superados. |
| `400` | `INVALID_ARGUMENT` | `lineType` desconocido o `allowedDependencies` mal formado. |
| `400` | `MALFORMED_BODY` | JSON inválido o parte `request` inválida. |
| `400` | `INVALID_MULTIPART` | Cuerpo multipart ilegible. |
| `413` | `UPLOAD_TOO_LARGE` | El multipart supera `PARSER_MAX_UPLOAD_SIZE` / `PARSER_MAX_REQUEST_SIZE`. |
| `415` | `UNSUPPORTED_MEDIA_TYPE` | `Content-Type` distinto de JSON o multipart. |
| `500` | `INTERNAL_ERROR` / `INTERNAL_IO_ERROR` | Error inesperado del servidor. |

Los errores 400 nunca incluyen stack traces ni rutas internas del servidor.

---

## 9. Límites y configuración

Valores por defecto:

| Límite | Defecto | Variable de entorno |
|---|---|---|
| Archivos `.java` por petición | 5.000 | `PARSER_MAX_FILES` |
| Tamaño de un `.java` descomprimido | 2 MB | `PARSER_MAX_FILE_BYTES` |
| Tamaño total descomprimido | 32 MB | `PARSER_MAX_TOTAL_BYTES` |
| Tamaño máximo del archivo multipart | 50 MB | `PARSER_MAX_UPLOAD_SIZE` |
| Tamaño máximo de la petición multipart | 60 MB | `PARSER_MAX_REQUEST_SIZE` |

Otras variables:

| Variable | Defecto | Descripción |
|---|---|---|
| `PORT` | `8080` | Puerto HTTP (Railway lo inyecta). |
| `PARSER_VERSION` | `1.0.0` | Versión reportada por `/health` y `/api/v1`. |
| `CORS_ALLOWED_ORIGINS` | `*` | Orígenes permitidos, separados por comas. |
| `PROYECTPARSER_API_TEMP_DIR` | temp del sistema | Directorio para los workspaces temporales. |

Todas las propiedades de `proyectparser.api` se pueden sobrescribir con el
prefijo `PROYECTPARSER_API_` (por ejemplo
`PROYECTPARSER_API_MAX_FILES=1000`).

---

## 10. Integración desde otra aplicación

### 10.1 Java 17+ (`java.net.http`)

```java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

String payload = """
        {
          "projectName": "mi-proyecto",
          "files": [
            {"path": "Curso.java", "content": "public class Curso { private Profesor p; }"},
            {"path": "Profesor.java", "content": "public class Profesor {}"}
          ],
          "options": {"showExternal": false}
        }
        """;

HttpRequest request = HttpRequest.newBuilder(
                URI.create("https://mi-servicio.up.railway.app/api/v1/diagram/json"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(payload))
        .build();

HttpResponse<String> response = HttpClient.newHttpClient()
        .send(request, HttpResponse.BodyHandlers.ofString());

if (response.statusCode() != 200) {
    throw new IllegalStateException("ProyectParser error: " + response.body());
}
String json = response.body(); // contiene plantUml, statistics, etc.
```

Para respuestas tipadas, deserializa con Jackson/Gson. El contrato está en
[`openapi.yaml`](/openapi.yaml).

### 10.2 Node.js 18+

```js
const BASE = "https://mi-servicio.up.railway.app";

const response = await fetch(`${BASE}/api/v1/diagram/json`, {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    projectName: "mi-proyecto",
    files: [
      { path: "Curso.java", content: "public class Curso { private Profesor p; }" },
      { path: "Profesor.java", content: "public class Profesor {}" }
    ],
    options: { showExternal: false }
  })
});

if (!response.ok) {
  const problem = await response.json();
  throw new Error(`${problem.code}: ${problem.detail}`);
}

const diagram = await response.json();
console.log(diagram.plantUml);
```

### 10.3 Python 3.9+

```python
import json
import requests

BASE = "https://mi-servicio.up.railway.app"

payload = {
    "projectName": "mi-proyecto",
    "files": [
        {"path": "Curso.java", "content": "public class Curso { private Profesor p; }"},
        {"path": "Profesor.java", "content": "public class Profesor {}"},
    ],
    "options": {"showExternal": False},
}

response = requests.post(f"{BASE}/api/v1/diagram/json", json=payload, timeout=60)
response.raise_for_status()
print(response.json()["plantUml"])
```

Desde ZIP:

```python
with open("proyecto.zip", "rb") as archive:
    response = requests.post(
        f"{BASE}/api/v1/diagram/file",
        files={
            "file": ("proyecto.zip", archive, "application/zip"),
            "request": (None, json.dumps({"projectName": "mi-proyecto"}), "application/json"),
        },
        timeout=120,
    )
response.raise_for_status()
open("mi-proyecto.puml", "wb").write(response.content)
```

> En `requests`, la parte `request` debe declararse con
> `Content-Type: application/json`; si se envía como campo de formulario
> normal, el servidor devuelve `400 MALFORMED_BODY`.

---

## 11. Despliegue en Railway

El repositorio incluye:

- `Dockerfile` multi-stage (Maven + JRE 17, usuario no root).
- `railway.json` con builder Dockerfile y healthcheck `/api/v1/health`.
- `.dockerignore`.
- `pom.xml` raíz que agrega `proyect-parser-core` y `proyect-parser-server`.

Pasos:

1. Sube el repositorio a GitHub.
2. En Railway: **New Project → Deploy from GitHub repo**.
3. Railway detecta `railway.json` y construye con el `Dockerfile`.
4. En **Settings → Networking**, genera un dominio público.
5. Verifica `GET https://<servicio>.up.railway.app/api/v1/health`.
6. Opcional: define variables de entorno (`CORS_ALLOWED_ORIGINS`,
   `PARSER_MAX_*`, etc.). Railway inyecta `PORT` automáticamente.

También puedes desplegar con la CLI:

```bash
railway login
railway init
railway up
```

Prueba local con Docker:

```bash
docker build -t proyect-parser-api .
docker run --rm -p 8080:8080 -e PORT=8080 proyect-parser-api
```

El contenedor escucha en `$PORT` (8080 por defecto). El healthcheck de Railway
debe apuntar a `/api/v1/health`.

---

## 12. Seguridad

- No hay autenticación ni autorización integradas. Si el servicio se expone
  públicamente, colócalo detrás de un proxy/API gateway o usa las redes
  privadas de Railway.
- El código subido **no se ejecuta**: solo se parsea con JavaParser.
- Solo se escriben archivos `.java`; las rutas se normalizan y se valida que
  queden dentro del workspace (protección contra *zip slip*).
- Se aplican límites de cantidad y tamaño para evitar *zip bombs*.
- Los workspaces temporales se eliminan al finalizar cada petición.
- CORS es configurable con `CORS_ALLOWED_ORIGINS`; por defecto permite `*`.

---

## 13. Notas y limitaciones

- El análisis es estático: no se resuelven dependencias externas ni se
  compila el proyecto.
- Solo se procesan archivos `.java`; otros archivos del ZIP se ignoran.
- Los tipos de la JDK y otras librerías se representan como cajas
  `@external` cuando se referencian desde atributos.
- Un archivo inválido no hace fallar la petición; revisa `failedFiles` y
  `failureReasons`.
- El tamaño de la respuesta depende del proyecto; proyectos muy grandes
  pueden devolver JSON extenso. Usa `filter` para reducir el modelo.
- La API no persiste resultados: cada petición es independiente.
