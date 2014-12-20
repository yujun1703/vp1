package com.imove.voipdemo.audioManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.imove.voipdemo.config.CommonConfig;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by zhangyun on 14/12/18.
 */
public class RecoderByMediaCodec {
    private MediaCodec mediaCodec;
    private long  timestamps=0;
    private long  starttimestamps=0;
    ByteBuffer[] inputBuffers ;
    ByteBuffer[] outputBuffers;
    private DataOutputStream dos;
    static int sampleRateInHz = CommonConfig.sampleRateInHz;
    static int channelConfig = CommonConfig.channelinConfig;
    static int audioFormat = CommonConfig.audioFormat;

    static int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)*2;


    static int channelCount=CommonConfig.getChannels(channelConfig);

    boolean isplaying;
    AudioRecord audioRecord ;
    /*
    public RecoderByMediaCodec() {
    }
    */

    public void prepare()
    {
        isplaying=true;
        mediaCodec = MediaCodec.createEncoderByType(CommonConfig.mediaType);
        MediaFormat mediaFormat = new MediaFormat();
        mediaFormat.setString(MediaFormat.KEY_MIME, CommonConfig.mediaType);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, CommonConfig.bitrate);
        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channelCount);
        mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, CommonConfig.sampleRateInHz);
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, bufferSizeInBytes);
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();
        inputBuffers = mediaCodec.getInputBuffers();
        outputBuffers = mediaCodec.getOutputBuffers();
    }

    public void startRecord()
    {
        new Thread() {
            public void run() {
                int audioSource = MediaRecorder.AudioSource.MIC;

                Log.d("AudioEncoder", "CHANNEL_IN_MONO:" + AudioFormat.CHANNEL_IN_MONO+"chanconfig:"+channelConfig);

                audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);

                audioRecord.startRecording();
                starttimestamps=System.nanoTime() ;

                //audioRecord.setRecordPositionUpdateListener(new )



                byte[] Data = new byte[bufferSizeInBytes];
                int len;

                while (isplaying) {
                    len=audioRecord.read(Data, 0, Data.length);
                    if(len>0)
                        offerEncoder(Data,len);
                }


            }
        }.start();
    }

    public void stopRecorder() {
        try {
            isplaying=false;

            mediaCodec.stop();
            mediaCodec.release();
            audioRecord.stop();
            audioRecord.release();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void offerEncoder(byte[] input,int length) {
        Log.d("AudioEncoder", length + " is coming");
        try {
            int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(input);
                timestamps = System.nanoTime() - starttimestamps;
                Log.d("AudioEncoder", "timestamps:" + timestamps);
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, length, timestamps, 0);
            }

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);

            if(outputBufferIndex >= 0) {
                while (outputBufferIndex >= 0)
                {
                    ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                    byte[] outData = new byte[bufferInfo.size];

                    Log.e("AudioEncoder", outData.length + " bytes  will be  written");
                    outputBuffer.get(outData);
                    outputBuffer.clear();
                    ServerSocket.getServerSocketInstance().SendAudioToServer(outData.length, outData);

                    mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                    outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);

                }
            }else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                Log.e("AudioEncoder", "INFO_OUTPUT_BUFFERS_CHANGED");
            }
             else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                Log.e("AudioEncoder", "INFO_OUTPUT_FORMAT_CHANGED");
             }
        } catch (Throwable t) {
            Log.e("AudioEncoder", "can read from media codec");
            t.printStackTrace();
        }

    }

}
