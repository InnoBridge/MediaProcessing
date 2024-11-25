package io.github.innobridge.mediaprocesing.utils;

import org.bytedeco.javacv.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class VideoToAudioConverter {
    
    public static String convertToMp3(MultipartFile videoFile, String outputDir) throws Exception {
        // Validate output directory
        File directory = new File(outputDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        if (!directory.canWrite()) {
            throw new IllegalArgumentException("Cannot write to output directory: " + outputDir);
        }

        // Create unique filename for the video
        String originalFilename = videoFile.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Create paths for input and output files
        Path videoPath = Paths.get(directory.getAbsolutePath(), uniqueFilename);
        String outputPath = videoPath.toString().substring(0, videoPath.toString().lastIndexOf('.')) + "_audio.mp3";
        
        // Save uploaded file
        File inputFile = videoPath.toFile();
        videoFile.transferTo(inputFile);
        
        try {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
            grabber.start();

            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputPath, 
                                                                 grabber.getAudioChannels());
            
            try {
                // Configure for MP3 output
                recorder.setFormat("mp3");
                recorder.setSampleRate(16000);  // Set to 16kHz for Whisper
                recorder.setAudioChannels(1);   // Use mono audio
                recorder.setAudioCodec(org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_MP3);
                recorder.setAudioBitrate(128000); // Set bitrate to 128kbps
                recorder.start();

                Frame frame;
                while ((frame = grabber.grab()) != null) {
                    if (frame.samples != null) {
                        recorder.record(frame);
                    }
                }

                return outputPath;
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
        } finally {
            // Clean up the input video file
            inputFile.delete();
        }
    }
}
