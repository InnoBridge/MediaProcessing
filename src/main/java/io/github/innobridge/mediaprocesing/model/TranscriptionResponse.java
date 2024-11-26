package io.github.innobridge.mediaprocesing.model;

public class TranscriptionResponse {
    private String status;
    private String text;
    private String message;
    private Long processingTimeMs;

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
}
