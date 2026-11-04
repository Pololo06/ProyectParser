package com.proyectparser.server.api;

import com.proyectparser.server.config.ApiProperties;
import com.proyectparser.server.dto.AnalysisResponse;
import com.proyectparser.server.dto.DiagramRequest;
import com.proyectparser.server.dto.DiagramResponse;
import com.proyectparser.server.dto.JsonAnalysisRequest;
import com.proyectparser.server.dto.SourceFileRequest;
import com.proyectparser.server.exception.InvalidSourceException;
import com.proyectparser.server.mapper.ProjectMapper;
import com.proyectparser.server.service.AnalysisOutcome;
import com.proyectparser.server.service.DiagramOutcome;
import com.proyectparser.server.service.ProjectAnalysisService;
import com.proyectparser.server.service.SourceWorkspace;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Analysis and diagram endpoints.
 *
 * <p>Two input families are offered:</p>
 * <ul>
 *   <li>{@code multipart/form-data}: a {@code .zip} archive (part {@code file}),
 *       loose {@code .java} files (part {@code files}) and an optional JSON
 *       {@code request} part with filters/options.</li>
 *   <li>{@code application/json}: a {@code files} array with path and content,
 *       which is the easiest format for another application.</li>
 * </ul>
 */
@RestController
@RequestMapping("${proyectparser.api.base-path:/api/v1}")
public class ProjectController {

    private static final MediaType PLANTUML_MEDIA_TYPE =
            new MediaType("text", "plain", StandardCharsets.UTF_8);

    private final ProjectAnalysisService analysisService;
    private final ProjectMapper mapper;
    private final ApiProperties properties;

    public ProjectController(ProjectAnalysisService analysisService,
                             ProjectMapper mapper,
                             ApiProperties properties) {
        this.analysisService = analysisService;
        this.mapper = mapper;
        this.properties = properties;
    }

    // ------------------------------------------------------------------
    // Analysis (full model)
    // ------------------------------------------------------------------

    @PostMapping(value = "/analysis", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnalysisResponse analyzeMultipart(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart(value = "request", required = false) DiagramRequest request) throws IOException {
        return withMultipart(file, files, request, (workspace, effective) -> {
            AnalysisOutcome outcome = analysisService.analyze(workspace.sourceRoot(), effective);
            return mapper.toAnalysis(workspace.projectName(), outcome);
        });
    }

    @PostMapping(value = "/analysis/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AnalysisResponse analyzeJson(@RequestBody JsonAnalysisRequest body) throws IOException {
        return withJson(body, (workspace, effective) -> {
            AnalysisOutcome outcome = analysisService.analyze(workspace.sourceRoot(), effective);
            return mapper.toAnalysis(workspace.projectName(), outcome);
        });
    }

    // ------------------------------------------------------------------
    // Diagram (PlantUML as JSON)
    // ------------------------------------------------------------------

    @PostMapping(value = "/diagram", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DiagramResponse diagramMultipart(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart(value = "request", required = false) DiagramRequest request) throws IOException {
        return withMultipart(file, files, request, (workspace, effective) -> {
            DiagramOutcome outcome = analysisService.diagram(workspace.sourceRoot(), effective);
            return mapper.toDiagram(workspace.projectName(), outcome);
        });
    }

    @PostMapping(value = "/diagram/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public DiagramResponse diagramJson(@RequestBody JsonAnalysisRequest body) throws IOException {
        return withJson(body, (workspace, effective) -> {
            DiagramOutcome outcome = analysisService.diagram(workspace.sourceRoot(), effective);
            return mapper.toDiagram(workspace.projectName(), outcome);
        });
    }

    // ------------------------------------------------------------------
    // Diagram as downloadable .puml file
    // ------------------------------------------------------------------

    @PostMapping(value = "/diagram/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> diagramFileMultipart(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart(value = "request", required = false) DiagramRequest request) throws IOException {
        return withMultipart(file, files, request, (workspace, effective) -> {
            DiagramOutcome outcome = analysisService.diagram(workspace.sourceRoot(), effective);
            return pumlFile(workspace.projectName(), outcome.plantUml());
        });
    }

    @PostMapping(value = "/diagram/file/json", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> diagramFileJson(@RequestBody JsonAnalysisRequest body) throws IOException {
        return withJson(body, (workspace, effective) -> {
            DiagramOutcome outcome = analysisService.diagram(workspace.sourceRoot(), effective);
            return pumlFile(workspace.projectName(), outcome.plantUml());
        });
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private <T> T withMultipart(MultipartFile file, List<MultipartFile> files, DiagramRequest request,
                                WorkspaceAction<T> action) throws IOException {
        DiagramRequest effective = request == null ? DiagramRequest.empty() : request;
        String projectName = projectNameOf(effective, file, files);
        try (SourceWorkspace workspace = SourceWorkspace.create(properties, projectName)) {
            workspace.addMultipartFile(file);
            if (files != null) {
                for (MultipartFile additional : files) {
                    workspace.addMultipartFile(additional);
                }
            }
            workspace.requireSources();
            return action.apply(workspace, effective);
        }
    }

    private <T> T withJson(JsonAnalysisRequest body, WorkspaceAction<T> action) throws IOException {
        if (body == null) {
            throw new InvalidSourceException("A JSON body with a 'files' array is required");
        }
        DiagramRequest effective = body.toDiagramRequest();
        if (body.files() == null || body.files().isEmpty()) {
            throw new InvalidSourceException("The 'files' array must contain at least one .java source");
        }
        try (SourceWorkspace workspace = SourceWorkspace.create(properties, effective.projectName())) {
            for (SourceFileRequest source : body.files()) {
                workspace.addSourceFile(source);
            }
            workspace.requireSources();
            return action.apply(workspace, effective);
        }
    }

    private String projectNameOf(DiagramRequest request, MultipartFile file, List<MultipartFile> files) {
        if (request != null && request.projectName() != null && !request.projectName().isBlank()) {
            return request.projectName();
        }
        String fromFile = nameFromUpload(file);
        if (fromFile != null) {
            return fromFile;
        }
        if (files != null) {
            for (MultipartFile additional : files) {
                String fromAdditional = nameFromUpload(additional);
                if (fromAdditional != null) {
                    return fromAdditional;
                }
            }
        }
        return "project";
    }

    private String nameFromUpload(MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) {
            return null;
        }
        String name = file.getOriginalFilename().replace('\\', '/');
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        if (name.toLowerCase().endsWith(".zip")) {
            name = name.substring(0, name.length() - 4);
        } else if (name.endsWith(".java")) {
            name = name.substring(0, name.length() - 5);
        }
        return name.isBlank() ? null : name;
    }

    private ResponseEntity<String> pumlFile(String projectName, String plantUml) {
        String fileName = projectName + ".puml";
        return ResponseEntity.ok()
                .contentType(PLANTUML_MEDIA_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(fileName, StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .body(plantUml);
    }

    @FunctionalInterface
    private interface WorkspaceAction<T> {
        T apply(SourceWorkspace workspace, DiagramRequest request) throws IOException;
    }
}
