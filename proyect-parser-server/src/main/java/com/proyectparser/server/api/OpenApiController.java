package com.proyectparser.server.api;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves the OpenAPI contract with a YAML content type. Without this mapping
 * Spring Boot serves {@code /static/openapi.yaml} as
 * {@code application/octet-stream}, which makes tooling integration awkward.
 */
@RestController
public class OpenApiController {

    @GetMapping(value = {"/openapi.yaml", "/api/v1/openapi.yaml"},
            produces = {"application/yaml", "text/yaml"})
    public Resource openApi() {
        return new ClassPathResource("static/openapi.yaml");
    }
}
