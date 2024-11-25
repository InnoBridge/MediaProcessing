# MediaProcessing

A Spring Boot application for processing media files, including video-to-text conversion using Whisper.

## Features

- Convert video files to text using Whisper speech recognition
- RESTful API endpoints for media processing
- Swagger UI for API documentation

## Requirements

- Java 22
- Maven
- Whisper model file (configured in application.properties)

## Building and Running

1. Clone the repository
2. Configure the Whisper model path in `application.properties`
3. Build the project:
   ```bash
   mvn clean install
   ```
4. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## API Documentation

Access the Swagger UI at: `http://localhost:8080/swagger-ui.html`
