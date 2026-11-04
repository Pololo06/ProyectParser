# ---- Build stage ----
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# The build needs both modules: the server depends on the core library.
COPY pom.xml ./
COPY proyect-parser-core/pom.xml ./proyect-parser-core/pom.xml
COPY proyect-parser-server/pom.xml ./proyect-parser-server/pom.xml
COPY proyect-parser-core/src ./proyect-parser-core/src
COPY proyect-parser-core/test ./proyect-parser-core/test
COPY proyect-parser-server/src ./proyect-parser-server/src

RUN mvn -B -pl proyect-parser-server -am package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

COPY --from=build /workspace/proyect-parser-server/target/proyect-parser-server-1.0.0.jar /app/app.jar

USER app
EXPOSE 8080

# Railway injects PORT; Spring Boot reads it from application.yml.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-Djava.awt.headless=true", "-jar", "/app/app.jar"]
