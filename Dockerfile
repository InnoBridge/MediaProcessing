FROM maven:3.9.7-sapmachine-22 AS build
COPY . .
RUN mvn clean package -DskipTests

# Use OpenJDK 22 as base image
FROM openjdk:22-slim
COPY --from=build /target/*-SNAPSHOT.jar  app.jar

# Install required packages
RUN apt-get update && \
    apt-get install -y gpg wget && \
    wget -O /etc/apt/keyrings/debian-archive-keyring.gpg https://deb.debian.org/debian/pool/main/d/debian-archive-keyring/debian-archive-keyring_2023.3+deb12u1_all.deb && \
    apt-get update && \
    apt-get install -y \
    ffmpeg \
    python3 \
    python3-pip \
    python3-venv \
    && rm -rf /var/lib/apt/lists/*

# Create and activate virtual environment
RUN python3 -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# Install huggingface tools in virtual environment
RUN pip3 install --no-cache-dir huggingface-hub

# Set working directory
WORKDIR /app

# Create directories
RUN mkdir -p /src/main/resources/models /app/media/audio

# Download Whisper model
RUN huggingface-cli download ggerganov/whisper.cpp ggml-tiny-q5_1.bin --local-dir src/main/resources/models --local-dir-use-symlinks False

# Copy application files
COPY target/*.jar app.jar

# Environment variable for model path
ENV MODEL_PATH=/app/models/ggml-tiny-q5_1.bin

# Expose port
EXPOSE 8080

# Run Java in background and keep container alive
CMD java -jar app.jar & tail -f /dev/null