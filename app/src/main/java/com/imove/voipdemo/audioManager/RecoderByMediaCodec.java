package com.imove.voipdemo.audioManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * Created by zhangyun on 14/12/18.
 */
public class RecoderByMediaCodec {

    private MediaCodec mediaCodec;
    private BufferedOutputStream outputStream;
    private String mediaType = "audio/mp4a-latm";


    public RecoderByMediaCodec(FileDescriptor fileDescriptor) {

       // File f = new File(Environment.getExternalStorageDirectory(), "Download/audio_encoded.aac");

        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(fileDescriptor));

            Log.d("AudioEncoder", "outputStream initialized");
        } catch (Exception e) {
            e.printStackTrace();
        }

        mediaCodec = MediaCodec.createEncoderByType(mediaType);
        final int kSampleRates[] = {8000, 11025, 22050, 44100, 48000};
        final int kBitRates[] = {64000, 128000};
        MediaFormat mediaFormat = MediaFormat.createAudioFormat(mediaType, kSampleRates[3], 1);
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectHE);
        Log.d("AudioEncoder", "aaaa");
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, kBitRates[0]);
        Log.d("AudioEncoder", "bbbb");
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        Log.d("AudioEncoder", "cccc");
        mediaCodec.start();
        Log.d("AudioEncoder", "dddd");
    }


    public void startRecord()
    {
       // audioRecord.read(Data, 0, Data.length);
      //  audioEncoder.offerEncoder(Data);

        new Thread() {
            public void run() {
                int audioSource = MediaRecorder.AudioSource.MIC;
                int sampleRateInHz = 44100;
                int channelConfig = AudioFormat.CHANNEL_IN_MONO;
                int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
                int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);

                AudioRecord audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
                audioRecord.startRecording();

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

    public void close() {
        try {
            mediaCodec.stop();
            mediaCodec.release();
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // called AudioRecord's read
    public synchronized void offerEncoder(byte[] input,int length) {
        Log.d("AudioEncoder", length + " is coming");

        try {
            ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
            ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
            int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();

                inputBuffer.put(input);


                mediaCodec.queueInputBuffer(inputBufferIndex, 0, length, 0, 0);
            }

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);


//Without ADTS header
            while (outputBufferIndex >= 0) {
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);
                outputStream.write(outData, 0, outData.length);
                Log.e("AudioEncoder", outData.length + " bytes written");

                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);

            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

}
