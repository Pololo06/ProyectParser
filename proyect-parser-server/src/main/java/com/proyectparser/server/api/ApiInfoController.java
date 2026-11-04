package com.proyectparser.server.api;

import com.proyectparser.server.config.ApiProperties;
import com.proyectparser.server.dto.ApiInfoResponse;
import com.proyectparser.server.dto.HealthResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/** Service discovery endpoints: healthcheck and machine-readable API index. */
@RestController
@RequestMapping("${proyectparser.api.base-path:/api/v1}")
public class ApiInfoController {

    private final ApiProperties properties;

    public ApiInfoController(ApiProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("UP", "proyect-parser-api", properties.getVersion(), Instant.now());
    }

    @GetMapping
    public ApiInfoResponse info() {
        String base = properties.getBasePath();
        return new ApiInfoResponse(
                "proyect-parser-api",
                properties.getVersion(),
                "Static analysis of Java sources and PlantUML class diagram generation",
                List.of(
                        new ApiInfoResponse.EndpointInfo("GET", base + "/health",
                                MediaType.APPLICATION_JSON_VALUE,
                                "Liveness/readiness check"),
                        new ApiInfoResponse.EndpointInfo("GET", base,
                                MediaType.APPLICATION_JSON_VALUE,
                                "This endpoint index"),
                        new ApiInfoResponse.EndpointInfo("GET", "/openapi.yaml",
                                "application/yaml",
                                "OpenAPI 3.0 contract of this service"),
                        new ApiInfoResponse.EndpointInfo("POST", base + "/analysis",
                                MediaType.MULTIPART_FORM_DATA_VALUE,
                                "Analyze a ZIP archive or loose .java files; returns the full model"),
                        new ApiInfoResponse.EndpointInfo("POST", base + "/analysis/json",
                                MediaType.APPLICATION_JSON_VALUE,
                                "Analyze a JSON file list; returns the full model"),
                        new ApiInfoResponse.EndpointInfo("POST", base + "/diagram",
                                MediaType.MULTIPART_FORM_DATA_VALUE,
                                "Generate PlantUML from a ZIP or loose .java files; returns JSON"),
                        new ApiInfoResponse.EndpointInfo("POST", base + "/diagram/json",
                                MediaType.APPLICATION_JSON_VALUE,
                                "Generate PlantUML from a JSON file list; returns JSON"),
                        new ApiInfoResponse.EndpointInfo("POST", base + "/diagram/file",
                                MediaType.MULTIPART_FORM_DATA_VALUE,
                                "Generate PlantUML from a ZIP or loose .java files; returns a .puml file"),
                        new ApiInfoResponse.EndpointInfo("POST", base + "/diagram/file/json",
                                MediaType.APPLICATION_JSON_VALUE,
                                "Generate PlantUML from a JSON file list; returns a .puml file")),
                new ApiInfoResponse.LimitsInfo(
                        properties.getMaxFiles(),
                        properties.getMaxFileBytes(),
                        properties.getMaxTotalBytes()));
    }
}
