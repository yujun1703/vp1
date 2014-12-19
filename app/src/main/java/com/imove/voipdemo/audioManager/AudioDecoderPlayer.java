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

import java.io.FileInputStream;
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
    //InputStream inputStream;
    PipedInputStream inputStream;

    AudioTrack player;
    public AudioDecoderPlayer(PipedInputStream is)
    {
        Log.d("AudioDecoderPlayer", "aaaa");
        inputStream=is;


        decoder = MediaCodec.createDecoderByType("audio/mp4a-latm");
        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");//aac raw
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 16 * 1024);//AAC-HE 64kbps
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectHE);
        decoder.configure(format, null, null, 0);
        decoder.start();
    }

    private boolean setPlayer()
    {

        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

        int bufferSizePlayer = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_MONO, audioFormat)*2;

        Log.d("====buffer Size player ", String.valueOf(bufferSizePlayer));

        player= new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO, audioFormat, bufferSizePlayer, AudioTrack.MODE_STREAM);

        if (player.getState() == AudioTrack.STATE_INITIALIZED)
        {
            player.play();
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
                byte[] data = new byte[2048];
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
                    int len=0;
                    setPlayer();

                    while (isPlaying) {
                        try {

                            int readlen=0;
                            len=0;
                            while(len<2048)
                            {
                                len+=inputStream.read(data,len,data.length-len);
                                //readlen=inputStream.read(data,0,data.length);
                                //len+=readlen;
                               // Log.d("AudioDecoder", "read avalable:"+inputStream.available()+",len:"+len);
                            }

                            //===========
                            inputBuffers = decoder.getInputBuffers();
                            outputBuffers = decoder.getOutputBuffers();
                            inputBufferIndex = decoder.dequeueInputBuffer(-1);
                            if (inputBufferIndex >= 0) {
                                inputBuffer = inputBuffers[inputBufferIndex];
                                inputBuffer.clear();
                                Log.d("AudioDecoder", "wait,len:"+len);
                                inputBuffer.put(data);
                                decoder.queueInputBuffer(inputBufferIndex, 0, len, 0, 0);

                            }



                            bufferInfo = new MediaCodec.BufferInfo();
                            outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 0);


                            while (outputBufferIndex >= 0) {
                                Thread.sleep(20);
                                outputBuffer = outputBuffers[outputBufferIndex];

                              //  outputBuffer.position(bufferInfo.offset);
                             //   outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                                Log.d("AudioDecoder", bufferInfo.size + " bufferInfo size");
                                outData = new byte[bufferInfo.size];//pcm
                                outputBuffer.get(outData);
                                outputBuffer.clear();

                                if (outData.length > 0) {
                                    player.write(outData, 0, outData.length);
                                }
                                Log.d("AudioDecoder", bufferInfo.presentationTimeUs + " bufferInfo.presentationTimeUs");

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

///*
//    final int res = codec.dequeueOutputBuffer(info, TIMEOUT_US);
//            if (res >= 0) {
//        int outputBufIndex = res;
//        ByteBuffer buf = codecOutputBuffers[outputBufIndex];
//
//        final byte[] chunk = new byte[info.size];
//        buf.get(chunk); // Read the buffer all at once
//        buf.clear(); // ** MUST DO!!! OTHERWISE THE NEXT TIME YOU GET THIS SAME BUFFER BAD THINGS WILL HAPPEN
//
//        if (chunk.length > 0) {
//            audioTrack.write(chunk, 0, chunk.length);
//        }
//        codec.releaseOutputBuffer(outputBufIndex, false /* render */);
//
///*
//        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//            sawOutputEOS = true;
//        }
//    } else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//        codecOutputBuffers = codec.getOutputBuffers();
//    } else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//        final MediaFormat oformat = codec.getOutputFormat();
//        Log.d(LOG_TAG, "Output format has changed to " + oformat);
//        mAudioTrack.setPlaybackRate(oformat.getInteger(MediaFormat.KEY_SAMPLE_RATE));
//    }
//*/
}
