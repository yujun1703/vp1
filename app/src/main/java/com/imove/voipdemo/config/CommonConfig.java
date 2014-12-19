package com.imove.voipdemo.config;

import android.media.AudioFormat;
import android.media.MediaRecorder;


import java.io.FileOutputStream;

public class CommonConfig {
	public static String SERVER_IP_ADDRESS = "172.16.2.60";

	public static final int AUDIO_SERVER_UP_PORT = 8803;
	public static final int AUDIO_SERVER_DOWN_PORT = 8804;

    public static final String FILEPATH="/sdcard/test.mp4";

    public static final String MEMORYFILE="AUDIOFILE";
    public static final int AUDIO_BITRATE=32;




    public static final int audioSource = MediaRecorder.AudioSource.MIC;
    public static final int sampleRateInHz = 44100;

    public static final int channelConfig= AudioFormat.CHANNEL_IN_MONO;
    public static final int audioFormat= AudioFormat.ENCODING_PCM_16BIT;

}
