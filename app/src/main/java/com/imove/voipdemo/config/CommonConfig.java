package com.imove.voipdemo.config;

import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.util.Log;


import java.io.FileOutputStream;

public class CommonConfig {
	public static String SERVER_IP_ADDRESS = "172.16.2.60";

	public static final int AUDIO_SERVER_UP_PORT = 8803;
	public static final int AUDIO_SERVER_DOWN_PORT = 8804;

    public static final String FILEPATH="/sdcard/test.mp4";

    public static final String MEMORYFILE="AUDIOFILE";
    public static final int AUDIO_BITRATE=32;



    public static final  String mediaType = "audio/mp4a-latm";

    public static final int audioSource = MediaRecorder.AudioSource.MIC;
    public static final int sampleRateInHz = 44100;

    public static final int channelinConfig= AudioFormat.CHANNEL_IN_MONO;
    public static final int channeloutConfig= AudioFormat.CHANNEL_OUT_MONO;
    public static final int audioFormat= AudioFormat.ENCODING_PCM_16BIT;
    public static final int bitrate= 16*1024;

    public static int getChannels(int channelConfig) {
        int channelCount = 0;
        switch (channelConfig) {
            case AudioFormat.CHANNEL_IN_DEFAULT: // AudioFormat.CHANNEL_CONFIGURATION_DEFAULT
            case AudioFormat.CHANNEL_IN_MONO:
                channelCount = 1;
                break;
            case AudioFormat.CHANNEL_IN_STEREO:
            case (AudioFormat.CHANNEL_IN_FRONT | AudioFormat.CHANNEL_IN_BACK):
                channelCount = 2;
                break;
            case AudioFormat.CHANNEL_INVALID:
            default:
                Log.e("aa", "getMinBufferSize(): Invalid channel configuration.");
                return -1;
        }
        return channelCount;
    }

}
