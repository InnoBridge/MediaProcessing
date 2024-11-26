package io.github.innobridge.mediaprocesing.utils;

import org.bytedeco.javacv.*;
import org.springframework.web.multipart.MultipartFile;

import io.github.innobridge.mediaprocesing.model.MediaType;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import static org.bytedeco.ffmpeg.global.avcodec.*;

public class VideoToAudioConverter {

    public static String convertToWav(MultipartFile videoFile, String outputDir) throws Exception {
        File wavFile = convertToAudioFile(videoFile, outputDir, MediaType.WAV);
        return wavFile.getAbsolutePath();
    }

    public static File convertToWavFile(MultipartFile videoFile, String outputDir) throws Exception {
        return convertToAudioFile(videoFile, outputDir, MediaType.WAV);
    }

    public static File convertToMp3File(MultipartFile videoFile, String outputDir) throws Exception {
        return convertToAudioFile(videoFile, outputDir, MediaType.MP3);
    }

    private static File convertToAudioFile(MultipartFile videoFile, String outputDir, 
                                         MediaType format) throws Exception {
        // Validate output directory
        File directory = new File(outputDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        if (!directory.canWrite()) {
            throw new IllegalArgumentException("Cannot write to output directory: " + outputDir);
        }

        // Create unique filename
        String originalFilename = videoFile.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Create paths
        Path videoPath = Paths.get(directory.getAbsolutePath(), uniqueFilename);
        File outputFile = Paths.get(directory.getAbsolutePath(), 
                                  uniqueFilename.substring(0, uniqueFilename.lastIndexOf('.')) + 
                                  "_audio." + format.extension)
                              .toFile();
        
        // Save uploaded file
        File inputFile = videoPath.toFile();
        videoFile.transferTo(inputFile);
        
        try {
            return convertVideo(inputFile, outputFile, format);
        } finally {
            // Clean up the input video file
            inputFile.delete();
        }
    }

    private static File convertVideo(File inputFile, File outputFile, MediaType format) throws Exception {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
        grabber.start();

        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, format.channels);
        
        try {
            // Configure for output format
            recorder.setFormat(format.extension);
            recorder.setSampleRate(format.sampleRate);
            recorder.setAudioChannels(format.channels);
            recorder.setAudioCodec(format.codec);
            if (format.bitrate != null) {
                recorder.setAudioBitrate(format.bitrate);
            }
            recorder.start();

            // Process frames
            Frame frame;
            while ((frame = grabber.grab()) != null) {
                if (frame.samples != null) {
                    recorder.record(frame);
                }
            }

            return outputFile;
        } finally {
            // Ensure resources are properly closed
            try {
                if (recorder != null) {
                    recorder.stop();
                    recorder.release();
                }
            } finally {
                if (grabber != null) {
                    grabber.stop();
                    grabber.release();
                }
            }
        }
    }
}
