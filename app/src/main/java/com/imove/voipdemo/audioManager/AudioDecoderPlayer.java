package com.imove.voipdemo.audioManager;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;

/**
 * Created by zhangyun on 14/12/18.
 */
public class AudioDecoderPlayer {
    MediaCodec decoder=null;
    AudioTrack player;
    //InputStream inputStream;
    PipedInputStream inputStream;
    public AudioDecoderPlayer(PipedInputStream is)
    {
        Log.d("AudioDecoderPlayer", "aaaa");
        inputStream=is;

        decoder = MediaCodec.createDecoderByType("audio/mp4a-latm");
        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");//aac raw
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 64 * 1024);//AAC-HE 64kbps
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectHE);
        decoder.configure(format, null, null, 0);
        decoder.start();
    }

    private boolean setPlayer(int rate)
    {

        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

        int bufferSizePlayer = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO, audioFormat);

        Log.d("====buffer Size player ", String.valueOf(bufferSizePlayer));

        AudioTrack player= new AudioTrack(AudioManager.STREAM_MUSIC, rate, AudioFormat.CHANNEL_OUT_MONO, audioFormat, bufferSizePlayer, AudioTrack.MODE_STREAM);

        if (player.getState() == AudioTrack.STATE_INITIALIZED)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public void StartPlay() {
        new Thread() {
            public void run() {
                byte[] data = new byte[1000];
                ByteBuffer[] inputBuffers;
                ByteBuffer[] outputBuffers;
                ByteBuffer inputBuffer;
                ByteBuffer outputBuffer;
                MediaCodec.BufferInfo bufferInfo;
                //  MediaExtractor    extractor = new MediaExtractor();
                int inputBufferIndex;
                int outputBufferIndex;
                byte[] outData;
                try {
                    boolean isPlaying = true;
                    while (isPlaying) {
                        try {

                            //===========
                            inputBuffers = decoder.getInputBuffers();
                            outputBuffers = decoder.getOutputBuffers();
                            inputBufferIndex = decoder.dequeueInputBuffer(-1);
                            if (inputBufferIndex >= 0) {
                                inputBuffer = inputBuffers[inputBufferIndex];
                                inputBuffer.clear();
                                Log.d("AudioDecoder", "wait");
                                inputStream.wait();
                                Log.d("AudioDecoder", "read  bytes ，avalable:" + inputStream.available());
                                int len=inputStream.read(data,0,data.length);

                                Log.d("AudioDecoder","after read  bytes ,length:"+len+",avalable:"+inputStream.available());
                                inputBuffer.put(data);

                                decoder.queueInputBuffer(inputBufferIndex, 0, len, 0, 0);
                            }

                            bufferInfo = new MediaCodec.BufferInfo();
                            outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 0);

                            while (outputBufferIndex >= 0) {
                                outputBuffer = outputBuffers[outputBufferIndex];

                                outputBuffer.position(bufferInfo.offset);
                                outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

                                outData = new byte[bufferInfo.size];//pcm
                                outputBuffer.get(outData);

                                Log.d("AudioDecoder", outData.length + " bytes decoded");

                                decoder.releaseOutputBuffer(outputBufferIndex, false);
                                outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 0);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    decoder.stop();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
