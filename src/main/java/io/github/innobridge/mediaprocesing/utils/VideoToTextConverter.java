package io.github.innobridge.mediaprocesing.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import io.github.givimad.whisperjni.WhisperJNI;
import io.github.givimad.whisperjni.WhisperContext;
import io.github.givimad.whisperjni.WhisperFullParams;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.file.Path;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

@Component
public class VideoToTextConverter {

    private WhisperJNI whisper;
    private Path modelPath;

    @Autowired
    public VideoToTextConverter(WhisperJNI whisper, Path modelPath) {
        this.whisper = whisper;
        this.modelPath = modelPath;
    }

    public String convertToText(MultipartFile videoFile, String outputDir) throws Exception {
        // First convert video to WAV
        String audioPath = VideoToAudioConverter.convertToWav(videoFile, "media/audio");
        File audioFile = new File(audioPath);

        System.out.println("audioPath: " + audioPath);
        StringBuilder transcribedText = new StringBuilder();
        try {
            WhisperContext ctx = whisper.init(modelPath);
            float[] samples = readAudioFileSamples(audioFile);
            WhisperFullParams params = new WhisperFullParams();
            
            // Configure parameters
            params.noSpeechThold = 0.6f;     // Lower threshold to detect more speech
            params.temperature = 0.0f;     // Use deterministic sampling
            params.printSpecial = false;     // Don't print special tokens
            params.printProgress = true;     // Show progress
            params.suppressBlank = true;     // Remove blank outputs
            params.singleSegment = true;     // Process as single segment to avoid repetition
            
            System.out.println("Starting transcription with " + samples.length + " samples");
            int result = whisper.full(ctx, params, samples, samples.length);

            if (result != 0) {
                throw new RuntimeException("Transcription failed with code " + result);
            }

            int numSegments = whisper.fullNSegments(ctx);
            System.out.println("numSegments: " + numSegments);
            for (int i = 0; i < numSegments; i++) {
                String segmentText = whisper.fullGetSegmentText(ctx, i);
                System.out.println(segmentText);
                transcribedText.append(segmentText).append(" ");
            }
            ctx.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Clean up the audio file after transcription
            if (audioFile != null && audioFile.exists()) {
                audioFile.delete();
            }
        }
        return transcribedText.toString();
    }

    private float[] readAudioFileSamples(File audioFile) throws IOException, UnsupportedAudioFileException {
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile)) {
            // Read all bytes
            byte[] audioData = audioInputStream.readAllBytes();
            
            // Convert bytes to shorts (16-bit samples)
            ShortBuffer shortBuffer = ByteBuffer.wrap(audioData)
                                              .order(ByteOrder.LITTLE_ENDIAN)
                                              .asShortBuffer();
            short[] shortSamples = new short[shortBuffer.remaining()];
            shortBuffer.get(shortSamples);
            
            // Convert shorts to normalized floats
            float[] samples = new float[shortSamples.length];
            for (int i = 0; i < shortSamples.length; i++) {
                samples[i] = shortSamples[i] / 32768.0f;
            }
            
            return samples;
        }
    }
}