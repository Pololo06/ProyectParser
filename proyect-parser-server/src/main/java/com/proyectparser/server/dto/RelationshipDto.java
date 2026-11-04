package com.proyectparser.server.dto;

/**
 * HTTP representation of an inferred relationship.
 *
 * <p>{@code type} is one of {@code EXTENDS}, {@code IMPLEMENTS},
 * {@code ASSOCIATION}, {@code DEPENDENCY} or {@code NESTING}.</p>
 */
public record RelationshipDto(String source, String target, String type) {
}
