package com.proyectparser.server.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectApiIntegrationTest {

    private static final String CURSO = """
            package com.example.model;

            public class Curso {
                private String nombre;
                private Profesor profesor;

                public Curso(String nombre) {
                    this.nombre = nombre;
                }

                public String getNombre() {
                    return nombre;
                }
            }
            """;

    private static final String PROFESOR = """
            package com.example.model;

            public class Profesor {
                private String nombre;
            }
            """;

    private static final String JSON_BODY = """
            {
              "projectName": "demo",
              "files": [
                {"path": "com/example/model/Curso.java", "content": %s},
                {"path": "com/example/model/Profesor.java", "content": %s}
              ],
              "options": {"showGettersSetters": false, "showExternal": false}
            }
            """.formatted(quote(CURSO), quote(PROFESOR));

    private static final String DIAGRAM_BODY = """
            {
              "projectName": "demo",
              "files": [
                {"path": "com/example/model/Curso.java", "content": %s},
                {"path": "com/example/model/Profesor.java", "content": %s}
              ],
              "filter": {"excludePackages": [], "excludeClasses": [],
                         "includePackages": [], "includeClasses": []},
              "options": {"showGettersSetters": false, "showExternal": false, "summary": true}
            }
            """.formatted(quote(CURSO), quote(PROFESOR));

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointIsUp() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("proyect-parser-api"));
    }

    @Test
    void infoEndpointListsResources() throws Exception {
        mockMvc.perform(get("/api/v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endpoints[*].path", hasItems(
                        "/api/v1/analysis", "/api/v1/diagram", "/api/v1/diagram/file")));
    }

    @Test
    void servesOpenApiContract() throws Exception {
        mockMvc.perform(get("/openapi.yaml"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("yaml")))
                .andExpect(content().string(containsString("openapi: 3.0.3")));
    }

    @Test
    void analyzesJsonSources() throws Exception {
        mockMvc.perform(post("/api/v1/analysis/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectName").value("demo"))
                .andExpect(jsonPath("$.statistics.classes").value(2))
                .andExpect(jsonPath("$.statistics.packages").value(1))
                .andExpect(jsonPath("$.statistics.failedJavaFiles").value(0))
                .andExpect(jsonPath("$.kinds.Class").value(2))
                .andExpect(jsonPath("$.classes[*].name", hasItems("Curso", "Profesor")))
                .andExpect(jsonPath("$.relationships[*].type", hasItem("ASSOCIATION")))
                .andExpect(jsonPath("$.packages[0].displayName").value("com.example.model"));
    }

    @Test
    void generatesDiagramAsJson() throws Exception {
        mockMvc.perform(post("/api/v1/diagram/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DIAGRAM_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectName").value("demo"))
                .andExpect(jsonPath("$.fileName").value("demo.puml"))
                .andExpect(jsonPath("$.plantUml", containsString("@startuml")))
                .andExpect(jsonPath("$.plantUml", containsString("hide members")))
                .andExpect(jsonPath("$.plantUml", containsString("Curso")));
    }

    @Test
    void generatesDiagramAsPumlFile() throws Exception {
        mockMvc.perform(post("/api/v1/diagram/file/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DIAGRAM_BODY))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(header().string("Content-Disposition", containsString("demo.puml")))
                .andExpect(content().string(containsString("@startuml")));
    }

    @Test
    void acceptsZipUpload() throws Exception {
        MockMultipartFile zip = new MockMultipartFile("file", "demo.zip", "application/zip", zipOf(
                "src/com/example/model/Curso.java", CURSO,
                "src/com/example/model/Profesor.java", PROFESOR));

        mockMvc.perform(multipart("/api/v1/analysis").file(zip))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectName").value("demo"))
                .andExpect(jsonPath("$.statistics.classes").value(2));
    }

    @Test
    void acceptsLooseJavaFiles() throws Exception {
        MockMultipartFile curso = new MockMultipartFile("files", "Curso.java",
                "text/x-java-source", CURSO.getBytes(StandardCharsets.UTF_8));
        MockMultipartFile profesor = new MockMultipartFile("files", "Profesor.java",
                "text/x-java-source", PROFESOR.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/diagram").file(curso).file(profesor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectName").value("Curso"))
                .andExpect(jsonPath("$.plantUml", containsString("@startuml")))
                .andExpect(jsonPath("$.statistics.classes").value(2));
    }

    @Test
    void appliesExcludeFilter() throws Exception {
        String body = """
                {
                  "projectName": "filtered",
                  "files": [
                    {"path": "a/Curso.java", "content": %s},
                    {"path": "b/Profesor.java", "content": %s}
                  ],
                  "filter": {"excludePackages": ["com.example.model"], "excludeClasses": [],
                             "includePackages": [], "includeClasses": []}
                }
                """.formatted(quote(CURSO), quote(PROFESOR));

        mockMvc.perform(post("/api/v1/analysis/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statistics.classes").value(0));
    }

    @Test
    void rejectsJsonWithoutFiles() throws Exception {
        mockMvc.perform(post("/api/v1/analysis/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"projectName\":\"empty\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SOURCE"));
    }

    @Test
    void rejectsPathTraversal() throws Exception {
        String body = """
                {
                  "projectName": "evil",
                  "files": [{"path": "../Evil.java", "content": "class Evil {}"}]
                }
                """;

        mockMvc.perform(post("/api/v1/analysis/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SOURCE"));
    }

    @Test
    void rejectsZipSlip() throws Exception {
        MockMultipartFile zip = new MockMultipartFile("file", "evil.zip", "application/zip", zipOf(
                "../../Evil.java", "class Evil {}"));

        mockMvc.perform(multipart("/api/v1/analysis").file(zip))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SOURCE"));
    }

    @Test
    void ignoresNonJavaZipEntries() throws Exception {
        MockMultipartFile zip = new MockMultipartFile("file", "demo.zip", "application/zip", zipOf(
                "pom.xml", "<project/>",
                "README.md", "# demo",
                "src/Curso.java", CURSO));

        mockMvc.perform(multipart("/api/v1/analysis").file(zip))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statistics.totalJavaFiles").value(1))
                .andExpect(jsonPath("$.statistics.classes").value(1));
    }

    @Test
    void rejectsInvalidLineType() throws Exception {
        String body = """
                {
                  "projectName": "demo",
                  "files": [{"path": "Curso.java", "content": %s}],
                  "options": {"lineType": "curved"}
                }
                """.formatted(quote(CURSO));

        mockMvc.perform(post("/api/v1/diagram/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    private static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "") + "\"";
    }

    private static byte[] zipOf(String... entries) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
            for (int i = 0; i < entries.length; i += 2) {
                zip.putNextEntry(new ZipEntry(entries[i]));
                zip.write(entries[i + 1].getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return out.toByteArray();
    }
}
