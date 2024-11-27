# Use OpenJDK 22 as base image
FROM openjdk:22-slim AS build
COPY . /app
WORKDIR /app
RUN ./mvnw clean package -DskipTests

FROM openjdk:22-slim
# Copy the pre-built JAR
COPY --from=build /app/target/*-SNAPSHOT.jar  /app/app.jar

# Install required packages
RUN apt-get update && \
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

# Create directories
RUN mkdir -p /app/src/main/resources/models /app/media/audio

# Download Whisper model
RUN huggingface-cli download ggerganov/whisper.cpp ggml-base-q5_1.bin --local-dir /app/src/main/resources/models --local-dir-use-symlinks False

# Environment variable for model path
ENV MODEL_PATH=/app/models/ggml-tiny-q5_1.bin

WORKDIR /app

# Expose port
EXPOSE 8080

ENTRYPOINT ["java", "-Dhttps.protocols=TLSv1.2,TLSv1.3", "-Djdk.tls.client.protocols=TLSv1.2,TLSv1.3", "-Djavax.net.debug=ssl,handshake", "-jar", "app.jar"]