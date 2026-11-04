# proyect-parser-server

REST service for `proyect-parser-core`. It exposes the library's analysis and
PlantUML generation through HTTP so another application can consume it.

- Full integration contract: [`../docs/api.md`](../docs/api.md)
- OpenAPI 3.0 spec: `src/main/resources/static/openapi.yaml` (served at `/openapi.yaml`)
- Healthcheck: `GET /api/v1/health`
- Feature index: `GET /api/v1`

## Local build and run

From the repository root:

```bash
mvn -pl proyect-parser-server -am package
java -jar proyect-parser-server/target/proyect-parser-server-1.0.0.jar
```

The service listens on `PORT` (default `8080`).

```bash
curl http://localhost:8080/api/v1/health
```

Run tests (core + server):

```bash
mvn -pl proyect-parser-server -am test
```

## Quick example

```bash
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

## Docker

```bash
docker build -t proyect-parser-api .
docker run --rm -p 8080:8080 -e PORT=8080 proyect-parser-api
```

## Railway

The repository root contains `railway.json` and `Dockerfile`. Railway builds the
image with Maven, runs the fat JAR as a non-root user and checks
`/api/v1/health`.

1. Push the repository to GitHub.
2. Create a Railway project from the repository.
3. Generate a public domain under **Settings → Networking**.
4. Optionally set `CORS_ALLOWED_ORIGINS` and the `PARSER_MAX_*` limits.

See [`../docs/api.md`](../docs/api.md#11-despliegue-en-railway) for the complete
deployment notes.

## Package layout

```text
com.proyectparser.server
├── ProyectParserServerApplication   Spring Boot entry point
├── api                              Controllers and RFC 7807 error handling
├── config                           API properties and CORS
├── dto                              Request/response records (Jackson)
├── mapper                           Domain model -> HTTP DTOs
└── service                          Workspace extraction + core orchestration
```

The server never reimplements parser logic: `ProjectAnalysisService` creates a
new `DiagramFacade` per request (the core analyzer keeps last-run statistics as
instance state) and the library does the analysis, filtering and rendering.
