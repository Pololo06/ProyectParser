package com.proyectparser.server.api;

import com.proyectparser.server.exception.InvalidSourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;

/**
 * RFC 7807 ({@code application/problem+json}) error responses with a stable
 * {@code code} property so clients do not need to parse free-form messages.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(InvalidSourceException.class)
    public ResponseEntity<ProblemDetail> invalidSource(InvalidSourceException exception) {
        return problem(HttpStatus.BAD_REQUEST, "INVALID_SOURCE", "Invalid source input", exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> illegalArgument(IllegalArgumentException exception) {
        return problem(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", "Invalid request", exception.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> uploadTooLarge(MaxUploadSizeExceededException exception) {
        return problem(HttpStatus.PAYLOAD_TOO_LARGE, "UPLOAD_TOO_LARGE", "Upload too large",
                "The multipart request exceeds the configured upload size");
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ProblemDetail> multipart(MultipartException exception) {
        return problem(HttpStatus.BAD_REQUEST, "INVALID_MULTIPART", "Invalid multipart request",
                "The request could not be read as multipart/form-data");
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ProblemDetail> missingPart(MissingServletRequestPartException exception) {
        return problem(HttpStatus.BAD_REQUEST, "MISSING_PART", "Missing multipart part",
                "Missing multipart part: " + exception.getRequestPartName());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> missingParameter(MissingServletRequestParameterException exception) {
        return problem(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", "Missing query parameter",
                "Missing query parameter: " + exception.getParameterName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> unreadableBody(HttpMessageNotReadableException exception) {
        return problem(HttpStatus.BAD_REQUEST, "MALFORMED_BODY", "Malformed request body",
                "The JSON body or the 'request' part is not valid");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> unsupportedMediaType(HttpMediaTypeNotSupportedException exception) {
        return problem(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE",
                "Unsupported media type",
                "Supported types: application/json and multipart/form-data");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> unsupportedMethod(HttpRequestMethodNotSupportedException exception) {
        return problem(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "Method not allowed",
                "HTTP method not supported for this endpoint");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> notFound(NoResourceFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "NOT_FOUND", "Not found",
                "No endpoint matches the requested path");
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ProblemDetail> ioError(IOException exception) {
        log.error("I/O error while processing a request", exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_IO_ERROR", "Internal error",
                "The source tree could not be processed");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> unexpected(Exception exception) {
        log.error("Unexpected error while processing a request", exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Internal error",
                "An unexpected error occurred while processing the request");
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String title, String detail) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail == null ? title : detail);
        body.setTitle(title);
        body.setProperty("code", code);
        return ResponseEntity.status(status)
                .contentType(org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }
}
