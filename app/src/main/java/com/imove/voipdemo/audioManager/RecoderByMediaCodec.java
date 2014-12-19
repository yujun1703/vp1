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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by zhangyun on 14/12/18.
 */
public class RecoderByMediaCodec {
    private MediaCodec mediaCodec;
    private BufferedOutputStream outputStream;
    private String mediaType = "audio/mp4a-latm";
    private long  timestamps=0;
    private long  starttimestamps=0;

    private File file;
    private FileOutputStream fos;

    ByteBuffer[] inputBuffers ;
    ByteBuffer[] outputBuffers;

    public RecoderByMediaCodec(FileDescriptor fileDescriptor) {
       file = new File("/sdcard/", "audio_encoded.amr");

        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(fileDescriptor));
            fos=new FileOutputStream(file);

            Log.d("AudioEncoder", "outputStream initialized");
        } catch (Exception e) {
            e.printStackTrace();
        }

        int sampleRateInHz = 44100;
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)*2;


        mediaCodec = MediaCodec.createEncoderByType(mediaType);
        MediaFormat mediaFormat = new MediaFormat();

        mediaFormat.setString(MediaFormat.KEY_MIME, mediaType);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 16*1024);
        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
        mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100);
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, bufferSizeInBytes);
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();

        inputBuffers = mediaCodec.getInputBuffers();
        outputBuffers = mediaCodec.getOutputBuffers();
    }
/*
    public void prepare(FileDescriptor fileDescriptor)
    {
        file = new File("/sdcard/", "audio_encoded.amr");

        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(fileDescriptor));
            fos=new FileOutputStream(file);

            Log.d("AudioEncoder", "outputStream initialized");
        } catch (Exception e) {
            e.printStackTrace();
        }

        int sampleRateInHz = 44100;
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)*2;

        mediaCodec = MediaCodec.createEncoderByType(mediaType);
        MediaFormat mediaFormat = new MediaFormat();

        mediaFormat.setString(MediaFormat.KEY_MIME, mediaType);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 16*1024);
        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
        mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100);
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, bufferSizeInBytes);
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }
*/

    public void startRecord()
    {
        new Thread() {
            public void run() {
                int audioSource = MediaRecorder.AudioSource.MIC;
                int sampleRateInHz = 44100;
                int channelConfig = AudioFormat.CHANNEL_IN_MONO;
                int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
                int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)*2;

                AudioRecord audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);

                audioRecord.startRecording();
                starttimestamps=System.nanoTime() ;
                //audioRecord.setRecordPositionUpdateListener();

                Log.d("AudioEncoder","new byte:"+ bufferSizeInBytes );

                byte[] Data = new byte[bufferSizeInBytes];
                int len;
                while (true) {
                    len=audioRecord.read(Data, 0, Data.length);
                    offerEncoder(Data,len);
                }
            }
        }.start();
    }

    public void stopRecorder() {
        try {
            mediaCodec.stop();
            mediaCodec.release();
            outputStream.flush();
          //  outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // called AudioRecord's read
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

//Without ADTS header
            if(outputBufferIndex >= 0) {
                while (outputBufferIndex >= 0) {
                    ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                    byte[] outData = new byte[bufferInfo.size];
                    outputBuffer.get(outData);
                    // outputStream.write(outData, 0, outData.length);
                    //  outputStream.flush();

                    fos.write(outData, 0, outData.length);
                    //fos.flush();
                    Log.e("AudioEncoder", outData.length + " bytes written");

                    mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                    outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);

                }
            }else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                Log.e("AudioEncoder", "INFO_OUTPUT_BUFFERS_CHANGED");
            }
             else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                Log.e("AudioEncoder", "INFO_OUTPUT_FORMAT_CHANGED");
             }

           // outputStream.flush();
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

}
