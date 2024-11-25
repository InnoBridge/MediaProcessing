package io.github.innobridge.mediaprocesing.model;

public class AudioConversionResponse {
    private String status;
    private String outputPath;
    private String message;
    private Long processingTimeMs;

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOutputPath() { return outputPath; }
    public void setOutputPath(String outputPath) { this.outputPath = outputPath; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
}