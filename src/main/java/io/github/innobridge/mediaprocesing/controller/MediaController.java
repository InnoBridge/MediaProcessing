package io.github.innobridge.mediaprocesing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.github.innobridge.mediaprocesing.model.AudioConversionResponse;
import io.github.innobridge.mediaprocesing.model.TranscriptionResponse;
import io.github.innobridge.mediaprocesing.utils.VideoToAudioConverter;
import io.github.innobridge.mediaprocesing.utils.VideoToTextConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.File;
import java.nio.file.Files;


@RestController
@RequestMapping("/api/media")
public class MediaController {

    @Autowired
    private VideoToTextConverter videoToTextConverter;

    @Operation(
        summary = "Convert video to audio",
        description = "Upload a video file and convert it to WAV or MP3 audio format",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Audio extracted successfully",
                content = {
                    @Content(mediaType = "audio/wav"),
                    @Content(mediaType = "audio/mpeg")
                }
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid input (empty file or invalid format)",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AudioConversionResponse.class)
                )
            )
        }
    )
    @PostMapping(
        value = "/toaudio",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = {"audio/wav", "audio/mpeg"}
    )
    public ResponseEntity<?> toAudio(
            @Parameter(
                description = "Video file to convert",
                required = true,
                content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestParam("file") MultipartFile file,
            
            @Parameter(
                description = "Output audio format (WAV or MP3)",
                required = false,
                schema = @Schema(implementation = io.github.innobridge.mediaprocesing.model.MediaType.class)
            )
            @RequestParam(value = "type", defaultValue = "WAV") io.github.innobridge.mediaprocesing.model.MediaType type) {
        
        if (file.isEmpty()) {
            AudioConversionResponse response = new AudioConversionResponse();
            response.setStatus("error");
            response.setMessage("Please select a video file to upload");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            File audioFile = VideoToAudioConverter.convertToAudioFile(file, "media/audio", type);
            String contentType = "audio/" + type.extension;
            String fileExtension = type.extension;
            
            // Read the audio file into a byte array
            byte[] audioData = Files.readAllBytes(audioFile.toPath());
            
            // Clean up the audio file after reading
            audioFile.delete();
            
            // Return the audio file as the response
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(audioData.length)
                    .header("Content-Disposition", "attachment; filename=\"" + 
                            file.getOriginalFilename().replaceFirst("[.][^.]+$", "." + fileExtension) + "\"")
                    .body(audioData);
            
        } catch (IllegalArgumentException e) {
            AudioConversionResponse response = new AudioConversionResponse();
            response.setStatus("error");
            response.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            AudioConversionResponse response = new AudioConversionResponse();
            response.setStatus("error");
            response.setMessage("Error extracting audio: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @Operation(
        summary = "Convert video to text",
        description = "Upload a video file and convert its audio to text using local Whisper model",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Video transcribed successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TranscriptionResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid input (empty file, invalid format, or invalid directory)",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TranscriptionResponse.class)
                )
            )
        }
    )
    @PostMapping(
        value = "/totext",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TranscriptionResponse> toText(
            @Parameter(
                description = "Video file to transcribe",
                required = true,
                content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestParam("file") MultipartFile file) {
        
        TranscriptionResponse response = new TranscriptionResponse();
        long startTime = System.currentTimeMillis();
        
        if (file.isEmpty()) {
            response.setStatus("error");
            response.setMessage("Please select a video file to upload");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String transcription = videoToTextConverter.convertToText(file, "media/audio");
            
            response.setStatus("success");
            response.setText(transcription);
            response.setMessage("Video transcribed successfully");
            response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.setStatus("error");
            response.setMessage(e.getMessage());
            response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.setStatus("error");
            response.setMessage("Error transcribing video: " + e.getMessage());
            response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
