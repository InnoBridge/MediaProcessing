package io.github.innobridge.mediaprocesing.model;

import static org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_MP3;
import static org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_PCM_S16LE;

public enum MediaType {
    WAV(16000, 1, AV_CODEC_ID_PCM_S16LE, "wav"),    // Speech optimized
    MP3(44100, 2, AV_CODEC_ID_MP3, "mp3", 192000);  // Music optimized

    public final int sampleRate;
    public final int channels;
    public final int codec;
    public final String extension;
    public final Integer bitrate;  // Only for MP3
    
    MediaType(int sampleRate, int channels, int codec, String extension) {
        this(sampleRate, channels, codec, extension, null);
    }
    
    MediaType(int sampleRate, int channels, int codec, String extension, Integer bitrate) {
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.codec = codec;
        this.extension = extension;
        this.bitrate = bitrate;
    }
}
