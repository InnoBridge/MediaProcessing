package io.github.innobridge.mediaprocesing.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import io.github.givimad.whisperjni.WhisperJNI;
import io.github.givimad.whisperjni.WhisperContext;
import io.github.givimad.whisperjni.WhisperFullParams;

import java.io.File;
import java.io.IOException;
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
        // First convert video to audio
        String audioPath = VideoToAudioConverter.convertToMp3(videoFile, outputDir);
        File audioFile = new File(audioPath);

        System.out.println("audioPath" +  audioPath);
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
        }
        return transcribedText.toString();
    }


    public float[] readAudioFileSamples(File audioFile) throws IOException, UnsupportedAudioFileException {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
        AudioFormat baseFormat = audioInputStream.getFormat();
        AudioFormat decodedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false
        );

        AudioInputStream decodedAudioInputStream = AudioSystem.getAudioInputStream(decodedFormat, audioInputStream);

        byte[] audioBytes = decodedAudioInputStream.readAllBytes();
        int sampleSizeInBytes = decodedFormat.getSampleSizeInBits() / 8;
        int sampleCount = audioBytes.length / sampleSizeInBytes;

        float[] audioData = new float[sampleCount];
        for (int i = 0; i < sampleCount; i++) {
            int sampleValue = 0;

            if (decodedFormat.getSampleSizeInBits() == 16) {
                // 16-bit samples (assuming little endian)
                sampleValue = ((audioBytes[i * 2 + 1] & 0xFF) << 8) | (audioBytes[i * 2] & 0xFF);
                if (sampleValue > 32767) {
                    sampleValue -= 65536;
                }
                audioData[i] = sampleValue / 32768f; // Normalize to [-1, 1]
            }
        }

        return audioData;
    }
}