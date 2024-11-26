package io.github.innobridge.mediaprocesing.configuration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.givimad.whisperjni.WhisperJNI;
import io.github.innobridge.mediaprocesing.utils.VideoToTextConverter;

@Configuration
public class WhisperConfig {

    @Bean
    public WhisperJNI whisper() throws IOException {
        WhisperJNI.loadLibrary();
        WhisperJNI.setLibraryLogger(null);
        WhisperJNI whisperJNI = new WhisperJNI();
        return whisperJNI;
    }

    @Bean
    public VideoToTextConverter videoToTextConverter(
        WhisperJNI whisper, 
        @Value("${whisper.model.path}") String modelPath) {
        
        return new VideoToTextConverter(whisper, Path.of(modelPath));
    } 
}