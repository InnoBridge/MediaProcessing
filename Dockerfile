# Use OpenJDK 22 as base image
FROM openjdk:22-slim

# Install required packages
RUN apt-get update && apt-get install -y \
    ffmpeg \
    python3 \
    python3-pip \
    && rm -rf /var/lib/apt/lists/*

# Install huggingface-cli
RUN pip3 install huggingface-cli

# Set working directory
WORKDIR /app

# Download Whisper model
RUN huggingface-cli download ggerganov/whisper.cpp ggml-tiny-q5_1.bin --local-dir /app/models --local-dir-use-symlinks False

# Copy application files
COPY target/*.jar app.jar
COPY models/ /app/models/

# Create directory for media files
RUN mkdir -p media/audio

# Environment variable for model path
ENV MODEL_PATH=/app/models/ggml-tiny-q5_1.bin

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]