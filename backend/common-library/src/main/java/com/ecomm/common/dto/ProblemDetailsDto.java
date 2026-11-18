package com.ecomm.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/**
 * Standard RFC 7807 Problem Details representation with distributed tracing correlation ID.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemDetailsDto {

    private String type;
    private String title;
    private int status;
    private String detail;
    private String instance;
    private String correlationId;
    private Instant timestamp;
    private Map<String, String> errors;

    public ProblemDetailsDto() {
        this.timestamp = Instant.now();
    }

    public ProblemDetailsDto(String type, String title, int status, String detail, String instance,
                             String correlationId, Instant timestamp, Map<String, String> errors) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
        this.correlationId = correlationId;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.errors = errors;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String type;
        private String title;
        private int status;
        private String detail;
        private String instance;
        private String correlationId;
        private Instant timestamp = Instant.now();
        private Map<String, String> errors;

        public Builder type(String type) { this.type = type; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder status(int status) { this.status = status; return this; }
        public Builder detail(String detail) { this.detail = detail; return this; }
        public Builder instance(String instance) { this.instance = instance; return this; }
        public Builder correlationId(String correlationId) { this.correlationId = correlationId; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public Builder errors(Map<String, String> errors) { this.errors = errors; return this; }

        public ProblemDetailsDto build() {
            return new ProblemDetailsDto(type, title, status, detail, instance, correlationId, timestamp, errors);
        }
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getInstance() { return instance; }
    public void setInstance(String instance) { this.instance = instance; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public Map<String, String> getErrors() { return errors; }
    public void setErrors(Map<String, String> errors) { this.errors = errors; }
}
