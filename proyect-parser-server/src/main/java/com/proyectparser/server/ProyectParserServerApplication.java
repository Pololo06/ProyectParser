package com.proyectparser.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point of the ProyectParser REST service.
 *
 * <p>The service is a thin HTTP adapter over {@code proyect-parser-core}:
 * every request is extracted into a temporary source tree and analyzed by the
 * library. No parsing, filtering or rendering logic lives here.</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ProyectParserServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProyectParserServerApplication.class, args);
    }
}
